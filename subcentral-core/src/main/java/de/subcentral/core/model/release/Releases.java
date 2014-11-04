package de.subcentral.core.model.release;

import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;

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
			if (rls != null && Releases.filterInternal(rls, requiredMediaName, containedTags, group, mediaNamingService))
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

	public static void standardizeTags(Release rls)
	{
		ListIterator<Tag> iter = rls.getTags().listIterator();
		Tag lastTag = null;
		while (iter.hasNext())
		{
			Tag tag = iter.next();
			// X264 -> x264
			if ("X264".equals(tag.getName()))
			{
				iter.set(new Tag("x264"));
			}
			else if (lastTag != null)
			{
				// DD5, 1 -> DD5.1
				if ("DD5".equals(lastTag.getName()) && "1".equals(tag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("DD5.1"));

				}
				// H, 264 -> H.264
				else if ("H".equals(lastTag.getName()) && "264".equals(tag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("H.264"));
				}
				// WEB, DL -> WEB-DL
				else if ("WEB".equals(lastTag.getName()) && "DL".equals(tag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("WEB-DL"));
				}
			}
			lastTag = tag;
		}
	}

	private Releases()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}