package de.subcentral.core.model.media;

public interface AvMedia extends Media
{
	public static final String	PROP_NAME_RUNNING_TIME	= "runningTime";

	/**
	 * The running time of an audio / video media is its playing duration.
	 * 
	 * @return The running time in milliseconds. <code>0</code> if unknown.
	 */
	public int getRunningTime();
}
