package de.subcentral.core.correct;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public class Correction {
    private final Object       bean;
    private final String       propertyName;
    private final Object       oldValue;
    private final Object       newValue;
    private final Corrector<?> corrector;

    public Correction(Object bean, String propertyName, Object oldValue, Object newValue, Corrector<?> corrector) {
        this.bean = Objects.requireNonNull(bean, "bean");
        this.propertyName = Objects.requireNonNull(propertyName, "propertyName");
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.corrector = corrector; // can be null if lambda
    }

    public Object getBean() {
        return bean;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Corrector<?> getCorrector() {
        return corrector;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Correction.class)
                .add("propertyName", propertyName)
                .add("oldValue", oldValue)
                .add("newValue", newValue)
                .add("bean", bean)
                .add("corrector", corrector)
                .toString();
    }
}
