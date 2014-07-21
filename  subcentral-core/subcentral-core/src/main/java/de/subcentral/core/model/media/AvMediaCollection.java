package de.subcentral.core.model.media;

public interface AvMediaCollection<M extends AvMediaItem> extends AvMedia, MediaCollection<M>
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
