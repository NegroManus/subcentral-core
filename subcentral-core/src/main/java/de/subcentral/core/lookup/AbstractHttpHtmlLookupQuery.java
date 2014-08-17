package de.subcentral.core.lookup;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;

public abstract class AbstractHttpHtmlLookupQuery<R> implements LookupQuery<R>
{
	private final URL	url;

	public AbstractHttpHtmlLookupQuery(URL url)
	{
		Validate.notNull(url, "url cannot be null");
		this.url = url;
	}

	public URL getUrl()
	{
		return url;
	}

	@Override
	public List<R> execute() throws LookupException
	{
		try
		{
			return getResults(getDocument(url));
		}
		catch (Exception e)
		{
			throw new LookupException(url, e);
		}
	}

	protected Document getDocument(URL url) throws IOException
	{
		System.out.println(url);
		Connection con = setupConnection(url);
		Document doc = con.get();
		// System.out.println(doc);
		return doc;
	}

	/**
	 * The default implementation sets up a connection without cookies or user-agent info.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	protected Connection setupConnection(URL url)
	{
		return Jsoup.connect(url.toExternalForm());
	}

	protected abstract List<R> getResults(Document doc);
}
