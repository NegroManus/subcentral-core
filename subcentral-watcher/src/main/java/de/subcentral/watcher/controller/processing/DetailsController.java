package de.subcentral.watcher.controller.processing;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.subcentral.core.correction.Correction;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleVariant;
import de.subcentral.core.util.StringUtil;
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
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DetailsController extends AbstractController
{
	// Model
	private final ProcessingTask		task;

	// View
	@FXML
	private Label						sourceFileLabel;
	@FXML
	private Accordion					sectionsAccordion;
	@FXML
	private ScrollPane					parsingResultsRootPane;
	@FXML
	private ScrollPane					releaseResultsRootPane;

	// Control
	private final ProcessingController	processingController;

	public DetailsController(ProcessingController processingController, ProcessingTask task)
	{
		this.processingController = Objects.requireNonNull(processingController, "processingController");
		this.task = Objects.requireNonNull(task, "task");
	}

	@Override
	protected void doInitialize() throws Exception
	{
		initHeader();

		// Expand release details
		sectionsAccordion.setExpandedPane(sectionsAccordion.getPanes().get(1));
		initParsingResultsSection();
		initReleaseResultsSection();
	}

	private void initHeader()
	{
		sourceFileLabel.setText(task.getSourceFile().getFileName().toString());
	}

	private void initParsingResultsSection()
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
		// contentPane.setGridLinesVisible(true);

		// Parsed object
		SubtitleVariant subAdj = task.getParsedObject();

		contentPane.add(createHeadline("Parsed object", SettingsController.PARSING_SECTION), 0, rowCounter++);

		if (subAdj != null)
		{
			contentPane.add(new Label(task.generateDisplayName(subAdj)), 0, rowCounter++, GridPane.REMAINING, 1);

			Subtitle sub = subAdj.getFirstSubtitle();
			Release rls = subAdj.getFirstMatchingRelease();
			contentPane.add(new Label("Media:"), 0, rowCounter);
			contentPane.add(new Label(task.generateDisplayName(rls.getMedia())), 1, rowCounter++);
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

			contentPane.add(createSeparator(true), 0, rowCounter++, GridPane.REMAINING, 1);

			// Corrections
			contentPane.add(createHeadline("Corrections", SettingsController.CORRECTION_SECTION), 0, rowCounter++, GridPane.REMAINING, 1);
			if (task.getParsingCorrections().isEmpty())
			{
				contentPane.add(new Label("No corrections"), 0, rowCounter++, GridPane.REMAINING, 1);
			}
			else
			{
				for (Correction c : task.getParsingCorrections())
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
		}
		else
		{
			contentPane.add(new Label("Could not parse the filename"), 0, rowCounter++, GridPane.REMAINING, 1);
		}

		// Add to root pane
		parsingResultsRootPane.setContent(contentPane);
	}

	private void initReleaseResultsSection()
	{
		if (task.getParsedObject() == null)
		{
			return;
		}

		int rowCounter = 0;

		GridPane contentPane = new GridPane();
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHgrow(Priority.NEVER);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHgrow(Priority.SOMETIMES);
		contentPane.getColumnConstraints().addAll(column1, column2);
		contentPane.setVgap(3d);
		contentPane.setHgap(3d);
		// contentPane.setGridLinesVisible(true);

		// Release DBs
		contentPane.add(createHeadline("Listed releases", SettingsController.RELEASE_DBS_SECTION), 0, rowCounter++, GridPane.REMAINING, 1);

		Release queryRls = task.getParsedObject().getFirstMatchingRelease();
		List<Media> queryMedia = queryRls.getMedia();
		Set<String> mediaNames = task.generateFilteringDisplayNames(queryMedia);

		// Found releases
		contentPane.add(new Label(
				task.getFoundReleases().size() + " release(s) were found for " + StringUtil.COMMA_JOINER.join(mediaNames.stream().map((String name) -> StringUtil.quoteString(name)).iterator())),
				0,
				rowCounter++,
				GridPane.REMAINING,
				1);
		for (Release rls : task.getFoundReleases())
		{
			contentPane.add(createReleaseHBox(rls), 0, rowCounter++, GridPane.REMAINING, 1);
		}

		if (!task.getFoundReleases().isEmpty())
		{
			// Matching releases
			contentPane.add(createSeparator(true), 0, rowCounter++, GridPane.REMAINING, 1);
			contentPane.add(createHeadline("Matching releases", null), 0, rowCounter++, GridPane.REMAINING, 1);

			Iterator<String> namesIter = mediaNames.iterator();

			contentPane.add(new Label("Filter criteria"), 0, rowCounter++, GridPane.REMAINING, 1);
			contentPane.add(new Label("- Media:"), 0, rowCounter);
			contentPane.add(new Label((namesIter.hasNext() ? namesIter.next() : "")), 1, rowCounter++);
			while (namesIter.hasNext())
			{
				contentPane.add(new Label(""), 0, rowCounter);
				contentPane.add(new Label(namesIter.next()), 1, rowCounter++);
			}
			contentPane.add(new Label("- Release tags:"), 0, rowCounter);
			contentPane.add(new Label(Tag.listToString(queryRls.getTags())), 1, rowCounter++);
			contentPane.add(new Label("- Release group:"), 0, rowCounter);
			contentPane.add(new Label(queryRls.getGroup() != null ? queryRls.getGroup().toString() : ""), 1, rowCounter++);

			contentPane.add(createSeparator(false), 0, rowCounter++, GridPane.REMAINING, 1);

			contentPane.add(new Label(task.getMatchingReleases().size() + " release(s) match the filter"), 0, rowCounter++, GridPane.REMAINING, 1);
			for (Release rls : task.getMatchingReleases())
			{
				contentPane.add(createReleaseHBox(rls), 0, rowCounter++, GridPane.REMAINING, 1);
			}
		}
		if (task.getMatchingReleases().isEmpty())
		{
			// Guessed releases
			contentPane.add(createSeparator(true), 0, rowCounter++, GridPane.REMAINING, 1);
			contentPane.add(createHeadline("Guessed releases", SettingsController.RELEASE_GUESSING_SECTION), 0, rowCounter++, GridPane.REMAINING, 1);
			if (!task.getConfig().isGuessingEnabled())
			{
				contentPane.add(new Label("Guessing disabled"), 0, rowCounter++, GridPane.REMAINING, 1);
			}
			else
			{
				for (Map.Entry<Release, StandardRelease> entry : task.getGuessedReleases().entrySet())
				{
					HBox releaseHBox = createReleaseHBox(entry.getKey());
					Label guessedLbl = WatcherFxUtil.createGuessedLabel(entry.getValue(), (Release rls) -> task.generateDisplayName(rls));
					releaseHBox.getChildren().add(guessedLbl);
					contentPane.add(releaseHBox, 0, rowCounter++, GridPane.REMAINING, 1);
				}
			}
		}

		if (!(task.getMatchingReleases().isEmpty() && task.getGuessedReleases().isEmpty()))
		{
			// Compatible releases
			contentPane.add(createSeparator(true), 0, rowCounter++, GridPane.REMAINING, 1);
			contentPane.add(createHeadline("Compatible releases", SettingsController.RELEASE_COMPATIBILITY_SECTION), 0, rowCounter++, GridPane.REMAINING, 1);
			contentPane.add(new Label(task.getCompatibleReleases().size() + " release(s) are compatible"), 0, rowCounter++, GridPane.REMAINING, 1);
			for (Map.Entry<Release, CompatibilityInfo> c : task.getCompatibleReleases().entrySet())
			{
				HBox releaseHBox = createReleaseHBox(c.getKey());
				Label compLbl = WatcherFxUtil.createCompatibilityLabel(c.getValue(), (Release rls) -> task.generateDisplayName(rls));
				releaseHBox.getChildren().add(compLbl);
				contentPane.add(releaseHBox, 0, rowCounter++, GridPane.REMAINING, 1);
			}
		}

		releaseResultsRootPane.setContent(contentPane);
	}

	private Separator createSeparator(boolean topMargin)
	{
		Separator sep = new Separator();
		if (topMargin)
		{
			GridPane.setMargin(sep, new Insets(15d, 0d, 0d, 0d));
		}
		return sep;
	}

	private Node createHeadline(String headline, String settingsSection)
	{
		HBox hbox = new HBox();
		hbox.setSpacing(5d);
		hbox.setAlignment(Pos.BOTTOM_LEFT);
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
		hbox.setSpacing(10d);
		hbox.setAlignment(Pos.CENTER_LEFT);

		Hyperlink furtherInfoLink = WatcherFxUtil.createFurtherInfoHyperlink(rls, processingController.getMainController().getCommonExecutor());
		if (furtherInfoLink != null)
		{
			hbox.getChildren().add(furtherInfoLink);
		}

		hbox.getChildren().add(new Label(task.generateDisplayName(rls)));

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