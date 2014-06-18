package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.Set;

import de.subcentral.core.contribution.Work;

/**
 * <b>Difference between title and name</b> <br/>
 * <ul>
 * <li>The <b>title</b> is given to the media by the author. It has not to be unique.</li>
 * <li>The <b>name</b> is unique for the media in its media class. It often contains more context to identify the media. For example the series and
 * season in case of a episode or the artist in case of a song. If there are two or more media that would have the same name, normally the country of
 * origin or the date is appended to distinguish the names, like in "The Office (UK)" or "Titanic (2002)".</li>
 * </ul>
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
 * <td>Series</td>
 * <td>The Office</td>
 * <td>The Office (UK)</td>
 * </tr>
 * <tr>
 * <td>Episode</td>
 * <td>Pilot</td>
 * <td>Psych S01E01 Pilot</td>
 * </tr>
 * </table>
 * 
 * 
 * @implSpec All implementations should implement {@link Comparable}.
 *
 */
public interface Media extends Work
{
	public static final int	UNNUMBERED	= Integer.MAX_VALUE;

	// Property accessors
	/**
	 * @return The unique name of this media.
	 */
	public String getName();

	/**
	 * 
	 * @return The title of this media.
	 */
	public String getTitle();

	public Temporal getDate();

	public Set<String> getGenres();

	public String getOriginalLanguage();

	public Set<String> getCountriesOfOrigin();

	public String getDescription();

	public String getCoverUrl();

	/**
	 * <ul>
	 * <li>Audio: http://en.wikipedia.org/wiki/Parental_Advisory</li>
	 * <li>Video: http://en.wikipedia.org/wiki/Motion_picture_rating_system</li>
	 * <li>Games: http://en.wikipedia.org/wiki/Video_game_content_rating_system</li>
	 * </ul>
	 * 
	 * @return the content advisory / content rating for this media
	 */
	public String getContentAdvisory();

	public Set<String> getFurtherInformationUrls();
}
