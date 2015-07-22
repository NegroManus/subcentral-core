package de.subcentral.watcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.fx.DirectoryWatchService;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.settings.CompatibilitySettingEntry;
import de.subcentral.watcher.settings.ReleaseTagsStandardizerSettingEntry;
import de.subcentral.watcher.settings.SeriesNameStandardizerSettingEntry;
import de.subcentral.watcher.settings.StandardizerSettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    public static void bindCompatibilities(final CompatibilityService service, ObservableList<CompatibilitySettingEntry> compatibilityList)
    {
	final CompatibilityEntryEnabledListener enabledListener = new CompatibilityEntryEnabledListener(service);
	for (CompatibilitySettingEntry entry : compatibilityList)
	{
	    // add listener for enabled property
	    entry.enabledProperty().addListener(enabledListener);
	    addCompatibility(service, entry);
	}
	// add listener to get notified about additions/removals
	compatibilityList.addListener(new ListChangeListener<CompatibilitySettingEntry>()
	{
	    @Override
	    public void onChanged(ListChangeListener.Change<? extends CompatibilitySettingEntry> c)
	    {
		while (c.next())
		{
		    if (c.wasRemoved())
		    {
			for (CompatibilitySettingEntry entry : c.getRemoved())
			{
			    // remove listener for enabled property
			    entry.enabledProperty().removeListener(enabledListener);
			    removeCompatibility(service, entry);
			}
		    }
		    if (c.wasAdded())
		    {
			for (CompatibilitySettingEntry entry : c.getAddedSubList())
			{
			    // add listener for enabled property
			    entry.enabledProperty().addListener(enabledListener);
			    addCompatibility(service, entry);
			}
		    }
		}
	    }
	});
    }

    private static class CompatibilityEntryEnabledListener implements ChangeListener<Boolean>
    {
	private final CompatibilityService service;

	private CompatibilityEntryEnabledListener(CompatibilityService service)
	{
	    this.service = service;
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
	{
	    BooleanProperty enabledProp = (BooleanProperty) observable;
	    CompatibilitySettingEntry entry = (CompatibilitySettingEntry) enabledProp.getBean();
	    if (newValue)
	    {
		addCompatibility(service, entry);
	    }
	    else
	    {
		removeCompatibility(service, entry);
	    }
	}
    }

    public static void addCompatibility(CompatibilityService service, CompatibilitySettingEntry entry)
    {
	if (entry.isEnabled())
	{
	    service.getCompatibilities().add(entry.getValue());
	}
    }

    public static boolean removeCompatibility(CompatibilityService service, CompatibilitySettingEntry entry)
    {
	return service.getCompatibilities().remove(entry.getValue());
    }

    public static String standardizingRuleTypeToString(Class<? extends StandardizerSettingEntry<?, ?>> type)
    {
	if (type == null)
	{
	    return "";
	}
	else if (type == SeriesNameStandardizerSettingEntry.class)
	{
	    return SeriesNameStandardizerSettingEntry.getStandardizerTypeString();
	}
	else if (type == ReleaseTagsStandardizerSettingEntry.class)
	{
	    return ReleaseTagsStandardizerSettingEntry.getStandardizerTypeString();
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
	    String host = new URL(rls.getFurtherInfoLinks().get(0)).getHost();
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
