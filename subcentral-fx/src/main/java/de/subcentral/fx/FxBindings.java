package de.subcentral.fx;

import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class FxBindings {
    static class ImmutableObservableValue<T> implements ObservableValue<T> {
        private final T value;

        ImmutableObservableValue(T value) {
            this.value = value;
        }

        @Override
        public void addListener(InvalidationListener listener) {
            // not needed because value never changes
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            // not needed because value never changes
        }

        @Override
        public void addListener(ChangeListener<? super T> listener) {
            // not needed because value never changes
        }

        @Override
        public void removeListener(ChangeListener<? super T> listener) {
            // not needed because value never changes
        }

        @Override
        public T getValue() {
            return value;
        }
    }

    private FxBindings() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static <T> Binding<T> immutableBinding(final T value) {
        return new ObjectBinding<T>() {
            @Override
            protected T computeValue() {
                return value;
            }
        };
    }

    public static BooleanBinding immutableBooleanBinding(final boolean value) {
        return new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return value;
            }
        };
    }

    public static StringBinding immutableStringBinding(final String value) {
        return new StringBinding() {
            @Override
            protected String computeValue() {
                return value;
            }
        };
    }

    public static <E> ListBinding<E> immutableListBinding(final ObservableList<E> value) {
        return new ListBinding<E>() {
            @Override
            protected ObservableList<E> computeValue() {
                return value;
            }
        };
    }

    public static <E> Observable observeBeanList(ObservableList<E> list, Function<E, Observable[]> propertiesExtractor) {
        ObservableHelper obsv = new ObservableHelper(null);
        // Observe the list itself
        obsv.getDependencies().add(list);
        // Observe the properties of the current list content
        for (E bean : list) {
            for (Observable o : propertiesExtractor.apply(bean)) {
                obsv.getDependencies().add(o);
            }
        }
        // React on list changes
        list.addListener(new ListChangeListener<E>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends E> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (E bean : c.getRemoved()) {
                            // remove listener for properties
                            for (Observable o : propertiesExtractor.apply(bean)) {
                                obsv.getDependencies().remove(o);
                            }
                        }
                    }
                    if (c.wasAdded()) {
                        for (E bean : c.getAddedSubList()) {
                            // add listener for properties
                            for (Observable o : propertiesExtractor.apply(bean)) {
                                obsv.getDependencies().add(o);
                            }
                        }
                    }
                }
            }
        });
        return obsv;
    }

    public static <K, V> Observable observeBeanMap(ObservableMap<K, V> map, Function<V, Observable[]> propertiesExtractor) {
        ObservableHelper obsv = new ObservableHelper(null);
        // Observe the list itself
        obsv.getDependencies().add(map);
        // Observe the properties of the current list content
        for (V value : map.values()) {
            for (Observable o : propertiesExtractor.apply(value)) {
                obsv.getDependencies().add(o);
            }
        }
        // React on list changes
        map.addListener(new MapChangeListener<K, V>() {
            @Override
            public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
                if (change.wasRemoved()) {
                    for (Observable o : propertiesExtractor.apply(change.getValueRemoved())) {
                        obsv.getDependencies().remove(o);
                    }
                }
                if (change.wasAdded()) {
                    for (Observable o : propertiesExtractor.apply(change.getValueAdded())) {
                        obsv.getDependencies().add(o);
                    }
                }
            }
        });
        return obsv;
    }

    public static <T> Observable observeBean(ReadOnlyProperty<T> bean, Function<T, Observable[]> propertiesExtractor) {
        ObservableHelper obsv = new ObservableHelper(null);
        // Observe the bean itself
        obsv.getDependencies().add(bean);
        // Observe the properties of the current bean
        if (bean.getValue() != null) {
            for (Observable o : propertiesExtractor.apply(bean.getValue())) {
                obsv.getDependencies().add(o);
            }
        }
        // React on changes
        bean.addListener(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                if (oldValue != null) {
                    for (Observable o : propertiesExtractor.apply(oldValue)) {
                        obsv.getDependencies().remove(o);
                    }
                }
                if (newValue != null) {
                    for (Observable o : propertiesExtractor.apply(newValue)) {
                        obsv.getDependencies().add(o);
                    }
                }
            }
        });
        return obsv;
    }

    public static <T> ObservableValue<T> immutableObservableValue(T value) {
        return new ImmutableObservableValue<>(value);
    }
}
