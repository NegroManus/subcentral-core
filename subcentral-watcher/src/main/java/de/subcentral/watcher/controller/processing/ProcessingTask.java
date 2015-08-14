package de.subcentral.watcher.controller.processing;

import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.db.MetadataDbUtil;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtil;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingUtil;
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
import de.subcentral.support.winrar.WinRarPackResult.Flag;
import de.subcentral.support.winrar.WinRarPackager;
import de.subcentral.watcher.controller.processing.ProcessingResult.CompatibilityMethodInfo;
import de.subcentral.watcher.controller.processing.ProcessingResult.DatabaseMethodInfo;
import de.subcentral.watcher.controller.processing.ProcessingResult.GuessingMethodInfo;
import de.subcentral.watcher.controller.processing.ProcessingResult.MethodInfo;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

public class ProcessingTask extends Task<Void>implements ProcessingItem
{
    private static final Logger log = LogManager.getLogger(ProcessingTask.class);

    private final ProcessingController controller;

    // for ProcessingItem implementation
    private final ListProperty<Path>	   files;
    private final Property<ProcessingInfo> info	= new SimpleObjectProperty<>(this, "info");

    // Config: is loaded on start of the task
    private ProcessingConfig		    config;
    // Important objects for processing and protocol
    private final TreeItem<ProcessingItem>  taskTreeItem;
    private SubtitleAdjustment		    parsedObject;
    private List<StandardizingChange>	    parsingCorrections	  = ImmutableList.of();
    private List<Release>		    foundReleases	  = ImmutableList.of();
    private List<Release>		    filteredFoundReleases = ImmutableList.of();
    private List<Release>		    matchingReleases	  = ImmutableList.of();
    private Map<Release, CompatibilityInfo> compatibleReleases	  = ImmutableMap.of();
    private SubtitleAdjustment		    resultObject;
    private ListProperty<ProcessingResult>  results		  = new SimpleListProperty<>(this, "results", FXCollections.observableArrayList());

    // package private
    ProcessingTask(Path sourceFile, ProcessingController controller, TreeItem<ProcessingItem> taskTreeItem)
    {
	this.controller = Objects.requireNonNull(controller, "controller");

	this.files = new SimpleListProperty<>(this, "files", FXCollections.singletonObservableList(sourceFile));

	this.taskTreeItem = taskTreeItem;

	updateTitle(sourceFile.getFileName().toString());
	updateMessage("In queue");
	// progress initial value already is -1 (intermediate)
    }

    public Path getSourceFile()
    {
	return files.get(0);
    }

    public ProcessingController getController()
    {
	return controller;
    }

    public TreeItem<ProcessingItem> getTaskTreeItem()
    {
	return taskTreeItem;
    }

    public ProcessingConfig getConfig()
    {
	return config;
    }

    @Override
    public ReadOnlyStringProperty nameProperty()
    {
	return titleProperty();
    }

    @Override
    public ListProperty<Path> getFiles()
    {
	return files;
    }

    @Override
    public ReadOnlyStringProperty statusProperty()
    {
	return messageProperty();
    }

    @Override
    public ReadOnlyProperty<ProcessingInfo> infoProperty()
    {
	return info;
    }

    private void updateInfo(final ProcessingTaskInfo info)
    {
	Platform.runLater(() -> ProcessingTask.this.info.setValue(info));
    }

    public SubtitleAdjustment getParsedObject()
    {
	return parsedObject;
    }

    public List<StandardizingChange> getParsingCorrections()
    {
	return parsingCorrections;
    }

    public List<Release> getFoundReleases()
    {
	return foundReleases;
    }

    public List<Release> getFilteredFoundReleases()
    {
	return filteredFoundReleases;
    }

    public List<Release> getMatchingReleases()
    {
	return matchingReleases;
    }

    public Map<Release, CompatibilityInfo> getCompatibleReleases()
    {
	return compatibleReleases;
    }

    public SubtitleAdjustment getResultObject()
    {
	return resultObject;
    }

    public ReadOnlyListProperty<ProcessingResult> resultsProperty()
    {
	return results;
    }

    public String name(Object obj)
    {
	return controller.getNamingService().name(obj, config.getNamingParameters());
    }

