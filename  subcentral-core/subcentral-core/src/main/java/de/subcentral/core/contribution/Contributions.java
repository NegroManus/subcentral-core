package de.subcentral.core.contribution;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public class Contributions
{
	public static ListMultimap<String, Contribution> sortByType(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return ImmutableListMultimap.of();
		}
		ListMultimap<String, Contribution> map = LinkedListMultimap.create();
		for (Contribution c : contributions)
		{
			map.put(c.getType(), c);
		}
		return map;
	}

	public static Collection<Contribution> getOfType(Collection<Contribution> contributions, String type)
	{
		return sortByType(contributions).get(type);
	}

	public static double calcProgress(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return 0.0f;
		}
		long totalAmount = 0;
		double amountDone = 0.0d;
		for (Contribution c : contributions)
		{
			totalAmount += c.getAmount();
			amountDone += c.getAmount() * c.getProgress();
		}
		return amountDone / totalAmount;
	}

	public static double calcProgress(Collection<Contribution> contributions, String type)
	{
		return Contributions.calcProgress(Contributions.getOfType(contributions, type));
	}

	public static Map<String, Double> calcProgresses(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return ImmutableMap.of();
		}
		ListMultimap<String, Contribution> sortedContributions = Contributions.sortByType(contributions);
		Map<String, Double> progresses = new HashMap<>();
		for (Map.Entry<String, Collection<Contribution>> entry : sortedContributions.asMap().entrySet())
		{
			progresses.put(entry.getKey(), Contributions.calcProgress(entry.getValue()));
		}
		return progresses;
	}

	private Contributions()
	{
		// utility class
	}
}
