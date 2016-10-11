package de.subcentral.core.metadata.release;

import java.io.Serializable;

import com.google.common.base.MoreObjects;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.ValidationUtil;

/**
 * Value object.
 * 
 * @implSpec #value-object #immutable #thread-safe
 */
public class Group implements Comparable<Group>, Serializable {
    private static final long serialVersionUID = -8704261988899599068L;

    private final String      name;

    private Group(String name) {
        this.name = ValidationUtil.requireNotBlankAndStrip(name, "name cannot be blank");
    }

    /**
     * 
     * @param name
     * @return
     * @throws IllegalArgumentException
     */
    public static Group of(String name) {
        return new Group(name);
    }

    public static Group ofOrNull(String name) {
        try {
            return of(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Group) {
            return ObjectUtil.stringEqualIgnoreCase(name, ((Group) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ObjectUtil.stringHashCodeIgnoreCase(name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("name", name).toString();
    }

    @Override
    public int compareTo(Group o) {
        if (this == o) {
            return 0;
        }
        // nulls first
        if (o == null) {
            return 1;
        }
        return ObjectUtil.getDefaultStringOrdering().compare(name, o.name);
    }
}
