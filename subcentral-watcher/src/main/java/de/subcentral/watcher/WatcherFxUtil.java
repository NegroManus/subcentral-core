package de.subcentral.watcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;

import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.fx.DirectoryWatchService;
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

public class WatcherFxUtil
{

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

    private WatcherFxUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

}
