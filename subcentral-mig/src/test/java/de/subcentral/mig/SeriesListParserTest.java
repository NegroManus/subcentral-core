package de.subcentral.mig;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.junit.Test;

import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;

public class SeriesListParserTest
{
	private final SeriesListParser parser = new SeriesListParser();

	@Test
	public void testParseSeriesList() throws IOException
	{
		Document seriesListDoc = MigTestUtil.parseDoc(getClass(), "serienliste.html");
		SeriesListContent cnt = parser.parseThread(seriesListDoc);
		System.out.println("Num of series: " + cnt.getSeries().size());
		System.out.println("Num of networks: " + cnt.getNetworks().size());
	}
}
