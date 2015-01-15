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
	 * The name of the parameter "preferName" of type {@link Boolean}. If set to {@code true} and the {@link SubtitleAdjustment#getName() subtitle
	 * adjustment's name} is not {@code null}, that name is returned, otherwise the computed name is returned. The default value is {@code false}.
	 */
	public static final String		PARAM_PREFER_NAME	= SubtitleAdjustmentNamer.class.getName() + ".preferName";

	/**
	 * The name of the parameter "release" of type {@link Release}. The specified release is used for naming the subtitle adjustment. The default
	 * value is the return value of {@link SubtitleAdjustment#getFirstMatchingRelease()}.
	 */
	public static final String		PARAM_RELEASE		= SubtitleAdjustmentNamer.class.getName() + ".release";

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
		// read useName parameter
		boolean preferName = Namings.readParameter(params, PARAM_PREFER_NAME, Boolean.class, Boolean.FALSE);
		if (preferName && adjustment.getName() != null)
		{
			b.append(SubtitleAdjustment.PROP_NAME, adjustment.getName());
			return;
		}

		// read other naming parameters
		Release rls = Namings.readParameter(params, PARAM_RELEASE, Release.class, adjustment.getFirstMatchingRelease());
		b.appendIfNotBlank(SubtitleAdjustment.PROP_MATCHING_RELEASES, releaseNamer.name(rls, params));

		Subtitle sub = adjustment.getFirstSubtitle();
		if (sub != null)
		{
			b.appendIfNotNull(Subtitle.PROP_LANGUAGE, sub.getLanguage());
			b.append(Subtitle.PROP_HEARING_IMPAIRED, sub.isHearingImpaired());
			b.append(Subtitle.PROP_FOREIGN_PARTS, sub.getForeignParts());
			b.appendAll(Subtitle.PROP_TAGS, sub.getTags());
			b.appendIfNotNull(Subtitle.PROP_VERSION, sub.getVersion());
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
