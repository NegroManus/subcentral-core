package de.subcentral.support.orlydbcom;

import de.subcentral.core.metadata.Site;

public class OrlyDbCom
{
	public static final Site SITE = new Site("orlydb.com", "OrlyDB.com", "http://orlydb.com/");

	private OrlyDbCom()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
