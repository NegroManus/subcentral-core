package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.db.AbstractHtmlHttpMetadataDb;
import de.subcentral.core.metadata.db.MetadataDbQueryException;
import de.subcentral.core.metadata.db.MetadataDbUnavailableException;
import de.subcentral.core.metadata.media.AbstractMedia;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.naming.EpisodeNamer;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.SeasonNamer;
import de.subcentral.core.util.TemporalComparator;

public class TheTvDbMediaDb extends AbstractHtmlHttpMetadataDb<Media>
{
    public static final String DOMAIN		    = "thetvdb.com";
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

    private static final Logger	  log		= LogManager.getLogger(TheTvDbMediaDb.class);
    private static final String	  BASE_PATH	= "http://thetvdb.com/";
    private static final String	  API_PATH	= BASE_PATH + "api/";
    private static final String	  IMG_PATH	= BASE_PATH + "banners/";
    private static final Splitter LIST_SPLITTER	= Splitter.on('|').trimResults().omitEmptyStrings();

    private String apiKey;

    public String getApiKey()
    {
	return apiKey;
    }

    public void setApiKey(String apiKey)
    {
	this.apiKey = apiKey;
    }

    @Override
    protected URL initHost() throws MalformedURLException
    {
	return new URL(BASE_PATH);
    }

    @Override
    public String getDomain()
    {
	return DOMAIN;
    }

    @Override
    public String getName()
    {
	return "TheTVDB";
    }

    @Override
    public Class<Media> getResultType()
    {
	return Media.class;
    }

    @Override
    public List<Media> queryDocument(Document doc) throws MetadataDbUnavailableException, MetadataDbQueryException
    {
	/**
	 * <pre>
	 * <mirrorpath>/api/GetSeries.php?seriesname=<seriesname>
	 * <mirrorpath>/api/GetSeries.php?seriesname=<seriesname>&language=<language>
	 * </pre>
	 * 
	 * Results
	 * 
	 * <pre>
	 * <Series>
	 * <seriesid>262980</seriesid>
	 * <language>en</language>
	 * <SeriesName>House of Cards (US)</SeriesName>
	 * <AliasNames>House of Cards (2013)</AliasNames>
	 * <banner>graphical/262980-g7.jpg</banner>
	 * <Overview>
	 * Ruthless and cunning, Congressman Francis Underwood and his wife Claire stop at nothing to conquer everything. This wicked political drama penetrates the shadowy world of greed, sex and corruption in modern D.C.
	 * </Overview>
	 * <FirstAired>2013-02-01</FirstAired>
	 * <Network>Netflix</Network>
	 * <IMDB_ID>tt1856010</IMDB_ID>
	 * <zap2it_id>SH01676454</zap2it_id>
	 * <id>262980</id>
	 * </Series>
	 * </pre>
	 */

	Elements seriesElems = doc.getElementsByTag("series");
	ImmutableList.Builder<Media> seriesList = ImmutableList.builder();
	for (Element seriesElem : seriesElems)
	{
	    Series series = new Series();

	    addTvDbId(series, seriesElem, "seriesid");

	    series.setName(getTextFromChild(seriesElem, "seriesname"));

	    String aliasNamesTxt = getTextFromChild(seriesElem, "aliasnames");
	    if (aliasNamesTxt != null)
	    {
		List<String> aliasNames = LIST_SPLITTER.splitToList(aliasNamesTxt);
		series.setAliasNames(aliasNames);
	    }

	    addImage(series, seriesElem, "banner", IMAGE_TYPE_BANNER);

	    series.setDescription(getTextFromChild(seriesElem, "overview"));

	    addDateAsLocaleDate(series, seriesElem, "firstaired");

	    addNetwork(series, seriesElem, "network");

	    addImdbId(series, seriesElem, "imdb_id");

	    seriesList.add(series);
	}
	return seriesList.build();
    }

