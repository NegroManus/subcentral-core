package de.subcentral.watcher.controller.processing;

import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CancellationException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.correct.Correction;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.MediaUtil;
import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtil;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.Tags;
import de.subcentral.core.metadata.service.MetadataService;
import de.subcentral.core.metadata.service.MetadataServiceUtil;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.name.NamingUtil;
import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.util.CollectionUtil;
import de.subcentral.core.util.Context;
import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.winrar.WinRarPackConfig;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;
import de.subcentral.support.winrar.WinRarPackResult;
import de.subcentral.support.winrar.WinRarPackResult.Flag;
import de.subcentral.support.winrar.WinRarPackager;
import de.subcentral.watcher.controller.settings.SettingsController;
import de.subcentral.watcher.settings.ProcessingSettings.LocateStrategy;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

public class ProcessingTask extends Task<Void> implements ProcessingItem {
	private static final Logger				log					= LogManager.getLogger(ProcessingTask.class);

	private final ProcessingController		controller;

	// for ProcessingItem implementation
	private final ListProperty<Path>		files;
	private final Property<ProcessingInfo>	info				= new SimpleObjectProperty<>(this, "info");
	private final WorkerStatus				status				= new WorkerStatus(stateProperty(), messageProperty(), exceptionProperty());

	// Config: is loaded on start of the task
	private ProcessingConfig				config;
	// Important objects for processing and details
	private final TreeItem<ProcessingItem>	taskTreeItem;
	private SubtitleRelease					parsedObject;
	private List<Correction>				parsingCorrections	= ImmutableList.of();
	private List<Release>					listedReleases		= ImmutableList.of();
	private SubtitleRelease					resultObject;
	private ListProperty<ProcessingResult>	results				= new SimpleListProperty<>(this, "results", FXCollections.observableArrayList());

	// package private
	ProcessingTask(Path sourceFile, ProcessingController controller, TreeItem<ProcessingItem> taskTreeItem) {
		this.controller = Objects.requireNonNull(controller, "controller");
		this.files = new SimpleListProperty<>(this, "files", FXCollections.singletonObservableList(sourceFile));
		this.taskTreeItem = taskTreeItem;

		updateTitle(sourceFile.getFileName().toString());
		updateMessage("In queue");
		// progress initial item is -1 (intermediate) -> set it to zero
		// reason: if many tasks are added the animation overhead can get quite big and it's very confusing to watch
		updateProgress(0d, 1d);
	}

	public Path getSourceFile() {
		return files.get(0);
	}

	public ProcessingController getController() {
		return controller;
	}

	public TreeItem<ProcessingItem> getTaskTreeItem() {
		return taskTreeItem;
	}

	public ProcessingConfig getConfig() {
		return config;
	}

	@Override
	public ReadOnlyStringProperty nameProperty() {
		return titleProperty();
	}

	@Override
	public ListProperty<Path> getFiles() {
		return files;
	}

	@Override
	public ReadOnlyProperty<ProcessingInfo> infoProperty() {
		return info;
	}

	@Override
	public ProcessingTaskInfo getInfo() {
		return (ProcessingTaskInfo) info.getValue();
	}

	private void updateInfo(final ProcessingTaskInfo info) {
		Platform.runLater(() -> ProcessingTask.this.info.setValue(info));
	}

	@Override
	public Binding<WorkerStatus> statusBinding() {
		return status;
	}

	public SubtitleRelease getParsedObject() {
		return parsedObject;
	}

	public List<Correction> getParsingCorrections() {
		return parsingCorrections;
	}

	public List<Release> getListedReleases() {
		return listedReleases;
	}

	public SubtitleRelease getResultObject() {
		return resultObject;
	}

	public ObservableList<ProcessingResult> getResults() {
		return results.get();
	}

	public ReadOnlyListProperty<ProcessingResult> resultsProperty() {
		return results;
	}

	@Override
	protected Void call() throws Exception {
		long start = System.nanoTime();
		log.debug("Processing {}", getSourceFile());
		try {
			// Load config
			checkCancelled();
			loadCurrentProcessingConfig();

			// Parse
			checkCancelled();
			parsedObject = parse(getSourceFile());

			updateProgress(0.25d, 1d);

			// Process
			checkCancelled();
			if (parsedObject != null) {
				createResultObject();
				processParsed();

				// May clean up
				deleteSourceFile();
			}
			return null;
		}
		finally {
			log.debug("Processed {} in {} ms", getSourceFile(), TimeUtil.durationMillis(start));

			// To ensure the message is "Cancelled":
			// Sometimes the task does not get interrupted immediately
			// and updates the message after the cancellation.
			// So we set the message again just in case.
			if (isCancelled()) {
				updateProgress(1d, 1d);
				updateMessage("Cancelled");
			}
		}
	}

