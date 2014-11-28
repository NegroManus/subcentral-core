package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.PropNames;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.NamedMedia;
import de.subcentral.core.util.Separation;
import de.subcentral.core.util.SimplePropDescriptor;

public class NamedMediaNamer extends AbstractPropertySequenceNamer<NamedMedia>
{
	protected NamedMediaNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	/**
	 * The name of the parameter "includeYear" of type {@link Boolean}. If set to {@code true}, the {@link Media#getYear() media's year} is included
	 * in the name, otherwise it is excluded. The default value is {@code false}.
	 */
	public static final String	PARAM_INCLUDE_YEAR	= NamedMediaNamer.class.getName() + "includeYear";

	@Override
	public void buildName(PropSequenceNameBuilder b, NamedMedia media, Map<String, Object> params)
	{
		// read naming parameters
		boolean includeYear = Namings.readParameter(params, PARAM_INCLUDE_YEAR, Boolean.class, Boolean.FALSE);

		b.appendIfNotNull(new SimplePropDescriptor(Media.class, PropNames.TITLE), media.getTitleOrName());
		if (includeYear)
		{
			b.appendIfNotNull(new SimplePropDescriptor(Media.class, PropNames.DATE), media.getYear());
		}
	}
}