    public void deleteSourceFile() throws IOException
    {
	log.info("Deleting source file {}", getSourceFile());
	Files.deleteIfExists(getSourceFile());
    }

    public void deleteResultFiles() throws IOException
    {
	log.debug("Deleting result files of {}", this);
	for (ProcessingResult result : results)
	{
	    result.deleteFiles();
	}
    }

    @Override
    protected Void call() throws Exception
    {
	long start = System.nanoTime();
	log.debug("Processing {}", getSourceFile());
	try
	{
	    // Load config
	    loadCurrentProcessingConfig();
	    if (isCancelled())
	    {
		return null;
	    }

	    // Parse
	    parsedObject = parse(getSourceFile());
	    if (isCancelled())
	    {
		return null;
	    }
	    updateProgress(0.25d, 1d);

	    // Process
	    if (parsedObject != null)
	    {
		createResultObject();
		processParsed();

		// May clean up
		if (config.isDeleteSource())
		{
		    try
		    {
			updateMessage("Deleting source file");
			deleteSourceFile();
			updateInfo(ProcessingTaskInfo.of("Source file deleted"));
		    }
		    catch (IOException e)
		    {
			log.warn("Could not delete source file", e);
		    }
		}
	    }
	    return null;
	}
	catch (CancellationException e)
	{
	    // ignore if due to cancellation of task
	    if (!isCancelled())
	    {
		throw e;
	    }
	    return null;
	}
	finally
	{
	    log.debug("Processed {} in {} ms", getSourceFile(), TimeUtil.durationMillis(start));

	    // To ensure the message is "Cancelled":
	    // Sometimes the task does not get interrupted immediately
	    // and updates the message after the cancellation.
	    // So we set the message again just in case.
	    if (isCancelled())
	    {
		updateMessage("Cancelled");
	    }
	}
    }

    @Override
    protected void cancelled()
    {
	updateProgress(1d, 1d);
	updateMessage("Cancelled");
	log.info("Cancelled: Processing of " + getSourceFile(), getException());
    }

    @Override
    protected void failed()
    {
	updateProgress(1d, 1d);
	updateMessage("Failed");
	updateInfo(ProcessingTaskInfo.of(getException().toString()));
	log.error("Failed: Processing of " + getSourceFile(), getException());
    }

    @Override
    protected void succeeded()
    {
	updateProgress(1d, 1d);
	if (parsedObject == null)
	{
	    updateMessage("Filename not recognized");
	}
	if (results.isEmpty())
	{
	    updateMessage("Nothing done - See protocol");
	}
	else
	{
	    updateMessage("Done");
	}
    }

    private void loadCurrentProcessingConfig()
    {
	// get the current ProcessingConfig> and use it for the entire process
	config = controller.getProcessingConfig().getValue();
	log.debug("Using processing config: {}", config);
    }

    private SubtitleAdjustment parse(Path file)
    {
	updateMessage("Parsing filename");
	List<ParsingService> parsingServices = config.getFilenameParsingServices();

	String filenameWithoutExt = IOUtil.splitIntoFilenameAndExtension(file.getFileName().toString())[0];
	log.trace("Trying to parse {} with {} to ", filenameWithoutExt, parsingServices, SubtitleAdjustment.class.getSimpleName());
	SubtitleAdjustment parsed = ParsingUtil.parse(filenameWithoutExt, SubtitleAdjustment.class, parsingServices);
	log.debug("Parsed {} to {}", file, parsed);
	if (parsed == null)
	{
	    log.info("No parser could parse the filename of " + file);
	    return null;
	}

	parsingCorrections = config.getBeforeQueryingStandardizingService().standardize(parsed);
	parsingCorrections.forEach(c -> log.debug("Corrected the parsed object: {}", c));

	return parsed;
    }

    private void createResultObject()
    {
	// Created result object
	SubtitleAdjustment convertedSubAdj = new SubtitleAdjustment();
	convertedSubAdj.setHearingImpaired(parsedObject.isHearingImpaired());
	for (Subtitle srcSub : parsedObject.getSubtitles())
	{
	    Subtitle convertedSub = new Subtitle();
	    convertedSub.setMedia(SerializationUtils.clone(srcSub.getMedia()));
	    convertedSub.setLanguage(srcSub.getLanguage());
	    convertedSub.setGroup(srcSub.getGroup());
	    convertedSubAdj.getSubtitles().add(convertedSub);
	}
	resultObject = convertedSubAdj;
    }

