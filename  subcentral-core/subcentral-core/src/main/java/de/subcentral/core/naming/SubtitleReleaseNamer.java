package de.subcentral.core.naming;

import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleRelease;

public class SubtitleReleaseNamer extends AbstractPropertySequenceNamer<SubtitleRelease>
{
	/**
	 * The parameter key for the MediaRelease value "mediaRelease".
	 */
	public static final String	PARAM_MEDIA_RELEASE_KEY	= "mediaRelease";

	@Override
	public Class<SubtitleRelease> getType()
	{
		return SubtitleRelease.class;
	}

	@Override
	public String doName(SubtitleRelease rls, NamingService namingService, Map<String, Object> params)
	{
		Validate.notNull(namingService, "namingService cannot be null");
		// read naming settings
		MediaRelease mediaRls = Namings.readParameter(params, PARAM_MEDIA_RELEASE_KEY, MediaRelease.class, rls.getFirstMatchingMediaRelease());

		Builder b = new Builder();
		b.appendString(SubtitleRelease.PROP_MATCHING_MEDIA_RELEASES, namingService.name(mediaRls, params));
		Subtitle sub = rls.getFirstMaterial();
		if (sub != null)
		{
			b.append(Subtitle.PROP_LANGUAGE, sub.getLanguage());
			b.appendAllIfNotEmpty(Subtitle.PROP_TAGS, sub.getTags());
			b.appendIfNotNull(Subtitle.PROP_GROUP, sub.getGroup());
		}
		return b.build();
	}
}