	@Override
	protected void cancelled() {
		updateProgress(1d, 1d);
		updateMessage("Cancelled");
		log.info("Processing of file was cancelled: " + getSourceFile(), getException());
	}

	@Override
	protected void failed() {
		updateProgress(1d, 1d);
		updateMessage("Failed");
		log.error("Processing of file failed: " + getSourceFile(), getException());
	}

	@Override
	protected void succeeded() {
		updateProgress(1d, 1d);
		if (parsedObject == null) {
			updateMessage("Filename not recognized");
		}
		if (results.isEmpty()) {
			updateMessage("Nothing done - See details");
		}
		else {
			updateMessage("Done");
		}
	}

	private void loadCurrentProcessingConfig() {
		// get the current ProcessingConfig> and use it for the entire process
		config = controller.getProcessingConfig().getValue();
		log.debug("Using processing config: {}", config);
	}

	private SubtitleRelease parse(Path file) {
		updateMessage("Parsing filename");
		ParsingService parsingService = config.getFilenameParsingService();

		String filenameWithoutExt = IOUtil.splitIntoFilenameAndExtension(file.getFileName().toString())[0];
		log.trace("Trying to parse {} with {} to ", filenameWithoutExt, parsingService, SubtitleRelease.class.getSimpleName());
		SubtitleRelease parsed = parsingService.parse(filenameWithoutExt, SubtitleRelease.class);
		log.debug("Parsed {} to {}", file, parsed);
		if (parsed == null) {
			log.info("No parser could parse the filename of " + file);
			return null;
		}

		parsingCorrections = config.getBeforeQueryingCorrectionService().correct(parsed);
		parsingCorrections.forEach((Correction c) -> log.debug("Before querying correction: {}", c));

		return parsed;
	}

	private void createResultObject() {
		// Created result object
		SubtitleRelease convertedSubAdj = new SubtitleRelease();
		convertedSubAdj.setHearingImpaired(parsedObject.isHearingImpaired());
		// tags are not copied
		for (Subtitle srcSub : parsedObject.getSubtitles()) {
			Subtitle convertedSub = new Subtitle();
			convertedSub.setMedia(SerializationUtils.clone(srcSub.getMedia()));
			convertedSub.setLanguage(srcSub.getLanguage());
			convertedSub.setGroup(srcSub.getGroup());
			convertedSub.setSource(srcSub.getSource());
			convertedSubAdj.getSubtitles().add(convertedSub);
		}
		resultObject = convertedSubAdj;
	}

