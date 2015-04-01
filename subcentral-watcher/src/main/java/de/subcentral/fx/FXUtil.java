package de.subcentral.fx;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.ExceptionUtil;
import de.subcentral.watcher.settings.WatcherSettings.PatternMode;

public class FXUtil
{
	private static final Logger							log									= LogManager.getLogger(FXUtil.class);

	public static final StringConverter<Path>			PATH_STRING_CONVERTER				= initPathStringConverter();
	public static final StringConverter<Locale>			LOCALE_DISPLAY_NAME_CONVERTER		= initLocaleDisplayNameConverter();
	public static final StringConverter<List<Locale>>	LOCALE_LIST_DISPLAY_NAME_CONVERTER	= initLocaleListDisplayNameConverter();
	public static final Comparator<Locale>				LOCALE_DISPLAY_NAME_COMPARATOR		= initLocaleDisplayNameComparator();
	public static final EventHandler<WorkerStateEvent>	DEFAULT_TASK_FAILED_HANDLER			= initDefaultTaskFailedHandler();

	private static StringConverter<Path> initPathStringConverter()
	{
		return new StringConverter<Path>()
		{
			@Override
			public String toString(Path path)
			{
				if (path == null)
				{
					return "";
				}
				return path.toString();
			}

			@Override
			public Path fromString(String string)
			{
				if (StringUtils.isBlank(string))
				{
					return null;
				}
				return Paths.get(string);
			}
		};
	}

