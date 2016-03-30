package de.subcentral.watcher.settings;

import java.util.concurrent.ExecutorService;

import de.subcentral.core.metadata.db.MetadataDb;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

public class MetadataDbSettingEntry<T> extends AbstractDeactivatableSettingEntry<MetadataDb>
{
	public static enum Availability
	{
		UNKNOWN, CHECKING, AVAILABLE, LIMITED, NOT_AVAILABLE;

		public static Availability of(MetadataDb.State state)
		{
			switch (state)
			{
				case AVAILABLE:
					return Availability.AVAILABLE;
				case AVAILABLE_LIMITED:
					return Availability.LIMITED;
				case NOT_AVAILABLE:
					return Availability.NOT_AVAILABLE;
				default:
					return Availability.UNKNOWN;

			}
		}
	}

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
				return Availability.of(value.checkState());
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