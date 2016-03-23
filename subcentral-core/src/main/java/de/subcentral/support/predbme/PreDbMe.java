package de.subcentral.support.predbme;

import de.subcentral.core.metadata.Site;

public class PreDbMe
{
	public static final Site SITE = new Site("predb.me", "PreDB.me", "http://predb.me/");

	private PreDbMe()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
