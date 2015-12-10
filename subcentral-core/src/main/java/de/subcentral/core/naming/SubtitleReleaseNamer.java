package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;

public class SubtitleReleaseNamer extends AbstractPropertySequenceNamer<SubtitleRelease>
{
	/**
	 * The name of the parameter "preferName" of type {@link Boolean}. If set to {@code true} and the {@link SubtitleRelease#getName() subtitle adjustment's name} is not {@code null}, that name is
	 * returned, otherwise the computed name is returned. The default value is {@code false}.
	 */
	public static final String		PARAM_PREFER_NAME		= SubtitleReleaseNamer.class.getName() + ".preferName";

	/**
	 * The name of the parameter "release" of type {@link Release}. The specified release is used for naming the subtitle adjustment. The default value is the return value of
	 * {@link SubtitleRelease#getFirstMatchingRelease()}.
	 */
	public static final String		PARAM_RELEASE			= SubtitleReleaseNamer.class.getName() + ".release";

	/**
	 * Shortcut to {@link SubtitleNamer#PARAM_INCLUDE_GROUP}.
	 */
	public static final String		PARAM_INCLUDE_GROUP		= SubtitleNamer.PARAM_INCLUDE_GROUP;

	/**
	 * Shortcut to {@link SubtitleNamer#PARAM_INCLUDE_SOURCE}.
	 */
	public static final String		PARAM_INCLUDE_SOURCE	= SubtitleNamer.PARAM_INCLUDE_SOURCE;

	private final Namer<Release>	releaseNamer;

	public SubtitleReleaseNamer(PropSequenceNameBuilder.Config config, Namer<Release> releaseNamer)
	{
		super(config);
		this.releaseNamer = Objects.requireNonNull(releaseNamer, "releaseNamer");
	}

	public Namer<Release> getReleaseNamer()
	{
		return releaseNamer;
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, SubtitleRelease adj, Map<String, Object> params)
	{
		// read useName parameter
		boolean preferName = NamingUtil.readParameter(params, PARAM_PREFER_NAME, Boolean.class, Boolean.FALSE);
		if (preferName && adj.getName() != null)
		{
			b.append(SubtitleRelease.PROP_NAME, adj.getName());
			return;
		}

		// read other naming parameters
		Release rls = NamingUtil.readParameter(params, PARAM_RELEASE, Release.class, adj.getFirstMatchingRelease());
		b.appendIfNotBlank(SubtitleRelease.PROP_MATCHING_RELEASES, releaseNamer.name(rls, params));

		Subtitle sub = adj.getFirstSubtitle();
		if (sub != null)
		{
			b.appendIfNotNull(Subtitle.PROP_LANGUAGE, sub.getLanguage());
		}
		b.appendAll(SubtitleRelease.PROP_TAGS, adj.getTags());
		b.appendIfNotNull(SubtitleRelease.PROP_VERSION, adj.getVersion());
		if (sub != null)
		{
			// read includeGroup parameter
			boolean includeGroup = NamingUtil.readParameter(params, PARAM_INCLUDE_GROUP, Boolean.class, Boolean.TRUE);
			if (includeGroup && sub.getGroup() != null)
			{
				b.append(Subtitle.PROP_GROUP, sub.getGroup());
			}
			else if (NamingUtil.readParameter(params, PARAM_INCLUDE_SOURCE, Boolean.class, Boolean.FALSE))
			{
				b.appendIfNotNull(Subtitle.PROP_SOURCE, sub.getSource());
			}
		}
	}
}