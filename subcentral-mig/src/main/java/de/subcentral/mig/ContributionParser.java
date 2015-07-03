package de.subcentral.mig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.file.subtitle.Item;
import de.subcentral.core.file.subtitle.SubtitleFile;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.Contributor;
import de.subcentral.core.standardizing.StandardizingChange;
import de.subcentral.core.standardizing.StandardizingService;

public class ContributionParser
{
    private static final Logger log = LogManager.getLogger(ContributionParser.class);

    private ImmutableList<ContributionPattern>	   contributionPatterns	    = ImmutableList.of();
    private ImmutableList<ContributionTypePattern> contributionTypePatterns = ImmutableList.of();
    private ImmutableList<ContributorPattern>	   contributorPatterns	    = ImmutableList.of();
    private ImmutableList<Pattern>		   irrelevantPatterns	    = ImmutableList.of();
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

    public ImmutableList<ContributorPattern> getContributorPatterns()
    {
	return contributorPatterns;
    }

    public void setContributorPatterns(Iterable<? extends ContributorPattern> contributorPatterns)
    {
	this.contributorPatterns = ImmutableList.copyOf(contributorPatterns);
    }

    public ImmutableList<Pattern> getIrrelevantPatterns()
    {
	return irrelevantPatterns;
    }

    public void setIrrelevantPatterns(Iterable<? extends Pattern> irrelevantPatterns)
    {
	this.irrelevantPatterns = ImmutableList.copyOf(irrelevantPatterns);
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
	String textOneLine = text.replace("\n", "||");

	List<Token> tokens = tokenize(normalizedText);
	if (tokens.isEmpty())
	{
	    return;
	}
	log.debug("Found credit item candidate. text: \"{}\" -> tokens: {}", textOneLine, tokens);
	int score = calculateCreditItemConfidence(tokens);
	if (score <= 0)
	{
	    log.info("Ignoring possible credit item because confidence score is not sufficient (score={}): {}", score, textOneLine);
	    return;
	}

	processTokens(tokens, contributions, normalizedText);

    }

    private void processTokens(List<Token> tokens, List<Contribution> contributions, String normalizedText)
    {
	Set<String> currentContributionTypes = new HashSet<>();
	Token.Type typeOfLastToken = null;
	for (Token token : tokens)
	{
	    switch (token.type)
	    {
		case CONTRIBUTION:
		    addContribution(contributions, token.contributor, token.contributionType);
		    break;
		case CONTRIBUTION_TYPE:
		    if (Token.Type.CONTRIBUTION_TYPE != typeOfLastToken)
		    {
			// if last token wasn't a contribution type, assume that a new contribution list is started
			currentContributionTypes.clear();
		    }
		    currentContributionTypes.add(token.contributionType);
		    break;
		case CONTRIBUTOR:
		    if (currentContributionTypes.isEmpty())
		    {
			addContribution(contributions, token.contributor, null);
		    }
		    else
		    {
			// add contributions for this contributor (one per contributionType)
			for (String contributionType : currentContributionTypes)
			{
			    addContribution(contributions, token.contributor, contributionType);
			}
		    }
		    break;
		case WORD:
		    if (currentContributionTypes.isEmpty())
		    {
			// do not add unrecognized words if no contribution type specified
			// addContribution(contributions, wordToContributor(normalizedText, token), null);
		    }
		    else
		    {
			// add contributions for this contributor (one per contributionType)
			for (String contributionType : currentContributionTypes)
			{
			    addContribution(contributions, wordToContributor(normalizedText, token), contributionType);
			}
		    }
		    break;
		default:
		    throw new AssertionError();
	    }
	    typeOfLastToken = token.type;
	}
    }

    private Contributor wordToContributor(String text, Token wordToken)
    {
	Subber subber = new Subber();
	subber.setName(text.substring(wordToken.start, wordToken.end));
	return subber;
    }

