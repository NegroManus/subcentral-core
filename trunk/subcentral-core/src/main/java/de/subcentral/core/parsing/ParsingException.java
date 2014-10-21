package de.subcentral.core.parsing;

public class ParsingException extends RuntimeException
{
	private static final long	serialVersionUID	= -1790109092415797792L;

	private final String		text;
	private final Class<?>		targetClass;

	public ParsingException(String text, Class<?> targetClass)
	{
		this(text, targetClass, "", null);
	}

	public ParsingException(String text, Class<?> targetClass, String message)
	{
		this(text, targetClass, message, null);
	}

	public ParsingException(String text, Class<?> targetClass, Throwable cause)
	{
		this(text, targetClass, "", cause);
	}

	public ParsingException(String text, Class<?> targetClass, String message, Throwable cause)
	{
		super(message, cause);
		this.text = text;
		this.targetClass = targetClass;
	}

	public String getText()
	{
		return text;
	}

	public Class<?> getEntityClass()
	{
		return targetClass;
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
		if (targetClass != null)
		{
			msg.append("; targetClass=");
			msg.append(targetClass);
		}
		return msg.toString();
	}
}
