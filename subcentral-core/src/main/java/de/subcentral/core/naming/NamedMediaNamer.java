package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.NamedMedia;

/**
 * Generic Namer for NamedMedia instances.
 */
public class NamedMediaNamer extends AbstractNamedMediaNamer<NamedMedia>
{
	public NamedMediaNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	@Override
	protected void appendName(PropSequenceNameBuilder b, NamedMedia media, Map<String, Object> params)
	{
		String name = NamingUtil.readParameter(params, PARAM_NAME, String.class, media.getName());
		b.appendIfNotNull(Movie.PROP_NAME, name);
	}
}