    private void processParsed() throws Exception
    {
	// Querying
	Release srcRls = parsedObject.getFirstMatchingRelease();
	ListMultimap<MetadataDb<Release>, Release> queryResults = query(srcRls);
	if (isCancelled())
	{
	    throw new CancellationException();
	}
	updateProgress(0.5d, 1d);

	// Process query results
	updateMessage("Processing query results");
	// Add StandardReleases with Scope=ALWAYS
	List<Release> existingRlss = new ArrayList<>(queryResults.values());
	for (StandardRelease standardRls : config.getStandardReleases())
	{
	    if (standardRls.getScope() == Scope.ALWAYS)
	    {
		Release standardRlsWithMedia = new Release(srcRls.getMedia(), standardRls.getRelease().getTags(), standardRls.getRelease().getGroup());
		existingRlss.add(standardRlsWithMedia);
	    }
	}

	// standardize
	List<StandardizingChange> changes = config.getAfterQueryingStandardizingService().standardize(resultObject);
	changes.forEach(c -> log.debug("Standardized after querying: {}", c));

	if (existingRlss.isEmpty())
	{
	    log.info("No releases found in databases and no standard releases with Scope=ALWAYS");
	    guess();
	}
	else
	{
	    // Distinct, enrich, standardize
	    foundReleases = processReleases(existingRlss);

	    // Filter by Media
	    filteredFoundReleases = foundReleases.stream()
		    .filter(NamingUtil.filterByNestedName(srcRls, controller.getNamingServiceForFiltering(), controller.getNamingParametersForFiltering(), (Release rls) -> rls.getMedia()))
		    .collect(Collectors.toList());

	    // Filter by Release Tags and Group (matching releases)
	    log.debug("Filtering found releases with media={}, tags={}, group={}", srcRls.getMedia(), srcRls.getTags(), srcRls.getGroup());
	    matchingReleases = filteredFoundReleases.stream()
		    .filter(ReleaseUtil.filterByTags(srcRls.getTags(), config.getReleaseMetaTags()))
		    .filter(ReleaseUtil.filterByGroup(srcRls.getGroup(), false))
		    .collect(Collectors.toList());

	    // Guess
	    if (matchingReleases.isEmpty())
	    {
		log.info("No matching releases found");
		guess();
	    }
	    else
	    {
		log.debug("Matching releases:");
		matchingReleases.forEach(r -> log.debug(r));

		// Add matching releases
		for (Release rls : matchingReleases)
		{
		    DatabaseMethodInfo methodInfo = new DatabaseMethodInfo();
		    addReleaseToResult(rls, methodInfo);
		}
		if (config.isCompatibilityEnabled())
		{
		    log.debug("Search for compatible releases enabled. Searching");
		    addCompatibleReleases(matchingReleases, filteredFoundReleases);
		}
		else
		{
		    log.debug("Search for compatible releases disabled");
		}
	    }
	}

	if (isCancelled())
	{
	    throw new CancellationException();
	}
	updateProgress(0.75d, 1d);

	if (isCancelled())
	{
	    throw new CancellationException();
	}
    }

    private ListMultimap<MetadataDb<Release>, Release> query(Release rls) throws InterruptedException
    {
	if (config.getReleaseDbs().isEmpty())
	{
	    log.info("No release databases configured");
	    return ImmutableListMultimap.of();
	}

	StringJoiner rlsDbs = new StringJoiner(", ");
	for (MetadataDb<Release> rlsDb : config.getReleaseDbs())
	{
	    rlsDbs.add(rlsDb.getName());
	}

	updateMessage("Querying " + rlsDbs.toString());
	log.debug("Querying release databases " + rlsDbs.toString());
	List<Media> queryObj = rls.getMedia();
	ListMultimap<MetadataDb<Release>, Release> queryResults = MetadataDbUtil.queryAll(config.getReleaseDbs(), queryObj, controller.getMainController().getCommonExecutor());
	for (Map.Entry<MetadataDb<Release>, Collection<Release>> entry : queryResults.asMap().entrySet())
	{
	    log.debug("Results of {}", entry.getKey().getName());
	    entry.getValue().stream().forEach((r) -> log.debug(r));
	}
	if (queryResults.isEmpty())
	{
	    log.info("No releases found in databases");
	}
	return queryResults;
    }

