package de.subcentral.core.parsing;

public class ParsingException extends RuntimeException
{
	private static final long	serialVersionUID	= -1790109092415797792L;

	private final String		text;
	private final Class<?>		entityClass;

	public ParsingException(String text, Class<?> entityClass)
	{
		this(text, entityClass, "", null);
	}

	public ParsingException(String text, Class<?> entityClass, String message)
	{
		this(text, entityClass, message, null);
	}

	public ParsingException(String text, Class<?> entityClass, Throwable cause)
	{
		this(text, entityClass, "", cause);
	}

	public ParsingException(String text, Class<?> entityClass, String message, Throwable cause)
	{
		super(message, cause);
		this.text = text;
		this.entityClass = entityClass;
	}

	public String getText()
	{
		return text;
	}

	public Class<?> getEntityClass()
	{
		return entityClass;
	}

	@Override
	public String getMessage()
	{
		StringBuilder msg = new StringBuilder();
		msg.append(super.getMessage());
		msg.append("; text=");
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
		if (entityClass != null)
		{
			msg.append("; entityClass=");
			msg.append(entityClass);
		}
		return msg.toString();
	}
}
