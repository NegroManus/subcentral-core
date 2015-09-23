package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Series;

public class SeriesNamer extends AbstractPropertySequenceNamer<Series>
{
	public SeriesNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Series series, Map<String, Object> params)
	{
		b.appendIfNotNull(Series.PROP_NAME, series.getName());
	}
}