	private void processParsed() throws Exception {
		// Querying
		Release srcRls = parsedObject.getFirstMatchingRelease();
		ListMultimap<MetadataService, Release> queryResults = query(srcRls);
		updateProgress(0.5d, 1d);

		// Process query results
		updateMessage("Processing query results");
		// Add StandardReleases with Scope=ALWAYS
		List<Release> existingRlss = new ArrayList<>(queryResults.values());
		for (StandardRelease standardRls : config.getStandardReleases()) {
			if (standardRls.getScope() == Scope.ALWAYS) {
				Release standardRlsWithMedia = new Release(srcRls.getMedia(), standardRls.getRelease().getTags(), standardRls.getRelease().getGroup());
				existingRlss.add(standardRlsWithMedia);
			}
		}

		// standardize
		List<Correction> corrections = config.getAfterQueryingCorrectionService().correct(resultObject);
		corrections.forEach((Correction c) -> log.debug("After querying correction: {}", c));

		if (existingRlss.isEmpty()) {
			log.info("No releases found in databases and no standard releases with Scope=ALWAYS");
			guess(ImmutableList.of());
		}
		else {
			// Distinct, enrich, standardize
			listedReleases = processReleases(existingRlss);

			// Filter by Media
			Function<Release, List<Media>> nestedObjRetriever = Release::getMedia;
			Function<List<Media>, List<Context>> ctxGenerator = MediaUtil::generateNamingContextsForAllNames;
			List<Release> mediaFilteredFoundReleases = listedReleases.stream()
					.filter(NamingUtil.filterByNestedName(srcRls, nestedObjRetriever, controller.getNamingServicesForFiltering(), ctxGenerator))
					.collect(Collectors.toList());

			// Filter by Release Tags and Group (matching releases)
			log.debug("Filtering found releases with media={}, tags={}, group={}", srcRls.getMedia(), srcRls.getTags(), srcRls.getGroup());
			List<Release> matchingReleases = mediaFilteredFoundReleases.stream()
					.filter(ReleaseUtil.filterByTags(srcRls.getTags()))
					.filter(ReleaseUtil.filterByGroup(srcRls.getGroup(), false))
					.collect(Collectors.toList());

			List<ReleaseAndInfo> mediaFilteredFoundRlssWithInfo = toReleaseAndInfoList(mediaFilteredFoundReleases, (Release rls) -> new ReleaseAndInfo(rls, ProcessingResultInfo.listedMatching()));
			// Guess
			if (matchingReleases.isEmpty()) {
				log.info("No matching releases found");
				guess(mediaFilteredFoundRlssWithInfo);
			}
			else {
				logItems(Level.DEBUG, "Matching releases:", matchingReleases);

				List<ReleaseAndInfo> matchingRlssWithInfo = toReleaseAndInfoList(matchingReleases, (Release rls) -> new ReleaseAndInfo(rls, ProcessingResultInfo.listedMatching()));

				// Add matching releases
				for (ReleaseAndInfo ri : matchingRlssWithInfo) {
					addReleaseToResult(ri);
				}

				log.debug("Searching for compatible releases among the listed releases");
				addCompatibleReleases(matchingRlssWithInfo, mediaFilteredFoundRlssWithInfo);
			}
		}
		updateProgress(0.75d, 1d);
	}

	private ListMultimap<MetadataService, Release> query(Release rls) throws InterruptedException {
		if (config.getReleaseDbs().isEmpty()) {
			log.info("No release databases configured");
			ImmutableListMultimap.Builder<MetadataService, Release> builder = ImmutableListMultimap.builder();
			// FOR DEBUGGING without available db, activate the following lines
			// for (StandardRelease stdRls : config.getStandardReleases())
			// {
			// Release r = new Release(rls.getMedia(), stdRls.getRelease().getTags(), stdRls.getRelease().getGroup());
			// builder.put(new XRelToMetadataService(), r);
			// }
			// /FOR DEBUGGING
			return builder.build();
		}

		StringJoiner rlsDbs = new StringJoiner(", ");
		for (MetadataService rlsDb : config.getReleaseDbs()) {
			rlsDbs.add(rlsDb.getSite().getDisplayName());
		}

		updateMessage("Querying " + rlsDbs.toString());
		log.debug("Querying release databases " + rlsDbs.toString());
		List<Media> queryObj = rls.getMedia();

		checkCancelled();
		ListMultimap<MetadataService, Release> queryResults = MetadataServiceUtil.searchInAll(config.getReleaseDbs(), queryObj, Release.class, controller.getExecutor());

		for (Map.Entry<MetadataService, Collection<Release>> entry : queryResults.asMap().entrySet()) {
			logItems(Level.DEBUG, "Results of " + entry.getKey().getSite().getName(), entry.getValue());
		}
		if (queryResults.isEmpty()) {
			log.info("No releases found in databases");
		}
		return queryResults;
	}

