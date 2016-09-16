package de.subcentral.core.name;

import de.subcentral.core.metadata.NamedMetadata;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.util.Context;

/**
 * Generic Namer for {@link NamedMetadata} instances.
 */
public class NamedMetadataNamer extends AbstractNamedMediaNamer<NamedMetadata> {
	public NamedMetadataNamer(PropSequenceNameBuilder.Config config) {
		super(config);
	}

	@Override
	protected void appendName(PropSequenceNameBuilder b, NamedMetadata metadata, Context ctx) {
		String name = ctx.getString(PARAM_NAME, metadata.getName());
		b.appendIfNotNull(Movie.PROP_NAME, name);
	}
}
