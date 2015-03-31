package de.subcentral.watcher.controller.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.db.MetadataDbUtil;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.AssumeExistence;
import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtil;
import de.subcentral.core.metadata.release.SameGroupCompatibility;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.ParsingException;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ParsingUtil;
import de.subcentral.core.standardizing.StandardizingChange;
import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;
import de.subcentral.support.winrar.WinRarPackResult;
import de.subcentral.support.winrar.WinRarPackager;
import de.subcentral.watcher.controller.processing.ProcessingController.ProcessingConfig;
import de.subcentral.watcher.model.ObservableNamableBeanWrapper;

public class ProcessingTask extends Task<Void>
{
	private static final Logger			log	= LogManager.getLogger(ProcessingTask.class);

	private final Path					sourceFile;
	private final ProcessingController	processingController;
	// is loaded on start of the task
	private ProcessingConfig			config;

	// private SourceProcessingItem srcItem;
	// private TreeItem<ProcessingItem> srcTreeItem;

	// package private
	ProcessingTask(Path sourceFile, ProcessingController processingController)
	{
		this.sourceFile = Objects.requireNonNull(sourceFile, "sourceFile");
		this.processingController = Objects.requireNonNull(processingController, "processingController");
	}

	public Path getSourceFile()
	{
		return sourceFile;
	}

	public ProcessingController getProcessingController()
	{
		return processingController;
	}

	public ProcessingConfig getConfig()
	{
		return config;
	}

	@Override
	protected Void call() throws Exception
	{
		long start = System.nanoTime();
		log.debug("Processing {}", sourceFile);
		try
		{
			loadCurrentProcessingConfig();

			if (!filter())
			{
				return null;
			}

			Object parsed = parse();
			if (parsed == null)
			{
				return null;
			}

			if (parsed instanceof Release)
			{
				log.warn("Processing of releases is currently not possible");
			}
			else if (parsed instanceof SubtitleAdjustment)
			{
				processSubtitleAdjustment((SubtitleAdjustment) parsed);
			}
			return null;
		}
		catch (Exception e)
		{
			log.error("Exception while processing " + sourceFile, e);
			throw e;
		}
		finally
		{
			log.debug("Processed {} in {} ms", sourceFile, TimeUtil.durationMillis(start));
		}
	}

	private void loadCurrentProcessingConfig()
	{
		// get the current ProcessingConfig> and use it for the entire process
		config = processingController.getProcessingConfig().getValue();
		log.debug("Using processing config: {}", config);
	}

	private boolean filter()
	{
		if (!Files.isRegularFile(sourceFile, LinkOption.NOFOLLOW_LINKS))
		{
			log.debug("Filtered out {} because it is no regular file", sourceFile);
			return false;
		}

		Pattern pattern = config.getFilenamePattern();
		if (pattern == null)
		{
			log.debug("Filtered out {} because no pattern is specified", sourceFile);
			return false;
		}
		if (!pattern.matcher(sourceFile.getFileName().toString()).matches())
		{
			log.debug("Filtered out {} because its name does not match the required pattern {}", sourceFile, pattern);
			return false;
		}

		return true;
	}

	private SubtitleAdjustment parse()
	{
		List<ParsingService> parsingServices = config.getFilenameParsingServices();

		String filenameWithoutExt = IOUtil.splitIntoFilenameAndExtension(sourceFile.getFileName().toString())[0];
		log.trace("Trying to parse {} with {} to ", filenameWithoutExt, parsingServices, SubtitleAdjustment.class.getSimpleName());
		SubtitleAdjustment parsed = ParsingUtil.parse(filenameWithoutExt, SubtitleAdjustment.class, parsingServices);
		log.debug("Parsed {} to {}", sourceFile, parsed);
		if (parsed == null)
		{
			log.info("No parser could parse the filename of " + sourceFile);
			return null;
		}

		List<StandardizingChange> parsedChanges = processingController.getParsedStandardizingService().standardize(parsed);
		parsedChanges.forEach(c -> log.debug("Standardized parsed: {}", c));

		return parsed;
	}

