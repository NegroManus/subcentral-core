package de.subcentral.core.metadata.release;

import java.time.temporal.Temporal;

public class Nuke extends AbstractNuke
{
	public Nuke(String reason)
	{
		this(reason, null);
	}

	public Nuke(String reason, Temporal date)
	{
		super(reason, date);
	}
}
