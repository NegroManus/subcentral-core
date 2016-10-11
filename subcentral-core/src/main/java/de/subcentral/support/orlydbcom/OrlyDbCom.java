package de.subcentral.support.orlydbcom;

import de.subcentral.core.metadata.Site;

public class OrlyDbCom {
    private static final Site                     SITE            = new Site("orlydb.com", "OrlyDB.com", "http://orlydb.com/");
    private static final OrlyDbComMetadataService metadataService = new OrlyDbComMetadataService();

    private OrlyDbCom() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static Site getSite() {
        return SITE;
    }

    public static OrlyDbComMetadataService getMetadataService() {
        return metadataService;
    }
}
