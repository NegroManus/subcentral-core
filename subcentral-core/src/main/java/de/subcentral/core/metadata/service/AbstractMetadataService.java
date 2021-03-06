package de.subcentral.core.metadata.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.MediaUtil;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.name.NamingService;
import de.subcentral.core.name.NamingUtil;

public abstract class AbstractMetadataService implements MetadataService {
    private static final Logger       log            = LogManager.getLogger(AbstractMetadataService.class);

    private final List<NamingService> namingServices = initNamingServices();

    protected List<NamingService> initNamingServices() {
        ImmutableList.Builder<NamingService> services = ImmutableList.builder();
        services.add(NamingDefaults.getDefaultNormalizingNamingService());
        services.add(NamingDefaults.getMultiEpisodeRangeNormalizingNamingService());
        return services.build();
    }

    @Override
    public Set<Site> getSupportedExternalSites() {
        return ImmutableSet.of();
    }

    @Override
    public <T> List<T> searchByObject(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException {
        return searchByObjectsName(queryObj, recordType);
    }

    protected <T> List<T> searchByObjectsName(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException {
        if (queryObj == null) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<T> results = ImmutableList.builder();
        Set<String> names = NamingUtil.generateNames(queryObj, namingServices, MediaUtil.generateNamingContextsForAllNames(queryObj));
        log.debug("Searching for records of type {} with generated names {} for query object {} of type {}", recordType.getName(), names, queryObj, queryObj.getClass().getName());
        for (String name : names) {
            results.addAll(search(name, recordType));
        }
        return results.build();
    }

    @Override
    public <T> List<T> searchByExternalId(Site externalSite, String externalId, Class<T> recordType) throws UnsupportedOperationException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("site.name", getSite().getName()).toString();
    }

    protected UnsupportedOperationException createUnsupportedRecordTypeException(Class<?> unsupportedType) {
        return new UnsupportedOperationException("The record type is not supported: " + unsupportedType + " (supported record types: " + getSupportedRecordTypes() + ")");
    }

    protected UnsupportedOperationException createRecordTypeNotSearchableException(Class<?> unsupportedType) {
        return new UnsupportedOperationException("The record type is not searchable: " + unsupportedType + " (searchable record types: " + getSearchableRecordTypes() + ")");
    }

    protected UnsupportedOperationException createUnsupportedExternalSiteException(Site unsupportedExternalSite) {
        return new UnsupportedOperationException("The external site is not supported: " + unsupportedExternalSite + " (supported external sites: " + getSupportedExternalSites() + ")");
    }
}
