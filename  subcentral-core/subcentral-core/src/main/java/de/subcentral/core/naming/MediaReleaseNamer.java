package de.subcentral.core.naming;

import com.google.common.base.Joiner;

import de.subcentral.core.media.Media;
import de.subcentral.core.release.MediaRelease;

public class MediaReleaseNamer extends AbstractReleaseNamer<Media, MediaRelease>
{
	@Override
	public Class<MediaRelease> getType()
	{
		return MediaRelease.class;
	}

	@Override
	public String name(MediaRelease rls, Media media, NamingService namingService)
	{
		if (rls == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(namingService.name(media));
		if (!rls.getTags().isEmpty())
		{
			sb.append(String.format(tagsFormat, Joiner.on(tagsSeparator).join(rls.getTags())));
		}
		if (rls.getGroup() != null)
		{
			sb.append(String.format(groupFormat, rls.getGroup().getName()));
		}
		return sb.toString();
	}
}
