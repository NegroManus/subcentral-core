package de.subcentral.mig;

import java.util.regex.Pattern;

public class ContributionPattern extends ConfidencePattern
{
    private final int contributionTypeGroup;
    private final int contributorGroup;

    public ContributionPattern(Pattern pattern, int confidence, int contributionTypeGroup, int contributorGroup)
    {
	super(pattern, confidence);
	this.contributionTypeGroup = contributionTypeGroup;
	this.contributorGroup = contributorGroup;
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