    @Override
    protected URL buildQueryUrl(String query) throws Exception
    {
	return buildUrl("/api/GetSeries.php", formatQuery("seriesname=", query));
    }

    @Override
    public <E> E get(String id, Class<E> type) throws MetadataDbUnavailableException, MetadataDbQueryException
    {
	if (SeriesRecord.class.equals(type))
	{
	    try
	    {
		return type.cast(getSeries(Integer.parseInt(id), "en", true));
	    }
	    catch (NumberFormatException | IOException e)
	    {
		throw new MetadataDbQueryException(this, id, e);
	    }
	}
	throw new IllegalArgumentException("Unknown type " + type.getName() + ". Known are [" + SeriesRecord.class.getName() + "]");
    }

    public SeriesRecord getSeries(int id, String language, boolean full) throws IOException
    {
	StringBuilder urlSpec = new StringBuilder();
	urlSpec.append(getApiPathWithKey(apiKey));
	urlSpec.append("series/");
	urlSpec.append(id);
	urlSpec.append('/');
	if (full)
	{
	    urlSpec.append("all/");
	    // TODO use zip instead of xml for full record
	}
	urlSpec.append(language);
	urlSpec.append(".xml");
	URL url = new URL(urlSpec.toString());
	log.debug("Requesting {}", url);
	Document doc = Jsoup.parse(url, 5000);

	Series series = new Series();
	List<Season> seasons = new ArrayList<>();
	List<Episode> episodes = new ArrayList<>();
	parseBaseSeriesRecord(doc, series);
	parseBaseEpisodeRecords(doc, series, seasons, episodes);

	return new SeriesRecord(series, seasons, episodes);
    }

    /**
     * Base Series Record example:
     * 
     * <pre>
     * {@literal
     *   <Series>
     *     <id>79335</id>
     *     <Actors>|James Roday|Dulé Hill|Maggie Lawson|Kirsten Nelson|Corbin Bernsen|Timothy Omundson|</Actors>
     *     <Airs_DayOfWeek></Airs_DayOfWeek>
     *     <Airs_Time></Airs_Time>
     *     <ContentRating>TV-PG</ContentRating>
     *     <FirstAired>2006-07-07</FirstAired>
     *     <Genre>|Comedy|Crime|Drama|</Genre>
     *     <IMDB_ID>tt0491738</IMDB_ID>
     *     <Language>de</Language>
     *     <Network>USA Network</Network>
     *     <NetworkID></NetworkID>
     *     <Overview>Shawn Spencer ist selbsternannter Detektiv. Von seinem Vater Henry, einem angesehenen Polizisten, wurde er trainiert, sich alle Dinge in seinem Umfeld genau einzuprägen, seien sie auch noch so klein oder unwichtig. Über seine Erziehung unzufrieden kehrte Shawn seinem Vater jedoch den Rücken. Nach einigen misslungenen Lebensabschnitten erkennt er seine Gabe, ungelöste Fälle der Polizei mithilfe seines fotografischen Gedächtnisses lösen zu können. Dabei gibt Shawn aber stets vor ein Hellseher zu sein. Nachdem er der Polizei in mehreren Fällen helfen konnte und diese ihn immer wieder als Unterstützung anfordert, gründet Shawn schließlich mit seinem Freund Burton Guster eine eigene Detektei.</Overview>
     *     <Rating>8.8</Rating>
     *     <RatingCount>181</RatingCount>
     *     <Runtime>60</Runtime>
     *     <SeriesID>59369</SeriesID>
     *     <SeriesName>Psych</SeriesName>
     *     <Status>Ended</Status>
     *     <added></added>
     *     <addedBy></addedBy>
     *     <banner>graphical/79335-g8.jpg</banner>
     *     <fanart>fanart/original/79335-17.jpg</fanart>
     *     <lastupdated>1432759112</lastupdated>
     *     <poster>posters/79335-8.jpg</poster>
     *     <tms_wanted_old>1</tms_wanted_old>
     *     <zap2it_id>EP00837834</zap2it_id>
     *   </Series>
     *  }
     * </pre>
     */
    private static void parseBaseSeriesRecord(Document doc, Series series)
    {
	Element seriesElem = doc.getElementsByTag("series").first();

	addTvDbId(series, seriesElem, "id");

	series.setContentRating(getTextFromChild(seriesElem, "contentrating"));

	addDateAsLocaleDate(series, seriesElem, "firstaired");

	String genresTxt = getTextFromChild(seriesElem, "genre");
	List<String> genres = LIST_SPLITTER.splitToList(genresTxt);
	series.setGenres(genres);

	addImdbId(series, seriesElem, "imdb_id");

	addNetwork(series, seriesElem, "network");

	series.setDescription(getTextFromChild(seriesElem, "overview"));

	addTheTvDbRating(series, seriesElem, "rating");

	String runtimeTxt = getTextFromChild(seriesElem, "runtime");
	int runtime = Integer.parseInt(runtimeTxt);
	series.setRegularRunningTime(runtime);

	series.setName(getTextFromChild(seriesElem, "seriesname"));

	addImage(series, seriesElem, "banner", IMAGE_TYPE_BANNER);
	addImage(series, seriesElem, "fanart", IMAGE_TYPE_FANART);
	addImage(series, seriesElem, "poster", IMAGE_TYPE_POSTER);
    }

