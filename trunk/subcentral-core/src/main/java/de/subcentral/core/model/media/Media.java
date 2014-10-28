package de.subcentral.core.model.media;

import java.time.Year;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.Work;
import de.subcentral.core.util.TimeUtil;

/**
 * <p>
 * <b>Difference between title and name</b> <br/>
 * <ul>
 * <li>The <b>title</b> is given to the media by the author. It has not to be unique.</li>
 * <li>The <b>name</b> is unique for the media in its media class. It often contains more context to identify the media. For example the series and
 * season in case of a episode or the artist in case of a song. If there are two or more media that would have the same name, normally the country
 * code or the date is appended to distinguish the names, like in "The Office (UK)" or "Titanic (2002)".</li>
 * </ul>
 * The name of a media should never be null. The title should only be non-null if it differs from the name. <br/>
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
 * <td>Psych S01E01 Pilot</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @implSpec All (non-standard) implementations should implement {@link Comparable}.
 *
 */
public interface Media extends Work
{
	public static final String	MEDIA_TYPE_EPISODE				= "EPISODE";
	public static final String	MEDIA_TYPE_SERIES				= "SERIES";
	public static final String	MEDIA_TYPE_SEASON				= "SEASON";
	public static final String	MEDIA_TYPE_MOVIE				= "MOVIE";
	public static final String	MEDIA_TYPE_GAME					= "GAME";
	public static final String	MEDIA_TYPE_SOFTWARE				= "SOFTWARE";
	public static final String	MEDIA_TYPE_DOCUMENTATION		= "DOCUMENTATION";
	public static final String	MEDIA_TYPE_SHOW					= "SHOW";
	public static final String	MEDIA_TYPE_CONCERT				= "CONCERT";
	public static final String	MEDIA_TYPE_SONG					= "SONG";
	public static final String	MEDIA_TYPE_ALBUM				= "ALBUM";
	public static final String	MEDIA_TYPE_IMG_SET				= "IMG_SET";
	public static final String	MEDIA_TYPE_MUSIC_VIDEO			= "MUSIC_VIDEO";
	public static final String	MEDIA_TYPE_IMAGE				= "IMAGE";
	public static final String	MEDIA_TYPE_EBOOK				= "EBOOK";
	public static final String	MEDIA_TYPE_AUDIOBOOK			= "AUDIOBOOK";

	public static final String	MEDIA_CONTENT_TYPE_TEXT			= "TEXT";
	public static final String	MEDIA_CONTENT_TYPE_IMAGE		= "IMAGE";
	public static final String	MEDIA_CONTENT_TYPE_AUDIO		= "AUDIO";
	public static final String	MEDIA_CONTENT_TYPE_VIDEO		= "VIDEO";
	public static final String	MEDIA_CONTENT_TYPE_APPLICATION	= "APPLICATION";
	public static final String	MEDIA_CONTENT_TYPE_MULTI		= "MULTI";
	public static final String	MEDIA_CONTENT_TYPE_COLLECTION	= "COLLECTION";

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
	 * The media type is useful to distinguish media instances that have no own class but are different. One of the <code>TYPE_*</code> constants.
	 * 
	 * @return The exact type of the media.
	 */
	public String getMediaType();

	/**
	 * 
	 * @return The content type of this media. One of the <code>CONTENT_TYPE_*</code> constants.
	 */
	public String getMediaContentType();

	/**
	 * The date can be an instance of {@link java.time.ZonedDateTime}, {@link java.time.LocalDateTime}, {@link java.time.LocalDate},
	 * {@link java.time.YearMonth} or {@link java.time.Year}, depending on how precise the information is. Other <code>Temporal</code> implementations
	 * are not allowed.
	 * 
	 * @return The publishing / air date of this media.
	 */
	public Temporal getDate();

	/**
	 * 
	 * @return The language codes of the original languages of this media. Typically, not available on medias of {@link Media#CONTENT_TYPE_IMAGE},
	 *         except if there is text in the picture.
	 */
	public List<String> getOriginalLanguages();

	/**
	 * 
	 * @return The country codes of the countries where this media was originally created. Can be multiple if it was a co-production.
	 */
	public List<String> getCountriesOfOrigin();

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
	 * @return A list of URLs each pointing to a cover image for this media. The first URL points to the primary cover.
	 */
	public List<String> getCoverUrls();

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
	public Set<String> getFurtherInfoUrls();

	public ListMultimap<String, Object> getAttributes();

	// Convenience
	/**
	 * 
	 * @return {@code true} if this media has a title, {@code false} otherwise
	 */
	public default boolean isTitled()
	{
		return getTitle() != null;
	}

	/**
	 * 
	 * @return the {@link #getTitle() title} if this media {@link #isTitled() is titled}, else the {@link #getName() name}
	 */
	public default String getTitleOrName()
	{
		return isTitled() ? getTitle() : getName();
	}

	/**
	 * 
	 * @return the year of the {@link #getDate() date} or <code>null</code> if the date is <code>null</code> or the year cannot be retrieved
	 */
	public default Year getYear()
	{
		return TimeUtil.getYear(getDate());
	}

	/**
	 * 
	 * @return the primary language or <code>null</code> if no languages
	 */
	public default String getPrimaryOriginalLanguage()
	{
		return !getOriginalLanguages().isEmpty() ? getOriginalLanguages().get(0) : null;
	}

	/**
	 * 
	 * @return the primary country or <code>null</code> if no countries
	 */
	public default String getPrimaryCountryOfOrigin()
	{
		return !getCountriesOfOrigin().isEmpty() ? getCountriesOfOrigin().get(0) : null;
	}
}
