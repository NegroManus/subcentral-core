package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.util.ServiceUtil;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ParsingServiceSettingsItem extends SimpleDeactivatableSettingsItem<ParsingService>
{
	private static final ConfigurationPropertyHandler<ObservableList<ParsingServiceSettingsItem>> HANDLER = new ListConfigurationPropertyHandler();

	public ParsingServiceSettingsItem(ParsingService parsingService, boolean enabled)
	{
		super(parsingService, enabled);
	}

	public static ObservableList<ParsingServiceSettingsItem> createObservableList()
	{
		return createObservableList(new ArrayList<>());
	}

	public static ObservableList<ParsingServiceSettingsItem> createObservableList(List<ParsingServiceSettingsItem> list)
	{
		return FXCollections.observableList(list, (ParsingServiceSettingsItem item) -> new Observable[] { item.enabledProperty() });
	}

	public static ConfigurationPropertyHandler<ObservableList<ParsingServiceSettingsItem>> getListConfigurationPropertyHandler()
	{
		return HANDLER;
	}

	private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<ParsingServiceSettingsItem>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<ParsingServiceSettingsItem> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<ParsingServiceSettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			List<HierarchicalConfiguration<ImmutableNode>> parsingServiceCfgs = cfg.configurationsAt(key + ".parser");
			List<ParsingServiceSettingsItem> services = new ArrayList<>(parsingServiceCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> parsingServiceCfg : parsingServiceCfgs)
			{
				String name = parsingServiceCfg.getString("");
				ParsingService service = ServiceUtil.getService(getAvailableParsingServices(), name);
				if (service == null)
				{
					throw new IllegalArgumentException("Unknown parsing service: " + name);
				}
				boolean enabled = parsingServiceCfg.getBoolean("[@enabled]", true);
				services.add(new ParsingServiceSettingsItem(service, enabled));
			}
			return createObservableList(services);
		}

		private static Set<ParsingService> getAvailableParsingServices()
		{
			ImmutableSet.Builder<ParsingService> services = ImmutableSet.builder();
			services.add(Addic7edCom.getParsingService());
			services.add(ItalianSubsNet.getParsingService());
			services.add(ReleaseScene.getParsingService());
			services.add(SubCentralDe.getParsingService());
			return services.build();
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<ParsingServiceSettingsItem> value)
		{
			for (int i = 0; i < value.size(); i++)
			{
				ParsingServiceSettingsItem ps = value.get(i);
				cfg.addProperty(key + ".parser(" + i + ")", ps.getItem().getName());
				cfg.addProperty(key + ".parser(" + i + ")[@enabled]", ps.isEnabled());
			}
		}
	}
}