    /**
     * Base Episode Record of a regular episode:
     * 
     * <pre>
     * {@literal
     * <Episode>
     * <id>4649178</id>
     * <Combined_episodenumber>10</Combined_episodenumber>
     * <Combined_season>8</Combined_season>
     * <DVD_chapter/>
     * <DVD_discid/>
     * <DVD_episodenumber/>
     * <DVD_season/>
     * <Director>Steve Franks</Director>
     * <EpImgFlag>2</EpImgFlag>
     * <EpisodeName>The Break Up</EpisodeName>
     * <EpisodeNumber>10</EpisodeNumber>
     * <FirstAired>2014-03-26</FirstAired>
     * <GuestStars/>
     * <IMDB_ID/>
     * <Language>en</Language>
     * <Overview>
     * Shawn, Gus and Juliet handle one last case in the series finale.
     * </Overview>
     * <ProductionCode/>
     * <Rating>8.6</Rating>
     * <RatingCount>17</RatingCount>
     * <SeasonNumber>8</SeasonNumber>
     * <Writer>Steve Franks</Writer>
     * <absolute_number>121</absolute_number>
     * <filename>episodes/79335/4649178.jpg</filename>
     * <lastupdated>1426781587</lastupdated>
     * <seasonid>523572</seasonid>
     * <seriesid>79335</seriesid>
     * <thumb_added>2014-03-21 08:42:40</thumb_added>
     * <thumb_height>225</thumb_height>
     * <thumb_width>400</thumb_width>
     * </Episode>
     * }
     * </pre>
     * 
     * <br>
     * <br>
     * Base Episode Record of a special episode:
     * 
     * <pre>
     * {@literal
     * <Episode>
     * <id>4840154</id>
     * <Combined_episodenumber>4</Combined_episodenumber>
     * <Combined_season>0</Combined_season>
     * <DVD_chapter/>
     * <DVD_discid/>
     * <DVD_episodenumber/>
     * <DVD_season/>
     * <Director/>
     * <EpImgFlag/>
     * <EpisodeName>Psych After Pshow</EpisodeName>
     * <EpisodeNumber>4</EpisodeNumber>
     * <FirstAired>2014-03-26</FirstAired>
     * <GuestStars/>
     * <IMDB_ID/>
     * <Language>en</Language>
     * <Overview>
     * The hour-long special bids farewell to USA Network's favorite detective duo. Celebrate Psych's 8 seasons with the cast's favorite clips, audience Q&A and never before seen footage.
     * </Overview>
     * <ProductionCode/>
     * <Rating>7.0</Rating>
     * <RatingCount>1</RatingCount>
     * <SeasonNumber>0</SeasonNumber>
     * <Writer/>
     * <absolute_number/>
     * <airsafter_season>8</airsafter_season>
     * <airsbefore_episode/>
     * <airsbefore_season/>
     * <filename/>
     * <lastupdated>1395959484</lastupdated>
     * <seasonid>26775</seasonid>
     * <seriesid>79335</seriesid>
     * <thumb_added/>
     * <thumb_height/>
     * <thumb_width/>
     * </Episode>
     * }
     * </pre>
     */
    private static void parseBaseEpisodeRecords(Document doc, Series series, List<Season> seasonList, List<Episode> episodeList)
    {
	Elements epiElems = doc.getElementsByTag("episode");
	// using a map for the seasons to allow using special map methods
	SortedMap<Season, Season> seasons = new TreeMap<>();
	// a list for all episodes (regular and specials)
	List<Episode> episodes = new ArrayList<>(epiElems.size());
	// a list only for specials, as they are sorted into the complete episode list afterwards
	List<SpecialEpisodeRecord> specials = new ArrayList<>();
	for (Element epiElem : epiElems)
	{
	    Episode epi = series.newEpisode();

	    // first determine whether it's a special episode or a regular
	    // and whether it belongs to a new season
	    int seasonNum = getIntegerFromChild(epiElem, "seasonnumber");
	    // special episodes are in season 0
	    if (seasonNum == 0)
	    {
		epi.setSpecial(true);
		Integer airsBeforeSeason = getIntegerFromChild(epiElem, "airsbefore_season");
		Integer airsAfterSeason = getIntegerFromChild(epiElem, "airsafter_season");
		Integer airsBeforeEpisode = getIntegerFromChild(epiElem, "airsbefore_episode");
		SpecialEpisodeRecord specialEpi = new SpecialEpisodeRecord(epi, airsBeforeSeason, airsAfterSeason, airsBeforeEpisode);
		specials.add(specialEpi);
	    }
	    else
	    {
		Season possiblyNewSeason = series.newSeason(seasonNum);
		Season previousValue = seasons.putIfAbsent(possiblyNewSeason, possiblyNewSeason);
		// if it is in fact a new season
		if (previousValue == null)
		{
		    epi.setSeason(possiblyNewSeason);
		    addTvDbId(possiblyNewSeason, epiElem, "seasonid");
		}
		else
		{
		    epi.setSeason(previousValue);
		}

		epi.setNumberInSeason(getIntegerFromChild(epiElem, "episodenumber"));
		episodes.add(epi);
	    }

	    // add rest of the properties
	    addTvDbId(epi, epiElem, "id");

	    epi.setTitle(getTextFromChild(epiElem, "episodename"));

	    addDateAsLocaleDate(epi, epiElem, "firstaired");

	    addImdbId(epi, epiElem, "imdb_id");

	    epi.setDescription(getTextFromChild(epiElem, "overview"));

	    addTheTvDbRating(epi, epiElem, "rating");

	    addImage(epi, epiElem, "filename", IMAGE_TYPE_EPISODE_IMAGE);

	    // can be null (for specials or info not available)
	    epi.setNumberInSeries(getIntegerFromChild(epiElem, "absolute_number"));
	}

	// sort the episodes in natural order (Series, Season number, Episode number))
	// should not be necessary as episodes are returned in that order but better be safe than sorry
	Collections.sort(episodes);

	// sort the specials by date
	specials.sort((SpecialEpisodeRecord r1, SpecialEpisodeRecord r2) -> TemporalComparator.INSTANCE.compare(r1.episode.getDate(), r2.episode.getDate()));

	insertSpecials(episodes, specials);

	seasonList.addAll(seasons.keySet());
	episodeList.addAll(episodes);
    }

