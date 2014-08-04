package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;

public class SeasonNamer extends AbstractPropertySequenceNamer<Season>
{
	@Override
	public Class<Season> getType()
	{
		return Season.class;
	}

	@Override
	protected String doName(Season season, NamingService namingService, Map<String, Object> parameters) throws Exception
	{
		Builder b = new Builder();
		if (season.getSeries() != null)
		{
			b.append(Series.PROP_NAME, season.getSeries());
		}
		b.appendIf(Season.PROP_NUMBER, season.getNumber(), season.isNumbered());
		b.appendIf(Season.PROP_TITLE, season.getTitle(), season.isTitled());
		return b.build();
	}
}
