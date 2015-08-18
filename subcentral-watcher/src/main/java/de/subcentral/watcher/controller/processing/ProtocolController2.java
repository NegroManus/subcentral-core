package de.subcentral.watcher.controller.processing;

import java.util.List;
import java.util.Objects;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.standardizing.StandardizingChange;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.settings.SettingsController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProtocolController2 extends AbstractController
{
    // Model
    private final ProcessingTask task;

    // View
    @FXML
    private Label      sourceFileLabel;
    @FXML
    private Accordion  sectionsAccordion;
    @FXML
    private ScrollPane parsingRootPane;
    @FXML
    private ScrollPane releaseMetadataRootPane;

    // Control
    private final ProcessingController processingController;

    public ProtocolController2(ProcessingController processingController, ProcessingTask task)
    {
	this.processingController = Objects.requireNonNull(processingController, "processingController");
	this.task = Objects.requireNonNull(task, "task");
    }

    @Override
    protected void doInitialize() throws Exception
    {
	initHeader();

	// Sections
	sectionsAccordion.setExpandedPane(sectionsAccordion.getPanes().get(0));
	initParsingSection();
	initReleaseMetadataSection();
    }

    private void initHeader()
    {
	sourceFileLabel.setText(task.getSourceFile().getFileName().toString());
    }

    private void initParsingSection()
    {
	int rowCounter = 0;

	GridPane contentPane = new GridPane();
	ColumnConstraints column1 = new ColumnConstraints();
	column1.setHgrow(Priority.NEVER);
	ColumnConstraints column2 = new ColumnConstraints();
	column2.setHgrow(Priority.SOMETIMES);
	contentPane.getColumnConstraints().addAll(column1, column2);
	contentPane.setVgap(3d);
	contentPane.setHgap(3d);
	// parsingPane.setGridLinesVisible(true);

	// Parsed object
	SubtitleAdjustment subAdj = task.getParsedObject();
	Subtitle sub = subAdj.getFirstSubtitle();
	Release rls = subAdj.getFirstMatchingRelease();

	contentPane.add(createHeadline("Parsed object", false, SettingsController.PARSING_SECTION), 0, rowCounter++);
	contentPane.add(new Label(task.name(subAdj)), 0, rowCounter++, GridPane.REMAINING, 1);
	contentPane.add(new Label("Media:"), 0, rowCounter);
	contentPane.add(new Label(task.name(rls.getMedia())), 1, rowCounter++);
	contentPane.add(new Label("Release tags:"), 0, rowCounter);
	contentPane.add(new Label(Tag.listToString(rls.getTags())), 1, rowCounter++);
	contentPane.add(new Label("Release group:"), 0, rowCounter);
	contentPane.add(new Label(rls.getGroup() != null ? rls.getGroup().toString() : ""), 1, rowCounter++);
	contentPane.add(new Label("Subtitle language:"), 0, rowCounter);
	contentPane.add(new Label(sub.getLanguage() != null ? sub.getLanguage() : ""), 1, rowCounter++);
	contentPane.add(new Label("Subtitle tags:"), 0, rowCounter);
	contentPane.add(new Label(Tag.listToString(subAdj.getTags())), 1, rowCounter++);
	contentPane.add(new Label("Subtitle source:"), 0, rowCounter);
	contentPane.add(new Label(sub.getSource() != null ? sub.getSource() : ""), 1, rowCounter++);
	contentPane.add(new Label("Subtitle group:"), 0, rowCounter);
	contentPane.add(new Label(sub.getGroup() != null ? sub.getGroup().toString() : ""), 1, rowCounter++);

	// Corrections
	contentPane.add(createHeadline("Corrections", true, SettingsController.CORRECTION_SECTION), 0, rowCounter++, GridPane.REMAINING, 1);
	if (task.getParsingCorrections().isEmpty())
	{
	    contentPane.add(new Label("No corrections"), 0, rowCounter++, GridPane.REMAINING, 1);
	}
	else
	{
	    for (StandardizingChange c : task.getParsingCorrections())
	    {
		StringBuilder sb = new StringBuilder();
		sb.append(WatcherFxUtil.beanTypeToString(c.getBean().getClass()));
		sb.append(' ');
		sb.append(c.getPropertyName());
		sb.append(": ");
		contentPane.add(new Label(sb.toString()), 0, rowCounter, 1, 1);

		sb = new StringBuilder();
		sb.append(c.getOldValue());
		sb.append(" -> ");
		sb.append(c.getNewValue());
		contentPane.add(new Label(sb.toString()), 1, rowCounter++, GridPane.REMAINING, 1);
	    }
	}

	// Add to root pane
	parsingRootPane.setContent(contentPane);
    }

    private void initReleaseMetadataSection()
    {
	int rowCounter = 0;

	GridPane contentPane = new GridPane();
	ColumnConstraints column1 = new ColumnConstraints();
	column1.setHgrow(Priority.SOMETIMES);
	contentPane.getColumnConstraints().add(column1);
	contentPane.setVgap(3d);

	// Release DBs
	contentPane.add(createHeadline("Release databases results", false, SettingsController.RELEASE_DBS_SECTION), 0, rowCounter++);

	Release queryRls = task.getParsedObject().getFirstMatchingRelease();
	List<Media> queryMedia = task.getParsedObject().getFirstMatchingRelease().getMedia();
	String mediaName = task.name(queryMedia);

	// Found releases
	contentPane.add(new Label(task.getFoundReleases().size() + " release(s) were found for \"" + mediaName + "\""), 0, rowCounter++);
	for (Release rls : task.getFoundReleases())
	{
	    contentPane.add(createReleaseHBox(rls), 0, rowCounter++, 1, 1);
	}

	// Matching releases
	if (!task.getFoundReleases().isEmpty())
	{
	    contentPane.add(createHeadline("Matching releases", true, null), 0, rowCounter++);
	    contentPane.add(new Label(task.getMatchingReleases().size() + " release(s) matched the filter criteria"), 0, rowCounter++);
	    contentPane.add(new Label("- Media: " + mediaName), 0, rowCounter++);
	    contentPane.add(new Label("- Release tags:  " + Tag.listToString(queryRls.getTags())), 0, rowCounter++, GridPane.REMAINING, 1);
	    contentPane.add(new Label("- Release group: " + (queryRls.getGroup() != null ? queryRls.getGroup().toString() : "")), 0, rowCounter++);

	    for (Release rls : task.getMatchingReleases())
	    {
		contentPane.add(createReleaseHBox(rls), 0, rowCounter++);
	    }
	}

	releaseMetadataRootPane.setContent(contentPane);
    }

    private Node createHeadline(String headline, boolean topMargin, String settingsSection)
    {
	HBox hbox = new HBox();
	hbox.setSpacing(5d);
	hbox.setAlignment(Pos.BOTTOM_LEFT);
	if (topMargin)
	{
	    GridPane.setMargin(hbox, new Insets(10d, 0d, 0d, 0d));
	}

	Label lbl = new Label(headline);
	lbl.setUnderline(true);
	lbl.setFont(Font.font(null, FontWeight.BOLD, -1d));
	hbox.getChildren().add(lbl);
	if (settingsSection != null)
	{
	    Hyperlink settingsLink = WatcherFxUtil.createSettingsHyperlink(task.getController().getMainController().getSettingsController(), settingsSection, null);
	    hbox.getChildren().add(settingsLink);
	}
	return hbox;
    }

    private HBox createReleaseHBox(Release rls)
    {
	HBox hbox = new HBox();
	hbox.setSpacing(5d);
	hbox.setAlignment(Pos.CENTER_LEFT);

	Hyperlink furtherInfoLink = WatcherFxUtil.createFurtherInfoHyperlink(rls, processingController.getMainController().getCommonExecutor());
	if (furtherInfoLink != null)
	{
	    hbox.getChildren().add(furtherInfoLink);
	}

	hbox.getChildren().add(new Label(task.name(rls)));

	// nuke
	Label nukedLbl = WatcherFxUtil.createNukedLabel(rls);
	if (nukedLbl != null)
	{
	    hbox.getChildren().add(nukedLbl);
	}

	// meta tags
	Label metaTagsLbl = WatcherFxUtil.createMetaTaggedLabel(rls, task.getConfig().getReleaseMetaTags());
	if (metaTagsLbl != null)
	{
	    hbox.getChildren().add(metaTagsLbl);
	}
	return hbox;
    }

    public ProcessingTask getTask()
    {
	return task;
    }

    public ProcessingController getProcessingController()
    {
	return processingController;
    }

}