package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;
import de.subcentral.support.orlydbcom.OrlyDbCom;
import de.subcentral.support.orlydbcom.OrlyDbComMetadataDb;
import de.subcentral.support.predbme.PreDbMe;
import de.subcentral.support.predbme.PreDbMeMetadataDb;
import de.subcentral.support.xrelto.XRelTo;
import de.subcentral.support.xrelto.XRelToMetadataDb;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class MetadataDbSettingsItem extends SimpleDeactivatableSettingsItem<MetadataDb>
{
	private static final ConfigurationPropertyHandler<ObservableList<MetadataDbSettingsItem>> HANDLER = new ListConfigurationPropertyHandler();

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

	public MetadataDbSettingsItem(MetadataDb database, boolean enabled)
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

	public static ObservableList<MetadataDbSettingsItem> createObservableList()
	{
		return createObservableList(new ArrayList<>());
	}

	public static ObservableList<MetadataDbSettingsItem> createObservableList(List<MetadataDbSettingsItem> list)
	{
		return FXCollections.observableList(list, (MetadataDbSettingsItem item) -> new Observable[] { item.enabledProperty() });
	}

	public static ConfigurationPropertyHandler<ObservableList<MetadataDbSettingsItem>> getListConfigurationPropertyHandler()
	{
		return HANDLER;
	}

	private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<MetadataDbSettingsItem>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<MetadataDbSettingsItem> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<MetadataDbSettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key + ".db");
			List<MetadataDbSettingsItem> dbs = new ArrayList<>(rlsDbCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> rlsDbCfg : rlsDbCfgs)
			{
				String name = rlsDbCfg.getString("");
				boolean enabled = rlsDbCfg.getBoolean("[@enabled]");
				if (PreDbMe.SITE.getName().equals(name))
				{
					dbs.add(new MetadataDbSettingsItem(new PreDbMeMetadataDb(), enabled));
				}
				else if (XRelTo.SITE.getName().equals(name))
				{
					dbs.add(new MetadataDbSettingsItem(new XRelToMetadataDb(), enabled));
				}
				else if (OrlyDbCom.SITE.getName().equals(name))
				{
					dbs.add(new MetadataDbSettingsItem(new OrlyDbComMetadataDb(), enabled));
				}
				else
				{
					throw new IllegalArgumentException("Unknown metadata database: " + name);
				}
			}
			return createObservableList(dbs);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<MetadataDbSettingsItem> value)
		{
			for (int i = 0; i < value.size(); i++)
			{
				MetadataDbSettingsItem db = value.get(i);
				cfg.addProperty(key + ".db(" + i + ")", db.getItem().getSite().getName());
				cfg.addProperty(key + ".db(" + i + ")[@enabled]", db.isEnabled());
			}
		}
	}
}