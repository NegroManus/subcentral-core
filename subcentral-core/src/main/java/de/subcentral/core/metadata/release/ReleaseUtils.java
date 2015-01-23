package de.subcentral.core.metadata.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
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
import de.subcentral.core.standardizing.StandardizingChange;

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
			if (containsAllIgnoreMetaTags(rls.getTags(), containedTags, metaTagsToIgnore) && (group == null || group.equals(rls.getGroup())))
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

	public static List<StandardizingChange> standardizeTags(Release rls)
	{
		if (rls == null || rls.getTags().isEmpty())
		{
			return ImmutableList.of();
		}

		boolean changed = false;
		List<Tag> oldTags = ImmutableList.copyOf(rls.getTags());

		ListIterator<Tag> iter = rls.getTags().listIterator();
		Tag lastTag = null;
		while (iter.hasNext())
		{
			Tag tag = iter.next();
			// X264 -> x264
			if ("X264".equals(tag.getName()))
			{
				iter.set(new Tag("x264"));
				changed = true;
			}
			else if (lastTag != null)
			{
				// DD5, 1 -> DD5.1
				if ("DD5".equals(lastTag.getName()) && "1".equals(tag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("DD5.1"));
					changed = true;
				}
				else if ("H".equals(lastTag.getName()))
				{
					// H, 264 -> H.264
					if ("264".equals(tag.getName()))
					{
						iter.remove();
						iter.previous();
						iter.set(new Tag("H.264"));
						changed = true;
					}
					// H, 265 -> H.265
					else if ("265".equals(tag.getName()))
					{
						iter.remove();
						iter.previous();
						iter.set(new Tag("H.265"));
						changed = true;
					}
				}
				// AAC2, 0 -> AAC2.0
				else if ("AAC2".equals(lastTag.getName()) && "0".equals(tag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("AAC2.0"));
					changed = true;
				}
				// WEB, DL -> WEB-DL
				else if ("WEB".equals(lastTag.getName()) && "DL".equals(tag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("WEB-DL"));
					changed = true;
				}
			}
			lastTag = tag;
		}
		return changed ? ImmutableList.of(new StandardizingChange(rls, Release.PROP_TAGS.getPropName(), oldTags, rls.getTags())) : ImmutableList.of();
	}

	public static boolean replaceTags(Release rls, Collection<Tag> newTags, Collection<Tag> metaTagsToRetain)
	{
		return replaceTags(rls.getTags(), newTags, metaTagsToRetain);
	}

	public static boolean replaceTags(List<Tag> tags, Collection<Tag> newTags, Collection<Tag> metaTagsToRetain)
	{
		// no shortcut (||) because both operations need to be performed
		return (tags.retainAll(metaTagsToRetain) | tags.addAll(newTags));
	}

	public static void transferMetaTags(List<Tag> sourceTags, List<Tag> targetTags, Collection<Tag> metaTags)
	{
		// iterate the source tags in reverse order so that the order in the target tags is correct
		for (int i = sourceTags.size() - 1; i >= 0; i--)
		{
			Tag sourceTag = sourceTags.get(i);
			if (metaTags.contains(sourceTag))
			{
				targetTags.add(0, sourceTag);
			}
		}
	}

	public static boolean containsAllIgnoreMetaTags(List<Tag> tags1, List<Tag> tags2, Collection<Tag> metaTagsToIgnore)
	{
		return copyAndRemoveMetaTags(tags1, metaTagsToIgnore).containsAll(copyAndRemoveMetaTags(tags2, metaTagsToIgnore));
	}

	public static boolean equalsIgnoreMetaTags(List<Tag> tags1, List<Tag> tags2, Collection<Tag> metaTagsToIgnore)
	{
		return copyAndRemoveMetaTags(tags1, metaTagsToIgnore).equals(copyAndRemoveMetaTags(tags2, metaTagsToIgnore));
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
			transferMetaTags(partialRls.getTags(), guessedRls.getTags(), metaTags);
			guessedRlss.add(guessedRls);
		}
		return guessedRlss.build();
	}

	private static List<Tag> copyAndRemoveMetaTags(List<Tag> tags, Collection<Tag> metaTagsToRemove)
	{
		if (tags.isEmpty())
		{
			return ImmutableList.of();
		}
		List<Tag> tagsWithoutMetaTags = new ArrayList<>(tags);
		tagsWithoutMetaTags.removeAll(metaTagsToRemove);
		return tagsWithoutMetaTags;
	}

	private ReleaseUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
