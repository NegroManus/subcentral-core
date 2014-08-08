package de.subcentral.core.lookup;

public class LookupException extends RuntimeException
{
	private static final long	serialVersionUID	= -5423474090679019566L;

	private final Object		query;

	public LookupException(Object query)
	{
		super(generateMessage(query, null));
		this.query = query;
	}

	public LookupException(Object query, String message)
	{
		super(generateMessage(query, message));

		this.query = query;
	}

	public LookupException(Object query, Throwable cause)
	{
		super(generateMessage(query, null), cause);
		this.query = query;
	}

	public LookupException(Object query, String message, Throwable cause)
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
		sb.append("Lookup with query ");
		sb.append(query);
		sb.append(" failed");
		if (additionalMsg != null)
		{
			sb.append(": ");
			sb.append(additionalMsg);
		}
		return sb.toString();
	}
}
