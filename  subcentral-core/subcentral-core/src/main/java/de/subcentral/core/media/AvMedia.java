package de.subcentral.core.media;

public interface AvMedia extends Media
{
	/**
	 * The running time of an audio / video media is its playing duration.
	 * 
	 * @return The running time in milliseconds. 0 if unknown.
	 */
	public int getRunningTime();
}
