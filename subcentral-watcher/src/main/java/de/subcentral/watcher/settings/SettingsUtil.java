package de.subcentral.watcher.settings;

import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import com.google.common.collect.ImmutableList;

import de.subcentral.watcher.model.ObservableBean;

public class SettingsUtil
{
    public static Observable observeEnablementOfSettingEntries(ObservableList<? extends SettingEntry<?>> configEntries)
    {
	ObservableBean obsv = new ObservableBean();
	for (SettingEntry<?> entry : configEntries)
	{
	    obsv.getDependencies().add(entry.enabledProperty());
	}
	configEntries.addListener(new ListChangeListener<SettingEntry<?>>()
	{
	    @Override
	    public void onChanged(ListChangeListener.Change<? extends SettingEntry<?>> c)
	    {
		while (c.next())
		{
		    if (c.wasRemoved())
		    {
			for (SettingEntry<?> entry : c.getRemoved())
			{
			    // remove listener for enabled property
			    obsv.getDependencies().remove(entry.enabledProperty());
			}
		    }
		    if (c.wasAdded())
		    {
			for (SettingEntry<?> entry : c.getAddedSubList())
			{
			    // add listener for enabled property
			    obsv.getDependencies().add(entry.enabledProperty());
			}
		    }
		}

	    }
	});
	return obsv;
    }

    public static <V, T extends SettingEntry<V>> ImmutableList<V> getValuesOfEnabledSettingEntries(Iterable<T> entries)
    {
	ImmutableList.Builder<V> enabledEntries = ImmutableList.builder();
	for (T entry : entries)
	{
	    if (entry.isEnabled())
	    {
		enabledEntries.add(entry.getValue());
	    }
	}
	return enabledEntries.build();
    }

    public SettingsUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