	private void guess(List<ReleaseAndInfo> mediaFilteredFoundReleases) throws Exception {
		Release srcRls = parsedObject.getFirstMatchingRelease();
		if (config.isGuessingEnabled()) {
			log.trace("Guessing enabled");
			displaySystemTrayNotification("Guessing release", getSourceFile().getFileName().toString(), MessageType.WARNING, SettingsController.SETTINGS.getGuessingWarningEnabled().property());

			List<StandardRelease> stdRlss = config.getStandardReleases();
			Map<Release, StandardRelease> guessedReleases = ReleaseUtil.guessMatchingReleases(srcRls, stdRlss, config.getReleaseMetaTags());
			logItems(Level.DEBUG, "Guessed releases:", guessedReleases.keySet());

			List<ReleaseAndInfo> guessedRlssWithInfos = toReleaseAndInfoList(guessedReleases.entrySet(),
					(Map.Entry<Release, StandardRelease> entry) -> new ReleaseAndInfo(entry.getKey(), ProcessingResultInfo.guessedMatching(entry.getValue())));
			for (ReleaseAndInfo entry : guessedRlssWithInfos) {
				addReleaseToResult(entry);
			}

			log.trace("Searching for compatible releases among the listed releases");
			boolean foundCompatibleListedReleases = addCompatibleReleases(guessedRlssWithInfos, mediaFilteredFoundReleases);
			if (!foundCompatibleListedReleases) {
				log.debug("No compatible releases found among the listed releases. Searching for compatible releases among the standard releases");
				List<ReleaseAndInfo> stdRlssWithMediaAndMetaTags = new ArrayList<>(stdRlss.size());
				for (StandardRelease stdRls : stdRlss) {
					Release rls = new Release(srcRls.getMedia(), stdRls.getRelease().getTags(), stdRls.getRelease().getGroup());
					CollectionUtil.transferElementsToHead(srcRls.getTags(), rls.getTags(), config.getReleaseMetaTags());
					stdRlssWithMediaAndMetaTags.add(new ReleaseAndInfo(rls, ProcessingResultInfo.guessedMatching(stdRls)));
				}
				addCompatibleReleases(guessedRlssWithInfos, stdRlssWithMediaAndMetaTags);
			}
		}
		else {
			log.info("Guessing disabled");
		}
	}

	private boolean addCompatibleReleases(Collection<ReleaseAndInfo> matchingRlss, Collection<ReleaseAndInfo> listedRlss) throws Exception {
		if (config.isCompatibilityEnabled()) {
			log.trace("Search for compatible releases enabled");
			// Find compatibles
			CompatibilityService compatibilityService = config.getCompatibilityService();
			Set<Compatibility> compatibles = compatibilityService.findCompatibilities(toReleaseList(matchingRlss), toReleaseList(listedRlss));

			if (compatibles.isEmpty()) {
				log.debug("No compatible releases found");
				return false;
			}
			else {
				logItems(Level.DEBUG, "Compatible releases:", compatibles);

				// Add compatible releases
				for (Compatibility compatible : compatibles) {
					ProcessingResultInfo info;

					ProcessingResultInfo compatibleRlsResultInfo = null;
					for (ReleaseAndInfo ri : listedRlss) {
						if (ri.getRelease() == compatible.getCompatible()) {
							compatibleRlsResultInfo = ri.getInfo();
							break;
						}
					}
					switch (compatibleRlsResultInfo.getSourceType()) {
						case LISTED:
							info = ProcessingResultInfo.listedCompatible(compatible);
							break;
						case GUESSED:
							info = ProcessingResultInfo.guessedCompatible(compatibleRlsResultInfo.getStandardRelease(), compatible);
							break;
						default:
							throw new AssertionError();
					}

					addReleaseToResult(compatible.getCompatible(), info);
				}
				return true;
			}
		}
		else {
			log.debug("Search for compatible releases disabled");
			return false;
		}
	}

	// protected: also callable from DetailsController
	protected void addReleaseToResult(ReleaseAndInfo rlsAndInfo) throws Exception {
		addReleaseToResult(rlsAndInfo.getRelease(), rlsAndInfo.getInfo());
	}

	protected void addReleaseToResult(Release rls, ProcessingResultInfo info) throws Exception {
		List<Correction> corrections = config.getAfterQueryingCorrectionService().correct(rls);
		corrections.forEach((Correction c) -> log.debug("After querying correction: {}", c));

		if (rls.isNuked()) {
			displaySystemTrayNotification("Release is nuked", generateDisplayName(rls), MessageType.WARNING, SettingsController.SETTINGS.getReleaseNukedWarningEnabled().property());
		}
		List<Tag> containedMetaTags = CollectionUtil.getCommonElements(rls.getTags(), config.getReleaseMetaTags());
		if (!containedMetaTags.isEmpty()) {
			String caption = "Release is meta-tagged: " + Tags.join(containedMetaTags);
			displaySystemTrayNotification(caption, generateDisplayName(rls), MessageType.WARNING, SettingsController.SETTINGS.getReleaseMetaTaggedWarningEnabled().property());
		}

		resultObject.getMatchingReleases().add(rls);
		ProcessingResult result = addResult(rls, info);

		createResultFiles(result);

	}

