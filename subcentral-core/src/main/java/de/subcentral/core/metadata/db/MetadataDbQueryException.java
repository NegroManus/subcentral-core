package de.subcentral.core.metadata.db;

import org.jsoup.nodes.Document;

public class MetadataDbQueryException extends RuntimeException
{
	private static final long serialVersionUID = -5423474090679019566L;

	private final MetadataDb<?>	metadataDb;
	private final Object		query;

	public MetadataDbQueryException(MetadataDb<?> metadataDb, Object query)
	{
		super(generateMessage(metadataDb, query, null));
		this.metadataDb = metadataDb;
		this.query = query;
	}

	public MetadataDbQueryException(MetadataDb<?> metadataDb, Object query, String message)
	{
		super(generateMessage(metadataDb, query, message));
		this.metadataDb = metadataDb;
		this.query = query;
	}

	public MetadataDbQueryException(MetadataDb<?> metadataDb, Object query, Throwable cause)
	{
		super(generateMessage(metadataDb, query, null), cause);
		this.metadataDb = metadataDb;
		this.query = query;
	}

	public MetadataDbQueryException(MetadataDb<?> metadataDb, Object query, String message, Throwable cause)
	{
		super(generateMessage(metadataDb, query, message), cause);
		this.metadataDb = metadataDb;
		this.query = query;
	}

	public MetadataDb<?> getMetadataDb()
	{
		return metadataDb;
	}

	public Object getQuery()
	{
		return query;
	}

	private static final String generateMessage(MetadataDb<?> metadataDb, Object query, String additionalMsg)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Querying of metadata database ");
		sb.append(metadataDb);
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
