package de.subcentral.mig.parse;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.subcentral.mig.ScContributor;

public class ContributorPattern extends ConfidencePattern {
    private final String             contributorName;
    private final int                contributorId;
    private final ScContributor.Type contributorType;

    public ContributorPattern(Pattern pattern, int confidence, ScContributor.Type contributorType, String contributorName, int contributorId) {
        super(pattern, confidence);
        this.contributorType = Objects.requireNonNull(contributorType, "contributorType");
        this.contributorName = contributorName;
        this.contributorId = contributorId;
    }

    public ScContributor.Type getContributorType() {
        return contributorType;
    }

    public String getContributorName() {
        return contributorName;
    }

    public int getContributorId() {
        return contributorId;
    }

    public ScContributor match(String s) {
        Matcher m = pattern.matcher(s);
        if (m.matches()) {
            return contributorFromMatch(m);
        }
        return null;
    }

    public ScContributor contributorFromMatch(Matcher matcher) {
        return new ScContributor(contributorType, contributorName != null ? contributorName : matcher.group(), contributorId);
    }
}