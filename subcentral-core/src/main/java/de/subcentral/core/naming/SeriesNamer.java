package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Series;
import de.subcentral.core.util.Separation;

public class SeriesNamer extends AbstractPropertySequenceNamer<Series>
{
	protected SeriesNamer(PropToStringService propToStringService, Set<Separation> separations, Function<String, String> finalFormatter)
	{
		super(propToStringService, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Series series, Map<String, Object> parameters)
	{
		b.append(Series.PROP_NAME, series.getName());
	}
}
