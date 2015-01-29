package de.subcentral.core.metadata.infodb;

import java.io.IOException;

public class InfoDbUnavailableException extends IOException
{
	private static final long	serialVersionUID	= 3669402029190061652L;

	private final InfoDb<?>		infoDb;

	public InfoDbUnavailableException(InfoDb<?> infoDb)
	{
		super(generateMessage(infoDb, null));
		this.infoDb = infoDb;
	}

	public InfoDbUnavailableException(InfoDb<?> infoDb, String message)
	{
		super(generateMessage(infoDb, message));
		this.infoDb = infoDb;
	}

	public InfoDbUnavailableException(InfoDb<?> infoDb, Throwable cause)
	{
		super(generateMessage(infoDb, null), cause);
		this.infoDb = infoDb;
	}

	public InfoDbUnavailableException(InfoDb<?> infoDb, String message, Throwable cause)
	{
		super(generateMessage(infoDb, message), cause);
		this.infoDb = infoDb;
	}

	public InfoDb<?> getInfoDb()
	{
		return infoDb;
	}

	private static final String generateMessage(InfoDb<?> infoDb, String additionalMsg)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Information database ");
		sb.append(infoDb);
		sb.append(" is not available ");
		if (additionalMsg != null)
		{
			sb.append(": ");
			sb.append(additionalMsg);
		}
		return sb.toString();
	}

}
