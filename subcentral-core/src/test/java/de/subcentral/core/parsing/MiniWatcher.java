package de.subcentral.core.parsing;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CompletableFuture;

public class MiniWatcher
{
	public static void main(String[] args) throws IOException
	{
		Path watchDir = Paths.get(System.getProperty("user.home"), "Downloads");
		WatchService watcher = FileSystems.getDefault().newWatchService();
		WatchKey watchKey = watchDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

		// ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		// service.schedule(() -> {
		// System.out.println("Cancelling");
		// watchKey.cancel();
		// try
		// {
		// watcher.close();
		// }
		// catch (Exception e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println("Cancelled");
		// },
		// 2,
		// TimeUnit.SECONDS);

		for (;;)
		{
			// wait for key to be signaled
			WatchKey currentKey;
			try
			{
				currentKey = watcher.take();
			}
			catch (InterruptedException | ClosedWatchServiceException x)
			{
				x.printStackTrace();
				break;
			}

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
					continue;
				}

				// The filename is the
				// context of the event.
				@SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				processWatchEventAsync(ev);
			}

			// Reset the key -- this step is critical if you want to
			// receive further watch events. If the key is no longer valid,
			// the directory is inaccessible so exit the loop.
			boolean valid = currentKey.reset();
			if (!valid)
			{
				System.err.println("Key was invalid, stopping watch");
				break;
			}
		}
	}

	private static void processWatchEventAsync(WatchEvent<Path> watchEvent)
	{
		CompletableFuture.supplyAsync(() -> watchEvent.context()).thenAccept(p -> System.out.println("New file: " + p));

		System.out.println("done");
	}
}
