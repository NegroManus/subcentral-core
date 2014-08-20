package de.subcentral.core.model.release;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;

public class Releases
{
	private Releases()
	{
		// utility class
	}

	public static List<Release> filter(List<Release> rlss, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		return rlss.stream().filter(r -> Releases.filter(r, media, containedTags, group, mediaNamingService)).collect(Collectors.toList());
	}

	public static boolean filter(Release rls, List<Media> media, List<Tag> containedTags, Group group, NamingService mediaNamingService)
	{
		if (rls == null)
		{
			return false;
		}
		List<String> requiredMediaNames = media.stream().map((Media m) -> mediaNamingService.name(m)).collect(Collectors.toList());
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
			if (lastTag != null)
			{
				// DD5, 1 -> DD5.1
				if ("1".equals(tag.getName()) && "DD5".equals(lastTag.getName()))
				{
					lastTag.setName("DD5.1");
					iter.remove();
				}
				// H, 264 -> H.264
				else if ("264".equals(tag.getName()) && "H".equals(lastTag.getName()))
				{
					lastTag.setName("H.264");
					iter.remove();
				}
			}
			lastTag = tag;
		}
	}
}
