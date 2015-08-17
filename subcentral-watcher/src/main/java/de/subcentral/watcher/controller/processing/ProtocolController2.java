package de.subcentral.watcher.controller.processing;

import java.util.List;
import java.util.Objects;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Release;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

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
    @FXML
    private ScrollPane fileTransformationRootPane;

    public ProtocolController2(ProcessingTask task)
    {
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
	contentPane.add(createHeadline("Parsed object", false, SettingsController.PARSING_SECTION), 0, rowCounter++);
	TextField parsedObjTxtFld = new TextField(task.name(task.getParsedObject()));
	parsedObjTxtFld.setEditable(false);
	contentPane.add(parsedObjTxtFld, 0, rowCounter++, GridPane.REMAINING, 1);

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
		sb.append('.');
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
	contentPane.add(createHeadline("Releases databases results", false, SettingsController.RELEASE_DBS_SECTION), 0, rowCounter++);

	List<Media> queryObj = task.getParsedObject().getFirstMatchingRelease().getMedia();
	String query = task.name(queryObj);

	// Found releases
	contentPane.add(new Label("Found " + task.getFoundReleases().size() + " releases for \"" + query + "\""), 0, rowCounter++, GridPane.REMAINING, 1);
	for (Release rls : task.getFoundReleases())
	{
	    contentPane.add(new Label(task.name(rls)), 0, rowCounter++, 1, 1);
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
	// lbl.setFont(Font.font(null, FontWeight.BOLD, -1d));
	hbox.getChildren().add(lbl);
	if (settingsSection != null)
	{
	    Hyperlink settingsLink = WatcherFxUtil.createSettingsHyperlink(task.getController().getMainController().getSettingsController(), settingsSection, null);
	    hbox.getChildren().add(settingsLink);
	}
	return hbox;
    }

}