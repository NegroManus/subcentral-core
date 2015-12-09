package de.subcentral.core.metadata.release;

import java.time.temporal.Temporal;

public class Unnuke extends NukeBase
{
	private static final long serialVersionUID = -2346945547233329896L;

	public Unnuke(String reason)
	{
		this(reason, null);
	}

	public Unnuke(String reason, Temporal date)
	{
		super(reason, date);
	}
}
