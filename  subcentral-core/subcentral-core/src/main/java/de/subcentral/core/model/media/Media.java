package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;

import de.subcentral.core.model.Work;

/**
 * <b>Difference between title and name</b> <br/>
 * <ul>
 * <li>The <b>title</b> is given to the media by the author. It has not to be unique.</li>
 * <li>The <b>name</b> is unique for the media in its media class. It often contains more context to identify the media. For example the series and
 * season in case of a episode or the artist in case of a song. If there are two or more media that would have the same name, normally the country
 * code or the date is appended to distinguish the names, like in "The Office (UK)" or "Titanic (2002)".</li>
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
	public static final String	PROP_NAME_NAME						= "name";
	public static final String	PROP_NAME_TITLE						= "title";
	public static final String	PROP_NAME_MEDIA_TYPE				= "mediaType";
	public static final String	PROP_NAME_DATE						= "date";
	public static final String	PROP_NAME_ORIGINAL_LANGUAGE			= "originalLanguage";
	public static final String	PROP_NAME_COUNTRIES_OF_ORIGIN		= "countriesOfOrigin";
	public static final String	PROP_NAME_GENRES					= "genres";
	public static final String	PROP_NAME_DESCRIPTION				= "description";
	public static final String	PROP_NAME_COVER_URL					= "coverUrl";
	public static final String	PROP_NAME_CONTENT_ADVISORY			= "contentAdvisory";
	public static final String	PROP_NAME_FURHTER_INFORMATION_URLS	= "furtherInformationUrls";

	public static final String	TYPE_TEXT							= "TEXT";
	public static final String	TYPE_IMAGE							= "IMAGE";
	public static final String	TYPE_AUDIO							= "AUDIO";
	public static final String	TYPE_VIDEO							= "VIDEO";
	public static final String	TYPE_APPLICATION					= "APPLICATION";
	public static final String	TYPE_MULTI							= "MULTI";
	public static final String	TYPE_COLLECTION						= "COLLECTION";

	// Property accessors
	/**
	 * @return The unique name of this media.
	 */
	public String getName();

	/**
	 * Normally a media only has a {@link #getName() name} which is in fact its title. The title is only set if it differs from the name (like name:
	 * "The Office (UK)", title: "The Office").<br/>
	 * In case of numbered Media, like an {@link Episode} or a Song, the title may be optional.
	 * 
	 * @return The title of this media.
	 */
	public String getTitle();

	/**
	 * 
	 * @return The type of this media. One of the <code>TYPE_*</code> constants.
	 */
	public String getMediaType();

	/**
	 * The date can be an instance of {@link java.time.ZonedDateTime}, {@link java.time.LocalDateTime}, {@link java.time.LocalDate} or
	 * {@link java.time.Year}, depending on how precise the information is.
	 * 
	 * @return The publishing / air date of this media.
	 */
	public Temporal getDate();

	/**
	 * 
	 * @return The language code of the original language of this media. Typically, not available on medias of {@link Media#TYPE_IMAGE}.
	 */
	public String getOriginalLanguage();

	/**
	 * 
	 * @return The country codes of the countries where this media was originally created.
	 */
	public Set<String> getCountriesOfOrigin();

	/**
	 * @return The genres of this media.
	 */
	public Set<String> getGenres();

	/**
	 * The description can for example be facts about this media or a plot summary (in case of Episodes or Movies).
	 * 
	 * @return The description of this media.
	 */
	public String getDescription();

	/**
	 * 
	 * @return The URL pointing to a cover image for this media.
	 */
	public String getCoverUrl();

	/**
	 * <ul>
	 * <li>Audio: http://en.wikipedia.org/wiki/Parental_Advisory</li>
	 * <li>Video: http://en.wikipedia.org/wiki/Motion_picture_rating_system</li>
	 * <li>Games: http://en.wikipedia.org/wiki/Video_game_content_rating_system</li>
	 * </ul>
	 * 
	 * @return The parental advisory / content advisory / content rating for this media.
	 */
	public String getContentAdvisory();

	/**
	 * 
	 * @return A set of URLs where further information about this media can be found.
	 */
	public List<String> getFurtherInformationUrls();

	// public ListMultimap<String, String> getAttributes();

	// Convenience
	/**
	 * 
	 * @return Whether this media has a {@link #getTitle() title} or not.
	 */
	public boolean isTitled();
}