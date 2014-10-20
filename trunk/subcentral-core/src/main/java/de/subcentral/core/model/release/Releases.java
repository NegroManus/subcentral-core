package de.subcentral.core.model.release;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

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
		List<String> requiredMediaNames = media.stream().map((Media m) -> mediaNamingService.name(m)).collect(Collectors.toList());
		return rlss.stream()
				.filter(r -> Releases.doFilter(r, requiredMediaNames, containedTags, group, mediaNamingService))
				.collect(Collectors.toList());
	}

	public static boolean filter(Release rls, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		if (rls == null)
		{
			return false;
		}
		List<String> requiredMediaNames = media.stream().map((Media m) -> mediaNamingService.name(m)).collect(Collectors.toList());
		return doFilter(rls, requiredMediaNames, containedTags, group, mediaNamingService);
	}

	private static boolean doFilter(Release rls, List<String> requiredMediaNames, List<Tag> containedTags, Group group,
			NamingService mediaNamingService)
	{
		if (rls == null)
		{
			return false;
		}
		List<String> actualMediaNames = rls.getMedia().stream().map((Media m) -> mediaNamingService.name(m)).collect(Collectors.toList());

		return requiredMediaNames.equals(actualMediaNames) && (group == null ? true : group.equals(rls.getGroup()))
				&& (rls.getTags().containsAll(containedTags));
	}

	public static void enrichByParsingName(Release rls, ParsingService ps, boolean overwrite)
	{
		if (rls == null || rls.getName() == null)
		{
			return;
		}
		Release parsedName = ps.parseTyped(rls.getName(), Release.class);
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
					if (compatibleRls != null)
					{
						compatibleRlss.put(compatibleRls, c);
					}
					break;
				default:
					break;
			}
		}
		return compatibleRlss.build();
	}

	private static final Release buildCompatible(Release sourceRls, Compatibility c, MatchDirection md)
	{
		if (MatchDirection.NONE == md)
		{
			return null;
		}
		Release compatibleRls = new Release();
		compatibleRls.setMedia(sourceRls.getMedia());
		if (MatchDirection.FORWARD == md)
		{
			compatibleRls.setGroup(c.getCompatibleGroup() == null ? sourceRls.getGroup() : c.getCompatibleGroup());
			compatibleRls.setTags(c.getCompatibleTags() == null ? sourceRls.getTags() : c.getCompatibleTags());
		}
		else if (MatchDirection.BACKWARD == md)
		{
			compatibleRls.setGroup(c.getSourceGroup() == null ? sourceRls.getGroup() : c.getSourceGroup());
			compatibleRls.setTags(c.getSourceTags() == null ? sourceRls.getTags() : c.getSourceTags());
		}
		return compatibleRls;
	}

	private Releases()
	{
		throw new AssertionError(getClass() + " cannot be insantiated. It is an utility class.");
	}
}
