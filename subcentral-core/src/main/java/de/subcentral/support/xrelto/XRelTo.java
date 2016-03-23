package de.subcentral.support.xrelto;

import de.subcentral.core.metadata.Site;

public class XRelTo
{
	public static final Site SITE = new Site("xrel.to", "XRel.to", "https://www.xrel.to/");

	private XRelTo()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
