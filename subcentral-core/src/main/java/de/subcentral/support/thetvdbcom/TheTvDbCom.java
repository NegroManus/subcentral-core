package de.subcentral.support.thetvdbcom;

import de.subcentral.core.metadata.Site;

public class TheTvDbCom
{
	private static final Site SITE = new Site("thetvdb.com", "TheTVDB.com", "http://thetvdb.com/");

	private TheTvDbCom()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static Site getSite()
	{
		return SITE;
	}

	public static TheTvDbComMetadataService getMetadataService(String apiKey)
	{
		return new TheTvDbComMetadataService(apiKey);
	}
}
