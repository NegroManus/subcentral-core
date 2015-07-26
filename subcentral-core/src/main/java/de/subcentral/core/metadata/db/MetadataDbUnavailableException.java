package de.subcentral.core.metadata.db;

import java.io.IOException;

public class MetadataDbUnavailableException extends IOException
{
	private static final long serialVersionUID = 3669402029190061652L;

	private final MetadataDb<?> metadataDb;

	public MetadataDbUnavailableException(MetadataDb<?> metadataDb)
	{
		super(generateMessage(metadataDb, null));
		this.metadataDb = metadataDb;
	}

	public MetadataDbUnavailableException(MetadataDb<?> metadataDb, String message)
	{
		super(generateMessage(metadataDb, message));
		this.metadataDb = metadataDb;
	}

	public MetadataDbUnavailableException(MetadataDb<?> metadataDb, Throwable cause)
	{
		super(generateMessage(metadataDb, null), cause);
		this.metadataDb = metadataDb;
	}

	public MetadataDbUnavailableException(MetadataDb<?> metadataDb, String message, Throwable cause)
	{
		super(generateMessage(metadataDb, message), cause);
		this.metadataDb = metadataDb;
	}

	public MetadataDb<?> getMetadataDb()
	{
		return metadataDb;
	}

	private static final String generateMessage(MetadataDb<?> metadataDb, String additionalMsg)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Metadata database ");
		sb.append(metadataDb);
		sb.append(" is not available ");
		if (additionalMsg != null)
		{
			sb.append(": ");
			sb.append(additionalMsg);
		}
		return sb.toString();
	}

}
