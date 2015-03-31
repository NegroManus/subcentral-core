package de.subcentral.watcher.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.Collection;
import java.util.function.BiConsumer;

import de.subcentral.fx.DirectoryWatchService;

public class DirectoryWatchServicePlayground
{
	public static void main(String[] args) throws Exception
	{
		DirectoryWatchService service = new DirectoryWatchService(new BiConsumer<Path, Collection<Path>>()
		{
			@Override
			public void accept(Path dir, Collection<Path> files)
			{
				System.out.println(dir + ": " + files);
			}
		});
		service.registerDirectory(Paths.get("D:\\Downloads"),
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		service.start();

		Thread.sleep(5000);
		service.unregisterDirectory(Paths.get("D:\\Downloads"));
		Thread.sleep(5000);
		service.cancel();
		Thread.sleep(5000);
	}
}
