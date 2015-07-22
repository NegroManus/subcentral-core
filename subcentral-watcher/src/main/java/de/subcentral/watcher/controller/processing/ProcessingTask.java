package de.subcentral.watcher.controller.processing;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
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
import de.subcentral.watcher.controller.processing.ProcessingController.ProcessingConfig;
import de.subcentral.watcher.controller.processing.ProcessingResult.CompatibilityMethodInfo;
import de.subcentral.watcher.controller.processing.ProcessingResult.GuessingMethodInfo;
import de.subcentral.watcher.controller.processing.ProcessingResult.DatabaseMethodInfo;
import de.subcentral.watcher.controller.processing.ProcessingResult.MethodInfo;
import javafx.application.Platform;
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
    private ProcessingConfig		   config;
    // Important objects
    private final TreeItem<ProcessingItem> taskTreeItem;
    private SubtitleAdjustment		   sourceObject;
    private SubtitleAdjustment		   targetObject;
    private ListProperty<ProcessingResult> results		 = new SimpleListProperty<>(this, "results", FXCollections.observableArrayList());
    // Protocol
    private List<Release>		   processedQueryResults = ImmutableList.of();
    private List<Release>		   matchingReleases	 = ImmutableList.of();

    // package private
    ProcessingTask(Path sourceFile, ProcessingController controller, TreeItem<ProcessingItem> taskTreeItem)
    {
	this.controller = Objects.requireNonNull(controller, "controller");

	this.files = new SimpleListProperty<>(this, "files", FXCollections.singletonObservableList(sourceFile));

	this.taskTreeItem = taskTreeItem;

	updateTitle(sourceFile.getFileName().toString());
	updateMessage("In queue");
    }

    public Path getSourceFile()
    {
	return files.get(0);
    }

    public ProcessingController getController()
    {
	return controller;
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

    public SubtitleAdjustment getSourceObject()
    {
	return sourceObject;
    }

    public SubtitleAdjustment getTargetObject()
    {
	return targetObject;
    }

    public ReadOnlyListProperty<ProcessingResult> resultsProperty()
    {
	return results;
    }

    public TreeItem<ProcessingItem> getTaskTreeItem()
    {
	return taskTreeItem;
    }

    public List<Release> getProcessedQueryResults()
    {
	return processedQueryResults;
    }

    public List<Release> getMatchingReleases()
    {
	return matchingReleases;
    }

    @Override
    protected Void call() throws Exception
    {
	long start = System.nanoTime();
	log.debug("Processing {}", getSourceFile());
	try
	{
	    loadCurrentProcessingConfig();

	    // Parse
	    updateMessage("Parsing file");
	    sourceObject = parse(getSourceFile());
	    if (isCancelled())
	    {
		return null;
	    }
	    updateProgress(0.25d, 1d);

	    // Process
	    if (sourceObject != null)
	    {
		createTargetObject();
		processParsed();
	    }
	    return null;
	}
	finally
	{
	    log.debug("Processed {} in {} ms", getSourceFile(), TimeUtil.durationMillis(start));
	}
    }

    @Override
    protected void cancelled()
    {
	updateMessage("Cancelled");
	updateProgress(1d, 1d);
	log.info("Processing of " + getSourceFile() + " was cancelled", getException());
    }

    @Override
    protected void failed()
    {
	updateMessage("Failed");
	updateProgress(1d, 1d);
	updateInfo(ProcessingTaskInfo.of(getException().toString()));
	log.error("Processing of " + getSourceFile() + " failed", getException());
    }

    @Override
    protected void succeeded()
    {
	updateMessage("Done");
	updateProgress(1d, 1d);
    }

    private void loadCurrentProcessingConfig()
    {
	// get the current ProcessingConfig> and use it for the entire process
	config = controller.getProcessingConfig().getValue();
	log.debug("Using processing config: {}", config);
    }

    private SubtitleAdjustment parse(Path file)
    {
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

	List<StandardizingChange> parsedChanges = config.getBeforeQueryingStandardizingService().standardize(parsed);
	parsedChanges.forEach(c -> log.debug("Standardized before querying: {}", c));

	return parsed;
    }

    private void createTargetObject()
    {
	// Created target object
	SubtitleAdjustment convertedSubAdj = new SubtitleAdjustment();
	convertedSubAdj.setHearingImpaired(sourceObject.isHearingImpaired());
	for (Subtitle srcSub : sourceObject.getSubtitles())
	{
	    Subtitle convertedSub = new Subtitle();
	    convertedSub.setMedia(srcSub.getMedia());
	    convertedSub.setLanguage(srcSub.getLanguage());
	    convertedSub.setGroup(srcSub.getGroup());
	    convertedSubAdj.getSubtitles().add(convertedSub);
	}
	List<StandardizingChange> changes = config.getAfterQueryingStandardizingService().standardize(convertedSubAdj);
	changes.forEach(c -> log.debug("Standardized after querying: {}", c));
	targetObject = convertedSubAdj;
    }

    private void processParsed() throws Exception
    {
	// Querying

	Release srcRls = sourceObject.getFirstMatchingRelease();
	ListMultimap<MetadataDb<Release>, Release> queryResults = query(srcRls);
	if (isCancelled())
	{
	    return;
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

	if (existingRlss.isEmpty())
	{
	    log.info("No releases found in databases and no standard releases with Scope=ALWAYS");
	    guess();
	}
	else
	{
	    // Distinct, enrich, standardize
	    processedQueryResults = processReleases(existingRlss);

	    // Filter
	    log.debug("Filtering found releases with media={}, tags={}, group={}", srcRls.getMedia(), srcRls.getTags(), srcRls.getGroup());
	    matchingReleases = processedQueryResults.stream()
		    .filter(NamingUtil.filterByNestedName(srcRls, controller.getNamingServiceForFiltering(), controller.getNamingParametersForFiltering(), (Release rls) -> rls.getMedia()))
		    .filter(ReleaseUtil.filterByTags(srcRls.getTags(), config.getReleaseMetaTags()))
		    .filter(ReleaseUtil.filterByGroup(srcRls.getGroup(), false))
		    .collect(Collectors.toList());

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
		    addMatchingRelease(rls, methodInfo);
		}
		if (config.isCompatibilityEnabled())
		{
		    log.debug("Search for compatible releases enabled. Searching");
		    addCompatibleReleases(matchingReleases, processedQueryResults);
		}
		else
		{
		    log.debug("Search for compatible releases disabled");
		}
	    }
	}

	if (isCancelled())
	{
	    return;
	}
	updateProgress(0.75d, 1d);

	// May clean up
	if (config.isDeleteSource())
	{
	    try
	    {
		updateMessage("Deleting source file");
		Files.delete(getSourceFile());
		log.info("Deleted source file {}", getSourceFile());
		updateInfo(ProcessingTaskInfo.of("Source file deleted"));
	    }
	    catch (IOException e)
	    {
		log.warn("Could not delete source file", e);
	    }
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
	Release srcRls = sourceObject.getFirstMatchingRelease();
	if (config.isGuessingEnabled())
	{
	    log.info("Guessing enabled");
	    List<StandardRelease> stdRlss = config.getStandardReleases();
	    Map<Release, StandardRelease> guessedRlss = ReleaseUtil.guessMatchingReleases(srcRls, config.getStandardReleases(), config.getReleaseMetaTags());
	    logReleases(Level.DEBUG, "Guessed releases:", guessedRlss.keySet());
	    for (Map.Entry<Release, StandardRelease> entry : guessedRlss.entrySet())
	    {
		GuessingMethodInfo methodInfo = new GuessingMethodInfo(entry.getValue());
		addMatchingRelease(entry.getKey(), methodInfo);
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

    private void addCompatibleReleases(Collection<Release> matchingRlss, Collection<Release> existingReleases) throws Exception
    {
	// Find compatibles
	CompatibilityService compatibilityService = config.getCompatibilityService();
	Map<Release, CompatibilityInfo> compatibleRlss = compatibilityService.findCompatibles(matchingRlss, existingReleases);

	if(compatibleRlss.isEmpty())
	{
		log.debug("No compatible releases found");
	}
	else{
		log.debug("Compatible releases:");
		compatibleRlss.entrySet().forEach(e -> log.debug(e));

		// Add compatible releases
		for (Map.Entry<Release, CompatibilityInfo> entry : compatibleRlss.entrySet())
		{
		    CompatibilityMethodInfo methodInfo = new CompatibilityMethodInfo(entry.getValue());
		    addMatchingRelease(entry.getKey(), methodInfo);
		}
	}
    }

    private void addMatchingRelease(Release rls, MethodInfo methodInfo) throws Exception
    {
	List<StandardizingChange> changes = config.getAfterQueryingStandardizingService().standardize(rls);
	changes.forEach(c -> log.debug("Standardized after querying: {}", c));

	targetObject.getMatchingReleases().add(rls);
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

    public String name(Object obj)
    {
	return controller.getNamingService().name(obj, config.getNamingParameters());
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

	String fileExtension = IOUtil.splitIntoFilenameAndExtension(srcFile.getFileName().toString())[1];
	Path targetFile = targetDir.resolve(result.getName() + fileExtension);

	IOUtil.waitUntilCompletelyWritten(srcFile, 1, TimeUnit.MINUTES);

	Path newFile = Files.copy(srcFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
	result.addFile(newFile);
	log.debug("Copied {} to {}", srcFile, targetFile);

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
	    if (packResult.failed())
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
    }
}
