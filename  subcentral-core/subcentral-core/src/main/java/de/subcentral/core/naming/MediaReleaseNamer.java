package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.release.MediaRelease;

public class MediaReleaseNamer extends AbstractPropertySequenceNamer<MediaRelease>
{
	@Override
	public Class<MediaRelease> getType()
	{
		return MediaRelease.class;
	}

	@Override
	public String doName(MediaRelease rls, NamingService namingService, Map<String, Object> params) throws IntrospectionException
	{
		Validate.notNull(namingService, "namingService cannot be null");
		Builder b = new Builder();
		b.appendString(MediaRelease.PROP_MATERIALS,
				Medias.name(rls.getMaterials(),
						namingService,
						params,
						getSeparatorBetween(MediaRelease.PROP_MATERIALS, MediaRelease.PROP_MATERIALS, null)));
		b.appendAllIfNotEmpty(MediaRelease.PROP_TAGS, rls.getTags());
		b.appendIfNotNull(MediaRelease.PROP_GROUP, rls.getGroup());
		return b.build();
	}
}
