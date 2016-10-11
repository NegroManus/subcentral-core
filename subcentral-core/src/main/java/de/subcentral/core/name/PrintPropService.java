package de.subcentral.core.name;

import de.subcentral.core.util.SimplePropDescriptor;

@FunctionalInterface
public interface PrintPropService {
    public String print(SimplePropDescriptor propDescriptor, Object property);
}