	private TreeItem<ProcessingItem> addSourceTreeItem(ProcessingItem sourceItem)
	{
		TreeItem<ProcessingItem> sourceTreeItem = new TreeItem<>(sourceItem);
		Platform.runLater(() -> processingController.getProcessingTreeTable().getRoot().getChildren().add(sourceTreeItem));
		return sourceTreeItem;
	}

	private void processSubtitleAdjustment(SubtitleAdjustment srcSubAdj) throws InterruptedException
	{
		ObservableNamableBeanWrapper<SubtitleAdjustment> subAdjWrapper = createSubAdjWrapper(srcSubAdj);
		SourceProcessingItem srcItem = new SourceProcessingItem(sourceFile, subAdjWrapper);

		TreeItem<ProcessingItem> srcTreeItem = addSourceTreeItem(srcItem);

		// Querying
		Release srcRls = srcSubAdj.getFirstMatchingRelease();
		List<Media> queryObj = srcRls.getMedia();
		srcItem.updateStatus("Querying release databases");
		srcItem.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		ListMultimap<MetadataDb<Release>, Release> results = MetadataDbUtil.queryAll(config.getReleaseDbs(),
				queryObj,
				processingController.getMainController().getCommonExecutor());
		for (Map.Entry<MetadataDb<Release>, Collection<Release>> entry : results.asMap().entrySet())
		{
			log.debug("Results of {}", entry.getKey().getName());
			entry.getValue().stream().forEach((r) -> log.debug(r));
		}

		srcItem.updateProgress(0.5d);
		srcItem.updateStatus("Processing query results");

		List<Release> existingRlss = new ArrayList<>(results.values().size());
		existingRlss.addAll(results.values());
		for (StandardRelease standardRls : config.getStandardReleases())
		{
			if (standardRls.getAssumeExistence() == AssumeExistence.ALWAYS)
			{
				Release standardRlsWithMedia = new Release(srcRls.getMedia(), standardRls.getStandardRelease().getTags(), standardRls.getStandardRelease()
						.getGroup());
				existingRlss.add(standardRlsWithMedia);
			}
		}

		// Distinct, enrich, standardize
		List<Release> foundRlss = processReleases(existingRlss);

		// Filter
		List<Release> matchingRlss = foundRlss.stream()
				.filter(ReleaseUtil.filterByMedia(srcRls.getMedia(),
						processingController.getMediaNamingServiceForReleaseFiltering(),
						config.getNamingParameters()))
				.filter(ReleaseUtil.filterByTags(srcRls.getTags(), config.getReleaseMetaTags()))
				.filter(ReleaseUtil.filterByGroup(srcRls.getGroup(), false))
				.collect(Collectors.toList());
		log.debug("Matching releases:");
		matchingRlss.forEach(r -> log.debug(r));

		SubtitleAdjustment convertedSubAdj = new SubtitleAdjustment();
		for (Subtitle srcSub : srcSubAdj.getSubtitles())
		{
			Subtitle convertedSub = new Subtitle();
			convertedSub.setMedia(srcSub.getMedia());
			convertedSub.setHearingImpaired(srcSub.isHearingImpaired());
			convertedSub.setLanguage(srcSub.getLanguage());
			convertedSub.setGroup(srcSub.getGroup());
			convertedSubAdj.getSubtitles().add(convertedSub);
		}
		List<StandardizingChange> rlsChanges = processingController.getPostMetadataStandardizingService().standardize(convertedSubAdj);
		rlsChanges.forEach((e) -> System.out.println(e));

		if (matchingRlss.isEmpty())
		{
			log.info("Found no matching releases");
			if (config.isGuessingEnabled())
			{
				log.info("Guessing enabled. Guessing");
				List<StandardRelease> commonRlss = config.getStandardReleases();
				List<Release> commonRlssWithMedia = new ArrayList<>(commonRlss.size());
				for (StandardRelease commonRls : commonRlss)
				{
					commonRlssWithMedia.add(new Release(srcRls.getMedia(), commonRls.getStandardRelease().getTags(), commonRls.getStandardRelease()
							.getGroup()));
				}
				List<Release> guessedRlss = ReleaseUtil.guessMatchingReleases(srcRls, commonRlssWithMedia, config.getReleaseMetaTags());
				logReleases(Level.DEBUG, "Guessed releases:", guessedRlss);
				for (Release rls : guessedRlss)
				{
					// fill media with media of source rls if not set
					if (rls.getMedia().isEmpty())
					{
						rls.getMedia().addAll(srcRls.getMedia());
					}
					addMatchingRelease(convertedSubAdj, rls, "Guessed release", srcTreeItem, srcItem);
				}
				addCompatibleReleases(guessedRlss, commonRlssWithMedia, convertedSubAdj, srcTreeItem, srcItem);
			}
			else
			{
				log.info("Guessing disabled");
			}
		}
		else
		{
			// Add matching releases
			for (Release rls : matchingRlss)
			{
				addMatchingRelease(convertedSubAdj, rls, "Matching release", srcTreeItem, srcItem);
			}
			if (config.isCompatibilityEnabled())
			{
				log.debug("Search for compatible releases enabled. Searching");
				addCompatibleReleases(matchingRlss, foundRlss, convertedSubAdj, srcTreeItem, srcItem);
			}
			else
			{
				log.debug("Search for compatible releases disabled");
			}
		}

		srcItem.updateProgress(0.75d);

		srcItem.updateStatus("Done");
		srcItem.updateProgress(1d);
	}

