package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.release.MediaRelease;

public class MediaReleaseNamer extends AbstractSeparatedPropertiesNamer<MediaRelease>
{
	private PropertyDescriptor	propMaterials;
	private PropertyDescriptor	propTags;
	private PropertyDescriptor	propGroup;

	public MediaReleaseNamer()
	{
		try
		{
			propMaterials = new PropertyDescriptor("materials", MediaRelease.class);
			propTags = new PropertyDescriptor("tags", MediaRelease.class);
			propGroup = new PropertyDescriptor("group", MediaRelease.class);
		}
		catch (IntrospectionException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Class<MediaRelease> getType()
	{
		return MediaRelease.class;
	}

	@Override
	public String doName(MediaRelease rls, NamingService namingService, Map<String, Object> params) throws IntrospectionException
	{
		Builder b = new Builder();
		b.appendString(propMaterials, Medias.name(rls.getMaterials(), namingService, params, getSeparatorBetween(propMaterials, propMaterials, null)));
		b.appendCollectionIfNotEmpty(propTags, rls.getTags());
		b.appendIfNotNull(propGroup, rls.getGroup());
		return b.build();
	}
}
