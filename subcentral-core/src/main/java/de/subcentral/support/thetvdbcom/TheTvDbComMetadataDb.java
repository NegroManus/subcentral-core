package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.Metadata;
import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.db.HttpMetadataDb;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.MediaBase;
import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.util.TemporalComparator;
import de.subcentral.support.StandardSites;

public class TheTvDbComMetadataDb extends HttpMetadataDb
{
	/**
	 * An unsigned integer indicating the season number this episode comes after. This field is only available for special episodes. Can be null.
	 */
	public static final String		ATTRIBUTE_AIRSAFTER_SEASON		= "airsafter_season";
	/**
	 * An unsigned integer indicating the episode number this special episode airs before. Must be used in conjunction with airsbefore_season, do not with airsafter_season. This field is only
	 * available for special episodes. Can be null.
	 */
	public static final String		ATTRIBUTE_AIRSBEFORE_EPISODE	= "airsbefore_episode";
	/**
	 * An unsigned integer indicating the season number this special episode airs before. Should be used in conjunction with airsbefore_episode for exact placement. This field is only available for
	 * special episodes. Can be null.
	 */
	public static final String		ATTRIBUTE_AIRSBEFORE_SEASON		= "airsbefore_season";

	public static final String		IMAGE_TYPE_BANNER				= "banner";
	public static final String		IMAGE_TYPE_FANART				= "fanart";
	public static final String		IMAGE_TYPE_POSTER				= "poster";
	public static final String		IMAGE_TYPE_EPISODE_IMAGE		= "episode_image";

	private static final Logger		log								= LogManager.getLogger(TheTvDbComMetadataDb.class);

	private static final String		API_SUB_PATH					= "/api/";
	private static final String		IMG_SUB_PATH					= "/banners/";
	private static final Splitter	LIST_SPLITTER					= Splitter.on('|').trimResults().omitEmptyStrings();

	private String					apiKey;

