package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.metadata.release.CrossGroupCompatibilityRule;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class CrossGroupCompatibilityRuleSettingsItem extends SimpleDeactivatableSettingsItem<CrossGroupCompatibilityRule> implements Comparable<CrossGroupCompatibilityRuleSettingsItem> {
    public static final StringConverter<CrossGroupCompatibilityRuleSettingsItem>                               STRING_CONVERTER = initStringConverter();

    private static final ConfigurationPropertyHandler<ObservableList<CrossGroupCompatibilityRuleSettingsItem>> HANDLER          = new ListConfigurationPropertyHandler();

    public CrossGroupCompatibilityRuleSettingsItem(CrossGroupCompatibilityRule value, boolean enabled) {
        super(value, enabled);
    }

    private static StringConverter<CrossGroupCompatibilityRuleSettingsItem> initStringConverter() {
        return new StringConverter<CrossGroupCompatibilityRuleSettingsItem>() {
            @Override
            public String toString(CrossGroupCompatibilityRuleSettingsItem entry) {
                return entry.getItem().toShortString();
            }

            @Override
            public CrossGroupCompatibilityRuleSettingsItem fromString(String string) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int compareTo(CrossGroupCompatibilityRuleSettingsItem o) {
        // nulls first
        if (o == null) {
            return 1;
        }
        return item.compareTo(o.item);
    }

    public static ObservableList<CrossGroupCompatibilityRuleSettingsItem> createObservableList() {
        return createObservableList(new ArrayList<>());
    }

    public static ObservableList<CrossGroupCompatibilityRuleSettingsItem> createObservableList(List<CrossGroupCompatibilityRuleSettingsItem> list) {
        return FXCollections.observableList(list, (CrossGroupCompatibilityRuleSettingsItem item) -> new Observable[] { item.enabledProperty() });
    }

    public static ConfigurationPropertyHandler<ObservableList<CrossGroupCompatibilityRuleSettingsItem>> getListConfigurationPropertyHandler() {
        return HANDLER;
    }

    private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<CrossGroupCompatibilityRuleSettingsItem>> {
        @SuppressWarnings("unchecked")
        @Override
        public ObservableList<CrossGroupCompatibilityRuleSettingsItem> get(ImmutableConfiguration cfg, String key) {
            if (cfg instanceof HierarchicalConfiguration<?>) {
                return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
            }
            throw new IllegalArgumentException("Configuration type not supported: " + cfg);
        }

        private static ObservableList<CrossGroupCompatibilityRuleSettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key) {
            // read GroupsCompatibilities
            List<HierarchicalConfiguration<ImmutableNode>> groupsCompCfgs = cfg.configurationsAt(key + ".crossGroupCompatibilityRule");
            List<CrossGroupCompatibilityRuleSettingsItem> compatibilities = new ArrayList<>(groupsCompCfgs.size());
            for (HierarchicalConfiguration<ImmutableNode> groupsCompCfg : groupsCompCfgs) {
                boolean enabled = groupsCompCfg.getBoolean("[@enabled]");
                Group sourceGroup = Group.ofOrNull(groupsCompCfg.getString("[@sourceGroup]"));
                Group compatibleGroup = Group.ofOrNull(groupsCompCfg.getString("[@compatibleGroup]"));
                boolean symmetric = groupsCompCfg.getBoolean("[@symmetric]", false);
                compatibilities.add(new CrossGroupCompatibilityRuleSettingsItem(new CrossGroupCompatibilityRule(sourceGroup, compatibleGroup, symmetric), enabled));
            }
            // Sort the cross-group compatibilities
            compatibilities.sort(null);
            return createObservableList(compatibilities);
        }

        @Override
        public void add(Configuration cfg, String key, ObservableList<CrossGroupCompatibilityRuleSettingsItem> list) {
            for (int i = 0; i < list.size(); i++) {
                CrossGroupCompatibilityRuleSettingsItem item = list.get(i);
                CrossGroupCompatibilityRule c = item.getItem();
                cfg.addProperty(key + ".crossGroupCompatibilityRule(" + i + ")[@enabled]", item.isEnabled());
                cfg.addProperty(key + ".crossGroupCompatibilityRule(" + i + ")[@sourceGroup]", c.getSourceGroup());
                cfg.addProperty(key + ".crossGroupCompatibilityRule(" + i + ")[@compatibleGroup]", c.getCompatibleGroup());
                cfg.addProperty(key + ".crossGroupCompatibilityRule(" + i + ")[@symmetric]", c.isSymmetric());
            }
        }
    }
}