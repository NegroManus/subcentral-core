package de.subcentral.core.metadata.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingException;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ParsingUtils;

public class ReleaseUtils
{
	private static final Logger	log	= LogManager.getLogger(ReleaseUtils.class);

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

	public static Predicate<Release> filterByMedia(List<Media> media, NamingService mediaNamingService, Map<String, Object> namingParams)
	{
		String requiredMediaName;
		if (media.isEmpty())
		{
			requiredMediaName = "";
		}
		else
		{
			requiredMediaName = mediaNamingService.name(media, namingParams);
		}
		return filterByMedia(requiredMediaName, mediaNamingService, namingParams);
	}

	public static Predicate<Release> filterByMedia(String requiredMediaName, NamingService mediaNamingService, Map<String, Object> namingParams)
	{
		return (rls) -> requiredMediaName.isEmpty() ? true
				: requiredMediaName.equalsIgnoreCase(mediaNamingService.name(rls.getMedia(), namingParams));
	}

	public static Predicate<Release> filterByTags(List<Tag> containedTags, Collection<Tag> metaTagsToIgnore)
	{
		return (rls) -> TagUtils.containsAllIgnoreMetaTags(rls.getTags(), containedTags, metaTagsToIgnore);
	}

	public static Predicate<Release> filterByGroup(Group group, boolean requireSameGroup)
	{
		return (rls) -> group == null ? (rls.getGroup() == null || !requireSameGroup) : group.equals(rls.getGroup());
	}

	public static void enrichByParsingName(Release rls, ParsingService parsingService, boolean overwrite)
	{
		enrichByParsingName(rls, ImmutableList.of(parsingService), overwrite);
	}

	public static void enrichByParsingName(Release rls, Iterable<ParsingService> parsingServices, boolean overwrite) throws ParsingException
	{
		if (rls == null || rls.getName() == null)
		{
			return;
		}
		Release parsedRls = ParsingUtils.parse(rls.getName(), Release.class, parsingServices);
		if (parsedRls == null)
		{
			log.warn("Failed to enrich release because its name could not be parsed: " + rls);
			return;
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
	}

	public static List<Release> guessMatchingReleases(Release partialRls, Collection<Release> commonRlss, Collection<Tag> metaTags)
	{
		List<Release> matchingCommonRlss = commonRlss.stream()
				.filter(filterByTags(partialRls.getTags(), metaTags))
				.filter(filterByGroup(partialRls.getGroup(), true))
				.collect(Collectors.toList());
		if (matchingCommonRlss.isEmpty())
		{
			return ImmutableList.of(new Release(partialRls));
		}
		ImmutableList.Builder<Release> guessedRlss = ImmutableList.builder();
		for (Release rls : matchingCommonRlss)
		{
			Release guessedRls = new Release(rls);
			guessedRls.setMedia(partialRls.getMedia());
			TagUtils.transferMetaTags(partialRls.getTags(), guessedRls.getTags(), metaTags);
			guessedRlss.add(guessedRls);
		}
		return guessedRlss.build();
	}

	private ReleaseUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
