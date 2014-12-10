package de.subcentral.core.model.release;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.infodb.InfoDb;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.standardizing.StandardizingChange;

public class Releases
{
	public static List<Release> filter(List<Release> rlss, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		if (rlss.isEmpty())
		{
			return ImmutableList.of();
		}
		String requiredMediaName = mediaNamingService.name(media, ImmutableMap.of());
		ImmutableList.Builder<Release> filteredRlss = ImmutableList.builder();
		for (Release rls : rlss)
		{
			if (Releases.filterInternal(rls, requiredMediaName, containedTags, group, mediaNamingService))
			{
				filteredRlss.add(rls);
			}
		}
		return filteredRlss.build();
	}

	public static boolean filter(Release rls, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		if (rls == null)
		{
			return false;
		}
		String requiredMediaName = mediaNamingService.name(media, ImmutableMap.of());
		return filterInternal(rls, requiredMediaName, containedTags, group, mediaNamingService);
	}

	private static boolean filterInternal(Release rls, String requiredMediaName, List<Tag> containedTags, Group group,
			NamingService mediaNamingService)
	{
		String actualMediaName = mediaNamingService.name(rls.getMedia(), ImmutableMap.of());
		return requiredMediaName.equalsIgnoreCase(actualMediaName) && (group == null ? true : group.equals(rls.getGroup()))
				&& (rls.getTags().containsAll(containedTags));
	}

	public static void enrichByParsingName(Release rls, ParsingService ps, boolean overwrite)
	{
		if (rls == null || rls.getName() == null)
		{
			return;
		}
		Release parsedName = ps.parse(rls.getName(), Release.class);
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
				// H, 264 -> H.264
				else if ("H".equals(lastTag.getName()) && "264".equals(tag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("H.264"));
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

	public static List<Release> distinctReleasesByName(ListMultimap<InfoDb<Release, ?>, Release> queryResults)
	{
		if (queryResults.isEmpty())
		{
			return ImmutableList.of();
		}
		List<Release> reducedList = new ArrayList<>();
		for (Map.Entry<InfoDb<Release, ?>, Release> foundEntry : queryResults.entries())
		{
			// System.out.println(foundEntry);
			Release foundRls = foundEntry.getValue();
			boolean notListed = true;
			for (Release reducedEntry : reducedList)
			{
				if (reducedEntry.equalsByName(foundRls))
				{
					notListed = false;
				}
			}
			if (notListed)
			{
				reducedList.add(foundRls);
			}
		}
		return reducedList;
	}

	private Releases()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
