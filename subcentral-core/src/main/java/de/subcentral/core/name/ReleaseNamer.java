package de.subcentral.core.name;

import java.util.Objects;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.util.Context;

public class ReleaseNamer extends AbstractPropertySequenceNamer<Release>
{
	/**
	 * The name of the parameter "preferName" of type {@link Boolean}. If set to {@code true} and the {@link Release#getName() release's name} is not {@code null}, that name is returned, otherwise the
	 * computed name is returned. The default value is {@code false}.
	 */
	public static final String	PARAM_PREFER_NAME		= ReleaseNamer.class.getName() + ".preferName";

	/**
	 * The name of the parameter "includeSource" of type {@link Boolean}. If set to {@code true} and {@link #PARAM_INCLUDE_GROUP} is false or there is no group, the source is included in the name. The
	 * default value is {@code false}.
	 */
	public static final String	PARAM_INCLUDE_SOURCE	= ReleaseNamer.class.getName() + ".includeSource";

	private final NamingService	mediaNamingService;

	public ReleaseNamer(PropSequenceNameBuilder.Config config, NamingService mediaNamingService)
	{
		super(config);
		this.mediaNamingService = Objects.requireNonNull(mediaNamingService, "mediaNamingService");
	}

	public NamingService getMediaNamingService()
	{
		return mediaNamingService;
	}

	@Override
	protected void appendName(PropSequenceNameBuilder b, Release rls, Context ctx)
	{
		// read naming parameters
		if (rls.getName() != null && ctx.getBoolean(PARAM_PREFER_NAME, Boolean.FALSE))
		{
			b.append(Release.PROP_NAME, rls.getName());
			return;
		}
		b.appendRaw(Release.PROP_MEDIA, mediaNamingService.name(rls.getMedia(), ctx));
		b.appendAll(Release.PROP_TAGS, rls.getTags());
		if (rls.getSource() != null && ctx.getBoolean(PARAM_INCLUDE_SOURCE, Boolean.FALSE))
		{
			b.append(Subtitle.PROP_SOURCE, rls.getSource());
		}
	}
}
