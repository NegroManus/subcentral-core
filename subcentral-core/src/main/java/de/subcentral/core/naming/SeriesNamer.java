package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Series;

public class SeriesNamer extends AbstractPropertySequenceNamer<Series>
{
	@Override
	protected String doName(Series candidate, Map<String, Object> parameters) throws Exception
	{
		return propToString(Series.PROP_NAME, candidate.getName());
	}
}
