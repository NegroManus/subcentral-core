package de.subcentral.support.predbme;

import de.subcentral.core.metadata.Site;

public class PreDbMe {
    private static final Site                   SITE            = new Site("predb.me", "PreDB.me", "http://predb.me/");
    private static final PreDbMeMetadataService metadataService = new PreDbMeMetadataService();

    private PreDbMe() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static Site getSite() {
        return SITE;
    }

    public static PreDbMeMetadataService getMetadataService() {
        return metadataService;
    }
}
