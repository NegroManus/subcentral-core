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
import de.subcentral.core.metadata.release.Tags;
import de.subcentral.core.util.CollectionUtil.ReplaceMode;
import de.subcentral.core.util.CollectionUtil.SearchMode;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleSettingsItem;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public abstract class CorrectorSettingsItem<T, C extends Corrector<? super T>> extends SimpleSettingsItem<C> implements Comparable<CorrectorSettingsItem<?, ?>> {
    public static final StringConverter<CorrectorSettingsItem<?, ?>>                               TYPE_AND_RULE_STRING_CONVERTER = new CorrectorTypeAndRuleStringConverter();
    public static final StringConverter<Class<? extends CorrectorSettingsItem<?, ?>>>              TYPE_STRING_CONVERTER          = new CorrectorTypeStringConverter();

    private static final ConfigurationPropertyHandler<ObservableList<CorrectorSettingsItem<?, ?>>> HANDLER                        = new ListConfigurationPropertyHandler();

    protected final Class<T>                                                                       beanType;
    protected final BooleanProperty                                                                beforeQuerying;
    protected final BooleanProperty                                                                afterQuerying;

    public CorrectorSettingsItem(Class<T> beanType, C corrector, boolean beforeQuerying, boolean afterQuerying) {
        super(corrector);
        this.beanType = Objects.requireNonNull(beanType, "beanType");
        this.beforeQuerying = new SimpleBooleanProperty(this, "beforeQuerying", beforeQuerying);
        this.afterQuerying = new SimpleBooleanProperty(this, "afterQuerying", afterQuerying);
    }

    public Class<T> getBeanType() {
        return beanType;
    }

    public abstract ObservableValue<String> ruleType();

    public abstract ObservableValue<String> rule();

    public final BooleanProperty beforeQueryingProperty() {
        return this.beforeQuerying;
    }

    public final boolean isBeforeQuerying() {
        return this.beforeQueryingProperty().get();
    }

    public final void setBeforeQuerying(final boolean beforeQuerying) {
        this.beforeQueryingProperty().set(beforeQuerying);
    }

    public final BooleanProperty afterQueryingProperty() {
        return this.afterQuerying;
    }

    public final boolean isAfterQuerying() {
        return this.afterQueryingProperty().get();
    }

    public final void setAfterQuerying(final boolean afterQuerying) {
        this.afterQueryingProperty().set(afterQuerying);
    }

    @Override
    public int compareTo(CorrectorSettingsItem<?, ?> o) {
        return ObjectUtil.getDefaultStringOrdering().compare(rule().getValue(), o.rule().getValue());
    }

    public static ObservableList<CorrectorSettingsItem<?, ?>> createObservableList() {
        return createObservableList(new ArrayList<>());
    }

    public static ObservableList<CorrectorSettingsItem<?, ?>> createObservableList(List<CorrectorSettingsItem<?, ?>> list) {
        return FXCollections.observableList(list, (CorrectorSettingsItem<?, ?> entry) -> new Observable[] { entry.beforeQueryingProperty(), entry.afterQueryingProperty() });
    }

    public static ConfigurationPropertyHandler<ObservableList<CorrectorSettingsItem<?, ?>>> getListConfigurationPropertyHandler() {
        return HANDLER;
    }

    private static class CorrectorTypeStringConverter extends StringConverter<Class<? extends CorrectorSettingsItem<?, ?>>> {
        @Override
        public String toString(Class<? extends CorrectorSettingsItem<?, ?>> type) {
            if (type == null) {
                return "";
            }
            else if (type == SeriesNameCorrectorSettingsItem.class) {
                return SeriesNameCorrectorSettingsItem.getRuleType();
            }
            else if (type == ReleaseTagsCorrectorSettingsItem.class) {
                return ReleaseTagsCorrectorSettingsItem.getRuleType();
            }
            return type.getSimpleName();
        }

        @Override
        public Class<? extends CorrectorSettingsItem<?, ?>> fromString(String string) {
            // not needed
            throw new UnsupportedOperationException();
        }
    }

    private static class CorrectorTypeAndRuleStringConverter extends StringConverter<CorrectorSettingsItem<?, ?>> {
        @Override
        public String toString(CorrectorSettingsItem<?, ?> entry) {
            StringBuilder sb = new StringBuilder();
            sb.append("Rule type: ");
            sb.append(entry.ruleType().getValue());
            sb.append("\n");
            sb.append("Rule: ");
            sb.append(entry.rule().getValue());
            return sb.toString();
        }

        @Override
        public CorrectorSettingsItem<?, ?> fromString(String string) {
            // not needed
            throw new UnsupportedOperationException();
        }
    }

    private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<CorrectorSettingsItem<?, ?>>> {
        @SuppressWarnings("unchecked")
        @Override
        public ObservableList<CorrectorSettingsItem<?, ?>> get(ImmutableConfiguration cfg, String key) {
            if (cfg instanceof HierarchicalConfiguration<?>) {
                return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
            }
            throw new IllegalArgumentException("Configuration type not supported: " + cfg);
        }

        private static ObservableList<CorrectorSettingsItem<?, ?>> get(HierarchicalConfiguration<ImmutableNode> cfg, String key) {
            List<HierarchicalConfiguration<ImmutableNode>> seriesCorrectorCfgs = cfg.configurationsAt(key + ".seriesNameCorrectionRule");
            List<HierarchicalConfiguration<ImmutableNode>> rlsTagsCorrectorCfgs = cfg.configurationsAt(key + ".releaseTagsCorrectionRule");
            List<CorrectorSettingsItem<?, ?>> correctors = new ArrayList<>(seriesCorrectorCfgs.size() + rlsTagsCorrectorCfgs.size());
            for (HierarchicalConfiguration<ImmutableNode> correctorCfg : seriesCorrectorCfgs) {
                String namePatternStr = correctorCfg.getString("[@namePattern]");
                Mode namePatternMode = Mode.valueOf(correctorCfg.getString("[@namePatternMode]"));
                UserPattern nameUiPattern = new UserPattern(namePatternStr, namePatternMode);
                String nameReplacement = correctorCfg.getString("[@nameReplacement]");
                List<HierarchicalConfiguration<ImmutableNode>> aliasNameCfgs = correctorCfg.configurationsAt("aliasNames.aliasName");
                List<String> aliasNameReplacements = new ArrayList<>(aliasNameCfgs.size());
                for (HierarchicalConfiguration<ImmutableNode> aliasNameCfg : aliasNameCfgs) {
                    aliasNameReplacements.add(aliasNameCfg.getString(""));
                }
                boolean enabledPreMetadataDb = correctorCfg.getBoolean("[@beforeQuerying]");
                boolean enabledPostMetadataDb = correctorCfg.getBoolean("[@afterQuerying]");
                correctors.add(new SeriesNameCorrectorSettingsItem(nameUiPattern, nameReplacement, aliasNameReplacements, enabledPreMetadataDb, enabledPostMetadataDb));
            }

            for (HierarchicalConfiguration<ImmutableNode> correctorCfg : rlsTagsCorrectorCfgs) {
                List<Tag> queryTags = Tags.split(correctorCfg.getString("[@searchTags]"));
                List<Tag> replacement = Tags.split(correctorCfg.getString("[@replacement]"));
                SearchMode queryMode = SearchMode.valueOf(correctorCfg.getString("[@searchMode]"));
                ReplaceMode replaceMode = ReplaceMode.valueOf(correctorCfg.getString("[@replaceMode]"));
                boolean ignoreOrder = correctorCfg.getBoolean("[@ignoreOrder]", false);
                boolean beforeQuerying = correctorCfg.getBoolean("[@beforeQuerying]");
                boolean afterQuerying = correctorCfg.getBoolean("[@afterQuerying]");
                ReleaseTagsCorrector stdzer = new ReleaseTagsCorrector(new TagsReplacer(queryTags, replacement, queryMode, replaceMode, ignoreOrder));
                correctors.add(new ReleaseTagsCorrectorSettingsItem(stdzer, beforeQuerying, afterQuerying));
            }
            // Sort the correctors
            correctors.sort(null);
            return createObservableList(correctors);
        }

        @Override
        public void add(Configuration cfg, String key, ObservableList<CorrectorSettingsItem<?, ?>> list) {
            // one index for each element name
            int seriesNameIndex = 0;
            int releaseTagsIndex = 0;
            for (CorrectorSettingsItem<?, ?> genericEntry : list) {
                if (genericEntry instanceof SeriesNameCorrectorSettingsItem) {
                    SeriesNameCorrectorSettingsItem entry = (SeriesNameCorrectorSettingsItem) genericEntry;
                    SeriesNameCorrector corrector = entry.getItem();
                    UserPattern namePattern = entry.getNameUserPattern();

                    cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePattern]", namePattern.getPattern());
                    cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePatternMode]", namePattern.getMode());
                    cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@nameReplacement]", corrector.getNameReplacement());
                    cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
                    cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@afterQuerying]", entry.isAfterQuerying());
                    for (String aliasName : corrector.getAliasNamesReplacement()) {
                        cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ").aliasNames.aliasName", aliasName);
                    }
                    seriesNameIndex++;
                }
                else if (genericEntry instanceof ReleaseTagsCorrectorSettingsItem) {
                    ReleaseTagsCorrectorSettingsItem entry = (ReleaseTagsCorrectorSettingsItem) genericEntry;
                    TagsReplacer replacer = (TagsReplacer) entry.getItem().getReplacer();

                    cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchTags]", Tags.join(replacer.getSearchTags()));
                    cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replacement]", Tags.join(replacer.getReplacement()));
                    cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchMode]", replacer.getSearchMode());
                    cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replaceMode]", replacer.getReplaceMode());
                    cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@ignoreOrder]", replacer.getIgnoreOrder());
                    cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
                    cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@afterQuerying]", entry.isAfterQuerying());
                    releaseTagsIndex++;
                }
                else {
                    throw new IllegalArgumentException("Unknown standardizer: " + genericEntry);
                }
            }
        }
    }
}