	public String getApiKey()
	{
		return apiKey;
	}

	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
	}

	@Override
	public Site getSite()
	{
		return TheTvDbCom.SITE;
	}

	@Override
	public Set<Class<?>> getSupportedRecordTypes()
	{
		return ImmutableSet.of(Series.class, Episode.class);
	}

	@Override
	public Set<Class<?>> getSearchableRecordTypes()
	{
		return ImmutableSet.of(Series.class);
	}

	@Override
	public Set<Site> getSupportedExternalSites()
	{
		return ImmutableSet.of(StandardSites.IMDB_COM, StandardSites.ZAP2IT_COM);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(String query, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (Series.class.equals(recordType))
		{
			return (List<T>) searchSeries(query);
		}
		throw newRecordTypeNotSearchableException(recordType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> searchByObject(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (Series.class.equals(recordType))
		{
			if (queryObj instanceof Series)
			{
				Series series = (Series) queryObj;
				if (series.getName() != null)
				{
					return (List<T>) searchSeries(series.getName());
				}
			}

			// Otherwise use the default implementation
			return super.searchByObject(queryObj, recordType);
		}
		throw newRecordTypeNotSearchableException(recordType);
	}

	public List<Series> searchSeries(String name) throws IOException
	{
		return searchSeries(name, null);
	}

	public List<Series> searchSeries(String name, String language) throws IOException
	{
		ImmutableMap.Builder<String, String> query = ImmutableMap.builder();
		query.put("seriesname", formatSeriesNameQueryValue(name));
		if (language != null)
		{
			query.put("language", language);
		}
		URL url = buildRelativeUrl(API_SUB_PATH + "GetSeries.php", query.build());
		log.debug("Searching for series (name={}) using url {}", name, url);
		return parseSeriesSearchResults(getDocument(url));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> searchByExternalId(Site externalSite, String externalId, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (recordType.isAssignableFrom(Series.class))
		{
			return (List<T>) searchSeriesByExternalId(externalSite, externalId);
		}
		throw newUnsupportedRecordTypeException(recordType);
	}

	public List<Series> searchSeriesByExternalId(Site externalSite, String externalId) throws UnsupportedOperationException, IOException
	{
		return searchSeriesByExternalId(externalSite, externalId, null);
	}

	public List<Series> searchSeriesByExternalId(Site externalSite, String externalId, String language) throws UnsupportedOperationException, IOException
	{
		ImmutableMap.Builder<String, String> query = ImmutableMap.builder();
		if (StandardSites.IMDB_COM.equals(externalSite))
		{
			query.put("imdbid", externalId);
		}
		else if (StandardSites.ZAP2IT_COM.equals(externalSite))
		{
			query.put("zap2it", externalId);
		}
		else
		{
			throw newUnsupportedExternalSiteException(externalSite);
		}
		if (language != null)
		{
			query.put("language", language);
		}
		URL url = buildRelativeUrl(API_SUB_PATH + "GetSeriesByRemoteID.php", query.build());
		log.debug("Searching for series ({} id={}) using url {}", externalSite, externalId, url);
		return

		parseSeriesSearchResults(getDocument(url));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (Series.class.equals(recordType))
		{
			return (T) getSeries(Integer.parseInt(id), true, null);
		}
		else if (Episode.class.equals(recordType))
		{
			return (T) getEpisode(Integer.parseInt(id), null);
		}
		throw newUnsupportedRecordTypeException(recordType);
	}

	public Series getSeries(int id, boolean full, String language) throws IOException
	{
		StringBuilder path = new StringBuilder();
		path.append(getApiSubPathWithKey());
		path.append("series/");
		path.append(id);
		path.append('/');
		if (full)
		{
			path.append("all/");
		}

		if (language != null)
		{
			path.append(language);
			path.append(".xml");
		}

		URL url = buildRelativeUrl(path.toString());
		log.debug("Getting series with id {} using url {}", id, url);
		return parseSeriesRecord(getDocument(url));
	}

	public Episode getEpisode(int id, String language) throws IOException
	{
		StringBuilder path = new StringBuilder();
		path.append(getApiSubPathWithKey());
		path.append("episodes/");
		path.append(id);
		path.append('/');
		if (language != null)
		{
			path.append(language);
			path.append(".xml");
		}

		URL url = buildRelativeUrl(path.toString());
		log.debug("Getting episode with id {} using url {}", id, url);
		return parseEpisodeRecord(getDocument(url));
	}

	private String formatSeriesNameQueryValue(String name)
	{
		return NamingDefaults.getDefaultNormalizingFormatter().apply(name);
	}

	private List<Series> parseSeriesSearchResults(Document doc)
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
		ImmutableList.Builder<Series> results = ImmutableList.builder();
		for (Element seriesElem : seriesElems)
		{
			Series series = new Series();

			addId(series, seriesElem, "seriesid", TheTvDbCom.SITE);

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

			addId(series, seriesElem, "imdb_id", StandardSites.IMDB_COM);
			addId(series, seriesElem, "zap2it_id", StandardSites.ZAP2IT_COM);

			results.add(series);
		}
		return results.build();
	}

	protected Series parseSeriesRecord(Document doc)
	{
		Series series = parseBaseSeriesRecord(doc);
		if (series == null)
		{
			return null;
		}
		parseBaseEpisodeRecords(doc, series);
		return series;
	}

	protected Episode parseEpisodeRecord(Document doc)
	{
		Element epiElem = doc.getElementsByTag("episode").first();
		if (epiElem == null)
		{
			return null;
		}
		return parseBaseEpisodeRecord(epiElem);
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
	private Series parseBaseSeriesRecord(Element superElem)
	{
		Element seriesElem = superElem.getElementsByTag("series").first();

		if (seriesElem == null)
		{
			return null;
		}

		Series series = new Series();

		addId(series, seriesElem, "id", TheTvDbCom.SITE);

		series.setContentRating(getTextFromChild(seriesElem, "contentrating"));

		addDateAsLocaleDate(series, seriesElem, "firstaired");

		String genresTxt = getTextFromChild(seriesElem, "genre");
		List<String> genres = LIST_SPLITTER.splitToList(genresTxt);
		series.setGenres(genres);

		addId(series, seriesElem, "imdb_id", StandardSites.IMDB_COM);
		addId(series, seriesElem, "zap2it_id", StandardSites.ZAP2IT_COM);

		addNetwork(series, seriesElem, "network");

		addDescription(series, seriesElem, "overview");

		addRating(series, seriesElem, "rating", TheTvDbCom.SITE);

		String runtimeTxt = getTextFromChild(seriesElem, "runtime");
		int runtime = Integer.parseInt(runtimeTxt);
		series.setRegularRunningTime(runtime);

		series.setName(getTextFromChild(seriesElem, "seriesname"));

		addImage(series, seriesElem, "banner", IMAGE_TYPE_BANNER);
		addImage(series, seriesElem, "fanart", IMAGE_TYPE_FANART);
		addImage(series, seriesElem, "poster", IMAGE_TYPE_POSTER);

		return series;
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
	private void parseBaseEpisodeRecords(Element superElem, Series series)
	{
		Elements epiElems = superElem.getElementsByTag("episode");
		// using a map for the seasons to allow using special map methods
		// use LinkedHashMap to keep insertion order
		Map<String, Season> seasons = new LinkedHashMap<>();
		// a list for all episodes (regular and specials)
		List<Episode> episodes = new ArrayList<>(epiElems.size());
		// a list only for specials, as they are sorted into the complete episode list afterwards
		List<SpecialEpisodeRecord> specials = new ArrayList<>();

		for (Element epiElem : epiElems)
		{
			Episode epi = parseBaseEpisodeRecord(epiElem);

			// Overwrite series
			epi.setSeries(series);

			// Overwrite season
			if (epi.getSeason() != null)
			{
				Season possiblyNewSeason = series.newSeason(epi.getSeason().getNumber());
				// Check whether this season is already stored, if yes, return it, if no return the new season
				Season season = seasons.computeIfAbsent(epi.getSeason().getIds().get(TheTvDbCom.SITE), (String key) -> possiblyNewSeason);
				epi.setSeason(season);
			}
			// May add to special episode list
			if (epi.isSpecial())
			{
				SpecialEpisodeRecord specialEpi = new SpecialEpisodeRecord(epi,
						epi.getAttributeValue(ATTRIBUTE_AIRSAFTER_SEASON),
						epi.getAttributeValue(ATTRIBUTE_AIRSBEFORE_EPISODE),
						epi.getAttributeValue(ATTRIBUTE_AIRSBEFORE_SEASON));
				specials.add(specialEpi);
			}
			else
			{
				episodes.add(epi);
			}
		}

		// sort the episodes in natural order (Series, Season number, Episode number))
		// should not be necessary as episodes are returned in that order but better be safe than sorry
		Collections.sort(episodes);

		// sort the specials by date
		specials.sort((SpecialEpisodeRecord r1, SpecialEpisodeRecord r2) -> TemporalComparator.INSTANCE.compare(r1.episode.getDate(), r2.episode.getDate()));

		insertSpecials(episodes, specials);

		// Add to the series
		series.setEpisodes(episodes);
		series.setSeasons(seasons.values());

	}

	private Episode parseBaseEpisodeRecord(Element epiElem)
	{
		Series series = new Series();
		addId(series, epiElem, "seriesid", TheTvDbCom.SITE);

		Episode epi = series.newEpisode();

		// Episode number
		int seasonNum = getIntegerFromChild(epiElem, "seasonnumber");
		// special episodes are in season 0
		if (seasonNum == 0)
		{
			epi.setSpecial(true);
			Integer airsAfterSeason = getIntegerFromChild(epiElem, "airsafter_season");
			if (airsAfterSeason != null)
			{
				epi.getAttributes().put(ATTRIBUTE_AIRSAFTER_SEASON, airsAfterSeason);
			}
			Integer airsBeforeEpisode = getIntegerFromChild(epiElem, "airsbefore_episode");
			if (airsBeforeEpisode != null)
			{
				epi.getAttributes().put(ATTRIBUTE_AIRSBEFORE_EPISODE, airsBeforeEpisode);
			}
			Integer airsBeforeSeason = getIntegerFromChild(epiElem, "airsbefore_season");
			if (airsBeforeSeason != null)
			{
				epi.getAttributes().put(ATTRIBUTE_AIRSBEFORE_SEASON, airsBeforeSeason);
			}
		}
		else
		{
			Season season = new Season(series, seasonNum);
			addId(season, epiElem, "seasonid", TheTvDbCom.SITE);

			epi.setSeason(season);
			epi.setNumberInSeason(getIntegerFromChild(epiElem, "episodenumber"));
		}
		// add rest of the properties
		addId(epi, epiElem, "id", TheTvDbCom.SITE);

		epi.setTitle(getTextFromChild(epiElem, "episodename"));

		addDateAsLocaleDate(epi, epiElem, "firstaired");

		addId(epi, epiElem, "imdb_id", StandardSites.IMDB_COM);

		addDescription(epi, epiElem, "overview");

		addRating(epi, epiElem, "rating", TheTvDbCom.SITE);

		addImage(epi, epiElem, "filename", IMAGE_TYPE_EPISODE_IMAGE);

		// can be null (for specials or info not available)
		epi.setNumberInSeries(getIntegerFromChild(epiElem, "absolute_number"));

		return epi;
	}

	private void insertSpecials(List<Episode> episodes, List<SpecialEpisodeRecord> specials)
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
				while (iter.hasPrevious())
				{
					Episode currentEpi = iter.previous();
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
				while (iter.hasNext())
				{
					Episode currentEpi = iter.next();
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
				while (iter.hasPrevious())
				{
					Episode currentEpi = iter.previous();
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
					while (iter.hasNext())
					{
						Episode currentEpi = iter.next();
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

	private String getApiSubPathWithKey()
	{
		return new StringBuilder(API_SUB_PATH).append(apiKey).append('/').toString();
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

	private static void addId(Metadata metadata, Element parentElem, String tag, Site site)
	{
		String idTxt = getTextFromChild(parentElem, tag);
		if (idTxt != null)
		{
			metadata.getIds().put(site, idTxt);
		}
	}

	private void addImage(Media media, Element parentElem, String tag, String mediaImageType)
	{
		String imgTxt = getTextFromChild(parentElem, tag);
		if (imgTxt != null)
		{
			String img = buildRelativeUrl(IMG_SUB_PATH + imgTxt).toExternalForm();
			media.getImages().put(mediaImageType, img);
		}
	}

	private static void addDateAsLocaleDate(MediaBase media, Element parentElem, String tag)
	{
		String dateTxt = getTextFromChild(parentElem, tag);
		if (dateTxt != null)
		{
			LocalDate date = LocalDate.parse(dateTxt);
			media.setDate(date);
		}
	}

	private static void addDescription(MediaBase media, Element parentElem, String tag)
	{
		media.setDescription(getTextFromChild(parentElem, tag));
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

	private static void addRating(Media media, Element parentElem, String tag, Site site)
	{
		String ratingTxt = getTextFromChild(parentElem, tag);
		if (ratingTxt != null)
		{
			float rating = Float.parseFloat(ratingTxt);
			media.getRatings().put(site, rating);
		}
	}

	private static class SpecialEpisodeRecord
	{
		private final Episode	episode;
		/**
		 * An unsigned integer indicating the season number this episode comes after. This field is only available for special episodes. Can be null
		 */
		private final Integer	airsAfterSeason;

		/**
		 * An unsigned integer indicating the episode number this special episode airs before. Must be used in conjunction with airsbefore_season, do not with airsafter_season. This field is only
		 * available for special episodes. Can be null.
		 */
		private final Integer	airsBeforeEpisode;
		/**
		 * An unsigned integer indicating the season number this special episode airs before. Should be used in conjunction with airsbefore_episode for exact placement. This field is only available
		 * for special episodes. Can be null.
		 */
		private final Integer	airsBeforeSeason;

		public SpecialEpisodeRecord(Episode episode, Integer airsAfterSeason, Integer airsBeforeEpisode, Integer airsBeforeSeason)
		{
			this.episode = Objects.requireNonNull(episode, "episode");
			this.airsAfterSeason = airsAfterSeason;
			this.airsBeforeEpisode = airsBeforeEpisode;
			this.airsBeforeSeason = airsBeforeSeason;
		}
	}
}
