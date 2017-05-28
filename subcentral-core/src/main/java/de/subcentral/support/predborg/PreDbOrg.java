package de.subcentral.support.predborg;

import de.subcentral.core.metadata.Site;

public class PreDbOrg {
    private static final Site                   SITE            = new Site("predb.org", "PreDB.org", "https://predb.org");
    private static final PreDbOrgMetadataService metadataService = new PreDbOrgMetadataService();

    private PreDbOrg() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static Site getSite() {
        return SITE;
    }

    public static PreDbOrgMetadataService getMetadataService() {
        return metadataService;
    }
}
