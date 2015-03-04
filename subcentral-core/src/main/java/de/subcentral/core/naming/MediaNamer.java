package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.PropNames;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.util.SimplePropDescriptor;

public class MediaNamer extends AbstractPropertySequenceNamer<Media>
{
	public MediaNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	/**
	 * The name of the parameter "includeYear" of type {@link Boolean}. If set to {@code true}, the {@link Media#getYear() media's year} is included
	 * in the name, otherwise it is excluded. The default value is {@code false}.
	 */
	public static final String	PARAM_INCLUDE_YEAR	= MediaNamer.class.getName() + "includeYear";

	@Override
	public void buildName(PropSequenceNameBuilder b, Media media, Map<String, Object> params)
	{
		// read naming parameters
		boolean includeYear = NamingUtil.readParameter(params, PARAM_INCLUDE_YEAR, Boolean.class, Boolean.FALSE);

		b.appendIfNotNull(new SimplePropDescriptor(Media.class, PropNames.TITLE), media.getTitleOrName());
		if (includeYear)
		{
			b.appendIfNotNull(new SimplePropDescriptor(Media.class, PropNames.DATE), media.getYear());
		}
	}
}
