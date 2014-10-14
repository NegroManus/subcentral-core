package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.PropNames;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.util.SimplePropDescriptor;

public class MediaNamer extends AbstractPropertySequenceNamer<Media>
{
	/**
	 * The parameter key for the Boolean value "includeYear". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_YEAR_KEY	= "includeYear";

	@Override
	public String doName(Media media, Map<String, Object> params)
	{
		// settings
		boolean includeYear = Namings.readParameter(params, PARAM_INCLUDE_YEAR_KEY, Boolean.class, Boolean.FALSE);

		Builder b = newBuilder();
		b.append(new SimplePropDescriptor(Media.class, PropNames.NAME), media.getTitleOrName());
		if (includeYear)
		{
			b.appendIfNotNull(new SimplePropDescriptor(Media.class, PropNames.DATE), media.getYear());
		}
		return b.build();
	}
}
