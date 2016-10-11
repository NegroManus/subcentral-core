package de.subcentral.core.parse;

import java.util.Map;
import java.util.function.Function;

import de.subcentral.core.util.SimplePropDescriptor;

public interface Mapper<T> extends Function<Map<SimplePropDescriptor, String>, T> {
    public T map(Map<SimplePropDescriptor, String> props);

    @Override
    public default T apply(Map<SimplePropDescriptor, String> props) {
        return map(props);
    }
}
