package de.subcentral.core.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Tag;
import de.subcentral.core.subtitle.Subtitle;
import de.subcentral.core.subtitle.SubtitleRelease;
import de.subcentral.core.util.Replacer;

public class SubtitleReleaseNamer extends AbstractReleaseNamer<SubtitleRelease, Subtitle> implements Namer<SubtitleRelease>
{
	/**
	 * The naming setting key for the MediaRelease value "mediaRelease".
	 */
	public static final String	PARAM_MEDIA_RELEASE_KEY						= SubtitleReleaseNamer.class.getName() + ".mediaRelease";

	private Replacer			subtitleLanguageReplacer					= NamingStandards.STANDARD_REPLACER;
	private String				subtitleLanguageFormat						= "%s";

	private String				mediaReleaseAndSubtitleLanguageSeparator	= ".";

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

	@Override
	public String doName(SubtitleRelease rls, NamingService namingService, Map<String, Object> params)
	{
		// read naming settings
		MediaRelease mediaRls = Namings.readParameter(params, PARAM_MEDIA_RELEASE_KEY, MediaRelease.class, rls.getFirstCompatibleMediaRelease());

		StringBuilder sb = new StringBuilder();
		sb.append(namingService.name(mediaRls));
		Subtitle sub = rls.getFirstMaterial();
		if (sub != null)
		{
			sb.append(formatSubtitleLanguage(sub.getLanguage()));
		}
		if (!rls.getTags().isEmpty())
		{
			sb.append(mediaReleaseAndSubtitleLanguageSeparator);
			List<String> formattedTags = new ArrayList<>();
			for (Tag tag : rls.getTags())
			{
				formattedTags.add(formatTag(tag.getName()));
			}
			sb.append(Joiner.on(tagsSeparator).join(rls.getTags()));
		}
		if (rls.getGroup() != null)
		{
			sb.append(tagsAndGroupSeparator);
			sb.append(formatGroup(rls.getGroup().getName()));
		}
		return sb.toString();
	}

	public String formatSubtitleLanguage(String language)
	{
		return String.format(subtitleLanguageFormat, Replacer.replace(language, subtitleLanguageReplacer));
	}

}
