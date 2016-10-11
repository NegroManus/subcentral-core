package de.subcentral.core.parse;

import java.util.Map;
import java.util.function.Function;

public interface MappingMatcher<K> extends Function<String, Map<K, String>> {
    /**
     * 
     * @param text
     *            the text to
     * @return the mapped groups. If the matcher does no match, an empty map is returned
     */
    public Map<K, String> match(String text);

    @Override
    public default Map<K, String> apply(String t) {
        return match(t);
    }
}