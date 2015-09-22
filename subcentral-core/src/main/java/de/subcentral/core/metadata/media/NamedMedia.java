package de.subcentral.core.metadata.media;

import java.util.List;

/**
 * Some medias have explicit names (like {@link Movie} or {@link Series}).
 *
 */
public interface NamedMedia extends Media
{
	/**
	 * The unique, primary name of this media.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Alias names for this media. For example, "House of Cards (US)"'s alias name is "House of Cards (2013)".
	 * 
	 * @return the alias names
	 */
	public List<String> getAliasNames();

	// Convenience
	/**
	 * For NamedMediaBase the title is the name without the extension to identify the media (e.g. without the year "(2014)" or country code "(UK)"). If the title equals the name, no title is
	 * specified.
	 * <p>
	 * Examples
	 * <ul>
	 * <li>name: "The Office (UK)" -> title: "The Office"</li>
	 * <li>name: "Titanic (2002)" -> title: "Titanic".</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the {@link #getTitle() title} if this media {@link #isTitled() is titled}, else the {@link #getName() name}
	 */
	public default String getTitleOrName()
	{
		return isTitled() ? getTitle() : getName();
	}
}