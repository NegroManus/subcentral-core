package de.subcentral.core.parsing;

import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

public class ParsingException extends RuntimeException
{
	private static final long	serialVersionUID	= -1790109092415797792L;

	private final String		text;
	private final Set<Class<?>>	targetTypes;

	// no targetType
	public ParsingException(String text)
	{
		this(text, ImmutableSet.of(), "", null);
	}

	public ParsingException(String text, String message)
	{
		this(text, ImmutableSet.of(), message, null);
	}

	public ParsingException(String text, Throwable cause)
	{
		this(text, ImmutableSet.of(), "", cause);
	}

	public ParsingException(String text, String message, Throwable cause)
	{
		this(text, ImmutableSet.of(), message, cause);
	}

	// single targetType
	public ParsingException(String text, Class<?> targetType)
	{
		this(text, ImmutableSet.of(targetType), "", null);
	}

	public ParsingException(String text, Class<?> targetType, String message)
	{
		this(text, ImmutableSet.of(targetType), message, null);
	}

	public ParsingException(String text, Class<?> targetType, Throwable cause)
	{
		this(text, ImmutableSet.of(targetType), "", cause);
	}

	public ParsingException(String text, Class<?> targetType, String message, Throwable cause)
	{
		this(text, ImmutableSet.of(targetType), message, cause);
	}

	// targetTypes
	public ParsingException(String text, Set<Class<?>> targetTypes)
	{
		this(text, targetTypes, "", null);
	}

	public ParsingException(String text, Set<Class<?>> targetTypes, String message)
	{
		this(text, targetTypes, message, null);
	}

	public ParsingException(String text, Set<Class<?>> targetTypes, Throwable cause)
	{
		this(text, targetTypes, "", cause);
	}

	public ParsingException(String text, Set<Class<?>> targetTypes, String message, Throwable cause)
	{
		super(message, cause);
		this.text = text;
		this.targetTypes = targetTypes;
	}

	public String getText()
	{
		return text;
	}

	public Set<Class<?>> gettargetTypes()
	{
		return targetTypes;
	}

	@Override
	public String getMessage()
	{
		StringBuilder msg = new StringBuilder();
		msg.append(super.getMessage());
		if (msg.length() > 0)
		{
			msg.append("; ");
		}
		msg.append("text=");
		if (text != null)
		{
			msg.append('"');
			msg.append(text);
			msg.append('"');
		}
		else
		{
			msg.append("null");
		}
		if (!targetTypes.isEmpty())
		{
			msg.append("; targetTypes=");
			Joiner.on(", ").appendTo(msg, targetTypes);
		}
		return msg.toString();
	}
}