    private static void insertSpecials(List<Episode> episodes, List<SpecialEpisodeRecord> specials)
    {
	if (episodes.isEmpty())
	{
	    // if no regular episodes, just add the specials at the end
	    for (SpecialEpisodeRecord special : specials)
	    {
		episodes.add(special.episode);
	    }
	    return;
	}
	// insert specials at the right position in the episode list
	for (SpecialEpisodeRecord special : specials)
	{
	    boolean added = false;
	    // "airsAfter_season" is just used alone
	    if (special.airsAfterSeason != null)
	    {
		// add it after the last episode of the season
		ListIterator<Episode> iter = episodes.listIterator(episodes.size());
		Episode currentEpi = null;
		while (iter.hasPrevious())
		{
		    currentEpi = iter.previous();
		    // if current epi is part the season to add after, add it after that episode
		    if (currentEpi.isPartOfSeason() && special.airsAfterSeason.equals(currentEpi.getSeason().getNumber()))
		    {
			special.episode.setSeason(currentEpi.getSeason());
			// advance past the last episode of this season and add the episode there
			iter.next();
			iter.add(special.episode);
			added = true;
			break;
		    }
		}
	    }
	    // "airsBefore_season" may be used alone
	    else if (special.airsBeforeSeason != null && special.airsBeforeEpisode == null)
	    {
		// add it right before the first episode of the next season
		ListIterator<Episode> iter = episodes.listIterator();
		Episode currentEpi = null;
		while (iter.hasNext())
		{
		    currentEpi = iter.next();
		    // if current epi is part of the next season, add it before current episode
		    if (currentEpi.isPartOfSeason() && special.airsBeforeSeason.equals(currentEpi.getSeason().getNumber()))
		    {
			special.episode.setSeason(currentEpi.getSeason());
			iter.previous();
			iter.add(special.episode);
			added = true;
			break;
		    }
		}
	    }
	    // "airsBefore_season" should be used in conjunction with "airsBefore_episode"
	    else if (special.airsBeforeSeason != null && special.airsBeforeEpisode != null)
	    {
		// add it before the season
		ListIterator<Episode> iter = episodes.listIterator(episodes.size());
		Episode currentEpi = null;
		while (iter.hasPrevious())
		{
		    currentEpi = iter.previous();
		    // if previous episode was the denoted episode by "airsBefore_season" and "airsBefore_episode"
		    // Note: all regular episodes have a season number and a episode number
		    if (!currentEpi.isSpecial() && special.airsBeforeSeason.equals(currentEpi.getSeason().getNumber()) && special.airsBeforeEpisode.equals(currentEpi.getNumberInSeason()))
		    {
			special.episode.setSeason(currentEpi.getSeason());
			iter.add(special.episode);
			added = true;
			break;
		    }
		}
	    }

	    // if could not find the right position in a season to add, try to add it by date
	    if (!added)
	    {
		if (special.episode.isDated())
		{
		    ListIterator<Episode> iter = episodes.listIterator();
		    Episode currentEpi = null;
		    while (iter.hasNext())
		    {
			currentEpi = iter.next();
			// if special is before
			if (currentEpi.isDated() && TemporalComparator.INSTANCE.compare(special.episode.getDate(), currentEpi.getDate()) < 0)
			{
			    iter.previous();
			    iter.add(special.episode);
			    added = true;
			    break;
			}
		    }
		}
	    }

	    // if every sorting method fails, just add it at the end
	    if (!added)
	    {
		episodes.add(special.episode);
	    }
	}
    }

