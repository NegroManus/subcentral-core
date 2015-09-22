package de.subcentral.core.metadata.media;

import java.util.List;

/**
 * Some medias have explicit names (like {@link Movie} or {@link Series}).
 * 
 * *
 * <p>
 * <b>Difference between title and name</b> <br/>
 * <ul>
 * <li>The <b>title</b> is given to the media by the author. It has not to be unique.</li>
 * <li>The <b>name</b> is unique for the media in its media class. It often contains more context to identify the media. For example the series and season in case of a episode or the artist in case of
 * a song. If there are two or more media that would have the same name, normally the country code or the date is appended to distinguish the names, like in "The Office (UK)" or "Titanic (2002)".</li>
 * </ul>
 * Every media should be named, but not all medias have an explicit name because there is no unique naming scheme. In that case the name can be generated by a {@link #de
 * de.subcentral.core.naming.NamingService}.<br>
 * If the media has an explicit name, the title should only be non-null if it differs from the name. <br/>
 * <br/>
 * Examples:
 * <table>
 * <tr>
 * <th>Media class</th>
 * <th>Title</th>
 * <th>Name</th>
 * </tr>
 * <tr>
 * <td>Song</td>
 * <td>Never Gonna Give You Up</td>
 * <td>Rick Astley - Never Gonna Give You Up</td>
 * </tr>
 * <tr>
 * <td>Movie</td>
 * <td>Gladiator</td>
 * <td>Gladiator (2002)</td>
 * </tr>
 * <tr>
 * <td>Movie</td>
 * <td>null</td>
 * <td>Transformers</td>
 * </tr>
 * <tr>
 * <td>Series</td>
 * <td>The Office</td>
 * <td>The Office (UK)</td>
 * </tr>
 * <tr>
 * <td>Series</td>
 * <td>null</td>
 * <td>Psych</td>
 * </tr>
 * <tr>
 * <td>Episode</td>
 * <td>Pilot</td>
 * <td>Psych S01E01 Pilot (generated)</td>
 * </tr>
 * </table>
 * </p>
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