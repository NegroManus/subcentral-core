package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.release.MediaRelease;

public class MediaReleaseNamer extends AbstractNamer<MediaRelease>
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
	public String doName(MediaRelease rls, NamingService namingService, Map<String, Object> parameters) throws IntrospectionException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(propToString(propMaterials,
				Medias.name(rls.getMaterials(), namingService, parameters, getSeparatorBetween(propMaterials, propMaterials))));
		if (!rls.getTags().isEmpty())
		{
			sb.append(getSeparatorBetween(propMaterials, propTags));
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
