package de.subcentral.watcher.controller.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.correct.Correction;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.Controller;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.settings.SettingsController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DetailsController extends Controller
{
	// Model
	private final ProcessingTask		task;

	// View
	@FXML
	private Label						sourceFileLabel;
	@FXML
	private Accordion					sectionsAccordion;
	@FXML
	private ScrollPane					parsingDetailsRootPane;
	@FXML
	private ScrollPane					releaseDetailsRootPane;

	// Control
	private final ProcessingController	processingController;

	public DetailsController(ProcessingController processingController, ProcessingTask task)
	{
		this.processingController = Objects.requireNonNull(processingController, "processingController");
		this.task = Objects.requireNonNull(task, "task");
	}

	@Override
	protected void initialize() throws Exception
	{
		updateHeader();
		updateParsingDetailsSection();
		updateReleaseDetailsSection();

		// Expand release details
		sectionsAccordion.setExpandedPane(sectionsAccordion.getPanes().get(1));
	}

	private void updateHeader()
	{
		sourceFileLabel.setText(task.getSourceFile().getFileName().toString());
		sourceFileLabel.setTooltip(new Tooltip(task.getSourceFile().toString()));
	}

	private void updateParsingDetailsSection()
	{
		VBox vbox = createVBox();

		// Parsed object
		SubtitleRelease parsedObj = task.getParsedObject();

		vbox.getChildren().add(createHeadline("Parsed subtitle", SettingsController.PARSING_SECTION));

		if (parsedObj != null)
		{
			Node parsedSubNode = createParsedSubtitleNode(parsedObj, task::generateDisplayName);
			vbox.getChildren().add(parsedSubNode);

			// Corrections
			vbox.getChildren().add(createSeparator(true));
			vbox.getChildren().add(createHeadline("Corrections", SettingsController.CORRECTION_SECTION));
			if (task.getParsingCorrections().isEmpty())
			{
				vbox.getChildren().add(new Label("No corrections"));
			}
			else
			{
				Node correctionsNode = createCorrectionsNode(task.getParsingCorrections());
				vbox.getChildren().add(correctionsNode);
			}
		}
		else
		{
			vbox.getChildren().add(new Label("Filename could not be parsed"));
		}

		// Add to root pane
		parsingDetailsRootPane.setContent(vbox);
	}

	private void updateReleaseDetailsSection()
	{
		if (task.getParsedObject() != null)
		{
			VBox vbox = createVBox();

			List<ProcessingResult> results = new ArrayList<>(task.getResults());

			// Listed releases
			vbox.getChildren().add(createHeadline("Listed releases", SettingsController.RELEASE_DBS_SECTION));

			Release queryRls = task.getParsedObject().getFirstMatchingRelease();
			Set<String> mediaNames = task.generateFilteringDisplayNames(queryRls.getMedia());

			vbox.getChildren().add(new Label(
					task.getListedReleases().size() + " release(s) were found for " + StringUtil.COMMA_JOINER.join(mediaNames.stream().map((String name) -> StringUtil.quoteString(name)).iterator())));
			if (!task.getListedReleases().isEmpty())
			{
				vbox.getChildren().add(createSeparator(false));
				vbox.getChildren().add(new Label("Match criteria:"));
				vbox.getChildren().add(createMatchCriteriaNode(queryRls, mediaNames));

				vbox.getChildren().add(createSeparator(false));

				int rows = task.getListedReleases().size();
				Release[] releases = new Release[rows];
				ProcessingResultInfo[] infos = new ProcessingResultInfo[rows];
				for (int i = 0; i < rows; i++)
				{
					Release rls = task.getListedReleases().get(i);
					ProcessingResultInfo info = null;
					ListIterator<ProcessingResult> iter = results.listIterator();
					while (iter.hasNext())
					{
						ProcessingResult r = iter.next();
						if (rls.equals(r.getRelease()))
						{
							info = (ProcessingResultInfo) r.getInfo();
							iter.remove();
							break;
						}
					}
					releases[i] = rls;
					infos[i] = info;
				}
				vbox.getChildren().add(createReleaseListGridPane(releases, infos));
			}

			if (!results.isEmpty())
			{
				// Guessed releases
				vbox.getChildren().add(createSeparator(true));
				vbox.getChildren().add(createHeadline("Guessed releases", SettingsController.RELEASE_GUESSING_SECTION));
				if (!task.getConfig().isGuessingEnabled())
				{
					vbox.getChildren().add(new Label("Guessing disabled"));
				}
				else
				{
					int rows = results.size();
					Release[] releases = new Release[rows];
					ProcessingResultInfo[] infos = new ProcessingResultInfo[rows];
					for (int i = 0; i < rows; i++)
					{
						ProcessingResult result = results.get(i);
						releases[i] = result.getRelease();
						infos[i] = (ProcessingResultInfo) result.getInfo();
					}
					vbox.getChildren().add(createReleaseListGridPane(releases, infos));
				}
			}

			releaseDetailsRootPane.setContent(vbox);
		}
		else
		{
			parsingDetailsRootPane.setContent(new Label("Filename could not be parsed"));
		}
	}

	private static VBox createVBox()
	{
		VBox vbox = new VBox();
		vbox.setSpacing(3d);
		return vbox;
	}

	private static Node createParsedSubtitleNode(SubtitleRelease subRls, Function<Object, String> printer)
	{
		Subtitle sub = subRls.getFirstSubtitle();
		Release rls = subRls.getFirstMatchingRelease();

		String[] keys = { "Computed name:", "Media:", "Release tags:", "Release group:", "Subtitle language:", "Subtitle tags:", "Subtitle source:", "Subtitle group:" };
		String[] values = {
				printer.apply(subRls),
				printer.apply(rls.getMedia()),
				Tag.listToString(rls.getTags()),
				rls.getGroup() != null ? rls.getGroup().toString() : "",
				sub.getLanguage() != null ? sub.getLanguage() : "",
				Tag.listToString(subRls.getTags()),
				sub.getSource() != null ? sub.getSource() : "",
				sub.getGroup() != null ? sub.getGroup().toString() : "" };
		GridPane pane = createKeyValueGridPane(keys, values);
		return pane;
	}

	private static Node createCorrectionsNode(List<Correction> corrections)
	{
		String[] keys = new String[corrections.size()];
		String[] values = new String[corrections.size()];

		for (int i = 0; i < corrections.size(); i++)
		{
			Correction c = corrections.get(i);

			StringBuilder key = new StringBuilder();
			key.append(WatcherFxUtil.beanTypeToString(c.getBean().getClass()));
			key.append(' ');
			key.append(c.getPropertyName());
			key.append(": ");
			keys[i] = key.toString();

			StringBuilder val = new StringBuilder();
			val.append(c.getOldValue());
			val.append(" -> ");
			val.append(c.getNewValue());
			values[i] = val.toString();
		}

		GridPane pane = createKeyValueGridPane(keys, values);
		return pane;
	}

	private static Node createMatchCriteriaNode(Release rls, Set<String> mediaNames)
	{
		int rows = 2 + mediaNames.size();
		String[] keys = new String[rows];
		String[] values = new String[rows];
		int index = 0;

		Iterator<String> namesIter = mediaNames.iterator();
		keys[index] = "Media:";
		values[index] = namesIter.hasNext() ? namesIter.next() : "";
		index++;
		while (namesIter.hasNext())
		{
			keys[index] = "";
			values[index] = namesIter.next();
			index++;
		}

		keys[index] = "Release tags:";
		values[index] = Tag.listToString(rls.getTags());

		index++;
		keys[index] = "Release group:";
		values[index] = rls.getGroup() != null ? rls.getGroup().toString() : "";

		return createKeyValueGridPane(keys, values);
	}

	private static GridPane createKeyValueGridPane(String[] keys, String[] values)
	{
		GridPane gridPane = new GridPane();
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHgrow(Priority.NEVER);
		column1.setHalignment(HPos.RIGHT);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHgrow(Priority.SOMETIMES);
		gridPane.getColumnConstraints().addAll(column1, column2);
		gridPane.setVgap(3d);
		gridPane.setHgap(3d);

		int rowIndex = 0;
		for (int i = 0; i < keys.length; i++)
		{
			addKeyValueRow(gridPane, rowIndex++, keys[i], values[i]);
		}

		return gridPane;
	}

	private GridPane createReleaseListGridPane(Release[] releases, ProcessingResultInfo[] infos)
	{
		GridPane gridPane = new GridPane();
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setHgrow(Priority.NEVER);
		column1.setHalignment(HPos.CENTER);
		column1.setPrefWidth(30d);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHgrow(Priority.SOMETIMES);
		gridPane.getColumnConstraints().addAll(column1, column2);
		gridPane.setVgap(3d);
		gridPane.setHgap(3d);

		int rowIndex = 0;
		for (int i = 0; i < releases.length; i++)
		{
			addReleaseRow(gridPane, rowIndex++, releases[i], infos[i]);
		}

		return gridPane;
	}

	private static void addKeyValueRow(GridPane gridPane, int rowIndex, String key, String value)
	{
		gridPane.add(createKeyLabel(key), 0, rowIndex);
		gridPane.add(new Label(value), 1, rowIndex);
	}

	private static Label createKeyLabel(String key)
	{
		Label lbl = new Label(key);
		lbl.setTextFill(Color.GRAY);
		return lbl;
	}

	private Separator createSeparator(boolean topMargin)
	{
		Separator sep = new Separator();
		if (topMargin)
		{
			VBox.setMargin(sep, new Insets(15d, 0d, 0d, 0d));
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

	private void addReleaseRow(GridPane gridPane, int rowIndex, Release rls, ProcessingResultInfo info)
	{
		// Result type
		Node resultTypeNode;
		if (info != null)
		{
			switch (info.getResultType())
			{
				case LISTED:
					resultTypeNode = WatcherFxUtil.createMatchLabel(rls);
					break;
				case LISTED_MANUAL:
					resultTypeNode = WatcherFxUtil.createManualLabel();
					break;
				case LISTED_COMPATIBLE:
					// fall through
				case GUESSED_COMPATIBLE:
					resultTypeNode = WatcherFxUtil.createCompatibilityLabel(info.getCompatibilityInfo(), (Release r) -> task.generateDisplayName(r), false);
					break;
				case GUESSED:
					resultTypeNode = WatcherFxUtil.createGuessedLabel(info.getStandardRelease(), (Release r) -> task.generateDisplayName(r));
					break;
				default:
					resultTypeNode = new Label(info.getResultType().toString());
			}
		}
		else
		{
			resultTypeNode = createAddListedManuallyLink(rls);
		}
		gridPane.add(resultTypeNode, 0, rowIndex);

		// Name & Info
		HBox rlsHbox = new HBox();
		rlsHbox.setSpacing(5d);
		rlsHbox.setAlignment(Pos.CENTER_LEFT);

		// Name
		rlsHbox.getChildren().add(new Label(task.generateDisplayName(rls)));

		// Info
		Hyperlink furtherInfoLink = WatcherFxUtil.createFurtherInfoHyperlink(rls, processingController.getMainController().getCommonExecutor());
		if (furtherInfoLink != null)
		{
			rlsHbox.getChildren().add(furtherInfoLink);
		}

		// nuke
		rlsHbox.getChildren().addAll(WatcherFxUtil.createNukedLabels(rls));

		// meta tags
		Label metaTagsLbl = WatcherFxUtil.createMetaTaggedLabel(rls, task.getConfig().getReleaseMetaTags());
		if (metaTagsLbl != null)
		{
			rlsHbox.getChildren().add(metaTagsLbl);
		}

		gridPane.add(rlsHbox, 1, rowIndex);
	}

	private Node createAddListedManuallyLink(Release release)
	{
		Hyperlink btn = new Hyperlink("", new ImageView(FxUtil.loadImg("add_16.png")));
		btn.setUnderline(false);
		btn.setOpacity(0.5d);
		btn.setTooltip(new Tooltip("Add this release to the matching releases"));
		btn.setOnAction((ActionEvent evt) ->
		{
			btn.setDisable(true);
			btn.setText("\u2026");
			btn.setGraphic(null);
			Task<Void> addReleaseTask = new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					// Thread.sleep(2000L);
					task.addReleaseToResult(release, ProcessingResultInfo.listedManual());
					return null;
				}

				@Override
				protected void succeeded()
				{
					updateReleaseDetailsSection();
				}

				@Override
				protected void failed()
				{
					btn.setDisable(false);
				}
			};
			processingController.getMainController().getCommonExecutor().submit(addReleaseTask);
		});
		return btn;
	}
}