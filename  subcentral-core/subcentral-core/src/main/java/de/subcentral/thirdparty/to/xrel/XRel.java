package de.subcentral.thirdparty.to.xrel;

import java.net.MalformedURLException;
import java.net.URL;

import de.subcentral.core.naming.MediaReleaseNamer;
import de.subcentral.core.naming.MovieNamer;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SimpleNamingService;
import de.subcentral.core.naming.SeasonedEpisodeNamer;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.Replacer;

public class XRel
{
	public static final String				NAME								= "xREL";
	public static URL						HOST_URL;

	private static final CharReplacer		XREL_QUERY_ENTITY_REPLACER			= new CharReplacer();
	private static final SimpleNamingService	XREL_QUERY_ENTITY_NAMING_SERVICE	= new SimpleNamingService();
	static
	{
		// initialize host url
		try
		{
			HOST_URL = new URL("http://www.xrel.to/");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		// initialize the replacer
		XREL_QUERY_ENTITY_REPLACER.setAllowedChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray());
		XREL_QUERY_ENTITY_REPLACER.setReplacement(" ");
		XREL_QUERY_ENTITY_REPLACER.setCharsToDelete("'´`".toCharArray());

		// initialize the naming service
		SeasonedEpisodeNamer epiNamer = new SeasonedEpisodeNamer();
		epiNamer.setWholeNameOperator(XREL_QUERY_ENTITY_REPLACER);

		MovieNamer movieNamer = new MovieNamer();
		movieNamer.setWholeNameOperator(XREL_QUERY_ENTITY_REPLACER);

		MediaReleaseNamer mediaRlsNamer = new MediaReleaseNamer();
		mediaRlsNamer.setWholeNameOperator(XREL_QUERY_ENTITY_REPLACER);

		XREL_QUERY_ENTITY_NAMING_SERVICE.registerNamer(epiNamer);
		XREL_QUERY_ENTITY_NAMING_SERVICE.registerNamer(movieNamer);
		XREL_QUERY_ENTITY_NAMING_SERVICE.registerNamer(mediaRlsNamer);
	}

	public static final Replacer getXRelQueryEntityReplacer()
	{
		return XREL_QUERY_ENTITY_REPLACER;
	}

	public static final NamingService getXRelQueryEntityNamingService()
	{

		return XREL_QUERY_ENTITY_NAMING_SERVICE;
	}

	private XRel()
	{
		// utility class
	}
}