	private static StringConverter<Locale> initLocaleDisplayNameConverter()
	{
		return new StringConverter<Locale>()
		{
			@Override
			public String toString(Locale locale)
			{
				if (locale == null)
				{
					return "";
				}
				return locale.getDisplayName();
			}

			@Override
			public Locale fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	private static StringConverter<List<Locale>> initLocaleListDisplayNameConverter()
	{
		return new StringConverter<List<Locale>>()
		{
			@Override
			public String toString(List<Locale> locales)
			{
				if (locales == null)
				{
					return "";
				}
				StringJoiner joiner = new StringJoiner(", ");
				for (Locale l : locales)
				{
					if (l == null)
					{
						continue;
					}
					joiner.add(l.getDisplayName());
				}
				return joiner.toString();
			}

			@Override
			public List<Locale> fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	private static Comparator<Locale> initLocaleDisplayNameComparator()
	{
		return new Comparator<Locale>()
		{

			@Override
			public int compare(Locale o1, Locale o2)
			{
				// nulls first
				if (o1 == null)
				{
					return o2 == null ? 0 : -1;
				}
				if (o2 == null)
				{
					return 1;
				}
				return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
			}
		};
	}

	private static EventHandler<WorkerStateEvent> initDefaultTaskFailedHandler()
	{
		return (WorkerStateEvent evt) -> {
			StringBuilder sb = new StringBuilder();
			sb.append("Execution of ");
			if (!evt.getSource().getTitle().isEmpty())
			{
				sb.append(" the background task \"");
				sb.append(evt.getSource().getTitle());
				sb.append('"');
			}
			else
			{
				sb.append("a background task");
			}
			sb.append(" failed");
			String msg = sb.toString();

			Throwable exc = evt.getSource().getException();

			log.error(msg, exc);
			Alert alert = FXUtil.createExceptionAlert(msg, msg, exc);
			alert.show();
		};
	}

	public static void browse(URI uri, ExecutorService executor)
	{
		browse(uri, executor, DEFAULT_TASK_FAILED_HANDLER);
	}

	public static void browse(URI uri, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler)
	{
		Task<Void> browseTask = new Task<Void>()
		{
			@Override
			protected Void call() throws IOException, URISyntaxException
			{
				updateTitle("Browse " + uri);
				log.debug("Before browsing");
				Desktop.getDesktop().browse(uri);
				log.debug("After browsing");
				return null;
			}
		};
		browseTask.setOnFailed(onFailedHandler);
		executor.submit(browseTask);
	}

	public static Hyperlink createFileHyperlink(Path file, ExecutorService executor)
	{
		return createFileHyperlink(file, executor, DEFAULT_TASK_FAILED_HANDLER);
	}

	public static Hyperlink createFileHyperlink(Path file, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler)
	{
		URI uri = file.toUri();
		Hyperlink link = new Hyperlink();
		link.setVisited(true);
		link.setText(file.toString());
		link.setOnAction((ActionEvent evt) -> FXUtil.browse(uri, executor, onFailedHandler));
		return link;
	}

	public static Hyperlink createUrlHyperlink(URL url, ExecutorService executor) throws URISyntaxException
	{
		return createUrlHyperlink(url, executor, DEFAULT_TASK_FAILED_HANDLER);
	}

	public static Hyperlink createUrlHyperlink(URL url, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler)
			throws URISyntaxException
	{
		URI uri = url.toURI();
		Hyperlink link = new Hyperlink();
		link.setVisited(true);
		link.setText(url.toString());
		link.setOnAction((ActionEvent evt) -> FXUtil.browse(uri, executor, onFailedHandler));
		return link;
	}

	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread and waits for completion.
	 *
	 * @param action
	 *            the {@link Runnable} to run
	 * @throws NullPointerException
	 *             if {@code action} is {@code null}
	 */
	public static void runAndWait(Runnable action)
	{
		if (action == null)
			throw new NullPointerException("action");

		// run synchronously on JavaFX thread
		if (Platform.isFxApplicationThread())
		{
			action.run();
			return;
		}

		// queue on JavaFX thread and wait for completion
		final CountDownLatch doneLatch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try
			{
				action.run();
			}
			finally
			{
				doneLatch.countDown();
			}
		});

		try
		{
			doneLatch.await();
		}
		catch (InterruptedException e)
		{
			// ignore exception
		}
	}

	public static void setStandardMouseAndKeyboardSupportForTableView(TableView<?> tableView, ButtonBase editButton, ButtonBase removeButton)
	{
		tableView.setOnMouseClicked((MouseEvent evt) -> {
			if (evt.getClickCount() == 2 && (!tableView.getSelectionModel().isEmpty()))
			{
				editButton.fire();
			}
		});
		tableView.setOnKeyPressed((KeyEvent evt) -> {
			if (!tableView.getSelectionModel().isEmpty())
			{
				if (evt.getCode().equals(KeyCode.ENTER))
				{
					editButton.fire();
				}
				else if (evt.getCode().equals(KeyCode.DELETE))
				{
					removeButton.fire();
				}
			}
		});
	}

	public static void bindMoveButtonsForSingleSelection(final TableView<?> tableView, final ButtonBase moveUpBtn, final ButtonBase moveDownBtn)
	{
		updateMoveButtonsDisabilityWithSingleSelection(tableView, moveUpBtn, moveDownBtn);
		tableView.getSelectionModel()
				.selectedIndexProperty()
				.addListener((Observable observable) -> updateMoveButtonsDisabilityWithSingleSelection(tableView, moveUpBtn, moveDownBtn));

		moveUpBtn.setOnAction((ActionEvent event) -> {
			int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
			Collections.swap(tableView.getItems(), selectedIndex, selectedIndex - 1);
			tableView.getSelectionModel().select(selectedIndex - 1);
		});
		moveDownBtn.setOnAction((ActionEvent event) -> {
			int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
			Collections.swap(tableView.getItems(), selectedIndex, selectedIndex + 1);
			tableView.getSelectionModel().select(selectedIndex + 1);
		});
	}

	private static void updateMoveButtonsDisabilityWithSingleSelection(TableView<?> tableView, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
		moveUpBtn.setDisable(selectedIndex < 1);
		moveDownBtn.setDisable(selectedIndex >= tableView.getItems().size() - 1 || selectedIndex < 0);
	}

	public static Alert createExceptionAlert(String title, String headerText, Throwable exception)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(exception.toString());

		// Create expandable Exception.
		String exceptionText = ExceptionUtil.stackTraceToString(exception);

		Label label = new Label("Exception stacktrace:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		return alert;
	}

	public static Binding<UiPattern> createUiPatternTextFieldBinding(ToggleGroup patternModeToggleGrp, Toggle literalToggle, Toggle simpleToggle,
			Toggle regexToggle, TextField patternTxtFld, Label patternErrorLbl)
	{
		return new ObjectBinding<UiPattern>()
		{
			{
				super.bind(patternModeToggleGrp.selectedToggleProperty(), patternTxtFld.textProperty());
			}

			@Override
			protected UiPattern computeValue()
			{
				String pattern = patternTxtFld.getText();
				if (StringUtils.isBlank(pattern))
				{
					patternErrorLbl.setText("");
					return null;
				}
				PatternMode patternMode;
				if (patternModeToggleGrp.getSelectedToggle() == simpleToggle)
				{
					patternMode = PatternMode.SIMPLE;
				}
				else if (patternModeToggleGrp.getSelectedToggle() == regexToggle)
				{
					patternMode = PatternMode.REGEX;
				}
				else
				{
					patternMode = PatternMode.LITERAL;
				}
				try
				{
					UiPattern p = new UiPattern(pattern, patternMode);
					patternErrorLbl.setText("");
					return p;
				}
				catch (Exception e)
				{
					patternErrorLbl.setText("Invalid pattern: " + e.getMessage().split("\\n", 2)[0]);
					return null;
				}
			}
		};
	}

	public static StringBinding createConstantStringBinding(final String value)
	{
		return new StringBinding()
		{
			@Override
			protected String computeValue()
			{
				return value;
			}
		};
	}

	public static <T> Binding<T> createConstantBinding(final T value)
	{
		return new ObjectBinding<T>()
		{
			@Override
			protected T computeValue()
			{
				return value;
			}
		};
	}

	/**
	 * Load from fxml file and connect with ResourceBundle and controller.
	 * 
	 * @param fxmlFilename
	 * @param resourceBaseName
	 * @param locale
	 * @param controller
	 * @return
	 * @throws IOException
	 */
	public static <T> T loadFromFxml(String fxmlFilename, String resourceBaseName, Locale locale, Object controller) throws IOException
	{
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(FXUtil.class.getClassLoader().getResource("fxml/" + fxmlFilename));
		loader.setResources(resourceBaseName == null ? null : ResourceBundle.getBundle("i18n/" + resourceBaseName, locale));
		loader.setController(controller);
		return loader.load();
	}

	private FXUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