	private void addCompatibleReleases(Collection<Release> matchingRlss, Collection<Release> existingReleases, SubtitleAdjustment subAdj,
			TreeItem<ProcessingItem> srcTreeItem, SourceProcessingItem srcItem)
	{
		// Find compatibles
		CompatibilityService compatibilityService = processingController.getCompatibilityService();
		Map<Release, CompatibilityInfo> compatibleRlss = compatibilityService.findCompatibles(matchingRlss, existingReleases);

		log.debug("Compatible releases:");
		compatibleRlss.entrySet().forEach(e -> log.debug(e));

		// Add compatible releases
		for (Map.Entry<Release, CompatibilityInfo> entry : compatibleRlss.entrySet())
		{
			addMatchingRelease(subAdj, entry.getKey(), formatCompatibilityInfo(entry.getValue()), srcTreeItem, srcItem);
		}
	}

	private SubtitleTargetProcessingItem addMatchingRelease(SubtitleAdjustment subAdj, Release rls, String info,
			TreeItem<ProcessingItem> srcTreeItem, SourceProcessingItem srcItem)
	{
		List<StandardizingChange> rlsChanges = processingController.getPostMetadataStandardizingService().standardize(rls);
		rlsChanges.forEach((e) -> System.out.println(e));

		subAdj.getMatchingReleases().add(rls);
		SubtitleTargetProcessingItem targetItem = addTargetFilesItem(srcTreeItem, subAdj, rls, info);
		targetItem.updateStatus("Creating files");
		srcItem.updateStatus("Creating files");
		try
		{
			createFiles(sourceFile, targetItem);
			targetItem.updateStatus("Done");
		}
		catch (IOException | TimeoutException e)
		{
			targetItem.updateStatus("Failed to create files: " + e);
			log.warn("Failed to create files for " + targetItem, e);
		}
		targetItem.updateProgress(1d);
		return targetItem;
	}

	private List<Release> processReleases(Collection<Release> rlss)
	{
		// Distinct
		List<Release> processedRlss = ReleaseUtil.distinctByName(rlss);
		logReleases(Level.DEBUG, "Distinct releases (by name):", processedRlss);

		// Enrich
		for (Release r : processedRlss)
		{
			try
			{
				// the info from the parsed name should overwrite the info from the release db
				// because if matters how the series name is in the release (not how it is listed on tvrage or sth else)
				// therefore overwrite=true
				ReleaseUtil.enrichByParsingName(r, config.getReleaseParsingServices(), true);
			}
			catch (ParsingException e)
			{
				log.warn("Could not enrich " + r, e);
			}
		}
		logReleases(Level.DEBUG, "Enriched releases:", processedRlss);

		// Standardize
		for (Release r : processedRlss)
		{
			List<StandardizingChange> rlsChanges = processingController.getPostMetadataStandardizingService().standardize(r);
			for (StandardizingChange c : rlsChanges)
			{
				log.debug("Standardized to custom: {}", c);
			}
		}
		logReleases(Level.DEBUG, "Standardized releases:", processedRlss);

		return processedRlss;
	}

