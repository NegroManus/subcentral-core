package de.subcentral.core.model.media;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class Contributions
{
	public static ListMultimap<String, Contribution> groupByType(Collection<Contribution> contributions)
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

	public static List<Contribution> filterByType(Collection<Contribution> contributions, String type)
	{
		if (contributions.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<Contribution> matchingContributions = ImmutableList.builder();
		for (Contribution c : contributions)
		{
			if (Objects.equals(type, c.getType()))
			{
				matchingContributions.add(c);
			}
		}
		return matchingContributions.build();
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
		return calcProgress(filterByType(contributions, type));
	}

	public static Map<String, Double> calcProgresses(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return ImmutableMap.of();
		}
		ListMultimap<String, Contribution> sortedContributions = groupByType(contributions);
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
