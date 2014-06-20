package de.subcentral.core.media;

import java.time.temporal.Temporal;

public abstract class AbstractAvMediaItem extends AbstractMedia implements AvMediaItem
{
	protected Temporal	date;
	protected int		runningTime;

	@Override
	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		this.date = date;
	}

	@Override
	public int getRunningTime()
	{
		return runningTime;
	}

	public void setRunningTime(int runningTime)
	{
		this.runningTime = runningTime;
	}
}