    private void addContribution(List<Contribution> list, Contributor contributor, String contributionType)
    {
	if (standardizingService != null)
	{
	    List<StandardizingChange> changes = standardizingService.standardize(contributor);
	    changes.forEach((c) -> log.debug("Standardized contributor: {}", c));
	}
	list.add(new Contribution(contributor, contributionType));
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
	boolean possibleCreditItem = false;
	List<Token> tokens = new ArrayList<>();

	for (ContributionPattern pattern : contributionPatterns)
	{
	    Matcher m = pattern.getPattern().matcher(normalizedText);
	    while (m.find())
	    {
		possibleCreditItem = true;

		Subber subber = new Subber();
		subber.setName(m.group(pattern.getContributorGroup()));
		Contributor contributor = subber;
		for (ContributorPattern contrPattern : contributorPatterns)
		{
		    Matcher contrMatcher = contrPattern.getPattern().matcher(contributor.getName());
		    if (contrMatcher.matches())
		    {
			contributor = contrPattern.getContributor();
		    }
		}

		String contributionType = m.group(pattern.getContributionTypeGroup());
		for (ContributionTypePattern contrTypePattern : contributionTypePatterns)
		{
		    Matcher contrTypeMatcher = contrTypePattern.getPattern().matcher(contributionType);
		    if (contrTypeMatcher.matches())
		    {
			contributionType = contrTypePattern.getContributionType();
			break;
		    }
		}

		tokens.add(Token.forContribution(m.start(), m.end(), m.group(), contributor, contributionType, pattern.getConfidence()));
		normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
	    }
	}

	for (ContributorPattern pattern : contributorPatterns)
	{
	    Matcher m = pattern.getPattern().matcher(normalizedText);
	    while (m.find())
	    {
		possibleCreditItem = true;
		Contributor contributor = pattern.getContributor();
		// set the name to the match result if no explicit name set
		if (contributor.getName() == null)
		{
		    String name = normalizedText.substring(m.start(), m.end());
		    ((AbstractContributor) contributor).setName(name);
		}
		tokens.add(Token.forContributor(m.start(), m.end(), m.group(), pattern.getContributor(), pattern.getConfidence()));
		normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
	    }
	}

	for (ContributionTypePattern pattern : contributionTypePatterns)
	{
	    Matcher m = pattern.getPattern().matcher(normalizedText);
	    while (m.find())
	    {
		possibleCreditItem = true;
		tokens.add(Token.forContributionType(m.start(), m.end(), m.group(), pattern.getContributionType(), pattern.getConfidence()));
		normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
	    }
	}

	for (Pattern knownNonContributor : irrelevantPatterns)
	{
	    Matcher m = knownNonContributor.matcher(normalizedText);
	    while (m.find())
	    {
		normalizedText = replaceTokenWithSpaces(normalizedText, m.start(), m.end());
	    }
	}

	if (possibleCreditItem)
	{
	    // tokenize the remaining of the normalizedText
	    try (Scanner scanner = new Scanner(normalizedText);)
	    {
		scanner.useDelimiter(Pattern.compile("[\\s,/+]+"));
		while (scanner.hasNext())
		{
		    String token = scanner.next();
		    // contains letter
		    boolean isWord = CharMatcher.JAVA_LETTER.matchesAnyOf(token);
		    if (isWord)
		    {
			MatchResult match = scanner.match();
			// words have confidence -1
			tokens.add(Token.forWord(match.start(), match.end(), token, -1));
		    }
		}
	    }
	}

	// sort by token start index
	tokens.sort((Token t1, Token t2) -> t1.start - t2.start);
	return tokens;
    }

    private int calculateCreditItemConfidence(List<Token> tokens)
    {
	int score = 0;
	for (Token t : tokens)
	{
	    score += t.confidence;
	}
	log.debug("Credit item confidence score: {}", score);
	return score;
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
	    CONTRIBUTION, CONTRIBUTION_TYPE, CONTRIBUTOR, WORD
	}

	private final int	  start;
	private final int	  end;
	private final String	  text;
	private final Type	  type;
	private final String	  contributionType;
	private final Contributor contributor;
	private final int	  confidence;

	private static Token forContribution(int start, int end, String text, Contributor contributor, String contributionType, int confidence)
	{
	    return new Token(start, end, text, Type.CONTRIBUTION, contributor, contributionType, confidence);
	}

	private static Token forContributor(int start, int end, String text, Contributor contributor, int confidence)
	{
	    return new Token(start, end, text, Type.CONTRIBUTOR, contributor, null, confidence);
	}

	private static Token forContributionType(int start, int end, String text, String contributionType, int confidence)
	{
	    return new Token(start, end, text, Type.CONTRIBUTION_TYPE, null, contributionType, confidence);
	}

	private static Token forWord(int start, int end, String text, int confidence)
	{
	    return new Token(start, end, text, Type.WORD, null, null, confidence);
	}

	private Token(int start, int end, String text, Type type, Contributor contributor, String contributionType, int confidence)
	{
	    this.start = start;
	    this.end = end;
	    this.text = Objects.requireNonNull(text, "text");
	    this.type = Objects.requireNonNull(type, "type");
	    this.contributor = contributor;
	    this.contributionType = contributionType;
	    this.confidence = confidence;
	}

	@Override
	public String toString()
	{
	    return MoreObjects.toStringHelper(Token.class)
		    .omitNullValues()
		    .add("start", start)
		    .add("end", end)
		    .add("text", text)
		    .add("type", type)
		    .add("contributor", contributor)
		    .add("contributionType", contributionType)
		    .add("confidence", confidence)
		    .toString();
	}
    }
}
