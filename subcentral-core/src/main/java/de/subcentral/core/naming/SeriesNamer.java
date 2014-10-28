package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Series;

public class SeriesNamer extends AbstractPropertySequenceNamer<Series>
{
	@Override
	public void buildName(PropSequenceNameBuilder b, Series series, Map<String, Object> parameters)
	{
		b.append(Series.PROP_NAME, series.getName());
	}
}