	private static void logReleases(Level logLevel, String headline, Iterable<Release> rlss)
	{
		log.log(logLevel, headline);
		for (Release r : rlss)
		{
			log.log(logLevel, r);
		}
	}

	private String formatCompatibilityInfo(CompatibilityInfo info)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Compatible to ");
		sb.append(processingController.getNamingService().name(info.getCompatibleTo(), config.getNamingParameters()));
		sb.append(" because ");
		Compatibility c = info.getCompatibility();
		if (c instanceof SameGroupCompatibility)
		{
			sb.append("same group");
		}
		else if (c instanceof CrossGroupCompatibility)
		{
			sb.append(((CrossGroupCompatibility) c).toShortString());
		}
		else
		{
			sb.append(info.getCompatibility());
		}
		return sb.toString();
	}

	private SubtitleTargetProcessingItem addTargetFilesItem(TreeItem<ProcessingItem> srcFileTreeItem, SubtitleAdjustment targetSubAdj, Release rls,
			String info)
	{
		ObservableNamableBeanWrapper<SubtitleAdjustment> subAdjWrapper = createSubAdjWrapper(targetSubAdj);
		SubtitleTargetProcessingItem targetItem = new SubtitleTargetProcessingItem(subAdjWrapper, rls);
		targetItem.updateInfo(info);
		TreeItem<ProcessingItem> targetTreeItem = new TreeItem<>(targetItem);
		Platform.runLater(() -> {
			srcFileTreeItem.getChildren().add(targetTreeItem);
			srcFileTreeItem.setExpanded(true);
		});
		return targetItem;
	}

	private void createFiles(Path srcFile, ProcessingItem targetItem) throws IOException, TimeoutException
	{
		Path targetDir;
		if (config.getTargetDir() != null)
		{
			targetDir = srcFile.resolveSibling(config.getTargetDir());
		}
		else
		{
			targetDir = srcFile.getParent();
		}

		Files.createDirectories(targetDir);

		String fileExtension = IOUtil.splitIntoFilenameAndExtension(sourceFile.getFileName().toString())[1];
		Path targetFile = targetDir.resolve(targetItem.getName() + fileExtension);

		IOUtil.waitUntilCompletelyWritten(srcFile, 1, TimeUnit.MINUTES);

		Path newFile = Files.copy(srcFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
		Path otherNewFile = Paths.get(newFile.toString());
		Platform.runLater(() -> {
			targetItem.getFiles().add(otherNewFile);
		});
		log.debug("Copied {} to {}", srcFile, targetFile);

		if (config.isPackingEnabled())
		{
			final Path newRar = newFile.resolveSibling(targetItem.getName() + ".rar");
			LocateStrategy locateStrategy = config.isAutoLocateWinRar() ? LocateStrategy.RESOURCE : LocateStrategy.SPECIFY;
			WinRarPackager packager = WinRar.getPackager(locateStrategy, config.getRarExe());
			WinRarPackConfig cfg = new WinRarPackConfig();
			cfg.setCompressionMethod(CompressionMethod.BEST);
			cfg.setTargetOverwriteMode(OverwriteMode.REPLACE);
			cfg.setTimeout(1, TimeUnit.MINUTES);
			cfg.setSourceDeletionMode(config.getPackingSourceDeletionMode());
			WinRarPackResult packResult = packager.pack(newFile, newRar, cfg);
			Path otherNewRar = Paths.get(newRar.toString());
			Platform.runLater(() -> {
				targetItem.getFiles().add(otherNewRar);
			});
			log.debug("Packed {} to {} {}", newFile, newRar, packResult);
		}
	}

	private ObservableNamableBeanWrapper<SubtitleAdjustment> createSubAdjWrapper(SubtitleAdjustment subAdj)
	{
		ObservableNamableBeanWrapper<SubtitleAdjustment> subAdjWrapper = new ObservableNamableBeanWrapper<>(subAdj,
				processingController.getNamingService());
		subAdjWrapper.getNamingParameters().putAll(config.getNamingParameters());
		return subAdjWrapper;
	}
}
