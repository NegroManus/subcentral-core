package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class SubtitleNamer extends AbstractPropertySequenceNamer<Subtitle>
{
	/**
	 * The name of the parameter "includeGroup" of type {@link Boolean}. If set to {@code true}, the group is included in the name. The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_GROUP		= SubtitleNamer.class.getName() + ".includeGroup";

	/**
	 * The name of the parameter "includeSource" of type {@link Boolean}. If set to {@code true} and {@link #PARAM_INCLUDE_GROUP} is false or there is no group, the source is included in the name. The
	 * default value is {@code false}.
	 */
	public static final String	PARAM_INCLUDE_SOURCE	= SubtitleNamer.class.getName() + ".includeSource";

	private final NamingService	mediaNamingService;

	public SubtitleNamer(PropSequenceNameBuilder.Config config, NamingService mediaNamingService)
	{
		super(config);
		this.mediaNamingService = Objects.requireNonNull(mediaNamingService, "mediaNamingService");
	}

	public NamingService getMediaNamingService()
	{
		return mediaNamingService;
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Subtitle sub, Map<String, Object> params)
	{
		// media
		b.appendIfNotBlank(Subtitle.PROP_MEDIA, mediaNamingService.name(sub.getMedia(), params));

		// language
		b.appendIfNotNull(Subtitle.PROP_LANGUAGE, sub.getLanguage());

		// group / source
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
