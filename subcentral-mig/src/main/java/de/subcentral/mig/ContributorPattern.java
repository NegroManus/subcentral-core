package de.subcentral.mig;

import java.util.regex.Pattern;

import de.subcentral.core.metadata.Contributor;

public class ContributorPattern extends ConfidencePattern
{
    private final Contributor contributor;

    public ContributorPattern(Pattern pattern, int confidence, Contributor contributor)
    {
	super(pattern, confidence);
	this.contributor = contributor;
    }

    public Contributor getContributor()
    {
	return contributor;
    }
}