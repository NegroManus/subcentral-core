package de.subcentral.core.impl.scene;

import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingServiceImpl;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.Replacer;

public class Scene
{
	public static final String				DEFAULT_DOMAIN			= "scene";
	public static final char[]				DEFAULT_ALLOWED_CHARS	= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_".toCharArray();
	public static final String				DEFAULT_REPLACEMENT		= ".";
	public static final char[]				DEFAULT_CHARS_TO_DELETE	= "'´`".toCharArray();

	private static final CharReplacer		REPLACER				= new CharReplacer();
	private static final NamingServiceImpl	NAMING_SERVICE			= new NamingServiceImpl();
	static
	{
		REPLACER.setAllowedChars(DEFAULT_ALLOWED_CHARS);
		REPLACER.setReplacement(DEFAULT_REPLACEMENT);
		REPLACER.setCharsToDelete(DEFAULT_CHARS_TO_DELETE);

		NAMING_SERVICE.setDomain(DEFAULT_DOMAIN);

		SceneEpisodeNamer epiNamer = new SceneEpisodeNamer();
		epiNamer.setReplacer(REPLACER);
		NAMING_SERVICE.registerNamer(epiNamer);
	}

	public static Replacer getSceneReplacer()
	{
		return REPLACER;
	}

	public static NamingService getSceneNamingService()
	{
		return NAMING_SERVICE;
	}

	private Scene()
	{
		// utility class
	}
}
