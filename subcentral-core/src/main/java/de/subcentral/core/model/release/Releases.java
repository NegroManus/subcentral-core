package de.subcentral.core.model.release;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.Compatibility.MatchDirection;
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
			if ("X264".equals(tag.getName()))
			{
				iter.set(new Tag("x264"));
			}
			else if (lastTag != null)
			{
				// DD5, 1 -> DD5.1
				if ("1".equals(tag.getName()) && "DD5".equals(lastTag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("DD5.1"));

				}
				// H, 264 -> H.264
				else if ("264".equals(tag.getName()) && "H".equals(lastTag.getName()))
				{
					iter.remove();
					iter.previous();
					iter.set(new Tag("H.264"));
				}
			}
			lastTag = tag;
		}
	}

	public static Map<Release, Compatibility> findCompatibleReleases(Release rls, Collection<Compatibility> compatibilities,
			Collection<Release> existingReleases)
	{
		if (rls == null || compatibilities.isEmpty())
		{
			return ImmutableMap.of();
		}

		ImmutableMap.Builder<Release, Compatibility> compatibleRlss = ImmutableMap.builder();
		for (Compatibility c : compatibilities)
		{
			MatchDirection md = c.match(rls);
			if (MatchDirection.NONE == md)
			{
				continue;
			}
			switch (c.getScope())
			{
				case IF_EXISTS:
					for (Release existingRls : existingReleases)
					{
						if (c.matchesCompatible(existingRls, md))
						{
							compatibleRlss.put(existingRls, c);
						}
					}
					break;
				case ALWAYS:
					Release compatibleRls = buildCompatible(rls, c, md);
					compatibleRlss.put(compatibleRls, c);
					break;
			}
		}
		return compatibleRlss.build();
	}

	private static final Release buildCompatible(Release sourceRls, Compatibility c, MatchDirection md)
	{
		Release compatibleRls = new Release();
		compatibleRls.setMedia(sourceRls.getMedia());
		switch (md)
		{
			case FORWARD:
				compatibleRls.setGroup(c.getCompatibleGroup() == null ? sourceRls.getGroup() : c.getCompatibleGroup());
				compatibleRls.setTags(c.getCompatibleTags() == null ? sourceRls.getTags() : c.getCompatibleTags());
				break;
			case BACKWARD:
				compatibleRls.setGroup(c.getSourceGroup() == null ? sourceRls.getGroup() : c.getSourceGroup());
				compatibleRls.setTags(c.getSourceTags() == null ? sourceRls.getTags() : c.getSourceTags());
				break;
			case NONE:
				break;
		}

		return compatibleRls;
	}

	private Releases()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}
