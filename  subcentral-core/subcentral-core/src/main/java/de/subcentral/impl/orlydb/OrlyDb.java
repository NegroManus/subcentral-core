package de.subcentral.impl.orlydb;

import java.net.MalformedURLException;
import java.net.URL;

import de.subcentral.core.naming.MediaReleaseNamer;
import de.subcentral.core.naming.MovieNamer;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SeasonedEpisodeNamer;
import de.subcentral.core.naming.SimpleNamingService;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.Replacer;

public class OrlyDb
{
	public static final String					NAME								= "ORLYDB";
	public static URL							HOST_URL;

	private static final CharReplacer			ORLYDB_QUERY_ENTITY_REPLACER		= new CharReplacer();
	private static final SimpleNamingService	ORLYDB_QUERY_ENTITY_NAMING_SERVICE	= new SimpleNamingService();
	static
	{
		// initialize host url
		try
		{
			HOST_URL = new URL("http://www.orlydb.com/");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		// initialize the replacer
		ORLYDB_QUERY_ENTITY_REPLACER.setAllowedChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray());
		ORLYDB_QUERY_ENTITY_REPLACER.setReplacement(" ");
		ORLYDB_QUERY_ENTITY_REPLACER.setCharsToDelete("'´`".toCharArray());

		// initialize the naming service
		SeasonedEpisodeNamer epiNamer = new SeasonedEpisodeNamer();
		epiNamer.setWholeNameOperator(ORLYDB_QUERY_ENTITY_REPLACER);

		MovieNamer movieNamer = new MovieNamer();
		movieNamer.setWholeNameOperator(ORLYDB_QUERY_ENTITY_REPLACER);

		MediaReleaseNamer mediaRlsNamer = new MediaReleaseNamer();
		mediaRlsNamer.setWholeNameOperator(ORLYDB_QUERY_ENTITY_REPLACER);

		ORLYDB_QUERY_ENTITY_NAMING_SERVICE.registerNamer(epiNamer);
		ORLYDB_QUERY_ENTITY_NAMING_SERVICE.registerNamer(movieNamer);
		ORLYDB_QUERY_ENTITY_NAMING_SERVICE.registerNamer(mediaRlsNamer);
	}

	public static final Replacer getOrlyDbQueryEntityReplacer()
	{
		return ORLYDB_QUERY_ENTITY_REPLACER;
	}

	public static final NamingService getOrlyDbQueryEntityNamingService()
	{

		return ORLYDB_QUERY_ENTITY_NAMING_SERVICE;
	}

	private OrlyDb()
	{
		// utility class
	}
}
