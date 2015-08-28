package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.standardizing.StandardizingService;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackager.LocateStrategy;

// package private
class ProcessingConfig
{
    // parsing
    private Pattern			       filenamePattern;
    private ImmutableList<ParsingService>      filenameParsingServices;
    // release
    private ImmutableList<Tag>		       releaseMetaTags;
    // release - dbs
    private ImmutableList<MetadataDb<Release>> releaseDbs;
    private ImmutableList<ParsingService>      releaseParsingServices;
    // release - guessing
    private boolean			       guessingEnabled;
    private ImmutableList<StandardRelease>     standardReleases;
    // release - compatibility
    private boolean			       compatibilityEnabled;
    private CompatibilityService	       compatibilityService;
    // standardizing
    private StandardizingService	       beforeQueryingStandardizingService;
    private StandardizingService	       afterQueryingStandardizingService;
    // naming
    private ImmutableMap<String, Object>       namingParameters;
    // File Transformation - General
    private Path			       targetDir;
    private boolean			       deleteSource;
    // File Transformation - Packing
    private boolean			       packingEnabled;
    private Path			       rarExe;
    private LocateStrategy		       winRarLocateStrategy;
    private DeletionMode		       packingSourceDeletionMode;

    // private
    ProcessingConfig()
    {

    }

    Pattern getFilenamePattern()
    {
	return filenamePattern;
    }

    void setFilenamePattern(Pattern filenamePattern)
    {
	this.filenamePattern = filenamePattern;
    }

    ImmutableList<ParsingService> getFilenameParsingServices()
    {
	return filenameParsingServices;
    }

    void setFilenameParsingServices(ImmutableList<ParsingService> filenameParsingServices)
    {
	this.filenameParsingServices = filenameParsingServices;
    }

    ImmutableList<MetadataDb<Release>> getReleaseDbs()
    {
	return releaseDbs;
    }

    void setReleaseDbs(ImmutableList<MetadataDb<Release>> releaseDbs)
    {
	this.releaseDbs = releaseDbs;
    }

    ImmutableList<ParsingService> getReleaseParsingServices()
    {
	return releaseParsingServices;
    }

    void setReleaseParsingServices(ImmutableList<ParsingService> releaseParsingServices)
    {
	this.releaseParsingServices = releaseParsingServices;
    }

    boolean isGuessingEnabled()
    {
	return guessingEnabled;
    }

    void setGuessingEnabled(boolean guessingEnabled)
    {
	this.guessingEnabled = guessingEnabled;
    }

    ImmutableList<Tag> getReleaseMetaTags()
    {
	return releaseMetaTags;
    }

    void setReleaseMetaTags(ImmutableList<Tag> releaseMetaTags)
    {
	this.releaseMetaTags = releaseMetaTags;
    }

    ImmutableList<StandardRelease> getStandardReleases()
    {
	return standardReleases;
    }

    void setStandardReleases(ImmutableList<StandardRelease> standardReleases)
    {
	this.standardReleases = standardReleases;
    }

    boolean isCompatibilityEnabled()
    {
	return compatibilityEnabled;
    }

    void setCompatibilityEnabled(boolean compatibilityEnabled)
    {
	this.compatibilityEnabled = compatibilityEnabled;
    }

    CompatibilityService getCompatibilityService()
    {
	return compatibilityService;
    }

    void setCompatibilityService(CompatibilityService compatibilityService)
    {
	this.compatibilityService = compatibilityService;
    }

    StandardizingService getBeforeQueryingStandardizingService()
    {
	return beforeQueryingStandardizingService;
    }

    void setBeforeQueryingStandardizingService(StandardizingService beforeQueryingStandardizingService)
    {
	this.beforeQueryingStandardizingService = beforeQueryingStandardizingService;
    }

    StandardizingService getAfterQueryingStandardizingService()
    {
	return afterQueryingStandardizingService;
    }

    void setAfterQueryingStandardizingService(StandardizingService afterQueryingStandardizingService)
    {
	this.afterQueryingStandardizingService = afterQueryingStandardizingService;
    }

    ImmutableMap<String, Object> getNamingParameters()
    {
	return namingParameters;
    }

    void setNamingParameters(ImmutableMap<String, Object> namingParameters)
    {
	this.namingParameters = namingParameters;
    }

    Path getTargetDir()
    {
	return targetDir;
    }

    void setTargetDir(Path targetDir)
    {
	this.targetDir = targetDir;
    }

    boolean isDeleteSource()
    {
	return deleteSource;
    }

    void setDeleteSource(boolean deleteSource)
    {
	this.deleteSource = deleteSource;
    }

    boolean isPackingEnabled()
    {
	return packingEnabled;
    }

    void setPackingEnabled(boolean packingEnabled)
    {
	this.packingEnabled = packingEnabled;
    }

    Path getRarExe()
    {
	return rarExe;
    }

    void setRarExe(Path rarExe)
    {
	this.rarExe = rarExe;
    }

    LocateStrategy getWinRarLocateStrategy()
    {
	return winRarLocateStrategy;
    }

    void setWinRarLocateStrategy(LocateStrategy winRarLocateStrategy)
    {
	this.winRarLocateStrategy = winRarLocateStrategy;
    }

    DeletionMode getPackingSourceDeletionMode()
    {
	return packingSourceDeletionMode;
    }

    void setPackingSourceDeletionMode(DeletionMode packingSourceDeletionMode)
    {
	this.packingSourceDeletionMode = packingSourceDeletionMode;
    }

    @Override
    public String toString()
    {
	return MoreObjects.toStringHelper(ProcessingConfig.class)
		.omitNullValues()
		.add("filenamePattern", filenamePattern)
		.add("filenameParsingServices", filenameParsingServices)
		.add("releaseMetaTags", releaseMetaTags)
		.add("releaseDbs", releaseDbs)
		.add("releaseParsingServices", releaseParsingServices)
		.add("guessingEnabled", guessingEnabled)
		.add("standardReleases", standardReleases)
		.add("compatibilityEnabled", compatibilityEnabled)
		.add("compatibilityService", compatibilityService)
		.add("beforeQueryingStandardizingService", beforeQueryingStandardizingService)
		.add("afterQueryingStandardizingService", afterQueryingStandardizingService)
		.add("namingParameters", namingParameters)
		.add("targetDir", targetDir)
		.add("deleteSource", deleteSource)
		.add("packingEnabled", packingEnabled)
		.add("rarExe", rarExe)
		.add("winRarLocateStrategy", winRarLocateStrategy)
		.add("packingSourceDeletionMode", packingSourceDeletionMode)
		.toString();
    }
}