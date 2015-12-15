package de.subcentral.watcher.controller;

import java.io.IOException;
import java.util.Objects;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Nuke;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.Controller;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class ReleaseViewController extends Controller
{
	// Model
	private final ObservableRelease			release;

	// View
	@FXML
	private TextField						actualNameTextField;
	@FXML
	private CheckBox						preferActualNameCheckbox;
	@FXML
	private ComboBox<ObservableList<Tag>>	tagsComboBox;
	@FXML
	private ComboBox<Group>					groupComboBox;
	@FXML
	private TextField						nfoLinkTextField;
	@FXML
	private Button							nfoLinkBrowseButton;
	@FXML
	private TextArea						nfoTextArea;
	@FXML
	private ListView<Nuke>					nukesListView;
	@FXML
	private VBox							mediaRootPane;

	// Controller features
	private StringBinding					title;

	public ReleaseViewController()
	{
		this(new ObservableRelease());
	}

	public ReleaseViewController(ObservableRelease release)
	{
		this.release = Objects.requireNonNull(release, "release");
	}

	public ObservableRelease getRelease()
	{
		return release;
	}

	public StringBinding titleBinding()
	{
		return title;
	}

	@Override
	protected void initialize() throws Exception
	{
		initTitleBinding();

		actualNameTextField.textProperty().bindBidirectional(release.actualNameProperty());
		preferActualNameCheckbox.selectedProperty().bindBidirectional(release.preferActualNameProperty());

		// Media
		for (ObservableBeanWrapper<? extends Media> media : release.getMedia())
		{
			if (media instanceof ObservableEpisode)
			{
				mediaRootPane.getChildren().add(createEpisodePane((ObservableEpisode) media));
			}
		}

		// Tags
		tagsComboBox.setConverter(SubCentralFxUtil.OBSERVABLE_TAGS_STRING_CONVERTER);
		tagsComboBox.getItems().add(FXCollections.observableArrayList(new Tag("720p"), new Tag("HDTV"), new Tag("x264")));
		tagsComboBox.getItems().add(FXCollections.observableArrayList(new Tag("720p"), new Tag("WEB-DL"), new Tag("DD5.1"), new Tag("H.264")));
		tagsComboBox.getItems().add(FXCollections.observableArrayList(new Tag("HDTV"), new Tag("x264")));

		tagsComboBox.valueProperty().bindBidirectional(release.tagsProperty());

		// Group
		groupComboBox.setConverter(SubCentralFxUtil.GROUP_STRING_CONVERTER);
		groupComboBox.getItems().addAll(new Group("2HD"), new Group("DIMENSION"), new Group("EXCELLENCE"), new Group("KILLERS"), new Group("LOL"), new Group("REMARKABLE"));
		groupComboBox.valueProperty().bindBidirectional(release.groupProperty());

		// Nukes
		nukesListView.itemsProperty().bind(release.nukesProperty());
		// make it not selectable
		// nukesListView.setMouseTransparent(true);
		// nukesListView.setFocusTraversable(false);
		// set custom cell factory to stringify the nukes
		nukesListView.setCellFactory((ListView<Nuke> param) ->
		{
			return new ListCell<Nuke>()
			{
				protected void updateItem(Nuke item, boolean empty)
				{
					super.updateItem(item, empty);

					if (empty || item == null)
					{
						setText(null);
						setGraphic(null);
					}
					else
					{
						setText(new StringBuilder(item.getReason()).append(" [").append(item.getDate()).append(']').toString());
					}
				}
			};
		});

		// NFO
		nfoLinkTextField.textProperty().bind(release.nfoLinkProperty());
		nfoLinkBrowseButton.setOnAction((ActionEvent event) -> System.out.println("browsing info link"));
		nfoTextArea.textProperty().bind(release.nfoProperty());

	}

	private void initTitleBinding()
	{
		title = new StringBinding()
		{
			{
				super.bind(release.nameBinding());
			}

			@Override
			protected String computeValue()
			{
				return new StringBuilder("Release: ").append(release.getName()).toString();
			}
		};
	}

	private static TitledPane createEpisodePane(ObservableEpisode epi) throws IOException
	{
		TitledPane episodePane = new TitledPane();
		episodePane.setMinWidth(0d);
		episodePane.setTextOverrun(OverrunStyle.ELLIPSIS);
		episodePane.setWrapText(true);

		EpisodeViewController epiViewCtrl = new EpisodeViewController(epi);
		episodePane.setContent(FxUtil.loadFromFxml("EpisodeView.fxml", null, null, epiViewCtrl));
		episodePane.textProperty().bind(epiViewCtrl.titleBinding());
		return episodePane;
	}
}
