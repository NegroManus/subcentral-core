package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.Separation;

public class ReleaseNamer extends AbstractPropertySequenceNamer<Release>
{
	/**
	 * The parameter key for the Boolean value "preferName". The default value is {@code false}. If set to true, the release's name (if not null) is
	 * returned as the name.
	 */
	public static final String	PARAM_PREFER_NAME	= "preferName";

	private final NamingService	mediaNamingService;

	protected ReleaseNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter, NamingService mediaNamingService)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
		this.mediaNamingService = Objects.requireNonNull(mediaNamingService, "mediaNamingService");
	}

	public NamingService getMediaNamingService()
	{
		return mediaNamingService;
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Release rls, Map<String, Object> params)
	{
		// read naming parameters
		boolean preferName = Namings.readParameter(params, PARAM_PREFER_NAME, Boolean.class, Boolean.FALSE);

		if (preferName && rls.getName() != null)
		{
			b.append(Release.PROP_NAME, rls.getName());
			return;
		}
		b.appendIfNotBlank(Release.PROP_MEDIA, mediaNamingService.name(rls.getMedia(), params));
		b.appendAll(Release.PROP_TAGS, rls.getTags());
		b.appendIfNotNull(Release.PROP_GROUP, rls.getGroup());
	}
}
