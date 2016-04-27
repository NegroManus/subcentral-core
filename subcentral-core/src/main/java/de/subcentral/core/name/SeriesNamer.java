package de.subcentral.core.name;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.util.Context;

public class SeriesNamer extends AbstractNamedMediaNamer<Series>
{
	public SeriesNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	@Override
	protected void appendName(PropSequenceNameBuilder b, Series series, Context ctx)
	{
		String name = ctx.getString(PARAM_NAME, series.getName());
		b.appendIfNotNull(Series.PROP_NAME, name);
	}
}
