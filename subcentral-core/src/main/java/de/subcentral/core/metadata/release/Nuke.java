package de.subcentral.core.metadata.release;

import java.time.temporal.Temporal;

public class Nuke extends NukeBase
{
	private static final long serialVersionUID = 6761622798674703223L;

	public Nuke(String reason)
	{
		this(reason, null);
	}

	public Nuke(String reason, Temporal date)
	{
		super(reason, date);
	}
}
