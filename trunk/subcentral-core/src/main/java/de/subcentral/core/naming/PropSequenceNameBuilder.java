package de.subcentral.core.naming;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

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

	public PropSequenceNameBuilder appendIfNotBlank(SimplePropDescriptor propDescriptor, CharSequence propValue)
	{
		return appendIf(propDescriptor, propValue, !StringUtils.isBlank(propValue));
	}

	public PropSequenceNameBuilder appendIf(SimplePropDescriptor propDescriptor, Object propValue, boolean condition)
	{
		if (condition)
		{
			return append(propDescriptor, propValue, null);
		}
		return this;
	}

	public PropSequenceNameBuilder appendIfElse(SimplePropDescriptor propDescriptor, Object ifValue, Object elseValue, boolean condition)
	{
		if (condition)
		{
			return append(propDescriptor, ifValue, null);
		}
		return append(propDescriptor, elseValue, null);
	}

	public PropSequenceNameBuilder append(SimplePropDescriptor propDescriptor, Object propValue)
	{
		return append(propDescriptor, propValue, null);
	}

	public PropSequenceNameBuilder append(SimplePropDescriptor propDescriptor, Object propValue, String separationType)
	{
		return appendString(propDescriptor, config.propToStringService.convert(propDescriptor, propValue), separationType);
	}

	public PropSequenceNameBuilder appendString(SimplePropDescriptor propDescr, String propValue)
	{
		return appendString(propDescr, propValue, null);
	}

	public PropSequenceNameBuilder appendString(SimplePropDescriptor propDescr, String propValue, String separationType)
	{
		if (propValue != null && !propValue.isEmpty())
		{
			if (lastProp != null)
			{
				sb.append(Separation.getSeparatorBetween(lastProp, propDescr, separationType, config.separations, config.defaultSeparator));
			}
			sb.append(propValue);
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
		private PropToStringService			propToStringService;
		private Set<Separation>				separations;
		private String						defaultSeparator;
		private Function<String, String>	finalFormatter;

		public Config(Config config)
		{
			this(config.propToStringService, config.separations, config.defaultSeparator, config.finalFormatter);
		}

		public Config()
		{
			this(NamingDefaults.getDefaultPropToStringService(), ImmutableSet.of(), null, null);
		}

		public Config(PropToStringService propToStringService)
		{
			this(propToStringService, ImmutableSet.of(), null, null);
		}

		public Config(PropToStringService propToStringService, Set<Separation> separations)
		{
			this(propToStringService, separations, null, null);
		}

		public Config(PropToStringService propToStringService, Set<Separation> separations, String defaultSeparator)
		{
			this(propToStringService, separations, defaultSeparator, null);
		}

		public Config(PropToStringService propToStringService, Set<Separation> separations, String defaultSeparator,
				Function<String, String> finalFormatter)
		{
			setPropToStringService(propToStringService);
			setSeparations(separations);
			setDefaultSeparator(defaultSeparator);
			this.finalFormatter = finalFormatter;
		}

		public PropToStringService getPropToStringService()
		{
			return propToStringService;
		}

		public void setPropToStringService(PropToStringService propToStringService)
		{
			Objects.requireNonNull(propToStringService, "propToStringService");
			this.propToStringService = propToStringService;
		}

		public Set<Separation> getSeparations()
		{
			return separations;
		}

		public void setSeparations(Set<Separation> separations)
		{
			this.separations = ImmutableSet.copyOf(separations);
		}

		public String getDefaultSeparator()
		{
			return defaultSeparator;
		}

		public void setDefaultSeparator(String defaultSeparator)
		{
			this.defaultSeparator = defaultSeparator != null ? defaultSeparator : Separation.DEFAULT_SEPARATOR;
		}

		public Function<String, String> getFinalFormatter()
		{
			return finalFormatter;
		}

		public void setFinalFormatter(Function<String, String> finalFormatter)
		{
			this.finalFormatter = finalFormatter;
		}
	}
}
