package de.subcentral.core.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Objects;

import com.google.common.collect.ComparisonChain;

/**
 * Class for defining a bean / entity property. Unlike {@link PropertyDescriptor} no further checks are conducted. This class is simply a container for a Class value and a String value.
 * <code>SimplePropertyDescriptor</code> instances are not equal just because their write and read methods are equal due to inheritance - like it is the case with <code>PropertyDescriptor</code>.
 *
 */
public class SimplePropDescriptor implements Comparable<SimplePropDescriptor>, Serializable {
    private static final long serialVersionUID = 8306594862187006921L;

    private final Class<?>    beanClass;
    private final String      propName;

    public SimplePropDescriptor(Class<?> beanClass, String propName) {
        this.beanClass = Objects.requireNonNull(beanClass, "beanClass");
        this.propName = Objects.requireNonNull(propName, "propName");
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getPropName() {
        return propName;
    }

    // Convenience
    public String getName() {
        return beanClass.getName() + "." + propName;
    }

    public String getSimpleName() {
        return beanClass.getSimpleName() + "." + propName;
    }

    public PropertyDescriptor toPropertyDescriptor() throws IntrospectionException {
        return new PropertyDescriptor(propName, beanClass);
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimplePropDescriptor) {
            SimplePropDescriptor o = (SimplePropDescriptor) obj;
            return beanClass == o.beanClass && propName.equals(o.propName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanClass, propName);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(SimplePropDescriptor o) {
        if (this == o) {
            return 0;
        }
        // nulls first
        if (o == null) {
            return 1;
        }
        return ComparisonChain.start()
                .compare(beanClass.getName(), o.beanClass.getName(), ObjectUtil.getDefaultStringOrdering())
                .compare(propName, o.propName, ObjectUtil.getDefaultStringOrdering())
                .result();
    }
}
