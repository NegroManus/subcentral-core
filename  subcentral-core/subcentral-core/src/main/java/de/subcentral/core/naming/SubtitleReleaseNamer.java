package de.subcentral.core.naming;

import com.google.common.base.Joiner;

import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.subtitle.Subtitle;
import de.subcentral.core.subtitle.SubtitleRelease;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

public class SubtitleReleaseNamer extends AbstractReleaseNamer<Subtitle, SubtitleRelease>
{
	private Replacer	subtitleLanguageReplacer	= NamingStandards.STANDARD_REPLACER;
	private String		subtitleLanguageFormat		= ".%s";

	public Replacer getSubtitleLanguageReplacer()
	{
		return subtitleLanguageReplacer;
	}

	public void setSubtitleLanguageReplacer(Replacer subtitleLanguageReplacer)
	{
		this.subtitleLanguageReplacer = subtitleLanguageReplacer;
	}

	public String getSubtitleLanguageFormat()
	{
		return subtitleLanguageFormat;
	}

	public void setSubtitleLanguageFormat(String subtitleLanguageFormat)
	{
		this.subtitleLanguageFormat = subtitleLanguageFormat;
	}

	@Override
	public Class<SubtitleRelease> getType()
	{
		return SubtitleRelease.class;
	}

	public String name(SubtitleRelease rls, MediaRelease mediaRelease, NamingService namingService)
	{
		if (rls == null)
		{
			return null;
		}
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append(namingService.name(mediaRelease));
			Subtitle sub = rls.getFirstMaterial();
			sb.append(String.format(subtitleLanguageFormat, StringUtil.replace(sub.getLanguage(), subtitleLanguageReplacer)));
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
		catch (Exception e)
		{
			throw new NamingException(rls, e);
		}
	}

	@Override
	public String name(SubtitleRelease rls, NamingService namingService)
	{
		return name(rls, rls.getFirstCompatibleMediaRelease(), namingService);
	}

}
