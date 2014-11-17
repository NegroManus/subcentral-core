package de.subcentral.core.infodb;

public class InfoDbQueryException extends RuntimeException
{
	private static final long	serialVersionUID	= -5423474090679019566L;

	private final Object		query;

	public InfoDbQueryException(Object query)
	{
		super(generateMessage(query, null));
		this.query = query;
	}

	public InfoDbQueryException(Object query, String message)
	{
		super(generateMessage(query, message));

		this.query = query;
	}

	public InfoDbQueryException(Object query, Throwable cause)
	{
		super(generateMessage(query, null), cause);
		this.query = query;
	}

	public InfoDbQueryException(Object query, String message, Throwable cause)
	{
		super(generateMessage(query, message), cause);
		this.query = query;
	}

	public Object getQuery()
	{
		return query;
	}

	private static final String generateMessage(Object query, String additionalMsg)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Query failed: ");
		sb.append(query);
		if (additionalMsg != null)
		{
			sb.append(": ");
			sb.append(additionalMsg);
		}
		return sb.toString();
	}
}
