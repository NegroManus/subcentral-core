package de.subcentral.core.metadata.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.NoMatchException;
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

	public static List<Release> filter(Collection<Release> rlss, List<Tag> containedTags, Group group, Collection<Tag> metaTagsToIgnore)
	{
		if (rlss.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<Release> filteredRlss = ImmutableList.builder();
		for (Release rls : rlss)
		{
			if (TagUtils.containsAllIgnoreMetaTags(rls.getTags(), containedTags, metaTagsToIgnore) && (group == null || group.equals(rls.getGroup())))
			{
				filteredRlss.add(rls);
			}
		}
		return filteredRlss.build();
	}

	public static List<Release> filter(Collection<Release> rlss, Collection<Media> media, Collection<Tag> containedTags, Group group,
			NamingService mediaNamingService)
	{
		return filter(rlss, media, containedTags, group, mediaNamingService, ImmutableMap.of());
	}

	public static List<Release> filter(Collection<Release> rlss, Collection<Media> media, Collection<Tag> containedTags, Group group,
			NamingService mediaNamingService, Map<String, Object> namingParameters)
	{
		if (rlss.isEmpty())
		{
			return ImmutableList.of();
		}
		String requiredMediaName = mediaNamingService.name(media, namingParameters);
		ImmutableList.Builder<Release> filteredRlss = ImmutableList.builder();
		for (Release rls : rlss)
		{
			if (ReleaseUtils.filterInternal(rls, requiredMediaName, containedTags, group, mediaNamingService, namingParameters))
			{
				filteredRlss.add(rls);
			}
		}
		return filteredRlss.build();
	}

	public static boolean filter(Release rls, Collection<Media> media, Collection<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		return filter(rls, media, containedTags, group, mediaNamingService, ImmutableMap.of());
	}

	public static boolean filter(Release rls, Collection<Media> media, Collection<Tag> containedTags, Group group, NamingService mediaNamingService,
			Map<String, Object> namingParameters)
	{
		if (rls == null)
		{
			return false;
		}
		String requiredMediaName = mediaNamingService.name(media, ImmutableMap.of());
		return filterInternal(rls, requiredMediaName, containedTags, group, mediaNamingService, namingParameters);
	}

	private static boolean filterInternal(Release rls, String requiredMediaName, Collection<Tag> containedTags, Group group,
			NamingService mediaNamingService, Map<String, Object> namingParams)
	{
		String actualMediaName = mediaNamingService.name(rls.getMedia(), namingParams);
		return requiredMediaName.equalsIgnoreCase(actualMediaName) && (group == null ? true : group.equals(rls.getGroup()))
				&& (rls.getTags().containsAll(containedTags));
	}

	public static void enrichByParsingName(Release rls, ParsingService parsingService, boolean overwrite)
	{
		enrichByParsingName(rls, ImmutableList.of(parsingService), overwrite);
	}

	public static void enrichByParsingName(Release rls, List<ParsingService> parsingServices, boolean overwrite) throws ParsingException
	{
		if (rls == null || rls.getName() == null)
		{
			return;
		}
		Release parsedName;
		try
		{
			parsedName = ParsingUtils.parse(rls.getName(), Release.class, parsingServices);
		}
		catch (NoMatchException e)
		{
			log.warn("Failed to enrich release because its name could not be parsed: " + rls, e);
			return;
		}
		if (overwrite || rls.getMedia().isEmpty())
		{
			rls.setMedia(parsedName.getMedia());
		}
		if (overwrite || rls.getTags().isEmpty())
		{
			rls.setTags(parsedName.getTags());
		}
		if (overwrite || rls.getGroup() == null)
		{
			rls.setGroup(parsedName.getGroup());
		}
	}

	public static List<Release> guessMatchingReleases(Release partialRls, Collection<Release> commonRlss, Collection<Tag> metaTags)
	{
		List<Release> matchingCommonRlss = filter(commonRlss, partialRls.getTags(), partialRls.getGroup(), metaTags);
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
