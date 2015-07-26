package de.subcentral.core.metadata.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.parsing.ParsingException;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ParsingUtil;

public class ReleaseUtil
{
	private static final Logger log = LogManager.getLogger(ReleaseUtil.class);

	public static List<Release> distinctByName(Collection<Release> releases)
	{
		if (releases.isEmpty())
		{
			return ImmutableList.of();
		}

		List<Release> reducedList = new ArrayList<>();
		for (Release rls : releases)
		{
			boolean notListed = true;
			for (Release reducedEntry : reducedList)
			{
				if (reducedEntry.equalsByName(rls))
				{
					notListed = false;
				}
			}
			if (notListed)
			{
				reducedList.add(rls);
			}
		}
		return reducedList;
	}

	public static Predicate<Release> filterByTags(List<Tag> containedTags, Collection<Tag> metaTagsToIgnore)
	{
		return (rls) -> TagUtil.containsAllIgnoreMetaTags(rls.getTags(), containedTags, metaTagsToIgnore);
	}

	public static Predicate<Release> filterByGroup(Group group, boolean requireSameGroup)
	{
		return (rls) ->
		{
			return group == null ? (rls.getGroup() == null || !requireSameGroup) : group.equals(rls.getGroup());
		};
	}

	public static void enrichByParsingName(Release rls, ParsingService parsingService, boolean overwrite)
	{
		enrichByParsingName(rls, ImmutableList.of(parsingService), overwrite);
	}

	/**
	 * 
	 * @param rls
	 * @param parsingServices
	 * @param overwrite
	 * @return {@code true} if parsing was successful, {@code false} otherwise
	 * @throws ParsingException
	 */
	public static boolean enrichByParsingName(Release rls, Iterable<ParsingService> parsingServices, boolean overwrite) throws ParsingException
	{
		if (rls == null)
		{
			return false;
		}
		Release parsedRls = ParsingUtil.parse(rls.getName(), Release.class, parsingServices);
		if (parsedRls == null)
		{
			log.warn("Failed to enrich release because its name could not be parsed: " + rls);
			return false;
		}
		if (overwrite || rls.getMedia().isEmpty())
		{
			rls.setMedia(parsedRls.getMedia());
		}
		if (overwrite || rls.getTags().isEmpty())
		{
			rls.setTags(parsedRls.getTags());
		}
		if (overwrite || rls.getGroup() == null)
		{
			rls.setGroup(parsedRls.getGroup());
		}
		return true;
	}

	public static Map<Release, StandardRelease> guessMatchingReleases(Release partialRls, Collection<StandardRelease> standardRlss, Collection<Tag> metaTags)
	{
		// LinkedHashMap to maintain insertion order
		Map<Release, StandardRelease> guessedRlss = new LinkedHashMap<>(4);
		for (StandardRelease stdRls : standardRlss)
		{
			Release candidate = stdRls.getRelease();
			if (filterByTags(partialRls.getTags(), metaTags).test(candidate) && filterByGroup(partialRls.getGroup(), true).test(candidate))
			{
				Release guessedRls = new Release(candidate);
				guessedRls.setMedia(partialRls.getMedia());
				TagUtil.transferMetaTags(partialRls.getTags(), guessedRls.getTags(), metaTags);
				guessedRlss.put(guessedRls, stdRls);
			}
		}
		if (guessedRlss.isEmpty())
		{
			return Collections.singletonMap(partialRls, null);
		}
		return guessedRlss;
	}

	private ReleaseUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
