package de.subcentral.core.model.media;


public abstract class AbstractAvMediaItem extends AbstractMedia implements AvMediaItem
{
	protected int	runningTime	= 0;

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
