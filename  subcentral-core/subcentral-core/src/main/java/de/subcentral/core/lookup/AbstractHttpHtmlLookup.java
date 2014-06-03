package de.subcentral.core.lookup;

import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class AbstractHttpHtmlLookup<R, Q> extends AbstractHttpLookup<R, Q>
{
	@Override
	public LookupResult<R> lookupByUrl(String subPath) throws LookupException
	{
		try
		{
			Connection con = setupConnection(subPath);
			Document doc = con.get();
			return parseDocument(doc);
		}
		catch (Exception e)
		{
			if (e instanceof LookupException)
			{
				throw (LookupException) e;
			}
			throw new LookupException(e);
		}
	}

	// Sets up a connection without cookies or user-agent info
	protected Connection setupConnection(String subPath) throws Exception
	{
		return Jsoup.connect(new URL(getHost(), subPath).getPath());
	}

	protected abstract LookupResult<R> parseDocument(Document doc) throws Exception;
}
