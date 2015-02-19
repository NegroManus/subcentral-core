package de.subcentral.core.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class ContributionUtils
{
	public static final float	PROGRESS_NOT_MEASURABLE	= Float.NaN;

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

	public static float calcProgress(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return PROGRESS_NOT_MEASURABLE;
		}
		int totalAmount = 0;
		float amountDone = 0.0f;
		for (Contribution c : contributions)
		{
			totalAmount += c.getAmount();
			amountDone += c.getAmount() * c.getProgress();
		}
		if (totalAmount == 0)
		{
			// return not measurable if no amount at all
			return PROGRESS_NOT_MEASURABLE;
		}
		return amountDone / totalAmount;
	}

	public static float calcProgress(Collection<Contribution> contributions, String type)
	{
		return calcProgress(filterByType(contributions, type));
	}

	public static Map<String, Float> calcProgresses(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return ImmutableMap.of();
		}
		ListMultimap<String, Contribution> sortedContributions = groupByType(contributions);
		ImmutableMap.Builder<String, Float> progresses = ImmutableMap.builder();
		for (Map.Entry<String, Collection<Contribution>> entry : sortedContributions.asMap().entrySet())
		{
			progresses.put(entry.getKey(), calcProgress(entry.getValue()));
		}
		return progresses.build();
	}

	private ContributionUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
