package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.PropNames;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.util.Separation;
import de.subcentral.core.util.SimplePropDescriptor;

public class MediaNamer extends AbstractPropertySequenceNamer<Media>
{
	protected MediaNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	/**
	 * The parameter key for the Boolean value "includeYear". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_YEAR_KEY	= "includeYear";

	@Override
	public void buildName(PropSequenceNameBuilder b, Media media, Map<String, Object> params)
	{
		// settings
		boolean includeYear = Namings.readParameter(params, PARAM_INCLUDE_YEAR_KEY, Boolean.class, Boolean.FALSE);

		b.append(new SimplePropDescriptor(Media.class, PropNames.NAME), media.getTitleOrName());
		if (includeYear)
		{
			b.appendIfNotNull(new SimplePropDescriptor(Media.class, PropNames.DATE), media.getYear());
		}
	}
}
