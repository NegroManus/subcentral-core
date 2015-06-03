package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.AbstractMedia;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.metadata.media.Series;

public class TheTvDbHttpApi implements TheTvDbApi
{
	private static final String		MIRROR			= "http://thetvdb.com/";
	private static final String		IMG_MIRROR		= MIRROR + "banners/";
	private static final Splitter	LIST_SPLITTER	= Splitter.on('|').trimResults().omitEmptyStrings();

	private final String			apiKey;

	public TheTvDbHttpApi(String apiKey)
	{
		this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
	}

	@Override
	public List<Series> findSeries(String name) throws IOException
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

		URL url = new URL(getApiBasePath() + "GetSeries.php?seriesname=" + URLEncoder.encode(name, Charset.forName("UTF-8").name()));

		Document doc = Jsoup.parse(url, 5000);
		Elements seriesElems = doc.getElementsByTag("series");
		ImmutableList.Builder<Series> seriesList = ImmutableList.builder();
		for (Element seriesElem : seriesElems)
		{
			Series series = new Series();

			addTvDbId(series, seriesElem, "seriesid");

			series.setName(getText(seriesElem, "seriesname"));

			String aliasNamesTxt = getText(seriesElem, "aliasnames");
			if (aliasNamesTxt != null)
			{
				List<String> aliasNames = LIST_SPLITTER.splitToList(aliasNamesTxt);
				series.setAliasNames(aliasNames);
			}

			addImage(series, seriesElem, "banner", IMAGE_TYPE_BANNER);

			series.setDescription(getText(seriesElem, "overview"));

			addDateAsLocaleDate(series, seriesElem, "firstaired");

			addNetwork(series, seriesElem, "network");

			addImdbId(series, seriesElem, "imdb_id");

			seriesList.add(series);
		}
		return seriesList.build();
	}

	@Override
	public SeriesRecord getSeries(int id, String language, boolean full) throws IOException
	{
		StringBuilder urlSpec = new StringBuilder();
		urlSpec.append(getApiBasePathWithApiKey());
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
		System.out.println(urlSpec);
		URL url = new URL(urlSpec.toString());

		Document doc = Jsoup.parse(url, 5000);
		// System.out.println(doc);

		/**
		 * Base series record example
		 * 
		 * <pre>
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
		 * </pre>
		 */

		Element seriesElem = doc.getElementsByTag("series").first();

		Series series = new Series();

		addTvDbId(series, seriesElem, "id");

		series.setContentRating(getText(seriesElem, "contentrating"));

		addDateAsLocaleDate(series, seriesElem, "firstaired");

		String genresTxt = getText(seriesElem, "genre");
		List<String> genres = LIST_SPLITTER.splitToList(genresTxt);
		series.setGenres(genres);

		addImdbId(series, seriesElem, "imdb_id");

		addNetwork(series, seriesElem, "network");

		series.setDescription(getText(seriesElem, "overview"));

		String ratingTxt = getText(seriesElem, "rating");
		if (ratingTxt != null)
		{
			float rating = Float.parseFloat(ratingTxt);
			series.getRatings().put(RATING_AGENCY_THETVDB, rating);
		}

		String runtimeTxt = getText(seriesElem, "runtime");
		int runtime = Integer.parseInt(runtimeTxt);
		series.setRegularRunningTime(runtime);

		series.setName(getText(seriesElem, "seriesname"));

		addImage(series, seriesElem, "banner", IMAGE_TYPE_BANNER);
		addImage(series, seriesElem, "fanart", IMAGE_TYPE_FANART);
		addImage(series, seriesElem, "poster", IMAGE_TYPE_POSTER);

		/**
		 * Regular episode example
		 * 
		 * <pre>
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
		 * </pre>
		 */

		/**
		 * Special episode example
		 * 
		 * <pre>
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
		 * </pre>
		 */

		return new SeriesRecord(series, ImmutableList.of(), ImmutableList.of());
	}

	private String getApiBasePathWithApiKey()
	{
		return new StringBuilder(getApiBasePath()).append(apiKey).append('/').toString();
	}

	private String getApiBasePath()
	{
		return new StringBuilder(MIRROR).append("api/").toString();
	}

	private static String getText(Element parentElem, String tag)
	{
		Elements elems = parentElem.getElementsByTag(tag);
		if (elems.isEmpty())
		{
			return null;
		}
		return StringUtils.trimToNull(elems.first().text());
	}

	private static void addTvDbId(Media media, Element parentElem, String tag)
	{
		String idTxt = getText(parentElem, tag);
		if (idTxt != null)
		{
			media.getAttributes().put(ATTRIBUTE_THETVDB_ID, Integer.parseInt(idTxt));
		}
	}

	private static void addImage(Media media, Element parentElem, String tag, String mediaImageType)
	{
		String imgTxt = getText(parentElem, tag);
		if (imgTxt != null)
		{
			String img = IMG_MIRROR + imgTxt;
			media.getImages().put(mediaImageType, img);
		}
	}

	private static void addDateAsLocaleDate(AbstractMedia media, Element parentElem, String tag)
	{
		String dateTxt = getText(parentElem, "tag");
		if (dateTxt != null)
		{
			LocalDate date = LocalDate.parse(dateTxt);
			media.setDate(date);
		}
	}

	private static void addNetwork(Series series, Element parentElem, String tag)
	{
		String networkTxt = getText(parentElem, tag);
		if (networkTxt != null)
		{
			Network network = new Network(networkTxt);
			series.getNetworks().add(network);
		}
	}

	private static void addImdbId(Media media, Element parentElem, String tag)
	{
		String imdbIdTxt = getText(parentElem, tag);
		if (imdbIdTxt != null)
		{
			media.getAttributes().put(ATTRIBUTE_IMDB_ID, imdbIdTxt);
		}
	}

	public static void main(String[] args) throws IOException
	{
		TheTvDbApi api = new TheTvDbHttpApi("A3ACA9D28A27792D");
		List<Series> queryResult = api.findSeries("Game of Thrones");
		for (Series series : queryResult)
		{
			System.out.println(series);
		}

		SeriesRecord psych = api.getSeries(queryResult.get(0).getAttributeValue(ATTRIBUTE_THETVDB_ID), "en", true);
		System.out.println(psych.getSeries());
	}
}
