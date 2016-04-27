package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.service.MetadataService;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;
import de.subcentral.support.orlydbcom.OrlyDbCom;
import de.subcentral.support.predbme.PreDbMe;
import de.subcentral.support.xrelto.XRelTo;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class MetadataServiceSettingsItem extends SimpleDeactivatableSettingsItem<MetadataService>
{
	private static final ConfigurationPropertyHandler<ObservableList<MetadataServiceSettingsItem>> HANDLER = new ListConfigurationPropertyHandler();

	public static enum Availability
	{
		UNKNOWN, CHECKING, AVAILABLE, LIMITED, NOT_AVAILABLE;

		public static Availability of(MetadataService.State state)
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

	public MetadataServiceSettingsItem(MetadataService database, boolean enabled)
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
			{
				updateTitle("Checking availability of " + item.getSite().getDisplayNameOrName());
			}

			@Override
			protected Availability call() throws Exception
			{
				return Availability.of(item.checkState());
			}

			@Override
			protected void succeeded()
			{
				availability.setValue(getValue());
			}
		};
		executor.submit(updateAvailibilityTask);
	}

	public static ObservableList<MetadataServiceSettingsItem> createObservableList()
	{
		return createObservableList(new ArrayList<>());
	}

	public static ObservableList<MetadataServiceSettingsItem> createObservableList(List<MetadataServiceSettingsItem> list)
	{
		return FXCollections.observableList(list, (MetadataServiceSettingsItem item) -> new Observable[] { item.enabledProperty() });
	}

	public static ConfigurationPropertyHandler<ObservableList<MetadataServiceSettingsItem>> getListConfigurationPropertyHandler()
	{
		return HANDLER;
	}

	private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<MetadataServiceSettingsItem>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<MetadataServiceSettingsItem> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<MetadataServiceSettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key + ".db");
			List<MetadataServiceSettingsItem> services = new ArrayList<>(rlsDbCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> serviceCfg : rlsDbCfgs)
			{
				String name = serviceCfg.getString("");
				MetadataService service = null;
				for (MetadataService s : getAvailableMetadataServices())
				{
					if (s.getName().equals(name))
					{
						service = s;
						break;
					}
				}
				if (service == null)
				{
					throw new IllegalArgumentException("Unknown metadata service: " + name);
				}
				boolean enabled = serviceCfg.getBoolean("[@enabled]", true);
				services.add(new MetadataServiceSettingsItem(service, enabled));
			}
			return createObservableList(services);
		}

		private static Set<MetadataService> getAvailableMetadataServices()
		{
			ImmutableSet.Builder<MetadataService> services = ImmutableSet.builder();
			services.add(PreDbMe.getMetadataService());
			services.add(XRelTo.getMetadataService());
			services.add(OrlyDbCom.getMetadataService());
			return services.build();
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<MetadataServiceSettingsItem> value)
		{
			for (int i = 0; i < value.size(); i++)
			{
				MetadataServiceSettingsItem service = value.get(i);
				cfg.addProperty(key + ".db(" + i + ")", service.getItem().getName());
				cfg.addProperty(key + ".db(" + i + ")[@enabled]", service.isEnabled());
			}
		}
	}
}