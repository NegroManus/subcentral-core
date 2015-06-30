package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.util.List;

import de.subcentral.core.metadata.media.Series;

public interface TheTvDbApi
{
    /**
     * Value is of type Integer.
     */
    public static final String ATTRIBUTE_THETVDB_ID = "THETVDB_ID";
    /**
     * Value is of type String.
     */
    public static final String ATTRIBUTE_IMDB_ID    = "IMDB_ID";

    public static final String IMAGE_TYPE_BANNER	= "banner";
    public static final String IMAGE_TYPE_FANART	= "fanart";
    public static final String IMAGE_TYPE_POSTER	= "poster";
    public static final String IMAGE_TYPE_EPISODE_IMAGE	= "episode_image";

    public static final String RATING_AGENCY_THETVDB = "thetvdb.com";

    /**
     * 
     * @param name
     *            the series' name
     * @return
     */
    public List<Series> findSeries(String name) throws IOException;

    /**
     * @param apiKey
     *            your API key
     * @param id
     *            the TheTVDB.com id of the series
     * @param language
     *            two-letter language code. Some texts are available in other languages than English (currently: series name, series description,
     *            episode title, episode description)
     * @param full
     *            whether to retrieve the full record or just the base record (without seasons and episodes)
     * @return information about the series denoted by the id
     * @throws IOException
     */
    public SeriesRecord getSeries(String apiKey, int id, String language, boolean full) throws IOException;
}
