package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.parse.ParsingService;
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
				String domain = parsingServiceCfg.getString("");
				boolean enabled = parsingServiceCfg.getBoolean("[@enabled]");
				if (Addic7edCom.getParsingService().getName().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(Addic7edCom.getParsingService(), enabled));
				}
				else if (ItalianSubsNet.getParsingService().getName().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(ItalianSubsNet.getParsingService(), enabled));
				}
				else if (ReleaseScene.getParsingService().getName().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(ReleaseScene.getParsingService(), enabled));
				}
				else if (SubCentralDe.getParsingService().getName().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(SubCentralDe.getParsingService(), enabled));
				}
				else
				{
					throw new IllegalArgumentException("Unknown parsing service domain: " + domain);
				}
			}
			return createObservableList(services);
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