    private void guess() throws Exception
    {
	Release srcRls = parsedObject.getFirstMatchingRelease();
	if (config.isGuessingEnabled())
	{
	    log.info("Guessing enabled");
	    displayTrayMessage("Guessing", getSourceFile().getFileName().toString(), MessageType.WARNING, WatcherSettings.INSTANCE.guessingWarningEnabledProperty());

	    List<StandardRelease> stdRlss = config.getStandardReleases();
	    Map<Release, StandardRelease> guessedRlss = ReleaseUtil.guessMatchingReleases(srcRls, config.getStandardReleases(), config.getReleaseMetaTags());
	    logReleases(Level.DEBUG, "Guessed releases:", guessedRlss.keySet());
	    for (Map.Entry<Release, StandardRelease> entry : guessedRlss.entrySet())
	    {
		GuessingMethodInfo methodInfo = new GuessingMethodInfo(entry.getValue());
		addReleaseToResult(entry.getKey(), methodInfo);

		if (isCancelled())
		{
		    return;
		}
	    }

	    List<Release> stdRlssWithMediaAndMetaTags = new ArrayList<>(stdRlss.size());
	    for (StandardRelease stdRls : stdRlss)
	    {
		Release rls = new Release(srcRls.getMedia(), stdRls.getRelease().getTags(), stdRls.getRelease().getGroup());
		TagUtil.transferMetaTags(srcRls.getTags(), rls.getTags(), config.getReleaseMetaTags());
		stdRlssWithMediaAndMetaTags.add(rls);
	    }
	    addCompatibleReleases(guessedRlss.keySet(), stdRlssWithMediaAndMetaTags);
	}
	else
	{
	    log.info("Guessing disabled");
	}
    }

    private void addCompatibleReleases(Collection<Release> matchingRlss, Collection<Release> foundReleases) throws Exception
    {
	// Find compatibles
	CompatibilityService compatibilityService = config.getCompatibilityService();
	compatibleReleases = compatibilityService.findCompatibles(matchingRlss, foundReleases);

	if (compatibleReleases.isEmpty())
	{
	    log.debug("No compatible releases found");
	}
	else
	{
	    log.debug("Compatible releases:");
	    compatibleReleases.entrySet().forEach(e -> log.debug(e));

	    // Add compatible releases
	    for (Map.Entry<Release, CompatibilityInfo> entry : compatibleReleases.entrySet())
	    {
		CompatibilityMethodInfo methodInfo = new CompatibilityMethodInfo(entry.getValue());
		addReleaseToResult(entry.getKey(), methodInfo);
	    }
	}
    }

    private void addReleaseToResult(Release rls, MethodInfo methodInfo) throws Exception
    {
	List<StandardizingChange> changes = config.getAfterQueryingStandardizingService().standardize(rls);
	changes.forEach(c -> log.debug("Standardized after querying: {}", c));

	if (rls.isNuked())
	{
	    displayTrayMessage("Nuked release", name(rls), MessageType.WARNING, WatcherSettings.INSTANCE.nukedReleaseWarningEnabledProperty());
	}
	List<Tag> containedMetaTags = TagUtil.getMetaTags(rls.getTags(), config.getReleaseMetaTags());
	if (!containedMetaTags.isEmpty())
	{
	    String caption;
	    if (containedMetaTags.size() == 1)
	    {
		caption = "Contains meta tag ";
	    }
	    else
	    {
		caption = "Contains meta tags ";
	    }
	    displayTrayMessage(caption + Tag.listToString(containedMetaTags), name(rls), MessageType.WARNING, WatcherSettings.INSTANCE.metaTaggedReleaseWarningEnabledProperty());
	}

	resultObject.getMatchingReleases().add(rls);
	ProcessingResult result = addResult(rls, methodInfo);
	updateMessage("Creating files");
	result.updateStatus("Creating files");
	try
	{
	    createFiles(result);
	    result.updateStatus("Done");
	}
	catch (IOException | TimeoutException e)
	{
	    result.updateStatus("Failed to create files: " + e);
	    log.warn("Failed to create files for " + result, e);
	}
	finally
	{
	    result.updateProgress(1d);
	}
    }

