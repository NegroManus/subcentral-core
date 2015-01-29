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

public abstract class AbstractHtmlHttpMetadataDb<R> extends AbstractHttpMetadataDb<R>
{
	private static final Logger	log	= LogManager.getLogger(AbstractHtmlHttpMetadataDb.class);

	@Override
	public List<R> queryWithUrl(URL query) throws MetadataDbUnavailableException, MetadataDbQueryException
	{
		try
		{
			return queryWithHtmlDoc(getDocument(query));
		}
		catch (MetadataDbUnavailableException ue)
		{
			throw ue;
		}
		catch (IOException ioe)
		{
			throw new MetadataDbUnavailableException(this, ioe);
		}
		catch (Exception e)
		{
			throw new MetadataDbQueryException(this, query, e);
		}
	}

	public abstract List<R> queryWithHtmlDoc(Document doc) throws MetadataDbUnavailableException, MetadataDbQueryException;

	protected Document getDocument(URL url) throws IOException
	{
		log.trace("Retrieving contents of {}", url);
		long start = System.nanoTime();
		Document doc = setupConnection(url).get();
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
