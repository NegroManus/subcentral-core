package de.subcentral.watcher.settings;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.correct.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import javafx.util.StringConverter;

public class PatternToLanguageMapping implements Map.Entry<UserPattern, Locale>, Comparable<PatternToLanguageMapping> {
	public static final StringConverter<PatternToLanguageMapping>	STRING_CONVERTER	= initStringConverter();

	private final UserPattern										pattern;
	private final Locale											language;

	public PatternToLanguageMapping(UserPattern pattern, Locale language) {
		this.pattern = Objects.requireNonNull(pattern, "pattern");
		this.language = Objects.requireNonNull(language, "language");
	}

	public UserPattern getPattern() {
		return pattern;
	}

	public Locale getLanguage() {
		return language;
	}

	public LanguagePattern toLanguagePattern() {
		return new LanguagePattern(pattern.toPattern(), language);
	}

	// Map.Entry implementation
	@Override
	public UserPattern getKey() {
		return pattern;
	}

	@Override
	public Locale getValue() {
		return language;
	}

	@Override
	public Locale setValue(Locale value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof PatternToLanguageMapping) {
			PatternToLanguageMapping o = (PatternToLanguageMapping) obj;
			return pattern.equals(o.pattern) && language.equals(language);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(73, 113).append(pattern).append(language).toHashCode();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PatternToLanguageMapping.class).omitNullValues().add("pattern", pattern).add("language", language).toString();
	}

	@Override
	public int compareTo(PatternToLanguageMapping o) {
		// nulls first
		if (o == null) {
			return 1;
		}
		return ComparisonChain.start().compare(pattern, o.pattern).compare(language, o.language, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR).result();
	}

	private static StringConverter<PatternToLanguageMapping> initStringConverter() {
		return new StringConverter<PatternToLanguageMapping>() {
			@Override
			public String toString(PatternToLanguageMapping pattern) {
				return pattern.pattern.getPattern() + " (" + pattern.pattern.getMode() + ") -> " + pattern.language.getDisplayName();
			}

			@Override
			public PatternToLanguageMapping fromString(String string) {
				throw new UnsupportedOperationException();
			}
		};
	}
}
