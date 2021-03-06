package de.subcentral.core.metadata.media;

import java.time.Year;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.Metadata;
import de.subcentral.core.metadata.Site;
import de.subcentral.core.util.TimeUtil;

/**
 * 
 * 
 * @implSpec All implementations should implement {@link Comparable}.
 *
 */
public interface Media extends Metadata {
    public static final String MEDIA_TYPE_EPISODE                 = "episode";
    public static final String MEDIA_TYPE_SERIES                  = "series";
    public static final String MEDIA_TYPE_SEASON                  = "season";
    public static final String MEDIA_TYPE_MOVIE                   = "movie";
    public static final String MEDIA_TYPE_GAME                    = "game";
    public static final String MEDIA_TYPE_SOFTWARE                = "software";
    public static final String MEDIA_TYPE_DOCUMENTATION           = "documentation";
    public static final String MEDIA_TYPE_SHOW                    = "show";
    public static final String MEDIA_TYPE_CONCERT                 = "concert";
    public static final String MEDIA_TYPE_SONG                    = "song";
    public static final String MEDIA_TYPE_ALBUM                   = "album";
    public static final String MEDIA_TYPE_IMAGE_SET               = "image_set";
    public static final String MEDIA_TYPE_MUSIC_VIDEO             = "music_video";
    public static final String MEDIA_TYPE_IMAGE                   = "image";
    public static final String MEDIA_TYPE_EBOOK                   = "ebook";
    public static final String MEDIA_TYPE_AUDIOBOOK               = "audiobook";

    public static final String MEDIA_CONTENT_TYPE_TEXT            = "text";
    public static final String MEDIA_CONTENT_TYPE_IMAGE           = "image";
    public static final String MEDIA_CONTENT_TYPE_AUDIO           = "audio";
    public static final String MEDIA_CONTENT_TYPE_VIDEO           = "video";
    public static final String MEDIA_CONTENT_TYPE_APPLICATION     = "application";

    public static final String MEDIA_IMAGE_TYPE_WALLPAPER         = "wallpaper";
    public static final String MEDIA_IMAGE_TYPE_POSTER_VERTICAL   = "poster_vertical";
    public static final String MEDIA_IMAGE_TYPE_POSTER_HORIZONTAL = "poster_horizontal";
    public static final String MEDIA_IMAGE_TYPE_LOGO              = "logo";

    // Property accessors
    /**
     * Normally a media only has a {@link #getSiteId() name} which is in fact its title with some context. The title is only set if it differs from the name (like name: "The Office (UK)", title: "The
     * Office").<br>
     * In case of numbered media, like an {@link Episode} or a Song, the title may be optional to identify the media.
     * 
     * @return the title of this media
     */
    public String getTitle();

    /**
     * The media type is useful to distinguish media instances that have no own class but are different. One of the <code>TYPE_*</code> constants.
     * 
     * @return the exact type of the media
     */
    public String getMediaType();

    /**
     * 
     * @return the content type of this media. One of the <code>CONTENT_TYPE_*</code> constants
     */
    public String getMediaContentType();

    /**
     * The date can be an instance of {@link java.time.ZonedDateTime}, {@link java.time.LocalDateTime}, {@link java.time.LocalDate}, {@link java.time.YearMonth} or {@link java.time.Year}, depending on
     * how precise the information is. Other <code>Temporal</code> implementations are not allowed.
     * 
     * @return the publishing / air date of this media
     */
    public Temporal getDate();

    /**
     * 
     * @return the language codes of the original languages of this media. Typically, not available on medias of {@link Media#MEDIA_CONTENT_TYPE_IMAGE}, except if there is text in the picture
     */
    public List<String> getLanguages();

    /**
     * 
     * @return the country codes of the countries where this media was originally created. Can be multiple if it was a co-production
     */
    public List<String> getCountries();

    /**
     * @return the genres of this media
     */
    public Set<String> getGenres();

    /**
     * The running time of an audio / video media is its playing duration. Only for medias of type {@link #MEDIA_CONTENT_TYPE_AUDIO} or {@link #MEDIA_CONTENT_TYPE_VIDEO}.
     * 
     * @return the running time in milliseconds, <code>0</code> if none or unknown
     */
    public int getRunningTime();

    /**
     * The description can for example be facts about this media or a plot summary (in case of Episodes or Movies).
     * 
     * @return The description of this media.
     */
    public String getDescription();

    /**
     * Allows to store different ratings (IMDb, filmstarts, ...). The key of an entry is an identifier for the "rating agency", the value is a float value between 0.0 and 10.0.
     * 
     * @return the ratings
     */
    public Map<Site, Float> getRatings();

    /**
     * See: http://en.wikipedia.org/wiki/Content_rating.
     * 
     * @return the parental advisory (music industry) / content rating for this media
     */
    public String getContentRating();

    /**
     * The key of the map is the image type ({@link #MEDIA_IMAGE_TYPE_WALLPAPER}, {@link #MEDIA_IMAGE_TYPE_POSTER_VERTICAL},, {@link #MEDIA_IMAGE_TYPE_POSTER_HORIZONTAL} {@link #MEDIA_IMAGE_TYPE_LOGO}
     * , some custom image type or {@code null} if unknown), the values are resource paths (URLs) to the images.
     * 
     * @return all images associated with this media
     */
    public ListMultimap<String, String> getImages();

    /**
     * 
     * @return a set of links (typically URIs) where further information about this media can be found
     */
    public List<String> getFurtherInfoLinks();

    // Convenience
    /**
     * 
     * @return {@code true} if this media has a title, {@code false} otherwise
     */
    public default boolean isTitled() {
        return getTitle() != null;
    }

    /**
     * 
     * @return {@code true} if this media has a date, {@code false} otherwise
     */
    public default boolean isDated() {
        return getDate() != null;
    }

    /**
     * 
     * @return the year of the {@link #getDate() date} or <code>null</code> if the date is <code>null</code> or the year cannot be retrieved
     */
    public default Year getYear() {
        return TimeUtil.getYear(getDate());
    }

    /**
     * 
     * @return the primary language or <code>null</code> if no languages
     */
    public default String getPrimaryOriginalLanguage() {
        return !getLanguages().isEmpty() ? getLanguages().get(0) : null;
    }

    /**
     * 
     * @return the primary country or <code>null</code> if no countries
     */
    public default String getPrimaryCountryOfOrigin() {
        return !getCountries().isEmpty() ? getCountries().get(0) : null;
    }
}
