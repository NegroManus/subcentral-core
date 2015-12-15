package de.subcentral.watcher.controller;

import java.util.Objects;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.fx.Controller;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.converter.IntegerStringConverter;

public class EpisodeViewController extends Controller
{
	// Model
	private final ObservableEpisode	episode;

	// View
	@FXML
	private TextField				seriesNameTextField;
	@FXML
	private ComboBox<String>		seriesTypeComboBox;
	@FXML
	private TextField				seasonNumberTextField;
	@FXML
	private TextField				seasonTitleTextField;
	@FXML
	private TextField				numberInSeasonTextField;
	@FXML
	private TextField				titleTextField;
	@FXML
	private TextField				numberInSeriesTextField;
	@FXML
	private DatePicker				datePicker;

	// Controller features
	private StringBinding			title;

	public EpisodeViewController(ObservableEpisode episode)
	{
		this.episode = Objects.requireNonNull(episode, "episode");
	}

	public ObservableEpisode getEpisode()
	{
		return episode;
	}

	public StringBinding titleBinding()
	{
		return title;
	}

	@Override
	protected void initialize() throws Exception
	{
		initTitleBinding();

		seriesTypeComboBox.setEditable(false);
		seriesTypeComboBox.getItems().addAll(Series.TYPE_SEASONED, Series.TYPE_MINI_SERIES, Series.TYPE_DATED);

		IntegerStringConverter intStringConverter = new IntegerStringConverter();

		seriesNameTextField.textProperty().bindBidirectional(episode.seriesNameProperty());
		seriesTypeComboBox.valueProperty().bindBidirectional(episode.seriesTypeProperty());
		seasonNumberTextField.textProperty().bindBidirectional(episode.seasonNumberProperty(), intStringConverter);
		seasonTitleTextField.textProperty().bindBidirectional(episode.seasonTitleProperty());
		numberInSeasonTextField.textProperty().bindBidirectional(episode.numberInSeasonProperty(), intStringConverter);
		titleTextField.textProperty().bindBidirectional(episode.titleProperty());
		numberInSeriesTextField.textProperty().bindBidirectional(episode.numberInSeriesProperty(), intStringConverter);
		datePicker.valueProperty().bindBidirectional(episode.dateProperty());
	}

	private void initTitleBinding()
	{
		title = new StringBinding()
		{
			{
				super.bind(episode.nameBinding());
			}

			@Override
			protected String computeValue()
			{
				return new StringBuilder("Episode: ").append(episode.getName()).toString();
			}
		};
	}
}
