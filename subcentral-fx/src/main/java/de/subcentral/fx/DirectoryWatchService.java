package de.subcentral.fx;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class DirectoryWatchService extends Service<Void>
{
	private static final Logger log = LogManager.getLogger(DirectoryWatchService.class);

	private final Map<Path, WatchEntry>					watchDirs	= new HashMap<>();
	private final BiConsumer<Path, Collection<Path>>	watchEventConsumer;
	private WatchTask									watchTask;
	private AtomicBoolean								watchActive	= new AtomicBoolean(false);
	private AtomicBoolean								initialScan	= new AtomicBoolean(false);

	public DirectoryWatchService(BiConsumer<Path, Collection<Path>> watchEventConsumer)
	{
		this.watchEventConsumer = Objects.requireNonNull(watchEventConsumer, "watchEventConsumer");
	}

	public boolean registerDirectory(Path dir, WatchEvent.Kind<?>... kinds) throws IOException
	{
		Objects.requireNonNull(dir, "dir");
		Objects.requireNonNull(kinds, "kinds");
		synchronized (watchDirs)
		{
			if (watchDirs.containsKey(dir))
			{
				return false;
			}
			WatchEntry entry;
			if (watchActive.get())
			{
				entry = new WatchEntry(dir.register(watchTask.watchService, kinds), kinds);
				log.info("Registered directory while watching: {}", dir);
			}
			else
			{
				entry = new WatchEntry(null, kinds);
				log.info("Registered directory: {}", dir);
			}
			watchDirs.put(dir, entry);
			return true;
		}
	}

	public boolean unregisterDirectory(Path dir)
	{
		Objects.requireNonNull(dir, "dir");
		synchronized (watchDirs)
		{
			WatchEntry entry = watchDirs.remove(dir);
			if (entry != null)
			{
				if (entry.key != null)
				{
					entry.key.cancel();
				}
				if (watchActive.get())
				{
					log.info("Unregistered directory while watching: {}", dir);
					if (watchDirs.isEmpty())
					{
						log.info("No more directories to watch. Stopping");
						cancel();
					}
				}
				else
				{
					log.info("Unregistered directory: {}", dir);
				}
				return true;
			}
			return false;
		}
	}

	public Set<Path> getWatchDirectories()
	{
		synchronized (watchDirs)
		{
			return ImmutableSet.copyOf(watchDirs.keySet());
		}
	}

	public boolean getInitialScan()
	{
		return initialScan.get();
	}

	public void setInitialScan(boolean initialScan)
	{
		this.initialScan.set(initialScan);
	}

	@Override
	protected Task<Void> createTask()
	{
		watchTask = new WatchTask();
		return watchTask;
	}

	private class WatchTask extends Task<Void>
	{
		private WatchService watchService;

		@Override
		protected Void call() throws Exception
		{
			try
			{
				synchronized (watchDirs)
				{
					if (watchDirs.isEmpty())
					{
						log.info("No directories to watch");
						return null;
					}
					watchService = FileSystems.getDefault().newWatchService();
					// register the dirs
					for (Map.Entry<Path, WatchEntry> entry : watchDirs.entrySet())
					{
						Path dir = entry.getKey();
						WatchEvent.Kind<?>[] kinds = entry.getValue().kinds;
						watchDirs.put(dir, new WatchEntry(dir.register(watchService, kinds), kinds));
					}
					if (getInitialScan())
					{
						scanDirectories();
					}
					watchActive.set(true);
				}
				log.info("Watching {}", watchDirs.keySet());
				while (!Thread.interrupted())
				{
					// wait for key to be signaled
					WatchKey currentKey = watchService.take();

					ListMultimap<Path, Path> newFiles = ArrayListMultimap.create();
					for (WatchEvent<?> event : currentKey.pollEvents())
					{
						WatchEvent.Kind<?> kind = event.kind();

						// This key is registered only
						// for ENTRY_CREATE events,
						// but an OVERFLOW event can
						// occur regardless if events
						// are lost or discarded.
						if (kind == StandardWatchEventKinds.OVERFLOW)
						{
							log.info("Overflow event occured");
							continue;
						}

						// The filename is the
						// context of the event.
						@SuppressWarnings("unchecked")
						WatchEvent<Path> evt = (WatchEvent<Path>) event;
						Path dir = (Path) currentKey.watchable();
						log.debug("New event in {}: file={}, eventKind={}, count={}", dir, evt.context(), evt.kind(), evt.count());
						newFiles.put(dir, evt.context());
					}
					for (Map.Entry<Path, Collection<Path>> entry : newFiles.asMap().entrySet())
					{
						watchEventConsumer.accept(entry.getKey(), entry.getValue());
					}

					// Reset the key -- this step is critical if you want to
					// receive further watch events. If the key is no longer valid,
					// the directory is inaccessible so exit the loop.
					boolean valid = currentKey.reset();
					if (!valid)
					{
						log.debug("Key was invalid, canceling watch");
						break;
					}
				}
			}
			catch (InterruptedException e)
			{
				if (!isCancelled())
				{
					throw e;
				}
			}
			catch (RuntimeException e)
			{
				log.error("Exception while watching " + watchDirs.keySet(), e);
				throw e;
			}
			finally
			{
				synchronized (watchDirs)
				{
					watchActive.set(false);
					if (watchService != null)
					{
						try
						{
							watchService.close();
						}
						catch (IOException e)
						{
							log.warn("Exception while closing WatchService", e);
						}
					}
					log.info("Watch stopped");
				}
			}
			return null;
		}

		private void scanDirectories() throws IOException
		{
			for (Path dir : watchDirs.keySet())
			{
				if (isCancelled())
				{
					return;
				}
				List<Path> files = new ArrayList<>();
				try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir))
				{
					if (isCancelled())
					{
						return;
					}
					for (Path file : directoryStream)
					{
						files.add(file);
					}
				}
				catch (IOException ex)
				{
					log.error("Exception while scanning directory " + dir, ex);
					throw ex;
				}
				watchEventConsumer.accept(dir, files);
			}
		}
	}

	@Override
	protected void cancelled()
	{
		log.debug("Watch was cancelled");
	}

	private static class WatchEntry
	{
		private final WatchKey				key;
		private final WatchEvent.Kind<?>[]	kinds;

		public WatchEntry(WatchKey watchKey, Kind<?>[] kinds)
		{
			this.key = watchKey;
			this.kinds = kinds;
		}
	}
}
