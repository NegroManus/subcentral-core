package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

import de.subcentral.core.model.media.Series;

public class SeriesNamer extends AbstractSeparatedPropertiesNamer<Series>
{
	private PropertyDescriptor	propName;

	public SeriesNamer()
	{
		try
		{
			propName = new PropertyDescriptor("name", Series.class);
		}
		catch (IntrospectionException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Class<Series> getType()
	{
		return Series.class;
	}

	@Override
	protected String doName(Series candidate, NamingService namingService, Map<String, Object> parameters) throws Exception
	{
		return propToString(propName, candidate.getName());
	}
}
