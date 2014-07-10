package de.subcentral.core.model.media;

public interface Numbered
{
	/**
	 * Some medias can be numbered (like Episodes in a Series or Season or Songs in an Album). This value indicates that the media instance is not
	 * numbered.
	 */
	public static final int	UNNUMBERED	= Integer.MAX_VALUE;

	public int getNumber();

	public default boolean isNumbered()
	{
		return getNumber() != UNNUMBERED;
	}
}
