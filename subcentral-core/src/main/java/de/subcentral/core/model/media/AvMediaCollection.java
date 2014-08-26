package de.subcentral.core.model.media;

public interface AvMediaCollection<M extends AvMedia> extends AvMedia, MediaCollection<M>
{
	@Override
	public default int getRunningTime()
	{
		int runningTime = 0;
		for (AvMedia e : getMediaItems())
		{
			runningTime += e.getRunningTime();
		}
		return runningTime;
	}
}
