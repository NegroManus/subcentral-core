package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.Separation;

public class SubtitleAdjustmentNamer extends AbstractPropertySequenceNamer<SubtitleAdjustment>
{
	/**
	 * The parameter key for the Release value. The default value is {@link SubtitleAdjustment#getFirstMatchingRelease()}.
	 */
	public static final String		PARAM_KEY_RELEASE	= "release";

	private final Namer<Release>	releaseNamer;

	protected SubtitleAdjustmentNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter, Namer<Release> releaseNamer)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
		this.releaseNamer = Objects.requireNonNull(releaseNamer, "releaseNamer");
	}

	public Namer<Release> getReleaseNamer()
	{
		return releaseNamer;
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, SubtitleAdjustment adjustment, Map<String, Object> params)
	{
		// read naming settings
		Release rls = Namings.readParameter(params, PARAM_KEY_RELEASE, Release.class, adjustment.getFirstMatchingRelease());

		b.append(SubtitleAdjustment.PROP_MATCHING_RELEASES, releaseNamer.name(rls, params));

		Subtitle sub = adjustment.getFirstSubtitle();
		if (sub != null)
		{
			b.appendIfNotNull(Subtitle.PROP_LANGUAGE, sub.getLanguage());
			b.appendIf(Subtitle.PROP_HEARING_IMPAIRED, Subtitle.TAG_HEARING_IMPAIRED.getName(), sub.isHearingImpaired());
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
			b.appendAll(Subtitle.PROP_TAGS, sub.getTags());
			if (sub.getGroup() != null)
			{
				b.append(Subtitle.PROP_GROUP, sub.getGroup());
			}
			else
			{
				b.appendIfNotNull(Subtitle.PROP_SOURCE, sub.getSource());
			}
		}
	}
}
