package de.subcentral.core.lookup;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class AbstractHttpHtmlLookup<R, P> extends AbstractHttpLookup<R, P>
{
	private static final Logger	log	= LogManager.getLogger(AbstractHttpHtmlLookup.class);

	@Override
	public List<R> queryWithUrl(URL query) throws LookupException
	{
		try
		{
			return queryWithHtmlDoc(getDocument(query));
		}
		catch (LookupException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new LookupException(query, e);
		}
	}

	public abstract List<R> queryWithHtmlDoc(Document doc) throws LookupException;

	protected Document getDocument(URL url) throws IOException
	{
		log.debug("Retrieving contents of {}", url);
		Document doc = setupConnection(url).get();
		log.trace("Retrieved following contents of {}:{}{}{}", url, System.lineSeparator(), doc, System.lineSeparator());
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
