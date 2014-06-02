package de.subcentral.core.contribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.subcentral.core.util.TimeUtil;

public class ContributionTest
{
	public static void main(String[] args)
	{
		List<Contribution> clist = new ArrayList<>();
		Contribution c1 = new Contribution();
		c1.setAmount(50);
		c1.setProgress(1.0d);
		Contribution c2 = new Contribution();
		c2.setAmount(25);
		c2.setProgress(0.0d);
		Contribution c3 = new Contribution();
		c3.setAmount(25);
		c3.setProgress(0.5d);

		clist.add(c1);
		clist.add(c2);
		clist.add(c3);

		for (int i = 0; i < 100; i++)
		{
			long start = System.nanoTime();
			Map<String, Double> progresses = Contributions.calcProgresses(clist);
			double duration = TimeUtil.durationMillis(start, System.nanoTime());
			System.out.println(duration + "ms");
			System.out.println(progresses);
		}

	}
}
