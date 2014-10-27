package de.subcentral.core.naming;

import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.release.Release;

public class ReleaseNamer extends AbstractPropertySequenceNamer<Release>
{
	/**
	 * The parameter key for the Boolean value "useName". The default value is {@code false}.
	 */
	public static final String	PARAM_USE_NAME_KEY	= "useName";

	private NamingService		mediaNamingService	= NamingStandards.getDefaultNamingService();

	public NamingService getMediaNamingService()
	{
		return mediaNamingService;
	}

	public void setMediaNamingService(NamingService mediaNamingService)
	{
		Validate.notNull(mediaNamingService, "mediaNamingService cannot be null");
		this.mediaNamingService = mediaNamingService;
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
			b.appendAllIfNotEmpty(Release.PROP_TAGS, rls.getTags());
			if (rls.getGroup() != null)
			{
				b.append(Release.PROP_GROUP, rls.getGroup());
			}
		}
	}
}
