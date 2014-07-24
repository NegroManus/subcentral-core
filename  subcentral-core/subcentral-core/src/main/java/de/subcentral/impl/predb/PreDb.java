package de.subcentral.impl.predb;

import java.net.MalformedURLException;
import java.net.URL;

import de.subcentral.core.naming.ReleaseNamer;
import de.subcentral.core.naming.MovieNamer;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SeasonedEpisodeNamer;
import de.subcentral.core.naming.SimpleNamingService;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.Replacer;

public class PreDb
{
	public static final String					NAME								= "PreDB.me";
	public static URL							HOST_URL;

	private static final CharReplacer			PREDB_QUERY_ENTITY_REPLACER		= new CharReplacer();
	private static final SimpleNamingService	PREDB_QUERY_ENTITY_NAMING_SERVICE	= new SimpleNamingService();
	static
	{
		// initialize host url
		try
		{
			HOST_URL = new URL("http://www.predb.me/");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		// initialize the replacer
		PREDB_QUERY_ENTITY_REPLACER.setAllowedChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray());
		PREDB_QUERY_ENTITY_REPLACER.setReplacement(" ");
		PREDB_QUERY_ENTITY_REPLACER.setCharsToDelete("'´`".toCharArray());

		// initialize the naming service
		SeasonedEpisodeNamer epiNamer = new SeasonedEpisodeNamer();
		epiNamer.setWholeNameOperator(PREDB_QUERY_ENTITY_REPLACER);

		MovieNamer movieNamer = new MovieNamer();
		movieNamer.setWholeNameOperator(PREDB_QUERY_ENTITY_REPLACER);

		ReleaseNamer mediaRlsNamer = new ReleaseNamer();
		mediaRlsNamer.setWholeNameOperator(PREDB_QUERY_ENTITY_REPLACER);

		PREDB_QUERY_ENTITY_NAMING_SERVICE.registerNamer(epiNamer);
		PREDB_QUERY_ENTITY_NAMING_SERVICE.registerNamer(movieNamer);
		PREDB_QUERY_ENTITY_NAMING_SERVICE.registerNamer(mediaRlsNamer);
	}

	public static final Replacer getPreDbQueryEntityReplacer()
	{
		return PREDB_QUERY_ENTITY_REPLACER;
	}

	public static final NamingService getPreDbQueryEntityNamingService()
	{

		return PREDB_QUERY_ENTITY_NAMING_SERVICE;
	}

	private PreDb()
	{
		// utility class
	}

}
