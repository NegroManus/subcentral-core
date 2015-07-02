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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger(ContributionParser.class);

    private ImmutableList<ContributionPattern>	   contributionPatterns	    = ImmutableList.of();
    private ImmutableList<ContributionTypePattern> contributionTypePatterns = ImmutableList.of();
    private ImmutableList<Pattern>		   knownContributors	    = ImmutableList.of();
    private ImmutableList<Pattern>		   knownNonContributors	    = ImmutableList.of();
    private StandardizingService		   standardizingService	    = null;

    public ImmutableList<ContributionPattern> getContributionPatterns()
    {
	return contributionPatterns;
    }

    public void setContributionPatterns(Iterable<? extends ContributionPattern> contributionPatterns)
    {
	this.contributionPatterns = ImmutableList.copyOf(contributionPatterns);
    }

    public ImmutableList<ContributionTypePattern> getContributionTypePatterns()
    {
	return contributionTypePatterns;
    }

    public void setContributionTypePatterns(Iterable<? extends ContributionTypePattern> contributionTypePatterns)
    {
	this.contributionTypePatterns = ImmutableList.copyOf(contributionTypePatterns);
    }

    public ImmutableList<Pattern> getKnownContributors()
    {
	return knownContributors;
    }

    public void setKnownContributors(Iterable<? extends Pattern> knownContributors)
    {
	this.knownContributors = ImmutableList.copyOf(knownContributors);
    }

    public ImmutableList<Pattern> getKnownNonContributors()
    {
	return knownNonContributors;
    }

    public void setKnownNonContributors(Iterable<? extends Pattern> knownNonContributors)
    {
	this.knownNonContributors = ImmutableList.copyOf(knownNonContributors);
    }

    public StandardizingService getStandardizingService()
    {
	return standardizingService;
    }

    public void setStandardizingService(StandardizingService standardizingService)
    {
	this.standardizingService = standardizingService;
    }

    public List<Contribution> parse(SubtitleFile data)
    {
	List<Contribution> contributions = new ArrayList<>();
	for (Item item : data.getItems())
	{
	    parseItemText(item.getText(), contributions);
	}
	contributions = clean(contributions);
	return contributions;
    }

    private void parseItemText(String text, List<Contribution> contributions)
    {
	String normalizedText = normalizeText(text);

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
		case CONTRIBUTION:
		    addContribution(contributions, token.contributionType, token.contributor);
		    break;
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
			String contributor = normalizedText.substring(token.start, token.end);
			addContribution(contributions, null, contributor);
		    }
		    else
		    {
			// add contributions for this contributor (one per contributionType)
			for (String contributionType : currentContributionTypes)
			{
			    String name = normalizedText.substring(token.start, token.end);
			    addContribution(contributions, contributionType, name);
			}
		    }
		    break;
		default:
		    throw new AssertionError();
	    }
	    typeOfLastToken = token.type;
	}
    }

    private void addContribution(List<Contribution> list, String contributionType, String contributor)
    {
	Subber subber = new Subber(contributor);
	if (standardizingService != null)
	{
	    List<StandardizingChange> changes = standardizingService.standardize(subber);
	    changes.forEach((c) -> log.debug("Standardized contributor: {}", c));
	}
	list.add(new Contribution(subber, contributionType));
    }

    private String normalizeText(String text)
    {
	// remove html tags like <b>
	text = Jsoup.parse(text).text();
	// remove SRT tags like {\an8}
	text = text.replaceAll("\\{\\\\.*?\\}", "");
	return text;
    }

    private List<Token> tokenize(String text)
    {
	String normalizedText = text;
	boolean isCreditItem = false;
	List<Token> tokens = new ArrayList<>();

	for (ContributionPattern pattern : contributionPatterns)
	{
	    Matcher m = pattern.getPattern().matcher(normalizedText);
	    while (m.find())
	    {
		isCreditItem = true;
		String contributionType = m.group(pattern.contributionTypeGroup);
		for (ContributionTypePattern contrTypePattern : contributionTypePatterns)
		{
		    Matcher contrTypeMatcher = contrTypePattern.getPattern().matcher(contributionType);
		    if (contrTypeMatcher.matches())
		    {
			contributionType = contrTypePattern.getContributionType();
		    }
		}
		tokens.add(Token.forContribution(m.start(), m.end(), contributionType, m.group(pattern.contributorGroup)));
		normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
	    }
	}

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

	if (isCreditItem)
	{
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
	}

	// sort by token start index
	tokens.sort((Token t1, Token t2) -> t1.start - t2.start);
	return tokens;
    }

    private List<Contribution> clean(List<Contribution> contributions)
    {
	List<Contribution> list = contributions.stream().distinct().collect(Collectors.toList());

	return list;
    }

    private static String replaceTokenWithSpaces(String text, int start, int end)
    {
	return new StringBuilder(text).replace(start, end, StringUtils.repeat(' ', end - start)).toString();
    }

    private static class Token
    {
	private static enum Type
	{
	    CONTRIBUTION, CONTRIBUTION_TYPE, CONTRIBUTOR
	}

	private final int    start;
	private final int    end;
	private final Type   type;
	private final String contributionType;
	private final String contributor;

	private static Token forContribution(int start, int end, String contributionType, String contributor)
	{
	    return new Token(start, end, Type.CONTRIBUTION, contributionType, contributor);
	}

	private static Token forContributionType(int start, int end, String contributionType)
	{
	    return new Token(start, end, Type.CONTRIBUTION_TYPE, contributionType, null);
	}

	private static Token forContributor(int start, int end)
	{
	    return new Token(start, end, Type.CONTRIBUTOR, null, null);
	}

	private Token(int start, int end, Type type, String contributionType, String contributor)
	{
	    this.start = start;
	    this.end = end;
	    this.type = Objects.requireNonNull(type, "type");
	    this.contributionType = contributionType;
	    this.contributor = contributor;
	}
    }

    public static class ContributionPattern
    {
	private final Pattern pattern;
	private final int     contributionTypeGroup;
	private final int     contributorGroup;

	public ContributionPattern(Pattern pattern, int contributionTypeGroup, int contributorGroup)
	{
	    this.pattern = pattern;
	    this.contributionTypeGroup = contributionTypeGroup;
	    this.contributorGroup = contributorGroup;
	}

	public Pattern getPattern()
	{
	    return pattern;
	}

	public int getContributionTypeGroup()
	{
	    return contributionTypeGroup;
	}

	public int getContributorGroup()
	{
	    return contributorGroup;
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