	private List<Release> processReleases(Collection<Release> rlss) {
		if (rlss.isEmpty()) {
			return ImmutableList.of();
		}

		// Sort
		List<Release> processedRlss = rlss.stream().sorted(Release.NAME_COMPARATOR).collect(Collectors.toList());
		processedRlss = ReleaseUtil.distinctByName(processedRlss);
		logItems(Level.DEBUG, "Distinct releases (by name):", processedRlss);

		// Enrich
		for (Release r : processedRlss) {
			// the info from the parsed name should overwrite the info from
			// the release db
			// because it matters how the series name is in the release (not
			// how it is listed on tvrage or sth else)
			// therefore overwrite=true
			// For example a Series may be listed as "Good Wife" at XRel.to but the
			// official release name is "The Good Wife" (another example is XRel title name: "From Dusk Till Dawn: The Series", series name in release "From.Dusk.Till.Dawn")
			// on the other hand, different series name in the release and by the database
			// can be corrected by standardizers
			// TODO: sadly all the extra information about series and
			// episodes (episode title) is overwritten if true
			boolean successful = ReleaseUtil.enrichByParsingName(r, config.getReleaseParsingService(), true);
			if (!successful) {
				log.warn("Could not enrich " + r + " because no parser could parse the release name");
			}
		}
		logItems(Level.DEBUG, "Enriched releases:", processedRlss);

		// Standardize
		for (Release r : processedRlss) {
			List<Correction> corrections = config.getAfterQueryingCorrectionService().correct(r);
			corrections.forEach((Correction c) -> log.debug("After querying correction: {}", c));
		}
		logItems(Level.DEBUG, "Standardized releases:", processedRlss);

		return processedRlss;
	}

	private ProcessingResult addResult(Release rls, ProcessingResultInfo info) {
		ProcessingResult result = new ProcessingResult(this, rls, info);
		Platform.runLater(() -> {
			results.add(result);
			taskTreeItem.getChildren().add(new TreeItem<ProcessingItem>(result));
			taskTreeItem.setExpanded(true);
		});
		return result;
	}

	private void createResultFiles(ProcessingResult result) throws Exception {
		result.updateState(State.SCHEDULED);
		updateMessage("Creating files");
		result.updateMessage("Creating files");
		result.updateState(State.RUNNING);

		try {
			Path srcFile = getSourceFile();
			Path targetDir;
			if (config.getTargetDir() != null) {
				targetDir = srcFile.resolveSibling(config.getTargetDir());
			}
			else {
				targetDir = srcFile.getParent();
			}

			checkCancelled();
			Files.createDirectories(targetDir);

			String fileExtension = IOUtil.splitIntoFilenameAndExtension(srcFile.getFileName().toString())[1];
			Path targetFile = targetDir.resolve(result.getName() + fileExtension);

			checkCancelled();

			Path newFile = Files.copy(srcFile, targetFile, createCopyOptions());

			result.addFile(newFile);
			log.debug("Copied {} to {}", srcFile, targetFile);

			if (pack(result, newFile)) {
				result.updateMessage("Done");
				result.updateState(State.SUCCEEDED);
			}
		}
		catch (Exception e) {
			if (isCancelled()) {
				log.debug("Cancelled while creating file for {}. Exception: {}", result, e.toString());
				result.updateException(e);
				result.updateMessage("Cancelled");
				result.updateState(State.CANCELLED);
				throw e;
			}
			else {
				log.error("File creation failed for " + result, e);
				result.updateException(e);
				result.updateMessage("File creation failed");
				result.updateState(State.FAILED);
			}
		}
		finally {
			result.updateProgress(1d);
		}
	}

