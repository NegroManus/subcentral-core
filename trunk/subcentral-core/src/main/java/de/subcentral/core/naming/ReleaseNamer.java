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
	 * The parameter key for the Boolean value "useName". The default value is {@code false}.
	 */
	public static final String	PARAM_USE_NAME_KEY	= "useName";

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
		boolean useName = Namings.readParameter(params, PARAM_USE_NAME_KEY, Boolean.class, Boolean.FALSE);

		if (useName)
		{
			b.appendString(Release.PROP_NAME, rls.getName());
		}
		else
		{
			b.append(Release.PROP_MEDIA, mediaNamingService.name(rls.getMedia(), params));
			b.appendAll(Release.PROP_TAGS, rls.getTags());
			if (rls.getGroup() != null)
			{
				b.append(Release.PROP_GROUP, rls.getGroup());
			}
		}
	}
}
