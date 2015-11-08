package de.subcentral.mig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import de.subcentral.mig.SeriesListParser.SeriesListContent;

public class SeriesListParserTest
{
	private final SeriesListParser parser = new SeriesListParser();

	@Test
	public void testParseSeriesList() throws IOException
	{
		Document seriesListDoc = MigTestUtil.parseDoc(getClass(), "serienliste.html");
		SeriesListContent cnt = parser.parse(seriesListDoc);
		System.out.println("Num of series: " + cnt.getSeries().size());
		System.out.println("Num of seasons: " + cnt.getSeasons().size());
		System.out.println("Num of networks: " + cnt.getNetworks().size());
	}

	@Test
	public void test() throws IOException
	{
		Document doc = Jsoup.parse(new URL("https://www.reddit.com/"), 10000);
		Document doc2 = Jsoup.parse(new URL("https://www.subcentral.de/"), 10000);
	}

	@Test
	public void test2() throws IOException
	{
		URL url = new URL("https://www.subcentral.de/");
		URLConnection conn = url.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		int c = 0;

		while ((c = rd.read()) != -1)
			System.out.print((char) c);
	}

	@Test
	public void test3() throws Exception
	{
		// Load the JDK's cacerts keystore file
		String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
		FileInputStream is = new FileInputStream(filename);
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		String password = "changeit";
		keystore.load(is, password.toCharArray());

		// This class retrieves the most-trusted CAs from the keystore
		PKIXParameters params = new PKIXParameters(keystore);

		// Get the set of trust anchors, which contain the most-trusted CA certificates
		Iterator it = params.getTrustAnchors().iterator();
		while (it.hasNext())
		{
			TrustAnchor ta = (TrustAnchor) it.next();
			// Get certificate
			X509Certificate cert = ta.getTrustedCert();
			System.out.println(cert.getIssuerX500Principal());
		}
	}
}
