package de.subcentral.core.naming;

import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;

public class SubtitleAdjustmentNamer extends AbstractPropertySequenceNamer<SubtitleAdjustment>
{
	/**
	 * The parameter key for the Release value.
	 */
	public static final String	PARAM_KEY_RELEASE	= "release";

	@Override
	public Class<SubtitleAdjustment> getType()
	{
		return SubtitleAdjustment.class;
	}

	@Override
	public String doName(SubtitleAdjustment adjustment, NamingService namingService, Map<String, Object> params)
	{
		Validate.notNull(namingService, "namingService cannot be null");
		// read naming settings
		Release rls = Namings.readParameter(params, PARAM_KEY_RELEASE, Release.class, adjustment.getFirstMatchingRelease());

		Builder b = new Builder();
		b.appendString(SubtitleAdjustment.PROP_MATCHING_RELEASES, namingService.name(rls, params));
		Subtitle sub = adjustment.getFirstSubtitle();
		if (sub != null)
		{
			b.append(Subtitle.PROP_LANGUAGE, sub.getLanguage());
			b.appendAllIfNotEmpty(Subtitle.PROP_TAGS, sub.getTags());
			b.appendIfNotNull(Subtitle.PROP_GROUP, sub.getGroup());
		}
		return b.build();
	}
}
