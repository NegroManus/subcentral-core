package de.subcentral.mig;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.Contributor;

public class ScContributor implements Contributor, Serializable {
    private static final long serialVersionUID = -6276780419437614553L;

    public enum Type {
        SUBBER, GROUP
    }

    private final Type type;
    private String     name;
    private int        id;

    public ScContributor(Type type) {
        this(type, null, 0);
    }

    public ScContributor(Type type, String name) {
        this(type, name, 0);
    }

    public ScContributor(Type type, String name, int id) {
        this.type = Objects.requireNonNull(type, "type");
        this.name = name;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ScContributor) {
            ScContributor o = (ScContributor) obj;
            return type == o.type && Objects.equals(name, o.name) && id == o.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(973, 59).append(ScContributor.class).append(type).append(name).append(id).toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues().add("type", type).add("name", name).add("id", id).toString();
    }
}