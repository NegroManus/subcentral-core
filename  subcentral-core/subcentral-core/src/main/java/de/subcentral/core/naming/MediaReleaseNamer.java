package de.subcentral.core.naming;

import com.google.common.base.Joiner;

import de.subcentral.core.media.Media;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

public class MediaReleaseNamer extends AbstractReleaseNamer<Media, MediaRelease>
{
	private Replacer	mediaReplacer	= NamingStandards.STANDARD_REPLACER;
	private String		mediaFormat		= "%s";

	public Replacer getMediaReplacer()
	{
		return mediaReplacer;
	}

	public void setMediaReplacer(Replacer mediaReplacer)
	{
		this.mediaReplacer = mediaReplacer;
	}

	public String getMediaFormat()
	{
		return mediaFormat;
	}

	public void setMediaFormat(String mediaFormat)
	{
		this.mediaFormat = mediaFormat;
	}

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
		sb.append(String.format(mediaFormat, StringUtil.replace(namingService.name(media), mediaReplacer)));
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
