package de.subcentral.mig.repo_old;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.name.NamingDefaults;

public class NetworkKey {
    private final String name;

    public NetworkKey(Network network) {
        this(network.getName());
    }

    public NetworkKey(String name) {
        this.name = NamingDefaults.getDefaultNormalizingFormatter().apply(name);
    }

    public String getName() {
        return name;
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NetworkKey) {
            return this.name.equals(((NetworkKey) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(761, 131).append(name).toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(NetworkKey.class).add("name", name).toString();
    }
}
