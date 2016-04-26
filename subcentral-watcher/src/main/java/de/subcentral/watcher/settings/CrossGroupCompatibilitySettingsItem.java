package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class CrossGroupCompatibilitySettingsItem extends SimpleDeactivatableSettingsItem<CrossGroupCompatibility> implements Comparable<CrossGroupCompatibilitySettingsItem>
{
	public static final StringConverter<CrossGroupCompatibilitySettingsItem>								STRING_CONVERTER	= initStringConverter();

	private static final ConfigurationPropertyHandler<ObservableList<CrossGroupCompatibilitySettingsItem>>	HANDLER				= new ListConfigurationPropertyHandler();

	public CrossGroupCompatibilitySettingsItem(CrossGroupCompatibility value, boolean enabled)
	{
		super(value, enabled);
	}

	private static StringConverter<CrossGroupCompatibilitySettingsItem> initStringConverter()
	{
		return new StringConverter<CrossGroupCompatibilitySettingsItem>()
		{
			@Override
			public String toString(CrossGroupCompatibilitySettingsItem entry)
			{
				return entry.getItem().toShortString();
			}

			@Override
			public CrossGroupCompatibilitySettingsItem fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public int compareTo(CrossGroupCompatibilitySettingsItem o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return item.compareTo(o.item);
	}

	public static ObservableList<CrossGroupCompatibilitySettingsItem> createObservableList()
	{
		return createObservableList(new ArrayList<>());
	}

	public static ObservableList<CrossGroupCompatibilitySettingsItem> createObservableList(List<CrossGroupCompatibilitySettingsItem> list)
	{
		return FXCollections.observableList(list, (CrossGroupCompatibilitySettingsItem item) -> new Observable[] { item.enabledProperty() });
	}

	public static ConfigurationPropertyHandler<ObservableList<CrossGroupCompatibilitySettingsItem>> getListConfigurationPropertyHandler()
	{
		return HANDLER;
	}

	private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<CrossGroupCompatibilitySettingsItem>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<CrossGroupCompatibilitySettingsItem> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<CrossGroupCompatibilitySettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			// read GroupsCompatibilities
			List<HierarchicalConfiguration<ImmutableNode>> groupsCompCfgs = cfg.configurationsAt(key + ".crossGroupCompatibility");
			List<CrossGroupCompatibilitySettingsItem> compatibilities = new ArrayList<>(groupsCompCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> groupsCompCfg : groupsCompCfgs)
			{
				boolean enabled = groupsCompCfg.getBoolean("[@enabled]");
				Group sourceGroup = Group.from(groupsCompCfg.getString("[@sourceGroup]"));
				Group compatibleGroup = Group.from(groupsCompCfg.getString("[@compatibleGroup]"));
				boolean symmetric = groupsCompCfg.getBoolean("[@symmetric]", false);
				compatibilities.add(new CrossGroupCompatibilitySettingsItem(new CrossGroupCompatibility(sourceGroup, compatibleGroup, symmetric), enabled));
			}
			// Sort the cross-group compatibilities
			compatibilities.sort(null);
			return createObservableList(compatibilities);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<CrossGroupCompatibilitySettingsItem> list)
		{
			for (int i = 0; i < list.size(); i++)
			{
				CrossGroupCompatibilitySettingsItem item = list.get(i);
				CrossGroupCompatibility c = item.getItem();
				cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@enabled]", item.isEnabled());
				cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@sourceGroup]", c.getSourceGroup());
				cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@compatibleGroup]", c.getCompatibleGroup());
				cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@symmetric]", c.isSymmetric());
			}
		}
	}
}