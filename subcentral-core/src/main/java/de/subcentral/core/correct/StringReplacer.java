package de.subcentral.core.correct;

import java.util.Objects;
import java.util.function.UnaryOperator;

import com.google.common.base.MoreObjects;

public class StringReplacer implements UnaryOperator<String> {
    public enum Mode {
        ALL_OCCURENCES, COMPLETE
    }

    private final String searchString;
    private final String replacement;

    private final Mode   mode;

    public StringReplacer(String searchString, String replacement) {
        this(searchString, replacement, Mode.ALL_OCCURENCES);
    }

    public StringReplacer(String searchString, String replacement, Mode mode) {
        this.searchString = searchString;
        this.replacement = replacement;
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public String getSearchString() {
        return searchString;
    }

    public String getReplacement() {
        return replacement;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public String apply(String text) {
        switch (mode) {
            case ALL_OCCURENCES:
                return text != null ? text.replace(searchString, replacement) : null;
            case COMPLETE:
                if (Objects.equals(text, searchString)) {
                    return replacement;
                }
                return text;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof StringReplacer) {
            StringReplacer o = (StringReplacer) obj;
            return Objects.equals(searchString, o.searchString) && Objects.equals(replacement, o.replacement) && mode == o.mode;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchString, replacement, mode);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(StringReplacer.class).add("searchString", searchString).add("replacement", replacement).add("mode", mode).toString();
    }
}
