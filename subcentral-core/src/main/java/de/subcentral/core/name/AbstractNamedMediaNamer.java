package de.subcentral.core.name;

import de.subcentral.core.metadata.NamedMetadata;
import de.subcentral.core.metadata.media.NamedMedia;

public abstract class AbstractNamedMediaNamer<M extends NamedMetadata> extends AbstractPropertySequenceNamer<M>
{
	/**
	 * The name of the parameter "name" of type {@link String}. The specified name is used for naming the {@link NamedMetadata}. The default value is the return value of
	 * {@link NamedMetadata#getName()}. But for example any alias name of a media ({@link NamedMedia#getAliasNames() alias name}) may be used.
	 */
	public static final String PARAM_NAME = AbstractNamedMediaNamer.class.getName() + ".name";

	public AbstractNamedMediaNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}
}
