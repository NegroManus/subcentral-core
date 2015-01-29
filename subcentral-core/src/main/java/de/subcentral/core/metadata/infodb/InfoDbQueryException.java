package de.subcentral.core.metadata.infodb;

import org.jsoup.nodes.Document;

public class InfoDbQueryException extends RuntimeException
{
	private static final long	serialVersionUID	= -5423474090679019566L;

	private final InfoDb<?>		infoDb;
	private final Object		query;

	public InfoDbQueryException(InfoDb<?> infoDb, Object query)
	{
		super(generateMessage(infoDb, query, null));
		this.infoDb = infoDb;
		this.query = query;
	}

	public InfoDbQueryException(InfoDb<?> infoDb, Object query, String message)
	{
		super(generateMessage(infoDb, query, message));
		this.infoDb = infoDb;
		this.query = query;
	}

	public InfoDbQueryException(InfoDb<?> infoDb, Object query, Throwable cause)
	{
		super(generateMessage(infoDb, query, null), cause);
		this.infoDb = infoDb;
		this.query = query;
	}

	public InfoDbQueryException(InfoDb<?> infoDb, Object query, String message, Throwable cause)
	{
		super(generateMessage(infoDb, query, message), cause);
		this.infoDb = infoDb;
		this.query = query;
	}

	public InfoDb<?> getInfoDb()
	{
		return infoDb;
	}

	public Object getQuery()
	{
		return query;
	}

	private static final String generateMessage(InfoDb<?> infoDb, Object query, String additionalMsg)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Querying of information database ");
		sb.append(infoDb);
		sb.append(" failed. Query was: ");
		if (query != null && query.getClass().equals(Document.class))
		{
			// print Document instances differently
			sb.append("<HTML document of ");
			sb.append(((Document) query).baseUri());
			sb.append('>');
		}
		else
		{
			sb.append(query);
		}
		if (additionalMsg != null)
		{
			sb.append(": ");
			sb.append(additionalMsg);
		}
		return sb.toString();
	}
}
