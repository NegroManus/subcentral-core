package de.subcentral.support.xrelto;

import de.subcentral.core.metadata.Site;

public class XRelTo {
	private static final Site					SITE			= new Site("xrel.to", "XRel.to", "https://www.xrel.to/");
	private static final XRelToMetadataService	metadataService	= new XRelToMetadataService();

	private XRelTo() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static Site getSite() {
		return SITE;
	}

	public static XRelToMetadataService getMetadataService() {
		return metadataService;
	}
}
