package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.subcentral.core.util.TimeUtil;

public abstract class HtmlHttpMetadataDb2 extends HttpMetadataDb2
{
	private static final Logger log = LogManager.getLogger(HtmlHttpMetadataDb2.class);

	// Search
	@Override
	protected <T> List<T> parseSearchResults(URL query, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		return parseSearchResults(getDocument(query), recordType);
	}

	protected abstract <T> List<T> parseSearchResults(Document doc, Class<T> recordType) throws IllegalArgumentException;

	// Get
	protected <T> T parseRecord(URL url, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		return parseRecord(getDocument(url), recordType);
	}

	protected abstract <T> T parseRecord(Document doc, Class<T> recordType) throws IllegalArgumentException;

	protected Document getDocument(URL url) throws IOException
	{
		log.debug("Connecting to {}", url);
		long start = System.nanoTime();
		Connection con = setupConnection(url);
		Document doc = con.get();
		double duration = TimeUtil.durationMillis(start);
		log.printf(Level.DEBUG, "Retrieved contents of %s in %.0f ms", url, duration);
		log.printf(Level.TRACE, "Contents of %s were:%n%s%n", url, doc);
		return doc;
	}

	/**
	 * The default implementation sets up a connection with the timeout.
	 * <p>
	 * Subclasses may override this method, call super.setupConnection() and then set other configurations like cookies or user-agent info.
	 * </p>
	 * 
	 * @param url
	 *            thr url to connect to
	 * @return a connection
	 */
	protected Connection setupConnection(URL url)
	{
		return Jsoup.connect(url.toExternalForm()).timeout(timeout);
	}
}