    private static String getApiPathWithKey(String apiKey)
    {
	return new StringBuilder(API_PATH).append(apiKey).append('/').toString();
    }

    private static String getTextFromChild(Element parentElem, String tag)
    {
	Elements elems = parentElem.getElementsByTag(tag);
	if (elems.isEmpty())
	{
	    return null;
	}
	return StringUtils.trimToNull(elems.first().text());
    }

    private static Integer getIntegerFromChild(Element parentElem, String tag)
    {
	String txt = getTextFromChild(parentElem, tag);
	if (txt == null)
	{
	    return null;
	}
	return Integer.parseInt(txt);
    }

    private static void addTvDbId(Media media, Element parentElem, String tag)
    {
	String idTxt = getTextFromChild(parentElem, tag);
	if (idTxt != null)
	{
	    media.getAttributes().put(ATTRIBUTE_THETVDB_ID, Integer.parseInt(idTxt));
	}
    }

    private static void addImage(Media media, Element parentElem, String tag, String mediaImageType)
    {
	String imgTxt = getTextFromChild(parentElem, tag);
	if (imgTxt != null)
	{
	    String img = IMG_PATH + imgTxt;
	    media.getImages().put(mediaImageType, img);
	}
    }

    private static void addDateAsLocaleDate(AbstractMedia media, Element parentElem, String tag)
    {
	String dateTxt = getTextFromChild(parentElem, tag);
	if (dateTxt != null)
	{
	    LocalDate date = LocalDate.parse(dateTxt);
	    media.setDate(date);
	}
    }

