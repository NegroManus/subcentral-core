package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.metadata.media.Series;

public class TheTvDbHttpApi implements TheTvDbApi
{
	public static final String	ATTRIBUTE_THETVDB_ID	= "THETVDB_ID";
	public static final String	ATTRIBUTE_IMDB_ID		= "IMDB_ID";

	private static final String	MIRROR					= "http://thetvdb.com/";
	private static final String	BANNER_MIRROR			= MIRROR + "banners/";

	private final String		apiKey;

	public TheTvDbHttpApi(String apiKey)
	{
		this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
	}

	@Override
	public List<Series> getSeries(String name) throws IOException
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

		final Splitter listSplitter = Splitter.on('|').trimResults().omitEmptyStrings();

		URL url = new URL(MIRROR + "api/GetSeries.php?seriesname=" + URLEncoder.encode(name, Charset.forName("UTF-8").name()));
		Document doc = Jsoup.parse(url, 5000);
		Elements seriesElems = doc.getElementsByTag("series");
		ImmutableList.Builder<Series> seriesList = ImmutableList.builder();
		for (Element seriesElem : seriesElems)
		{
			Series series = new Series();
			int seriesId = Integer.parseInt(seriesElem.getElementsByTag("seriesid").first().text());
			series.getAttributes().put(ATTRIBUTE_THETVDB_ID, seriesId);

			String seriesName = seriesElem.getElementsByTag("seriesname").first().text();
			series.setName(seriesName);

			Element aliasNamesElem = seriesElem.getElementsByTag("aliasnames").first();
			if (aliasNamesElem != null)
			{
				List<String> aliasNames = listSplitter.splitToList(aliasNamesElem.text());
				series.setAliasNames(aliasNames);
			}

			Element bannerElem = seriesElem.getElementsByTag("banner").first();
			if (bannerElem != null)
			{
				String banner = BANNER_MIRROR + bannerElem.text();
				series.getImages().put(Media.MEDIA_IMAGE_TYPE_POSTER_HORIZONTAL, banner);
			}

			Element overviewElem = seriesElem.getElementsByTag("overview").first();
			if (overviewElem != null)
			{
				series.setDescription(overviewElem.text());
			}

			Element firstAiredElem = seriesElem.getElementsByTag("firstaired").first();
			if (firstAiredElem != null)
			{
				LocalDate firstAired = LocalDate.parse(firstAiredElem.text());
				series.setDate(firstAired);
			}

			Element networkElem = seriesElem.getElementsByTag("network").first();
			if (networkElem != null)
			{
				Network network = new Network(networkElem.text());
				series.getNetworks().add(network);
			}

			Element imdbIdElem = seriesElem.getElementsByTag("imdb_id").first();
			if (imdbIdElem != null)
			{
				series.getAttributes().put(ATTRIBUTE_IMDB_ID, imdbIdElem.text());
			}

			seriesList.add(series);
		}
		return seriesList.build();
	}

	private HttpURLConnection openConnection(String url) throws IOException
	{
		URLConnection conn = new URL(url).openConnection();
		// fake mozilla user agent
		conn.setRequestProperty("User-Agent", "User-Agent:	Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
		return (HttpURLConnection) conn;
	}

	public static void main(String[] args) throws IOException
	{
		TheTvDbApi api = new TheTvDbHttpApi("A3ACA9D28A27792D");
		List<Series> queryResult = api.getSeries("House of Cards");
		for (Series series : queryResult)
		{
			System.out.println(series);
		}
	}
}
