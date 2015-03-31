package de.subcentral.watcher.controller;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.fx.FXUtil;
import de.subcentral.watcher.model.ObservableRelease;

public class ReleaseViewApp extends Application
{
	private ReleaseViewController	ctrl		= new ReleaseViewController(initRelease());

	private BorderPane				rootPane	= new BorderPane();
	private GridPane				releasePane;

	@Override
	public void init() throws Exception
	{
		releasePane = FXUtil.loadFromFxml("ReleaseView.fxml", null, null, ctrl);
		rootPane.setCenter(releasePane);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setScene(new Scene(rootPane));
		primaryStage.titleProperty().bind(ctrl.titleBinding());
		primaryStage.show();
	}

	private static ObservableRelease initRelease()
	{
		Episode epi1 = Episode.createSeasonedEpisode("Psych", 8, 1, "Lock, Stock, Some Smoking Barrels and Burton Guster's Goblet of Fire");
		// Episode epi2 = Episode.createSeasonedEpisode("Psych", 8, 2);
		Release rls = Release.create("Psych.8x01.HDTV.x264-KILLERS", epi1, "KILLERS", "HDTV", "x264");
		// rls.getMedia().add(epi2);
		rls.nukeNow("because of");
		rls.nuke("another nuke reason");
		ObservableRelease obsvRls = new ObservableRelease(rls);
		return obsvRls;
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
