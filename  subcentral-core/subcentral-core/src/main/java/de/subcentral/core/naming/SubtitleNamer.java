package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

import de.subcentral.core.model.subtitle.Subtitle;

public class SubtitleNamer extends AbstractSeparatedPropertiesNamer<Subtitle>
{
	private PropertyDescriptor	propMediaItem;
	private PropertyDescriptor	propLanguage;

	public SubtitleNamer()
	{
		try
		{
			propMediaItem = new PropertyDescriptor("mediaItem", Subtitle.class);
			propLanguage = new PropertyDescriptor("language", Subtitle.class);
		}
		catch (IntrospectionException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Class<Subtitle> getType()
	{
		return Subtitle.class;
	}

	@Override
	public String doName(Subtitle sub, NamingService namingService, Map<String, Object> params)
	{
		Builder b = new Builder();
		b.appendString(propMediaItem, namingService.name(sub.getMediaItem(), params));
		b.append(propLanguage, sub.getLanguage());
		return b.build();
	}
}
