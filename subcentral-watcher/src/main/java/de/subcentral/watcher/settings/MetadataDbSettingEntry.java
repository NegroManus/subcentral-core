package de.subcentral.watcher.settings;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.db.MetadataDb;

public class MetadataDbSettingEntry<T> implements SettingEntry<MetadataDb<T>>
{
	private static final Logger		log	= LogManager.getLogger(MetadataDbSettingEntry.class);

	private final MetadataDb<T>		database;
	private final BooleanProperty	enabled;
	private final BooleanProperty	available;

	public MetadataDbSettingEntry(MetadataDb<T> database, boolean enabled)
	{
		this.database = Objects.requireNonNull(database, "database");
		this.enabled = new SimpleBooleanProperty(this, "enabled", enabled);
		this.available = new SimpleBooleanProperty(this, "available", false);
	}

	@Override
	public MetadataDb<T> getValue()
	{
		return database;
	}

	@Override
	public final BooleanProperty enabledProperty()
	{
		return this.enabled;
	}

	@Override
	public final boolean isEnabled()
	{
		return this.enabledProperty().get();
	}

	@Override
	public final void setEnabled(final boolean enabled)
	{
		this.enabledProperty().set(enabled);
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
				boolean isAvailable = database.isAvailable();
				log.debug("Rechecked whether {} is available: {}", database, isAvailable);
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