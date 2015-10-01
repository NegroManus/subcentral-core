package de.subcentral.watcher.settings;

import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.db.MetadataDb;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

public class MetadataDbSettingEntry<T> extends AbstractDeactivatableSettingEntry<MetadataDb>
{
	public static enum Availability
	{
		UNKNOWN, CHECKING, AVAILABLE, NOT_AVAILABLE
	}

	private static final Logger log = LogManager.getLogger(MetadataDbSettingEntry.class);

	private final Property<Availability> availability = new SimpleObjectProperty<>(this, "availability", Availability.UNKNOWN);

	public MetadataDbSettingEntry(MetadataDb database, boolean enabled)
	{
		super(database, enabled);
	}

	public Property<Availability> availabilityProperty()
	{
		return availability;
	}

	public Availability getAvailability()
	{
		return availability.getValue();
	}

	public void updateAvailability(ExecutorService executor)
	{
		availability.setValue(Availability.CHECKING);
		Task<Availability> updateAvailibilityTask = new Task<Availability>()
		{
			@Override
			protected Availability call() throws Exception
			{
				Availability availibity = value.isAvailable() ? Availability.AVAILABLE : Availability.NOT_AVAILABLE;
				log.debug("Availibility for {}: {}", value, availibity);
				return availibity;
			}

			@Override
			protected void succeeded()
			{
				availability.setValue(getValue());
			}
		};
		executor.submit(updateAvailibilityTask);
	}
}