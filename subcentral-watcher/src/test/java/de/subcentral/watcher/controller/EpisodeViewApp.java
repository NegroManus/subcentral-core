package de.subcentral.watcher.controller;

import java.time.LocalDate;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.naming.EpisodeNamer;
import de.subcentral.fx.FXUtil;
import de.subcentral.watcher.model.ObservableEpisode;

public class EpisodeViewApp extends Application
{
	private EpisodeViewController	ctrl		= new EpisodeViewController(initEpisode());

	private BorderPane				rootPane	= new BorderPane();
	private GridPane				episodePane;

	@Override
	public void init() throws Exception
	{
		episodePane = FXUtil.loadFromFxml("EpisodeView.fxml", null, null, ctrl);
		rootPane.setCenter(episodePane);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setScene(new Scene(rootPane));
		primaryStage.titleProperty().bind(ctrl.titleBinding());
		primaryStage.show();
	}

	private static ObservableEpisode initEpisode()
	{
		Episode epi = Episode.createSeasonedEpisode("Psych", 8, 1, "Lock, Stock, Some Smoking Barrels and Burton Guster's Goblet of Fire");
		epi.setDate(LocalDate.of(2014, 1, 8));
		epi.setNumberInSeries(111);
		ObservableEpisode obsEpi = new ObservableEpisode(epi);
		obsEpi.getNamingParameters().put(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE);
		return obsEpi;
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
