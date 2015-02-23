package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.util.Separation;

public class SeriesNamer extends AbstractPropertySequenceNamer<Series>
{
	public SeriesNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Series series, Map<String, Object> parameters)
	{
		b.appendIfNotNull(Series.PROP_NAME, series.getName());
	}
}
