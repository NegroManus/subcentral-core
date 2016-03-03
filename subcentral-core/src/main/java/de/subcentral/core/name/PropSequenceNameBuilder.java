package de.subcentral.core.name;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.util.Separation;
import de.subcentral.core.util.SimplePropDescriptor;

public class PropSequenceNameBuilder
{
	private final Config			config;
	private final StringBuilder		sb	= new StringBuilder();
	private SimplePropDescriptor	lastProp;

	public PropSequenceNameBuilder(Config config)
	{
		this.config = Objects.requireNonNull(config, "config");
	}

	public Config getConfig()
	{
		return config;
	}

	public PropSequenceNameBuilder appendAll(SimplePropDescriptor propDescriptor, Iterable<?> iterableProperty)
	{
		for (Object prop : iterableProperty)
		{
			append(propDescriptor, prop);
		}
		return this;
	}

	public PropSequenceNameBuilder appendIfNotNull(SimplePropDescriptor propDescriptor, Object propValue)
	{
		return appendIf(propDescriptor, propValue, propValue != null);
	}

	public PropSequenceNameBuilder appendIf(SimplePropDescriptor propDescriptor, Object propValue, boolean condition)
	{
		if (condition)
		{
			return append(propDescriptor, propValue, null);
		}
		return this;
	}

	public PropSequenceNameBuilder append(SimplePropDescriptor propDescriptor, Object propValue)
	{
		return append(propDescriptor, propValue, null);
	}

	public PropSequenceNameBuilder append(SimplePropDescriptor propDescriptor, Object propValue, String separationType)
	{
		return appendRaw(propDescriptor, config.propToStringService.convert(propDescriptor, propValue), separationType);
	}

	public PropSequenceNameBuilder appendRaw(SimplePropDescriptor propDescr, CharSequence propValueString)
	{
		return appendRaw(propDescr, propValueString, null);
	}

	public PropSequenceNameBuilder appendRaw(SimplePropDescriptor propDescr, CharSequence propValueString, String separationType)
	{
		if (propValueString != null && propValueString.length() > 0)
		{
			if (lastProp != null)
			{
				sb.append(Separation.getSeparatorBetween(lastProp, propDescr, separationType, config.separations, config.defaultSeparator));
			}
			sb.append(propValueString);
			lastProp = propDescr;
		}
		return this;
	}

	@Override
	public String toString()
	{
		if (config.finalFormatter == null)
		{
			return sb.toString();
		}
		return config.finalFormatter.apply(sb.toString());
	}

	public static final class Config
	{
		private final PropToStringService		propToStringService;
		private final String					defaultSeparator;
		private final Set<Separation>			separations;
		private final Function<String, String>	finalFormatter;

		public Config(Config config)
		{
			this(config.propToStringService, config.defaultSeparator, config.separations, config.finalFormatter);
		}

		public Config()
		{
			this(NamingDefaults.getDefaultPropToStringService(), null, ImmutableSet.of(), null);
		}

		public Config(PropToStringService propToStringService)
		{
			this(propToStringService, null, ImmutableSet.of(), null);
		}

		public Config(PropToStringService propToStringService, String defaultSeparator)
		{
			this(propToStringService, defaultSeparator, ImmutableSet.of(), null);
		}

		public Config(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations)
		{
			this(propToStringService, defaultSeparator, separations, null);
		}

		public Config(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations, Function<String, String> finalFormatter)
		{
			this.propToStringService = Objects.requireNonNull(propToStringService, "propToStringService");
			this.defaultSeparator = defaultSeparator;
			this.separations = ImmutableSet.copyOf(separations); // includes null check
			this.finalFormatter = finalFormatter;
		}

		public PropToStringService getPropToStringService()
		{
			return propToStringService;
		}

		public String getDefaultSeparator()
		{
			return defaultSeparator;
		}

		public Set<Separation> getSeparations()
		{
			return separations;
		}

		public Function<String, String> getFinalFormatter()
		{
			return finalFormatter;
		}
	}
}
