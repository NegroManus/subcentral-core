package de.subcentral.fx.settings;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.CollectionUtil;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ListSettingsProperty<E> extends ObjectSettingsPropertyBase<ObservableList<E>, ListProperty<E>> {
    private static final Logger log = LogManager.getLogger(ListSettingsProperty.class);

    public ListSettingsProperty(String key, ConfigurationPropertyHandler<ObservableList<E>> handler) {
        this(key, handler, FXCollections.observableArrayList());
    }

    public ListSettingsProperty(String key, ConfigurationPropertyHandler<ObservableList<E>> handler, ObservableList<E> initialValue) {
        super(key, (Object bean, String name) -> new SimpleListProperty<>(bean, name, initialValue), handler);
    }

    public void update(ImmutableConfiguration cfg, boolean add, boolean replace, boolean remove, BiPredicate<? super E, ? super E> equalTester) {
        try {
            List<E> origList = property;
            List<E> updateList = loadValue(cfg);
            CollectionUtil.updateList(origList, updateList, add, replace, remove, equalTester);
        }
        catch (Exception e) {
            log.error("Exception while loading settings property [" + key + "] from configuration", e);
        }
    }

    public void updateSorted(ImmutableConfiguration cfg, boolean add, boolean replace, boolean remove, BiPredicate<? super E, ? super E> equalTester, Comparator<? super E> comparator) {
        try {
            List<E> origList = property;
            List<E> updateList = loadValue(cfg);
            CollectionUtil.updateSortedList(origList, updateList, add, replace, remove, equalTester, comparator);
        }
        catch (Exception e) {
            log.error("Exception while loading settings property [" + key + "] from configuration", e);
        }
    }
}
