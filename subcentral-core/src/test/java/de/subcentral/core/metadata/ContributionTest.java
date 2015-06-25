package de.subcentral.core.metadata;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class ContributionTest
{
    @Test
    public void testContributions()
    {
	Contribution c1 = new Contribution();
	c1.setType(Subtitle.CONTRIBUTION_TYPE_TRANSLATION);
	c1.setAmount(50);
	c1.setProgress(1.0f);
	Contribution c2 = new Contribution();
	c2.setType(Subtitle.CONTRIBUTION_TYPE_TRANSLATION);
	c2.setAmount(25);
	c2.setProgress(0.0f);
	Contribution c3 = new Contribution();
	c3.setType(Subtitle.CONTRIBUTION_TYPE_TRANSLATION);
	c3.setAmount(25);
	c3.setProgress(0.5f);
	Contribution cb1 = new Contribution();
	cb1.setType(Subtitle.CONTRIBUTION_TYPE_REVISION);
	cb1.setAmount(1);
	cb1.setProgress(1f);

	List<Contribution> contributions = new ArrayList<>(4);
	contributions.add(c1);
	contributions.add(c2);
	contributions.add(c3);
	contributions.add(cb1);

	Map<String, Float> calculatedProgresses = ContributionUtil.calcProgresses(contributions);
	Map<String, Float> expectedProgresses = new HashMap<>(2);
	expectedProgresses.put(Subtitle.CONTRIBUTION_TYPE_TRANSLATION, 0.625f);
	expectedProgresses.put(Subtitle.CONTRIBUTION_TYPE_REVISION, 1.0f);

	assertEquals(expectedProgresses, calculatedProgresses);
    }
}
