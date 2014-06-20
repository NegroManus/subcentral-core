package de.subcentral.core.media;

public interface AvMediaCollection<M extends AvMediaItem> extends MediaCollection<M>, AvMedia
{
	@Override
	public default int getRunningTime()
	{
		int runningTime = 0;
		for (AvMediaItem e : getMediaItems())
		{
			runningTime += e.getRunningTime();
		}
		return runningTime;
	}
}
