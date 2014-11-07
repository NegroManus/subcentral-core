package de.subcentral.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.subcentral.core.model.media.Contribution;
import de.subcentral.core.model.media.Contributions;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.util.TimeUtil;

public class ContributionTest
{
	public static void main(String[] args)
	{
		List<Contribution> clist = new ArrayList<>();
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

		clist.add(c1);
		clist.add(c2);
		clist.add(c3);
		clist.add(cb1);

		for (int i = 0; i < 100; i++)
		{
			long start = System.nanoTime();
			Map<String, Float> progresses = Contributions.calcProgresses(clist);
			double duration = TimeUtil.durationMillis(start, System.nanoTime());
			System.out.println(duration + "ms");
			System.out.println(progresses);
		}
	}
}
