package de.subcentral.core.model.media;

import java.time.temporal.Temporal;

import de.subcentral.core.model.Models;

public abstract class AbstractAvMediaItem extends AbstractMedia implements AvMediaItem
{
	protected Temporal	date;
	protected int		runningTime	= 0;

	@Override
	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		Models.validateDateClass(date);
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
