package de.subcentral.core.model.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.Parsings;
import de.subcentral.core.standardizing.StandardizingChange;

public class Releases
{
	public static List<Release> filter(List<Release> rlss, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		return filter(rlss, media, containedTags, group, mediaNamingService, ImmutableMap.of());
	}

	public static List<Release> filter(List<Release> rlss, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService,
			Map<String, Object> namingParameters)
	{
		if (rlss.isEmpty())
		{
			return ImmutableList.of();
		}
		String requiredMediaName = mediaNamingService.name(media, namingParameters);
		ImmutableList.Builder<Release> filteredRlss = ImmutableList.builder();
		for (Release rls : rlss)
		{
			if (Releases.filterInternal(rls, requiredMediaName, containedTags, group, mediaNamingService, namingParameters))
			{
				filteredRlss.add(rls);
			}
		}
		return filteredRlss.build();
	}

	public static boolean filter(Release rls, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		return filter(rls, media, containedTags, group, mediaNamingService, ImmutableMap.of());
	}

	public static boolean filter(Release rls, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService,
			Map<String, Object> namingParameters)
	{
		if (rls == null)
		{
			return false;
		}
		String requiredMediaName = mediaNamingService.name(media, ImmutableMap.of());
		return filterInternal(rls, requiredMediaName, containedTags, group, mediaNamingService, namingParameters);
	}

	private static boolean filterInternal(Release rls, String requiredMediaName, List<Tag> containedTags, Group group,
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

	public static void enrichByParsingName(Release rls, List<ParsingService> parsingServices, boolean overwrite)
	{
		if (rls == null || rls.getName() == null)
		{
			return;
		}
		Release parsedName = Parsings.parse(rls.getName(), Release.class, parsingServices);
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
					else if ("H".equals(lastTag.getName()) && "265".equals(tag.getName()))
					{
						iter.remove();
						iter.previous();
						iter.set(new Tag("H.265"));
						changed = true;
					}
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

	private Releases()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
