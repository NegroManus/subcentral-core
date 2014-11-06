package de.subcentral.core.model.media;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class Contributions
{
	public static ListMultimap<String, Contribution> sortByType(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return ImmutableListMultimap.of();
		}
		ImmutableListMultimap.Builder<String, Contribution> sorted = ImmutableListMultimap.builder();
		for (Contribution c : contributions)
		{
			sorted.put(c.getType(), c);
		}
		return sorted.build();
	}

	public static Collection<Contribution> getOfType(Collection<Contribution> contributions, String type)
	{
		return sortByType(contributions).get(type);
	}

	public static double calcProgress(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return 0d;
		}
		long totalAmount = 0;
		double amountDone = 0d;
		for (Contribution c : contributions)
		{
			totalAmount += c.getAmount();
			amountDone += c.getAmount() * c.getProgress();
		}
		if (totalAmount == 0L)
		{
			// return zero if no amount at all
			return 0d;
		}
		return amountDone / totalAmount;
	}

	public static double calcProgress(Collection<Contribution> contributions, String type)
	{
		return calcProgress(Contributions.getOfType(contributions, type));
	}

	public static Map<String, Double> calcProgresses(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return ImmutableMap.of();
		}
		ListMultimap<String, Contribution> sortedContributions = Contributions.sortByType(contributions);
		ImmutableMap.Builder<String, Double> progresses = ImmutableMap.builder();
		for (Map.Entry<String, Collection<Contribution>> entry : sortedContributions.asMap().entrySet())
		{
			progresses.put(entry.getKey(), calcProgress(entry.getValue()));
		}
		return progresses.build();
	}

	private Contributions()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}
