package de.subcentral.core.metadata.media;

public interface AvMedia extends Media
{
	/**
	 * The running time of an audio / video media is its playing duration.
	 * 
	 * @return the running time in milliseconds, <code>0</code> if unknown
	 */
	public int getRunningTime();
}
