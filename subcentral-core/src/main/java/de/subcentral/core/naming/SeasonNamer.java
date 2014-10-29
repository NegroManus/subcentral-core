package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.util.Separation;

public class SeasonNamer extends AbstractPropertySequenceNamer<Season>
{
	protected SeasonNamer(PropToStringService propToStringService, Set<Separation> separations, Function<String, String> finalFormatter)
	{
		super(propToStringService, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Season season, Map<String, Object> parameters)
	{
		b.appendIf(Series.PROP_NAME, season.getSeries(), season.getSeries() != null);
		b.appendIf(Season.PROP_NUMBER, season.getNumber(), season.isNumbered());
		b.appendIf(Season.PROP_TITLE, season.getTitle(), season.isTitled());
	}
}
