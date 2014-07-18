package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.release.Release;

public class MediaReleaseNamer extends AbstractPropertySequenceNamer<Release>
{
	@Override
	public Class<Release> getType()
	{
		return Release.class;
	}

	@Override
	public String doName(Release rls, NamingService namingService, Map<String, Object> params) throws IntrospectionException
	{
		Validate.notNull(namingService, "namingService cannot be null");
		Builder b = new Builder();
		b.appendString(Release.PROP_MEDIA,
				Medias.name(rls.getMedia(),
						namingService,
						params,
						getSeparatorBetween(Release.PROP_MEDIA, Release.PROP_MEDIA, null)));
		b.appendAllIfNotEmpty(Release.PROP_TAGS, rls.getTags());
		b.appendIfNotNull(Release.PROP_GROUP, rls.getGroup());
		return b.build();
	}
}
