package de.subcentral.core.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public class PatternMappingMatcher<K> implements MappingMatcher<K> {
    /**
     * This separator is used to separate multiple values for the same key. These values are concatenated, using this separator.
     */
    public static final String    VALUES_WITH_SAME_KEY_SEPARATOR = " ";

    private final Pattern         pattern;
    private final Map<Integer, K> groups;
    private final Map<K, String>  predefinedMatches;

    public PatternMappingMatcher(Pattern pattern, Map<Integer, K> groups) {
        this(pattern, groups, ImmutableMap.of());
    }

    public PatternMappingMatcher(Pattern pattern, Map<Integer, K> groups, Map<K, String> predefinedMatches) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.groups = ImmutableMap.copyOf(groups); // includes null checks
        this.predefinedMatches = ImmutableMap.copyOf(predefinedMatches); // includes null checks
    }

    public Pattern getPattern() {
        return pattern;
    }

    /**
     * A Map:
     * <ul>
     * <li>Key: group number (0: whole match string, 1-n: capturing pattern groups)</li>
     * <li>Value: group key</li>
     * </ul>
     * 
     * @return
     */
    public Map<Integer, K> getGroups() {
        return groups;
    }

    public Map<K, String> getPredefinedMatches() {
        return predefinedMatches;
    }

    /**
     * @throws IndexOutOfBoundsException
     *             if there is no capturing group in the {@link #getPattern() pattern} associated with an index specified in {@link #getGroups()}.
     */
    @Override
    public Map<K, String> match(String text) throws IndexOutOfBoundsException {
        if (text == null) {
            return ImmutableMap.of();
        }
        Matcher m = pattern.matcher(text);
        if (m.matches()) {
            Map<K, String> mappedGroups = new HashMap<>(groups.size() + predefinedMatches.size());
            mappedGroups.putAll(predefinedMatches);
            for (Map.Entry<Integer, K> entry : groups.entrySet()) {
                K groupKey = entry.getValue();
                String groupValue = m.group(entry.getKey());
                // groupVal can be null for optional groups
                if (groupValue != null) {
                    // concat the values if multiple groups have the same key
                    mappedGroups.merge(groupKey, groupValue, (String oldVal, String newVal) -> oldVal + VALUES_WITH_SAME_KEY_SEPARATOR + newVal);
                }
            }
            return mappedGroups;
        }
        return ImmutableMap.of();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("pattern", pattern).add("groups", groups).add("predefinedMatches", predefinedMatches).toString();
    }
}
