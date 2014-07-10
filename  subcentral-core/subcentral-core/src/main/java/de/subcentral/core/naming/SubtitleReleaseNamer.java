package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleRelease;

public class SubtitleReleaseNamer extends AbstractSeparatedPropertiesNamer<SubtitleRelease>
{
	/**
	 * The parameter key for the MediaRelease value "mediaRelease".
	 */
	public static final String	PARAM_MEDIA_RELEASE_KEY	= "mediaRelease";

	private PropertyDescriptor	propSubtitleLanguage;
	private PropertyDescriptor	propCompatibleMediaReleases;
	private PropertyDescriptor	propTags;
	private PropertyDescriptor	propGroup;

	public SubtitleReleaseNamer()
	{
		try
		{
			propSubtitleLanguage = new PropertyDescriptor("language", Subtitle.class);
			propCompatibleMediaReleases = new PropertyDescriptor("compatibleMediaReleases", SubtitleRelease.class);
			propTags = new PropertyDescriptor("tags", SubtitleRelease.class);
			propGroup = new PropertyDescriptor("group", SubtitleRelease.class);
		}
		catch (IntrospectionException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Class<SubtitleRelease> getType()
	{
		return SubtitleRelease.class;
	}

	@Override
	public String doName(SubtitleRelease rls, NamingService namingService, Map<String, Object> params)
	{
		Validate.notNull(namingService, "namingService cannot be null");
		// read naming settings
		MediaRelease mediaRls = Namings.readParameter(params, PARAM_MEDIA_RELEASE_KEY, MediaRelease.class, rls.getFirstCompatibleMediaRelease());

		Builder b = new Builder();
		b.appendString(propCompatibleMediaReleases, namingService.name(mediaRls, params));
		Subtitle sub = rls.getFirstMaterial();
		if (sub != null)
		{
			b.append(propSubtitleLanguage, sub.getLanguage());
		}
		b.appendAllIfNotEmpty(propTags, rls.getTags());
		b.appendIfNotNull(propGroup, rls.getGroup());
		return b.build();
	}
}
