package de.subcentral.core.media;

public abstract class AbstractAvMedia extends AbstractMedia implements AvMedia
{
	protected int	runningTime;

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
