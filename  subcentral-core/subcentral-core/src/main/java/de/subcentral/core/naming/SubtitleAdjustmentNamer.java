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
			if (sub.isHearingImpaired())
			{
				b.append(Subtitle.PROP_HEARING_IMPAIRED, Subtitle.TAG_HEARING_IMPAIRED.getName());
			}
			switch (sub.getForeignParts())
			{
				case NONE:
					break;
				case INCLUDED:
					b.append(Subtitle.PROP_FOREIGN_PARTS, "FOREIGN PARTS INCL");
					break;
				case EXCLUDED:
					b.append(Subtitle.PROP_FOREIGN_PARTS, "FOREIGN PARTS EXCL");
					break;
				case ONLY:
					b.append(Subtitle.PROP_FOREIGN_PARTS, "FOREIGN PARTS ONLY");
					break;
				default:
					break;
			}
			b.appendAllIfNotEmpty(Subtitle.PROP_TAGS, sub.getTags());
			if (sub.getGroup() != null)
			{
				b.append(Subtitle.PROP_GROUP, sub.getGroup());
			}
			else if (sub.getSource() != null)
			{
				b.append(Subtitle.PROP_SOURCE, sub.getSource());
			}
		}
		return b.build();
	}
}