    private List<Release> processReleases(Collection<Release> rlss)
    {
	if (rlss.isEmpty())
	{
	    return ImmutableList.of();
	}

	// Sort
	List<Release> processedRlss = rlss.stream().sorted().collect(Collectors.toList());
	processedRlss = ReleaseUtil.distinctByName(processedRlss);
	logReleases(Level.DEBUG, "Distinct releases (by name):", processedRlss);

	// Enrich
	for (Release r : processedRlss)
	{
	    try
	    {
		// the info from the parsed name should overwrite the info from
		// the release db
		// because if matters how the series name is in the release (not
		// how it is listed on tvrage or sth else)
		// therefore overwrite=true
		// For example a Series may be listed as "Good Wife" but the
		// official release name is "The Good Wife"
		// TODO: sadly all the extra information about series and
		// episodes (episode title) is overwritten
		// on the other hands, those mistakes can be corrected by
		// standardizers
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
	    List<StandardizingChange> changes = config.getAfterQueryingStandardizingService().standardize(r);
	    changes.forEach(c -> log.debug("Standardized after querying: {}", c));
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

    private ProcessingResult addResult(Release rls, MethodInfo methodInfo)
    {
	ProcessingResult result = new ProcessingResult(this, rls);
	Platform.runLater(() -> {
	    result.updateInfo(ProcessingResultInfo.of(result, methodInfo));
	    results.add(result);
	    taskTreeItem.getChildren().add(new TreeItem<ProcessingItem>(result));
	    taskTreeItem.setExpanded(true);
	});
	return result;
    }

    private void createFiles(ProcessingResult result) throws Exception
    {
	Path srcFile = getSourceFile();
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
	if (isCancelled())
	{
	    throw new CancellationException();
	}

	String fileExtension = IOUtil.splitIntoFilenameAndExtension(srcFile.getFileName().toString())[1];
	Path targetFile = targetDir.resolve(result.getName() + fileExtension);

	IOUtil.waitUntilCompletelyWritten(srcFile, 1, TimeUnit.MINUTES);
	if (isCancelled())
	{
	    throw new CancellationException();
	}

	Path newFile = Files.copy(srcFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
	result.addFile(newFile);
	log.debug("Copied {} to {}", srcFile, targetFile);
	if (isCancelled())
	{
	    throw new CancellationException();
	}

	if (config.isPackingEnabled())
	{
	    final Path newRar = newFile.resolveSibling(result.getName() + ".rar");
	    LocateStrategy locateStrategy = config.getWinRarLocateStrategy();
	    WinRarPackager packager = WinRar.getPackager(locateStrategy, config.getRarExe());
	    WinRarPackConfig cfg = new WinRarPackConfig();
	    cfg.setCompressionMethod(CompressionMethod.BEST);
	    cfg.setTargetOverwriteMode(OverwriteMode.REPLACE);
	    cfg.setTimeout(1, TimeUnit.MINUTES);
	    cfg.setSourceDeletionMode(config.getPackingSourceDeletionMode());
	    WinRarPackResult packResult = packager.pack(newFile, newRar, cfg);
	    if (packResult.getFlags().contains(Flag.SOURCE_DELETED))
	    {
		result.removeFile(newFile);
	    }
	    if (packResult.failed() && !isCancelled())
	    {
		result.updateStatus("Packing failed");
		updateInfo(ProcessingTaskInfo.of(packResult.getException().toString()));
	    }
	    else
	    {
		result.addFile(newRar);
		log.debug("Packed {} to {} {}", newFile, newRar, packResult);
	    }
	}

	if (isCancelled())
	{
	    throw new CancellationException();
	}
    }

    private void displayTrayMessage(String caption, String text, MessageType messageType, BooleanProperty warningEnabledProperty)
    {
	Platform.runLater(() -> {
	    if (WatcherSettings.INSTANCE.isWarningsEnabled() && warningEnabledProperty.get())
	    {
		controller.getMainController().getWatcherApp().getTrayIcon().displayMessage(caption, text, messageType);
	    }
	});
    }
}
