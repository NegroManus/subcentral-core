package de.subcentral.fx;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

import de.subcentral.watcher.settings.WatcherSettings.PatternMode;

public class UiPattern
{
	private final String		pattern;
	private final PatternMode	patternMode;
	private final Pattern		compiledPattern;

	public UiPattern(String pattern, PatternMode patternMode) throws PatternSyntaxException
	{
		this.pattern = Objects.requireNonNull(pattern, "pattern");
		this.patternMode = Objects.requireNonNull(patternMode, "patternMode");
		this.compiledPattern = compilePattern(pattern, patternMode);
	}

	public String getPattern()
	{
		return pattern;
	}

	public PatternMode getPatternMode()
	{
		return patternMode;
	}

	public Pattern getCompiledPattern()
	{
		return compiledPattern;
	}

	private static Pattern compilePattern(String pattern, PatternMode patternMode) throws PatternSyntaxException
	{
		switch (patternMode)
		{
			case LITERAL:
				return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL);
			case SIMPLE:
				return parseSimplePattern(pattern);
			case REGEX:
				return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			default:
				throw new IllegalStateException();
		}
	}

	public static Pattern parseSimplePatterns(String simplePatternsString)
	{
		if (StringUtils.isBlank(simplePatternsString))
		{
			return null;
		}
		List<String> simplePatterns = Splitter.on(Pattern.compile("\\s*,\\s*")).omitEmptyStrings().splitToList(simplePatternsString);
		String[] convertedPatterns = new String[simplePatterns.size()];
		for (int i = 0; i < simplePatterns.size(); i++)
		{
			convertedPatterns[i] = convertToPattern(simplePatterns.get(i));
		}
		if (convertedPatterns.length == 1)
		{
			return Pattern.compile(convertedPatterns[0], Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		}
		else
		{
			StringJoiner strJoiner = new StringJoiner("|", "(", ")");
			for (String p : convertedPatterns)
			{
				strJoiner.add(p);
			}
			return Pattern.compile(strJoiner.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		}
	}

	public static Pattern parseSimplePattern(String simplePattern)
	{
		return Pattern.compile(convertToPattern(simplePattern), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}

	private static String convertToPattern(String simplePattern)
	{
		StringBuilder convertedPattern = new StringBuilder();
		Matcher mAsterisk = Pattern.compile("\\*").matcher(simplePattern);
		int index = 0;
		while (mAsterisk.find())
		{
			String head = simplePattern.substring(index, mAsterisk.start());
			if (!head.isEmpty())
			{
				convertedPattern.append(Pattern.quote(head));
			}
			convertedPattern.append(".*");
			index = mAsterisk.end();
		}
		String tail = simplePattern.substring(index);
		if (!tail.isEmpty())
		{
			convertedPattern.append(Pattern.quote(tail));
		}
		return convertedPattern.toString();
	}
}