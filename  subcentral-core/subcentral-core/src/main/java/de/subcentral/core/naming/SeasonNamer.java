package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;

import de.subcentral.core.model.media.Season;

public class SeasonNamer extends AbstractSeparatedPropertiesNamer<Season>
{
	private PropertyDescriptor	propNumber;
	private PropertyDescriptor	propTitle;

	public SeasonNamer()
	{
		try
		{
			propNumber = new PropertyDescriptor("number", Season.class);
			propTitle = new PropertyDescriptor("title", Season.class);
		}
		catch (IntrospectionException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Class<Season> getType()
	{
		return Season.class;
	}

	@Override
	protected String doName(Season season, NamingService namingService, Map<String, Object> parameters) throws Exception
	{
		Builder b = new Builder();
		b.appendIf(propNumber, season.getNumber(), season.isNumbered());
		b.appendIf(propTitle, season.getTitle(), season.isTitled());
		return b.build();
	}
}
