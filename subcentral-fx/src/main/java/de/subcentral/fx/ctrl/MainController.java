package de.subcentral.fx.ctrl;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.stage.Stage;

public abstract class MainController extends ExecutingController {
	// View
	protected final Stage	primaryStage;
	// Actions
	protected TaskExecutor	executor;

	// Initialization
	public MainController(Stage primaryStage) {
		this.primaryStage = Objects.requireNonNull(primaryStage, "primaryStage");
	}

	protected final void initExecutor(TaskExecutor executor) {
		this.executor = executor;
	}

	@Override
	public TaskExecutor getExecutor() {
		return executor;
	}

	// Getter, Setter
	@Override
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	// Shutdown
	public void exit() {
		// Explicit exit: Causes Application.stop()
		Platform.exit();
	}

	@Override
	public void shutdown() throws Exception {
		if (executor != null) {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.MINUTES);
		}
	}
}
