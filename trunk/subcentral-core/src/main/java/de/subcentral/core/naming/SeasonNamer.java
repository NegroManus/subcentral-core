package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;

public class SeasonNamer extends AbstractPropertySequenceNamer<Season>
{
	@Override
	public void buildName(PropSequenceNameBuilder b, Season season, Map<String, Object> parameters)
	{
		if (season.getSeries() != null)
		{
			b.append(Series.PROP_NAME, season.getSeries());
		}
		b.appendIf(Season.PROP_NUMBER, season.getNumber(), season.isNumbered());
		b.appendIf(Season.PROP_TITLE, season.getTitle(), season.isTitled());
	}
}
