package de.subcentral.core.model.release;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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

	public static Map<Release, Compatibility> findCompatibleReleases(Release rls, Collection<Compatibility> compatibilities,
			Collection<Release> existingReleases)
	{
		if (rls == null)
		{
			return ImmutableMap.of();
		}
		Map<Release, Compatibility> allCompatibleRlss = new HashMap<>();

		addCompatibleReleases(rls, compatibilities, existingReleases, allCompatibleRlss, new HashSet<>(4), rls);
		return ImmutableMap.copyOf(allCompatibleRlss);
	}

	private static final void addCompatibleReleases(Release rls, Collection<Compatibility> compatibilities, Collection<Release> existingReleases,
			Map<Release, Compatibility> allCompatibleRlss, Set<Release> alreadyCheckedRlss, Release sourceRls)
	{
		Map<Release, Compatibility> compatibleRlss = new HashMap<>(4);
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
						if (c.matchesCompatible(existingRls, md) && !sourceRls.equals(existingRls))
						{
							compatibleRlss.put(existingRls, c);
						}
					}
					break;
				case ALWAYS:
					Release compatibleRls = buildCompatible(rls, c, md);
					if (!sourceRls.equals(compatibleRls))
					{
						compatibleRlss.put(compatibleRls, c);
					}
					break;
			}
		}

		// add the previously checked Release to the checked Releases
		// so they are not checked again
		alreadyCheckedRlss.add(rls);
		// add the previously found compatible Releases to the total list
		// but just if they were not found before (putIfAbsent(): not overriding entries)
		for (Map.Entry<Release, Compatibility> entry : compatibleRlss.entrySet())
		{
			allCompatibleRlss.putIfAbsent(entry.getKey(), entry.getValue());
		}
		// Check if any compatible Releases has compatibilities of its own
		for (Release newCompatibleRls : compatibleRlss.keySet())
		{
			// skip if already checked for compatibilities of newCompatibleRls
			// For example: If LOL -> DIM, DIM -> LOL. Then don't check for LOL compatibilities again (would only bring up DIM again).
			if (!alreadyCheckedRlss.contains(newCompatibleRls))
			{
				addCompatibleReleases(newCompatibleRls, compatibilities, existingReleases, allCompatibleRlss, alreadyCheckedRlss, sourceRls);
			}
		}
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