    private static void addNetwork(Series series, Element parentElem, String tag)
    {
	String networkTxt = getTextFromChild(parentElem, tag);
	if (networkTxt != null)
	{
	    Network network = new Network(networkTxt);
	    series.getNetworks().add(network);
	}
    }

    private static void addImdbId(Media media, Element parentElem, String tag)
    {
	String imdbIdTxt = getTextFromChild(parentElem, tag);
	if (imdbIdTxt != null)
	{
	    media.getAttributes().put(ATTRIBUTE_IMDB_ID, imdbIdTxt);
	}
    }

    private static void addTheTvDbRating(Media media, Element parentElem, String tag)
    {
	String ratingTxt = getTextFromChild(parentElem, tag);
	if (ratingTxt != null)
	{
	    float rating = Float.parseFloat(ratingTxt);
	    media.getRatings().put(RATING_AGENCY_THETVDB, rating);
	}
    }

    private static class SpecialEpisodeRecord
    {
	private final Episode episode;
	/**
	 * An unsigned integer indicating the season number this special episode airs before. Should be used in conjunction with airsbefore_episode
	 * for exact placement. This field is only available for special episodes. Can be null.
	 */
	private final Integer airsBeforeSeason;
	/**
	 * An unsigned integer indicating the season number this episode comes after. This field is only available for special episodes. Can be null
	 */
	private final Integer airsAfterSeason;
	/**
	 * An unsigned integer indicating the episode number this special episode airs before. Must be used in conjunction with airsbefore_season, do
	 * not with airsafter_season. This field is only available for special episodes. Can be null.
	 */
	private final Integer airsBeforeEpisode;

	public SpecialEpisodeRecord(Episode episode, Integer airsBeforeSeason, Integer airsAfterSeason, Integer airsBeforeEpisode)
	{
	    this.episode = Objects.requireNonNull(episode, "episode");
	    this.airsBeforeSeason = airsBeforeSeason;
	    this.airsAfterSeason = airsAfterSeason;
	    this.airsBeforeEpisode = airsBeforeEpisode;
	}
    }

    public static void main(String[] args) throws IOException
    {
	TheTvDbMediaDb db = new TheTvDbMediaDb();
	List<Media> queryResult = db.query("psych");
	db.setApiKey("A3ACA9D28A27792D");
	SeriesRecord psych = db.get(queryResult.get(0).getAttributeValue(ATTRIBUTE_THETVDB_ID), SeriesRecord.class);
	System.out.println(psych.getSeries());
	for (Season season : psych.getSeasons())
	{
	    System.out.println(NamingDefaults.getDefaultSeasonNamer().name(season, ImmutableMap.of(SeasonNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE)));
	}
	for (Episode epi : psych.getEpisodes())
	{
	    System.out.println(NamingDefaults.getDefaultEpisodeNamer().name(epi, ImmutableMap.of(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE)) + " " + epi.getDate()
		    + (epi.isSpecial() ? " [Special]" : ""));
	}
    }
}
