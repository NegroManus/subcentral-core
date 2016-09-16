package tests;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

public class TestApp2 extends Application {
	public static void main(String[] args) {
		Application.launch();
	}

	@Override
	public void start(Stage stage) throws Exception {

		TreeView<String> treeView = new TreeView<String>();

		TreeItem<String> root = new TreeItem<String>();

		treeView.setRoot(root);
		treeView.setShowRoot(false);
		final TreeItem<String> item1 = new TreeItem<String>("Item1");

		final TreeItem<String> item1a = new TreeItem<String>("Item1.A");

		item1.getChildren().add(item1a);
		root.getChildren().add(item1);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							item1.getChildren().clear();
							item1.getChildren().add(item1a);
							System.gc();
							System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 + " KB");
						}
					});
				}
			}
		}).start();

		Scene scene = new Scene(VBoxBuilder.create().children(treeView).build());
		stage.setScene(scene);
		stage.show();
	}
}