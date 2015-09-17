package de.subcentral.watcher.settings;

import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.db.MetadataDb;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

public class MetadataDbSettingEntry<T> extends AbstractDeactivatableSettingEntry<MetadataDb>
{
	private static final Logger log = LogManager.getLogger(MetadataDbSettingEntry.class);

	private final BooleanProperty available;

	public MetadataDbSettingEntry(MetadataDb database, boolean enabled)
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

	public void updateAvailability(ExecutorService executor)
	{
		available.set(false);
		Task<Boolean> updateAvailibilityTask = new Task<Boolean>()
		{
			@Override
			protected Boolean call() throws Exception
			{
				boolean isAvailable = value.isAvailable();
				log.debug("Availibility for {}: {}", value, isAvailable);
				return isAvailable;
			}

			@Override
			protected void succeeded()
			{
				available.set(getValue());
			}
		};
		executor.submit(updateAvailibilityTask);
	}
}