	private boolean pack(ProcessingResult result, Path file) throws Exception {
		if (config.isPackingEnabled()) {
			try {
				final Path newRar = file.resolveSibling(result.getName() + ".rar");
				LocateStrategy locateStrategy = config.getWinRarLocateStrategy();
				WinRarPackager packager;
				switch (locateStrategy) {
					case SPECIFY:
						packager = controller.getParent().getWinRar().getPackager(config.getRarExe());
						break;
					case AUTO_LOCATE:
						// fall through
					default:
						packager = controller.getParent().getWinRar().getPackager();
				}
				WinRarPackConfig cfg = new WinRarPackConfig();
				cfg.setCompressionMethod(CompressionMethod.BEST);
				cfg.setTargetOverwriteMode(OverwriteMode.REPLACE);
				cfg.setSourceDeletionMode(config.getPackingSourceDeletionMode());

				checkCancelled();
				WinRarPackResult packResult = packager.pack(file, newRar, cfg);

				if (packResult.getFlags().contains(Flag.SOURCE_DELETED)) {
					result.removeFile(file);
				}
				if (packResult.failed()) {
					throw packResult.getException();
				}
				else {
					log.debug("Packed {} to {} {}", file, newRar, packResult);
					result.addFile(newRar);
					return true;
				}
			}
			catch (Exception e) {
				if (isCancelled()) {
					log.debug("Cancelled while packing file {}. Exception: {}", file, e.toString());
					result.updateException(e);
					result.updateMessage("Cancelled");
					result.updateState(State.CANCELLED);
					throw e;
				}
				else {
					log.error("Packing failed for {}", file, e);
					result.updateException(e);
					result.updateMessage("Packing failed");
					result.updateState(State.FAILED);
					return false;
				}
			}
		}
		else {
			return false;
		}
	}

	public String generateDisplayName(Object obj) {
		return controller.getNamingService().name(obj, config.getNamingParameters());
	}

	public Set<String> generateFilteringDisplayNames(Object obj) {
		return NamingUtil.generateNames(obj, ImmutableList.of(controller.getNamingService()), MediaUtil.generateNamingContextsForAllNames(obj));
	}

	public void deleteSourceFile() {
		if (config.isDeleteSource()) {
			try {
				updateMessage("Deleting source file");
				checkCancelled();
				log.info("Deleting source file {}", getSourceFile());
				Files.deleteIfExists(getSourceFile());
				updateInfo(ProcessingTaskInfo.withAdditonalFlags(getInfo(), ProcessingTaskInfo.Flag.DELETED_SOURCE_FILE));
			}
			catch (IOException e) {
				log.warn("Could not delete source file " + getSourceFile(), e);
				// updateInfo(ProcessingTaskInfo.of("Failed to delete source file: " + e));
			}
		}
	}

	public void deleteResultFiles() throws IOException {
		log.debug("Deleting result files of {}", this);
		for (ProcessingResult result : results) {
			result.deleteFiles();
		}
	}

	private void displaySystemTrayNotification(String caption, String text, MessageType messageType, BooleanProperty warningEnabledProperty) {
		if (controller.getParent().isSystemTrayAvailable()) {
			Platform.runLater(() -> {
				if (SettingsController.SETTINGS.getWarningsEnabled().get() && warningEnabledProperty.get()) {
					controller.getParent().displaySystemTrayNotification(caption, text, messageType);
				}
			});
		}
	}

	private void checkCancelled() throws CancellationException {
		if (isCancelled()) {
			throw new CancellationException();
		}
	}

	@SuppressWarnings("restriction")
	private static CopyOption[] createCopyOptions() {
		try {
			return new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE };
		}
		catch (Throwable t) {
			log.warn("CopyOption com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE could not be used. Copying won't be interruptible", t);
			return new CopyOption[] { StandardCopyOption.REPLACE_EXISTING };
		}
	}

	private static class ReleaseAndInfo {
		private final Release				release;
		private final ProcessingResultInfo	info;

		public ReleaseAndInfo(Release release, ProcessingResultInfo info) {
			this.release = Objects.requireNonNull(release, "release");
			this.info = Objects.requireNonNull(info, "info");
		}

		private Release getRelease() {
			return release;
		}

		private ProcessingResultInfo getInfo() {
			return info;
		}
	}

	private static List<Release> toReleaseList(Collection<ReleaseAndInfo> collection) {
		return collection.stream().map(ReleaseAndInfo::getRelease).collect(Collectors.toList());
	}

	private static <T> List<ReleaseAndInfo> toReleaseAndInfoList(Collection<T> collection, Function<T, ReleaseAndInfo> converter) {
		return collection.stream().map(converter::apply).collect(Collectors.toList());
	}

	private static void logItems(Level logLevel, String headline, Iterable<?> items) {
		log.log(logLevel, headline);
		for (Object o : items) {
			log.log(logLevel, o);
		}
	}
}
