package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.correct.Corrector;
import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.metadata.release.TagUtil.SearchMode;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleSettingsItem;
import javafx.beans.Observable;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class CorrectorSettingsItem<T, C extends Corrector<? super T>> extends SimpleSettingsItem<C>
{
	private static final ConfigurationPropertyHandler<ObservableList<CorrectorSettingsItem<?, ?>>>	HANDLER	= new ListConfigurationPropertyHandler();

	protected final Class<T>																		beanType;
	protected final BooleanProperty																	beforeQuerying;
	protected final BooleanProperty																	afterQuerying;

	public CorrectorSettingsItem(Class<T> beanType, C corrector, boolean beforeQuerying, boolean afterQuerying)
	{
		super(corrector);
		this.beanType = Objects.requireNonNull(beanType, "beanType");
		this.beforeQuerying = new SimpleBooleanProperty(this, "beforeQuerying", beforeQuerying);
		this.afterQuerying = new SimpleBooleanProperty(this, "afterQuerying", afterQuerying);
	}

	public Class<T> getBeanType()
	{
		return beanType;
	}

	public abstract StringBinding ruleTypeBinding();

	public abstract StringBinding ruleBinding();

	public final BooleanProperty beforeQueryingProperty()
	{
		return this.beforeQuerying;
	}

	public final boolean isBeforeQuerying()
	{
		return this.beforeQueryingProperty().get();
	}

	public final void setBeforeQuerying(final boolean beforeQuerying)
	{
		this.beforeQueryingProperty().set(beforeQuerying);
	}

	public final BooleanProperty afterQueryingProperty()
	{
		return this.afterQuerying;
	}

	public final boolean isAfterQuerying()
	{
		return this.afterQueryingProperty().get();
	}

	public final void setAfterQuerying(final boolean afterQuerying)
	{
		this.afterQueryingProperty().set(afterQuerying);
	}

	public static ObservableList<CorrectorSettingsItem<?, ?>> createObservableList()
	{
		return createObservableList(new ArrayList<>());
	}

	public static ObservableList<CorrectorSettingsItem<?, ?>> createObservableList(List<CorrectorSettingsItem<?, ?>> list)
	{
		return FXCollections.observableList(list, (CorrectorSettingsItem<?, ?> entry) -> new Observable[] { entry.beforeQueryingProperty(), entry.afterQueryingProperty() });
	}

	public static ConfigurationPropertyHandler<ObservableList<CorrectorSettingsItem<?, ?>>> getListConfigurationPropertyHandler()
	{
		return HANDLER;
	}

	private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<CorrectorSettingsItem<?, ?>>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<CorrectorSettingsItem<?, ?>> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<CorrectorSettingsItem<?, ?>> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			List<HierarchicalConfiguration<ImmutableNode>> seriesStdzerCfgs = cfg.configurationsAt(key + ".seriesNameCorrectionRule");
			List<HierarchicalConfiguration<ImmutableNode>> rlsTagsStdzerCfgs = cfg.configurationsAt(key + ".releaseTagsCorrectionRule");
			List<CorrectorSettingsItem<?, ?>> stdzers = new ArrayList<>(seriesStdzerCfgs.size() + rlsTagsStdzerCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> stdzerCfg : seriesStdzerCfgs)
			{
				String namePatternStr = stdzerCfg.getString("[@namePattern]");
				Mode namePatternMode = Mode.valueOf(stdzerCfg.getString("[@namePatternMode]"));
				UserPattern nameUiPattern = new UserPattern(namePatternStr, namePatternMode);
				String nameReplacement = stdzerCfg.getString("[@nameReplacement]");
				List<HierarchicalConfiguration<ImmutableNode>> aliasNameCfgs = stdzerCfg.configurationsAt("aliasNames.aliasName");
				List<String> aliasNameReplacements = new ArrayList<>(aliasNameCfgs.size());
				for (HierarchicalConfiguration<ImmutableNode> aliasNameCfg : aliasNameCfgs)
				{
					aliasNameReplacements.add(aliasNameCfg.getString(""));
				}
				boolean enabledPreMetadataDb = stdzerCfg.getBoolean("[@beforeQuerying]");
				boolean enabledPostMetadataDb = stdzerCfg.getBoolean("[@afterQuerying]");
				stdzers.add(new SeriesNameCorrectorSettingsItem(nameUiPattern, nameReplacement, aliasNameReplacements, enabledPreMetadataDb, enabledPostMetadataDb));
			}

			for (HierarchicalConfiguration<ImmutableNode> stdzerCfg : rlsTagsStdzerCfgs)
			{
				List<Tag> queryTags = Tag.parseList(stdzerCfg.getString("[@searchTags]"));
				List<Tag> replacement = Tag.parseList(stdzerCfg.getString("[@replacement]"));
				SearchMode queryMode = SearchMode.valueOf(stdzerCfg.getString("[@searchMode]"));
				ReplaceMode replaceMode = ReplaceMode.valueOf(stdzerCfg.getString("[@replaceMode]"));
				boolean ignoreOrder = stdzerCfg.getBoolean("[@ignoreOrder]", false);
				boolean beforeQuerying = stdzerCfg.getBoolean("[@beforeQuerying]");
				boolean afterQuerying = stdzerCfg.getBoolean("[@afterQuerying]");
				ReleaseTagsCorrector stdzer = new ReleaseTagsCorrector(new TagsReplacer(queryTags, replacement, queryMode, replaceMode, ignoreOrder));
				stdzers.add(new ReleaseTagsCorrectorSettingsItem(stdzer, beforeQuerying, afterQuerying));
			}
			return createObservableList(stdzers);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<CorrectorSettingsItem<?, ?>> list)
		{
			// one index for each element name
			int seriesNameIndex = 0;
			int releaseTagsIndex = 0;
			for (CorrectorSettingsItem<?, ?> genericEntry : list)
			{
				if (genericEntry instanceof SeriesNameCorrectorSettingsItem)
				{
					SeriesNameCorrectorSettingsItem entry = (SeriesNameCorrectorSettingsItem) genericEntry;
					SeriesNameCorrector corrector = entry.getItem();
					UserPattern namePattern = entry.getNameUserPattern();

					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePattern]", namePattern.getPattern());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePatternMode]", namePattern.getMode());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@nameReplacement]", corrector.getNameReplacement());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@afterQuerying]", entry.isAfterQuerying());
					for (String aliasName : corrector.getAliasNamesReplacement())
					{
						cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ").aliasNames.aliasName", aliasName);
					}
					seriesNameIndex++;
				}
				else if (genericEntry instanceof ReleaseTagsCorrectorSettingsItem)
				{
					ReleaseTagsCorrectorSettingsItem entry = (ReleaseTagsCorrectorSettingsItem) genericEntry;
					TagsReplacer replacer = (TagsReplacer) entry.getItem().getReplacer();

					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchTags]", Tag.formatList(replacer.getSearchTags()));
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replacement]", Tag.formatList(replacer.getReplacement()));
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchMode]", replacer.getSearchMode());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replaceMode]", replacer.getReplaceMode());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@ignoreOrder]", replacer.getIgnoreOrder());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@afterQuerying]", entry.isAfterQuerying());
					releaseTagsIndex++;
				}
				else
				{
					throw new IllegalArgumentException("Unknown standardizer: " + genericEntry);
				}
			}
		}
	}
}