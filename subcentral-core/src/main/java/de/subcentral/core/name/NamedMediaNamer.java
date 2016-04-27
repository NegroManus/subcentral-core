package de.subcentral.core.name;

import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.NamedMedia;
import de.subcentral.core.util.Context;

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
	protected void appendName(PropSequenceNameBuilder b, NamedMedia media, Context ctx)
	{
		String name = ctx.getString(PARAM_NAME, media.getName());
		b.appendIfNotNull(Movie.PROP_NAME, name);
	}
}
