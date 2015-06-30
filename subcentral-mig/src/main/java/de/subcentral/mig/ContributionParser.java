package de.subcentral.mig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.file.subtitle.Item;
import de.subcentral.core.file.subtitle.SubtitleFile;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.standardizing.StandardizingChange;
import de.subcentral.core.standardizing.StandardizingService;

public class ContributionParser
{
    private final List<ContributionTypePattern>	contributionTypePatterns;
    private final List<Pattern>			knownContributors;
    private final List<Pattern>			knownNonContributors;
    private final StandardizingService		standardizingService;

    public ContributionParser(List<ContributionTypePattern> contributionTypePatterns, List<Pattern> knownContributors, List<Pattern> knownNonContributors, StandardizingService standardizingService)
    {
	this.contributionTypePatterns = ImmutableList.copyOf(contributionTypePatterns);
	this.knownContributors = ImmutableList.copyOf(knownContributors);
	this.knownNonContributors = ImmutableList.copyOf(knownNonContributors);
	this.standardizingService = standardizingService;
    }

    public List<ContributionTypePattern> getContributionTypePatterns()
    {
	return contributionTypePatterns;
    }

    public List<Pattern> getKnownContributors()
    {
	return knownContributors;
    }

    public List<Pattern> getKnownNonContributors()
    {
	return knownNonContributors;
    }

    public StandardizingService getStandardizingService()
    {
	return standardizingService;
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
			    String name = normalizedText.substring(token.start, token.end);
			    Subber subber = new Subber(name);
			    List<StandardizingChange> changes = standardizingService.standardize(subber);
			    changes.forEach(System.out::println);
			    contributions.add(new Contribution(subber, contributionType));
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

	// tokenize the remaining of the normalizedText
	try (Scanner scanner = new Scanner(normalizedText);)
	{
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

	private final int    start;
	private final int    end;
	private final Type   type;
	private final String contributionType;

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
	private final Pattern pattern;
	private final String  contributionType;

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
