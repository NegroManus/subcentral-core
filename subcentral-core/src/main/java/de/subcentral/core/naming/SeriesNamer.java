package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Series;

public class SeriesNamer extends AbstractNamedMediaNamer<Series>
{
	public SeriesNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	@Override
	protected void appendName(PropSequenceNameBuilder b, Series series, Map<String, Object> params)
	{
		String name = NamingUtil.readParameter(params, PARAM_NAME, String.class, series.getName());
		b.appendIfNotNull(Series.PROP_NAME, name);
	}
}
