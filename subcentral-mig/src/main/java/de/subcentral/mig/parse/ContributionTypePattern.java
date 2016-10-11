package de.subcentral.mig.parse;

import java.util.regex.Pattern;

public class ContributionTypePattern extends ConfidencePattern {
    private final String contributionType;

    public ContributionTypePattern(Pattern pattern, int confidence, String contributionType) {
        super(pattern, confidence);
        this.contributionType = contributionType;
    }

    public String getContributionType() {
        return contributionType;
    }
}