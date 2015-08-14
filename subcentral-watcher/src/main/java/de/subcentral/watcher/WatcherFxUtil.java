package de.subcentral.watcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.fx.DirectoryWatchService;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.settings.CorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.ReleaseTagsCorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.SeriesNameCorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;

public class WatcherFxUtil
{
    private static final Logger log = LogManager.getLogger(WatcherFxUtil.class);

    public static void bindWatchDirectories(final DirectoryWatchService service, final ObservableList<Path> directoryList) throws IOException
    {
	for (Path dir : WatcherSettings.INSTANCE.getWatchDirectories())
	{
	    service.registerDirectory(dir, StandardWatchEventKinds.ENTRY_CREATE);
	}
	directoryList.addListener(new ListChangeListener<Path>()
	{
	    @Override
	    public void onChanged(Change<? extends Path> c)
	    {
		while (c.next())
		{
		    if (c.wasRemoved())
		    {
			for (Path removedDir : c.getRemoved())
			{
			    service.unregisterDirectory(removedDir);
			}
		    }
		    if (c.wasAdded())
		    {
			for (Path addedDir : c.getAddedSubList())
			{
			    try
			    {
				service.registerDirectory(addedDir, StandardWatchEventKinds.ENTRY_CREATE);
			    }
			    catch (IOException e)
			    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			}
		    }
		}
	    }
	});
    }

    public static String standardizingRuleTypeToString(Class<? extends CorrectionRuleSettingEntry<?, ?>> type)
    {
	if (type == null)
	{
	    return "";
	}
	else if (type == SeriesNameCorrectionRuleSettingEntry.class)
	{
	    return SeriesNameCorrectionRuleSettingEntry.getRuleType();
	}
	else if (type == ReleaseTagsCorrectionRuleSettingEntry.class)
	{
	    return ReleaseTagsCorrectionRuleSettingEntry.getStandardizerTypeString();
	}
	return type.getSimpleName();
    }

    public static String beanTypeToString(Class<?> beanClass)
    {
	if (SubtitleAdjustment.class == beanClass)
	{
	    return "Subtitle";
	}
	return beanClass.getSimpleName();
    }

    public static Hyperlink createFurtherInfoHyperlink(Release rls, ExecutorService executorService)
    {
	if (rls.getFurtherInfoLinks().isEmpty())
	{
	    return null;
	}
	try
	{
	    String host = new URL(rls.getFurtherInfoLinks().get(0)).getHost().replace("www.", "");
	    ImageView dbImg = new ImageView(FxUtil.loadImg("database_16.png"));
	    Hyperlink hl = new Hyperlink(host, dbImg);
	    hl.setVisited(true);
	    hl.setOnAction((ActionEvent evt) -> FxUtil.browse(rls.getFurtherInfoLinks().get(0), executorService));
	    return hl;
	}
	catch (MalformedURLException e)
	{
	    log.error("Could not create further info hyperlink", e);
	    return null;
	}

    }

    private WatcherFxUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

}
