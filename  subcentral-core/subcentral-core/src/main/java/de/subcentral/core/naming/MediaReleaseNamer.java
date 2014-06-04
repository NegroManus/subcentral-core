package de.subcentral.core.naming;

import com.google.common.base.Joiner;

import de.subcentral.core.release.MediaRelease;

public class MediaReleaseNamer implements Namer<MediaRelease>
{
	private String	tagsPrefix		= ".";
	private String	tagsSeparator	= ".";
	private String	groupPrefix		= "-";

	@Override
	public Class<MediaRelease> getType()
	{
		return MediaRelease.class;
	}

	@Override
	public String name(MediaRelease rls, NamingService namingService)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(namingService.name(rls.getFirstMaterial()));
		if (!rls.getTags().isEmpty())
		{
			sb.append(tagsPrefix);
			sb.append(Joiner.on(tagsSeparator).join(rls.getTags()));
		}
		if (rls.getGroup() != null)
		{
			sb.append(groupPrefix);
			sb.append(rls.getGroup().getName());
		}
		return sb.toString();
	}
}
