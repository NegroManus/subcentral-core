package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.correct.CorrectionService;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.service.MetadataService;
import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.util.Context;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.settings.ProcessingSettings.LocateStrategy;

// package private
class ProcessingConfig {
    // parsing
    private Pattern                        filenamePattern;
    private ParsingService                 filenameParsingService;
    // release
    private ImmutableList<Tag>             releaseMetaTags;
    // release - dbs
    private ImmutableList<MetadataService> releaseDbs;
    private ParsingService                 releaseParsingService;
    // release - guessing
    private boolean                        guessingEnabled;
    private ImmutableList<StandardRelease> standardReleases;
    // release - compatibility
    private boolean                        compatibilityEnabled;
    private CompatibilityService           compatibilityService;
    // standardizing
    private CorrectionService              beforeQueryingCorrectionService;
    private CorrectionService              afterQueryingCorrectionService;
    // naming
    private Context                        namingParameters;
    // File Transformation - General
    private Path                           targetDir;
    private boolean                        deleteSource;
    // File Transformation - Packing
    private boolean                        packingEnabled;
    private Path                           rarExe;
    private LocateStrategy                 locateStrategy;
    private DeletionMode                   packingSourceDeletionMode;

    // private
    ProcessingConfig() {

    }

    Pattern getFilenamePattern() {
        return filenamePattern;
    }

    void setFilenamePattern(Pattern filenamePattern) {
        this.filenamePattern = filenamePattern;
    }

    ParsingService getFilenameParsingService() {
        return filenameParsingService;
    }

    void setFilenameParsingService(ParsingService filenameParsingService) {
        this.filenameParsingService = filenameParsingService;
    }

    ImmutableList<MetadataService> getReleaseDbs() {
        return releaseDbs;
    }

    void setReleaseDbs(ImmutableList<MetadataService> releaseDbs) {
        this.releaseDbs = releaseDbs;
    }

    ParsingService getReleaseParsingService() {
        return releaseParsingService;
    }

    void setReleaseParsingService(ParsingService releaseParsingService) {
        this.releaseParsingService = releaseParsingService;
    }

    boolean isGuessingEnabled() {
        return guessingEnabled;
    }

    void setGuessingEnabled(boolean guessingEnabled) {
        this.guessingEnabled = guessingEnabled;
    }

    ImmutableList<Tag> getReleaseMetaTags() {
        return releaseMetaTags;
    }

    void setReleaseMetaTags(ImmutableList<Tag> releaseMetaTags) {
        this.releaseMetaTags = releaseMetaTags;
    }

    ImmutableList<StandardRelease> getStandardReleases() {
        return standardReleases;
    }

    void setStandardReleases(ImmutableList<StandardRelease> standardReleases) {
        this.standardReleases = standardReleases;
    }

    boolean isCompatibilityEnabled() {
        return compatibilityEnabled;
    }

    void setCompatibilityEnabled(boolean compatibilityEnabled) {
        this.compatibilityEnabled = compatibilityEnabled;
    }

    CompatibilityService getCompatibilityService() {
        return compatibilityService;
    }

    void setCompatibilityService(CompatibilityService compatibilityService) {
        this.compatibilityService = compatibilityService;
    }

    CorrectionService getBeforeQueryingCorrectionService() {
        return beforeQueryingCorrectionService;
    }

    void setBeforeQueryingStandardizingService(CorrectionService beforeQueryingStandardizingService) {
        this.beforeQueryingCorrectionService = beforeQueryingStandardizingService;
    }

    CorrectionService getAfterQueryingCorrectionService() {
        return afterQueryingCorrectionService;
    }

    void setAfterQueryingStandardizingService(CorrectionService afterQueryingStandardizingService) {
        this.afterQueryingCorrectionService = afterQueryingStandardizingService;
    }

    Context getNamingParameters() {
        return namingParameters;
    }

    void setNamingParameters(Context namingParameters) {
        this.namingParameters = namingParameters;
    }

    Path getTargetDir() {
        return targetDir;
    }

    void setTargetDir(Path targetDir) {
        this.targetDir = targetDir;
    }

    boolean isDeleteSource() {
        return deleteSource;
    }

    void setDeleteSource(boolean deleteSource) {
        this.deleteSource = deleteSource;
    }

    boolean isPackingEnabled() {
        return packingEnabled;
    }

    void setPackingEnabled(boolean packingEnabled) {
        this.packingEnabled = packingEnabled;
    }

    Path getRarExe() {
        return rarExe;
    }

    void setRarExe(Path rarExe) {
        this.rarExe = rarExe;
    }

    LocateStrategy getWinRarLocateStrategy() {
        return locateStrategy;
    }

    void setWinRarLocateStrategy(LocateStrategy locateStrategy) {
        this.locateStrategy = locateStrategy;
    }

    DeletionMode getPackingSourceDeletionMode() {
        return packingSourceDeletionMode;
    }

    void setPackingSourceDeletionMode(DeletionMode packingSourceDeletionMode) {
        this.packingSourceDeletionMode = packingSourceDeletionMode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ProcessingConfig.class)
                .omitNullValues()
                .add("filenamePattern", filenamePattern)
                .add("filenameParsingService", filenameParsingService)
                .add("releaseMetaTags", releaseMetaTags)
                .add("releaseDbs", releaseDbs)
                .add("releaseParsingService", releaseParsingService)
                .add("guessingEnabled", guessingEnabled)
                .add("standardReleases", standardReleases)
                .add("compatibilityEnabled", compatibilityEnabled)
                .add("compatibilityService", compatibilityService)
                .add("beforeQueryingCorrectionService", beforeQueryingCorrectionService)
                .add("afterQueryingCorrectionService", afterQueryingCorrectionService)
                .add("namingParameters", namingParameters)
                .add("targetDir", targetDir)
                .add("deleteSource", deleteSource)
                .add("packingEnabled", packingEnabled)
                .add("rarExe", rarExe)
                .add("locateStrategy", locateStrategy)
                .add("packingSourceDeletionMode", packingSourceDeletionMode)
                .toString();
    }
}