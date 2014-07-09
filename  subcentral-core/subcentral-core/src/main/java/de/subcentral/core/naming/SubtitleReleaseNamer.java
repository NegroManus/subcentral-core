package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleRelease;

public class SubtitleReleaseNamer extends AbstractNamer<SubtitleRelease>
{
	/**
	 * The naming setting key for the MediaRelease value "mediaRelease".
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
		// read naming settings
		MediaRelease mediaRls = Namings.readParameter(params, PARAM_MEDIA_RELEASE_KEY, MediaRelease.class, rls.getFirstCompatibleMediaRelease());

		StringBuilder sb = new StringBuilder();
		sb.append(namingService.name(mediaRls));
		Subtitle sub = rls.getFirstMaterial();
		if (sub != null)
		{
			sb.append(getSeparatorBetween(propCompatibleMediaReleases, propSubtitleLanguage));
			sb.append(propToString(propSubtitleLanguage, sub.getLanguage()));
		}
		if (!rls.getTags().isEmpty())
		{
			sb.append(getSeparatorBetween(null, propTags));
			sb.append(propToString(propTags, rls.getTags()));
		}
		if (rls.getGroup() != null)
		{
			sb.append(getSeparatorBetween(null, propGroup));
			sb.append(propToString(propGroup, rls.getGroup()));
		}
		return sb.toString();
	}
}
