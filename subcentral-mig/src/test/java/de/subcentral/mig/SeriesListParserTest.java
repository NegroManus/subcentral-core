package de.subcentral.mig;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.junit.Test;

import de.subcentral.mig.parse.SeriesListParser;
import de.subcentral.mig.parse.SeriesListParser.SeriesListData;

public class SeriesListParserTest
{
	private final SeriesListParser parser = new SeriesListParser();

	@Test
	public void testParseSeriesList() throws IOException
	{
		Document seriesListDoc = MigTestUtil.parseDoc(getClass(), "serienliste.html");
		SeriesListData cnt = parser.parseThreadPage(seriesListDoc);
		System.out.println("Num of series: " + cnt.getSeries().size());
		System.out.println("Num of networks: " + cnt.getNetworks().size());
	}
}
