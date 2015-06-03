package de.subcentral.mig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.file.subtitle.Item;
import de.subcentral.core.file.subtitle.SubtitleFile;
import de.subcentral.core.metadata.Contribution;

public class ContributionParser
{
	private final List<ContributionTypePattern>		contributionTypePatterns;
	private final Set<Pattern>						knownContributors;
	private final Set<Pattern>						knownNonContributors;
	private final List<Function<String, String>>	contributorReplacers;

	public ContributionParser(List<ContributionTypePattern> contributionTypePatterns, Set<Pattern> knownContributors,
			Set<Pattern> knownNonContributors, List<Function<String, String>> contributorReplacers)
	{
		this.contributionTypePatterns = ImmutableList.copyOf(contributionTypePatterns);
		this.knownContributors = ImmutableSet.copyOf(knownContributors);
		this.knownNonContributors = ImmutableSet.copyOf(knownNonContributors);
		this.contributorReplacers = contributorReplacers;
	}

	public List<ContributionTypePattern> getContributionTypePatterns()
	{
		return contributionTypePatterns;
	}

	public Set<Pattern> getKnownContributors()
	{
		return knownContributors;
	}

	public Set<Pattern> getKnownNonContributors()
	{
		return knownNonContributors;
	}

	public List<Function<String, String>> getContributorReplacers()
	{
		return contributorReplacers;
	}

	public List<Contribution> parse(SubtitleFile data)
	{
		List<Contribution> contributions = new ArrayList<>();
		for (Item item : data.getItems())
		{
			parseItemText(item.getText(), contributions);
		}
		contributions = contributions.stream().distinct().collect(Collectors.toList());
		return contributions;
	}

	private void parseItemText(String text, List<Contribution> contributions)
	{
		String normalizedText = text;
		// remove html tags
		normalizedText = Jsoup.parse(normalizedText).text();

		List<Token> tokens = tokenize(normalizedText);
		if (tokens.isEmpty())
		{
			return;
		}

		Set<String> currentContributionTypes = new HashSet<>();
		Token.Type typeOfLastToken = null;
		for (Token token : tokens)
		{
			switch (token.type)
			{
				case CONTRIBUTION_TYPE:
					if (Token.Type.CONTRIBUTOR == typeOfLastToken)
					{
						// if last token was a contributor, assume that a new contribution list is started
						currentContributionTypes.clear();
					}
					currentContributionTypes.add(token.contributionType);
					break;
				case CONTRIBUTOR:
					if (currentContributionTypes.isEmpty())
					{
						// contributions.add(new Contribution(new Subber(text.substring(token.start, token.end)), null));
					}
					else
					{
						for (String contributionType : currentContributionTypes)
						{
							String name = standardizeContributor(normalizedText.substring(token.start, token.end));
							contributions.add(new Contribution(new Subber(name), contributionType));
						}
					}
					break;
				default:
					throw new AssertionError();
			}
			typeOfLastToken = token.type;
		}
	}

	private List<Token> tokenize(String text)
	{
		String normalizedText = text;

		List<Token> tokens = new ArrayList<>();
		for (Pattern knownContributor : knownContributors)
		{
			Matcher m = knownContributor.matcher(normalizedText);
			while (m.find())
			{
				tokens.add(Token.forContributor(m.start(), m.end()));
				normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
			}
		}
		for (Pattern knownNonContributor : knownNonContributors)
		{
			Matcher m = knownNonContributor.matcher(normalizedText);
			while (m.find())
			{
				normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
			}
		}
		boolean isCreditItem = false;
		for (ContributionTypePattern mapping : contributionTypePatterns)
		{
			Matcher m = mapping.getPattern().matcher(normalizedText);
			while (m.find())
			{
				isCreditItem = true;
				tokens.add(Token.forContributionType(m.start(), m.end(), mapping.getContributionType()));
				normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
			}
		}

		if (!isCreditItem)
		{
			return ImmutableList.of();
		}

		// tokenize the remaining of the textnormalizedText
		try (Scanner scanner = new Scanner(normalizedText);)
		{
			// "\\W*(&|und|and|,)\\W*"
			scanner.useDelimiter(Pattern.compile("[\\s,/+]+"));
			while (scanner.hasNext())
			{
				String token = scanner.next();
				// contains letter or digit
				boolean isWord = CharMatcher.JAVA_LETTER_OR_DIGIT.matchesAnyOf(token);
				if (isWord)
				{
					int start = normalizedText.indexOf(token);
					int end = start + token.length();
					tokens.add(Token.forContributor(start, end));
				}
			}
		}

		// sort by token index
		tokens.sort((Token t1, Token t2) -> t1.start - t2.start);
		return tokens;
	}

	private String standardizeContributor(String name)
	{
		for (Function<String, String> replacer : contributorReplacers)
		{
			name = replacer.apply(name);
		}
		return name;
	}

	private static String replaceTokenWithSpaces(String text, int start, int end)
	{
		return new StringBuilder(text).replace(start, end, StringUtils.repeat(' ', end - start)).toString();
	}

	private static class Token
	{
		private static enum Type
		{
			CONTRIBUTION_TYPE, CONTRIBUTOR
		}

		private final int		start;
		private final int		end;
		private final Type		type;
		private final String	contributionType;

		private static Token forContributionType(int start, int end, String contributionType)
		{
			return new Token(start, end, Type.CONTRIBUTION_TYPE, contributionType);
		}

		private static Token forContributor(int start, int end)
		{
			return new Token(start, end, Type.CONTRIBUTOR, null);
		}

		private Token(int start, int end, Type type, String contributionType)
		{
			this.start = start;
			this.end = end;
			this.type = Objects.requireNonNull(type, "type");
			this.contributionType = contributionType;
		}
	}

	public static class ContributionTypePattern
	{
		private final Pattern	pattern;
		private final String	contributionType;

		public ContributionTypePattern(Pattern pattern, String contributionType)
		{
			this.pattern = Objects.requireNonNull(pattern, "pattern");
			this.contributionType = contributionType;
		}

		public Pattern getPattern()
		{
			return pattern;
		}

		public String getContributionType()
		{
			return contributionType;
		}
	}
}
