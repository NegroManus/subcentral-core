package de.subcentral.core.naming;

import de.subcentral.core.metadata.media.NamedMedia;

public abstract class AbstractNamedMediaNamer<M extends NamedMedia> extends AbstractPropertySequenceNamer<M>
{
	/**
	 * The name of the parameter "name" of type {@link String}. The specified name is used for naming the {@link NamedMedia}. The default value is the return value of {@link NamedMedia#getName()}. But
	 * for example any {@link NamedMedia#getAliasNames() alias name} may be used.
	 */
	public static final String PARAM_NAME = AbstractNamedMediaNamer.class.getName() + ".name";

	public AbstractNamedMediaNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}
}
