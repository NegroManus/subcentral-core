package de.subcentral.watcher.settings;

import java.util.concurrent.ExecutorService;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.db.MetadataDb;

public class MetadataDbSettingEntry<T> extends AbstractSettingEntry<MetadataDb<T>>
{
	private static final Logger		log	= LogManager.getLogger(MetadataDbSettingEntry.class);

	private final BooleanProperty	available;

	public MetadataDbSettingEntry(MetadataDb<T> database, boolean enabled)
	{
		super(database, enabled);
		this.available = new SimpleBooleanProperty(this, "available", false);
	}

	public ReadOnlyBooleanProperty availableProperty()
	{
		return available;
	}

	public boolean isAvailable()
	{
		return available.get();
	}

	public void recheckAvailability(ExecutorService executor)
	{
		available.set(false);
		Task<Boolean> checkAvailibilityTask = new Task<Boolean>()
		{
			@Override
			protected Boolean call() throws Exception
			{
				boolean isAvailable = value.isAvailable();
				log.debug("Rechecked whether {} is available: {}", value, isAvailable);
				return isAvailable;
			}

			@Override
			protected void succeeded()
			{
				available.set(getValue());
			}
		};
		executor.submit(checkAvailibilityTask);
	}
}