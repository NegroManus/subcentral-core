package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Series;

public class SeriesNamer extends AbstractPropertySequenceNamer<Series>
{
	/**
	 * The name of the parameter "name" of type {@link String}. The specified name is used for naming the series. The default value is the return value of {@link Series#getName()}. But for example any
	 * alias name may be used.
	 */
	public static final String PARAM_NAME = SeriesNamer.class.getName() + ".name";

	public SeriesNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Series series, Map<String, Object> params)
	{
		String name = NamingUtil.readParameter(params, PARAM_NAME, String.class, series.getName());
		b.appendIfNotNull(Series.PROP_NAME, name);
	}
}
