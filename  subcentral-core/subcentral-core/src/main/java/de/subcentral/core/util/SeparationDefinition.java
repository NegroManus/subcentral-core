package de.subcentral.core.util;

import java.util.Objects;
import java.util.Set;

public class SeparationDefinition
{
	public static final String	DEFAULT_SEPARATOR	= " ";

	public static String getSeparatorBetween(SimplePropertyDescriptor firstProperty, SimplePropertyDescriptor secondProperty,
			Set<SeparationDefinition> separationDefinitions)
	{
		return getSeparatorBetween(firstProperty, secondProperty, null, separationDefinitions);
	}

	public static String getSeparatorBetween(SimplePropertyDescriptor firstProperty, SimplePropertyDescriptor secondProperty, String separationType,
			Set<SeparationDefinition> separationDefinitions)
	{
		String after = null;
		String before = null;
		String betweenAny = null;
		for (SeparationDefinition sd : separationDefinitions)
		{
			boolean firstPropEquals = Objects.equals(firstProperty, sd.firstProperty);
			boolean secondPropEquals = Objects.equals(secondProperty, sd.secondProperty);
			boolean typeEquals = Objects.equals(separationType, sd.type);
			if (typeEquals)
			{
				if (firstPropEquals && secondPropEquals)
				{
					// between / in-between
					return sd.getSeparator();
				}
				else if (firstPropEquals && sd.secondProperty == null)
				{
					after = sd.separator;
				}
				else if (sd.firstProperty == null && secondPropEquals)
				{
					before = sd.separator;
				}
				else if (sd.firstProperty == null && sd.secondProperty == null)
				{
					betweenAny = sd.separator;
				}
			}

		}
		if (after != null)
		{
			return after;
		}
		else if (before != null)
		{
			return before;
		}
		else if (betweenAny != null)
		{
			return betweenAny;
		}
		return DEFAULT_SEPARATOR;
	}

	private final SimplePropertyDescriptor	firstProperty;
	private final SimplePropertyDescriptor	secondProperty;
	private final String					type;
	private final String					separator;

	public static SeparationDefinition betweenAny(String separator)
	{
		return new SeparationDefinition(null, null, null, separator);
	}

	public static SeparationDefinition betweenAny(String type, String separator)
	{
		return new SeparationDefinition(null, null, type, separator);
	}

	public static SeparationDefinition after(SimplePropertyDescriptor property, String separator)
	{
		return new SeparationDefinition(property, null, null, separator);
	}

	public static SeparationDefinition after(SimplePropertyDescriptor property, String type, String separator)
	{
		return new SeparationDefinition(property, null, type, separator);
	}

	public static SeparationDefinition before(SimplePropertyDescriptor property, String separator)
	{
		return new SeparationDefinition(null, property, null, separator);
	}

	public static SeparationDefinition before(SimplePropertyDescriptor property, String type, String separator)
	{
		return new SeparationDefinition(null, property, type, separator);
	}

	public static SeparationDefinition inBetween(SimplePropertyDescriptor property, String separator)
	{
		return new SeparationDefinition(property, property, null, separator);
	}

	public static SeparationDefinition inBetween(SimplePropertyDescriptor property, String type, String separator)
	{
		return new SeparationDefinition(property, property, type, separator);
	}

	public static SeparationDefinition between(SimplePropertyDescriptor firstProperty, SimplePropertyDescriptor secondProperty, String separator)
	{
		return new SeparationDefinition(firstProperty, secondProperty, null, separator);
	}

	public static SeparationDefinition between(SimplePropertyDescriptor firstProperty, SimplePropertyDescriptor secondProperty, String type,
			String separator)
	{
		return new SeparationDefinition(firstProperty, secondProperty, type, separator);
	}

	private SeparationDefinition(SimplePropertyDescriptor firstProperty, SimplePropertyDescriptor secondProperty, String type, String separator)
	{
		this.firstProperty = firstProperty;
		this.secondProperty = secondProperty;
		this.type = type;
		this.separator = separator == null ? DEFAULT_SEPARATOR : separator;
	}

	public SimplePropertyDescriptor getFirstProperty()
	{
		return firstProperty;
	}

	public SimplePropertyDescriptor getSecondProperty()
	{
		return secondProperty;
	}

	public String getType()
	{
		return type;
	}

	public String getSeparator()
	{
		return separator;
	}
}
