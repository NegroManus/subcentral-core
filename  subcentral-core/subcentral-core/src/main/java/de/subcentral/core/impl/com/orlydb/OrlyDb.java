package de.subcentral.core.impl.com.orlydb;

import de.subcentral.core.naming.MediaReleaseNamer;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingServiceImpl;
import de.subcentral.core.naming.SeriesEpisodeNamer;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.Replacer;

public class OrlyDb
{
	public static NamingService getOrlyDbQueryNamingService()
	{
		NamingServiceImpl ns = new NamingServiceImpl();
		SeriesEpisodeNamer epiNamer = new SeriesEpisodeNamer();
		MediaReleaseNamer mediaRlsNamer = new MediaReleaseNamer();
		mediaRlsNamer.setMediaReplacer(getOrlyDbQueryReplacer());
		mediaRlsNamer.setMediaFormat("%s");
		mediaRlsNamer.setTagsSeparator(" ");
		mediaRlsNamer.setTagsFormat(" %s");
		mediaRlsNamer.setGroupFormat(" %s");

		ns.registerNamer(epiNamer);
		ns.registerNamer(mediaRlsNamer);
		return ns;
	}

	public static Replacer getOrlyDbQueryReplacer()
	{
		CharReplacer r = new CharReplacer();
		r.setAllowedChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray());
		r.setReplacement(" ");
		r.setCharsToDelete("'´`".toCharArray());
		return r;
	}

	private OrlyDb()
	{
		// utility class
	}

}
