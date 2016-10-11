package de.subcentral.core.util;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;

public class StringUtil {
    public static final Joiner   COMMA_JOINER        = Joiner.on(", ").skipNulls();
    public static final Splitter COMMA_SPLITTER      = Splitter.on(',').omitEmptyStrings().trimResults();

    public static final Joiner   SPACE_JOINER        = Joiner.on(" ").skipNulls();
    public static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE);

    private StringUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static boolean startsWith(StringBuilder sb, char c) {
        return sb.length() > 0 && sb.charAt(0) == c;
    }

    public static boolean startsWith(StringBuilder sb, CharSequence cs) {
        return cs != null && sb.length() >= cs.length() && sb.substring(0, cs.length()).equals(cs);
    }

    public static boolean endsWith(StringBuilder sb, char c) {
        return sb.length() > 0 && sb.charAt(sb.length() - 1) == c;
    }

    public static boolean endsWith(StringBuilder sb, CharSequence cs) {
        return cs != null && sb.length() >= cs.length() && sb.substring(sb.length() - cs.length()).equals(cs);
    }

    public static StringBuilder appendIfNotEndsWith(StringBuilder sb, char c) {
        if (!endsWith(sb, c)) {
            sb.append(c);
        }
        return sb;
    }

    public static StringBuilder appendIfNotEndsWith(StringBuilder sb, CharSequence cs) {
        if (!StringUtils.endsWith(sb, cs)) {
            sb.append(cs);
        }
        return sb;
    }

    public static StringBuilder appendSpaceIfNotEndsWith(StringBuilder sb) {
        return appendIfNotEndsWith(sb, ' ');
    }

    public static StringBuilder stripStart(StringBuilder sb, char c) {
        if (startsWith(sb, c)) {
            sb.deleteCharAt(0);
        }
        return sb;
    }

    public static StringBuilder stripStart(StringBuilder sb, CharSequence cs) {
        while (startsWith(sb, cs)) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb;
    }

    public static StringBuilder stripStart(StringBuilder sb) {
        return stripStart(sb, ' ');
    }

    public static StringBuilder stripEnd(StringBuilder sb, char c) {
        while (endsWith(sb, c)) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb;
    }

    public static StringBuilder stripEnd(StringBuilder sb, CharSequence cs) {
        while (endsWith(sb, cs)) {
            sb.delete(sb.length() - cs.length(), sb.length());
        }
        return sb;
    }

    public static StringBuilder stripEnd(StringBuilder sb) {
        return stripEnd(sb, ' ');
    }

    public static StringBuilder trim(StringBuilder sb) {
        stripStart(sb);
        return stripEnd(sb);
    }

    public static String quoteString(String s) {
        if (s == null) {
            return null;
        }
        return '"' + s + '"';
    }

    public static String readerToString(Reader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        String s = CharStreams.toString(reader);
        reader.close();
        return s;
    }
}
