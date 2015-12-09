package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.correction.CorrectionService;
import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.settings.ProcessingSettings.WinRarLocateStrategy;

// package private
class ProcessingConfig
{
	// parsing
	private Pattern							filenamePattern;
	private ParsingService					filenameParsingService;
	// release
	private ImmutableList<Tag>				releaseMetaTags;
	// release - dbs
	private ImmutableList<MetadataDb>		releaseDbs;
	private ParsingService					releaseParsingService;
	// release - guessing
	private boolean							guessingEnabled;
	private ImmutableList<StandardRelease>	standardReleases;
	// release - compatibility
	private boolean							compatibilityEnabled;
	private CompatibilityService			compatibilityService;
	// standardizing
	private CorrectionService				beforeQueryingCorrectionService;
	private CorrectionService				afterQueryingCorrectionService;
	// naming
	private ImmutableMap<String, Object>	namingParameters;
	// File Transformation - General
	private Path							targetDir;
	private boolean							deleteSource;
	// File Transformation - Packing
	private boolean							packingEnabled;
	private Path							rarExe;
	private WinRarLocateStrategy			winRarLocateStrategy;
	private DeletionMode					packingSourceDeletionMode;

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

	ParsingService getFilenameParsingService()
	{
		return filenameParsingService;
	}

	void setFilenameParsingService(ParsingService filenameParsingService)
	{
		this.filenameParsingService = filenameParsingService;
	}

	ImmutableList<MetadataDb> getReleaseDbs()
	{
		return releaseDbs;
	}

	void setReleaseDbs(ImmutableList<MetadataDb> releaseDbs)
	{
		this.releaseDbs = releaseDbs;
	}

	ParsingService getReleaseParsingService()
	{
		return releaseParsingService;
	}

	void setReleaseParsingService(ParsingService releaseParsingService)
	{
		this.releaseParsingService = releaseParsingService;
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

	CorrectionService getBeforeQueryingCorrectionService()
	{
		return beforeQueryingCorrectionService;
	}

	void setBeforeQueryingStandardizingService(CorrectionService beforeQueryingStandardizingService)
	{
		this.beforeQueryingCorrectionService = beforeQueryingStandardizingService;
	}

	CorrectionService getAfterQueryingCorrectionService()
	{
		return afterQueryingCorrectionService;
	}

	void setAfterQueryingStandardizingService(CorrectionService afterQueryingStandardizingService)
	{
		this.afterQueryingCorrectionService = afterQueryingStandardizingService;
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

	WinRarLocateStrategy getWinRarLocateStrategy()
	{
		return winRarLocateStrategy;
	}

	void setWinRarLocateStrategy(WinRarLocateStrategy winRarLocateStrategy)
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
				.add("winRarLocateStrategy", winRarLocateStrategy)
				.add("packingSourceDeletionMode", packingSourceDeletionMode)
				.toString();
	}
}