package de.subcentral.core.file.subtitle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.Contributor;

public class ContributionParser
{
	private final List<ContributionTypePattern>	contributionTypePatterns;
	private final Set<String>					knownContributors;
	private final Set<String>					knownNonContributors;

	public ContributionParser(List<ContributionTypePattern> contributionTypePatterns, Set<String> knownContributors, Set<String> knownNonContributors)
	{
		this.contributionTypePatterns = ImmutableList.copyOf(contributionTypePatterns);
		this.knownContributors = ImmutableSet.copyOf(knownContributors);
		this.knownNonContributors = ImmutableSet.copyOf(knownNonContributors);
	}

	public List<ContributionTypePattern> getContributionTypePatterns()
	{
		return contributionTypePatterns;
	}

	public Set<String> getKnownContributors()
	{
		return knownContributors;
	}

	public Set<String> getKnownNonContributors()
	{
		return knownNonContributors;
	}

	public Set<Contribution> parse(SubtitleFile data)
	{
		Set<Contribution> contributions = new HashSet<>();
		for (Item item : data.getItems())
		{
			parseItemText(item.getText(), contributions);
		}
		return contributions;
	}

	private void parseItemText(String text, Set<Contribution> contributions)
	{
		List<Token> tokens = tokenize(text);
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
						contributions.add(new Contribution(new Subber(text.substring(token.start, token.end)), null));
					}
					else
					{
						for (String contributionType : currentContributionTypes)
						{
							contributions.add(new Contribution(new Subber(text.substring(token.start, token.end)), contributionType));
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
		// remove html tags
		Matcher tagMatcher = Pattern.compile("<[^>]+>").matcher(normalizedText);
		while (tagMatcher.find())
		{
			normalizedText = replaceTokenWithSpaces(normalizedText, tagMatcher.start(), tagMatcher.end());
		}

		List<Token> tokens = new ArrayList<>();
		for (String knownContributor : knownContributors)
		{
			int start = normalizedText.indexOf(knownContributor);
			if (start != -1)
			{
				int end = start + knownContributor.length();
				tokens.add(Token.forContributor(start, end));
				normalizedText = replaceTokenWithSpaces(normalizedText, start, end);
			}
		}
		for (String knownNonContributor : knownNonContributors)
		{
			int start = normalizedText.indexOf(knownNonContributor);
			if (start != -1)
			{
				int end = start + knownNonContributor.length();
				normalizedText = replaceTokenWithSpaces(normalizedText, start, end);
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
			scanner.useDelimiter(Pattern.compile("[,\\s]+"));
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

	public static class Subber implements Contributor
	{
		private final String	name;

		public Subber(String name)
		{
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		// Object methods
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof Subber)
			{
				return Objects.equals(name, ((Subber) obj).name);
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(973, 59).append(name).toHashCode();
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(Subber.class).omitNullValues().add("name", name).toString();
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
