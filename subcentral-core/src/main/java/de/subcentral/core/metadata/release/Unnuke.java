package de.subcentral.core.metadata.release;

import java.time.temporal.Temporal;

public class Unnuke extends AbstractNuke
{
	public Unnuke(String reason)
	{
		this(reason, null);
	}

	public Unnuke(String reason, Temporal date)
	{
		super(reason, date);
	}
}
