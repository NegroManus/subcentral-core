package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.release.Release;

public class ReleaseNamer extends AbstractPropertySequenceNamer<Release>
{
	/**
	 * The parameter key for the Boolean value "useName".
	 */
	public static final String	PARAM_USE_NAME_KEY		= "useName";
	public static final Boolean	PARAM_USE_NAME_DEFAULT	= Boolean.FALSE;

	private NamingService		mediaNamingService		= NamingStandards.NAMING_SERVICE;

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
	public Class<Release> getType()
	{
		return Release.class;
	}

	@Override
	public String doName(Release rls, Map<String, Object> params) throws IntrospectionException
	{
		boolean useName = Namings.readParameter(params, PARAM_USE_NAME_KEY, Boolean.class, PARAM_USE_NAME_DEFAULT);

		if (useName)
		{
			return rls.getName();
		}
		else
		{
			Builder b = new Builder();
			b.appendString(Release.PROP_MEDIA,
					Medias.name(rls.getMedia(), mediaNamingService, params, getSeparatorBetween(Release.PROP_MEDIA, Release.PROP_MEDIA, null)));
			b.appendAllIfNotEmpty(Release.PROP_TAGS, rls.getTags());
			if (rls.getGroup() != null)
			{
				b.append(Release.PROP_GROUP, rls.getGroup());
			}
			else if (rls.getSource() != null)
			{
				b.append(Release.PROP_SOURCE, rls.getSource());
			}

			return b.build();
		}
	}
}
