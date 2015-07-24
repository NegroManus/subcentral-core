package de.subcentral.fx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.ExceptionUtil;
import de.subcentral.fx.UserPattern.Mode;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class FxUtil
{
    private static final Logger log = LogManager.getLogger(FxUtil.class);

    public static final StringConverter<Path>			PATH_STRING_CONVERTER		   = initPathStringConverter();
    public static final StringConverter<Locale>			LOCALE_DISPLAY_NAME_CONVERTER	   = initLocaleDisplayNameConverter();
    public static final StringConverter<ObservableList<Locale>>	LOCALE_LIST_DISPLAY_NAME_CONVERTER = initLocaleListDisplayNameConverter();
    public static final Comparator<Locale>			LOCALE_DISPLAY_NAME_COMPARATOR	   = initLocaleDisplayNameComparator();
    public static final EventHandler<WorkerStateEvent>		DEFAULT_TASK_FAILED_HANDLER	   = initDefaultTaskFailedHandler();

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

    private static StringConverter<ObservableList<Locale>> initLocaleListDisplayNameConverter()
    {
	return new StringConverter<ObservableList<Locale>>()
	{
	    @Override
	    public String toString(ObservableList<Locale> locales)
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
	    public ObservableList<Locale> fromString(String string)
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
	    Alert alert = createExceptionAlert(msg, msg, exc);
	    alert.show();
	};
    }

    public static boolean isJavaFxLauncherThread()
    {
	return Thread.currentThread().getName().equals("JavaFX-Launcher");
    }

    public static <T> TextFormatter<T> bindPropertyToTextField(TextField txtFld, Property<T> prop, StringConverter<T> converter)
    {
	TextFormatter<T> formatter = new TextFormatter<T>(converter);
	formatter.valueProperty().bindBidirectional(prop);
	txtFld.setTextFormatter(formatter);
	return formatter;
    }

    public static TextFormatter<Path> bindPathToTextField(TextField pathTxtFld, Property<Path> path)
    {
	return bindPropertyToTextField(pathTxtFld, path, PATH_STRING_CONVERTER);
    }

    public static void setChooseDirectoryAction(Button chooseDirBtn, TextFormatter<Path> textFormatter, Stage stage, String title)
    {
	chooseDirBtn.setOnAction((ActionEvent event) -> {
	    DirectoryChooser dirChooser = new DirectoryChooser();
	    dirChooser.setTitle(title);
	    Path currentValue = textFormatter.getValue();
	    if (currentValue != null && Files.isDirectory(currentValue, LinkOption.NOFOLLOW_LINKS))
	    {
		dirChooser.setInitialDirectory(currentValue.toFile());
	    }
	    File selectedDirectory = dirChooser.showDialog(stage);
	    if (selectedDirectory == null)
	    {
		return;
	    }
	    Path newTargetDir = selectedDirectory.toPath();
	    textFormatter.setValue(newTargetDir);
	});
    }

    public static void setChooseFileAction(Button chooseFileBtn, TextFormatter<Path> textFormatter, Stage stage, String title, String filterDescription, String filter)
    {
	chooseFileBtn.setOnAction((ActionEvent event) -> {
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle(title);
	    Path currentValue = textFormatter.getValue();
	    if (currentValue != null)
	    {
		Path potentialParentDir = currentValue.getParent();
		if (potentialParentDir != null && Files.isDirectory(potentialParentDir, LinkOption.NOFOLLOW_LINKS))
		{
		    fileChooser.setInitialDirectory(potentialParentDir.toFile());
		}
	    }
	    ExtensionFilter exeFilter = new ExtensionFilter(filterDescription, filter);
	    fileChooser.getExtensionFilters().add(exeFilter);
	    fileChooser.setSelectedExtensionFilter(exeFilter);

	    File selectedFile = fileChooser.showOpenDialog(stage);
	    if (selectedFile != null)
	    {
		textFormatter.setValue(selectedFile.toPath());
	    }
	});
    }

    public static void browse(String uri, ExecutorService executor)
    {
	browse(uri, executor, DEFAULT_TASK_FAILED_HANDLER);
    }

    public static void browse(String uri, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler)
    {
	Task<Void> browseTask = new Task<Void>()
	{
	    @Override
	    protected Void call() throws IOException, URISyntaxException
	    {
		updateTitle("Browse " + uri);
		// log.debug("Before browsing");
		Desktop.getDesktop().browse(new URI(uri));
		// log.debug("After browsing");
		return null;
	    }
	};
	browseTask.setOnFailed(onFailedHandler);
	executor.submit(browseTask);
    }

    public static Hyperlink createPathHyperlink(Path file, ExecutorService executor)
    {
	return createFileHyperlink(file, executor, DEFAULT_TASK_FAILED_HANDLER);
    }

    public static Hyperlink createFileHyperlink(Path file, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler)
    {
	String uri = file.toUri().toString();
	Hyperlink link = new Hyperlink();
	link.setVisited(true);
	link.setText(file.toString());
	link.setOnAction((ActionEvent evt) -> FxUtil.browse(uri, executor, onFailedHandler));
	return link;
    }

    public static Hyperlink createUrlHyperlink(URL url, ExecutorService executor) throws URISyntaxException
    {
	return createUrlHyperlink(url, executor, DEFAULT_TASK_FAILED_HANDLER);
    }

    public static Hyperlink createUrlHyperlink(URL url, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler) throws URISyntaxException
    {
	String uri = url.toURI().toString();
	Hyperlink link = new Hyperlink();
	link.setVisited(true);
	link.setText(url.toString());
	link.setOnAction((ActionEvent evt) -> FxUtil.browse(uri, executor, onFailedHandler));
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

    public static void setStandardMouseAndKeyboardSupportForTableView(final TableView<?> tableView, final ButtonBase editButton, final ButtonBase removeButton)
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
	updateMoveBtnsDisabilityForSingleSelection(tableView, moveUpBtn, moveDownBtn);
	tableView.getSelectionModel().selectedIndexProperty().addListener((Observable observable) -> updateMoveBtnsDisabilityForSingleSelection(tableView, moveUpBtn, moveDownBtn));

	moveUpBtn.setOnAction((ActionEvent evt) -> {
	    int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
	    if (selectedIndex < 1)
	    {
		return;
	    }
	    Collections.swap(tableView.getItems(), selectedIndex, selectedIndex - 1);
	    tableView.getSelectionModel().select(selectedIndex - 1);
	});
	moveDownBtn.setOnAction((ActionEvent evt) -> {
	    int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
	    if (selectedIndex >= tableView.getItems().size() - 1 || selectedIndex < 0)
	    {
		return;
	    }
	    Collections.swap(tableView.getItems(), selectedIndex, selectedIndex + 1);
	    tableView.getSelectionModel().select(selectedIndex + 1);
	});
    }

    private static void updateMoveBtnsDisabilityForSingleSelection(TableView<?> tableView, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
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

    public static Binding<UserPattern> createUiPatternTextFieldBinding(ToggleGroup patternModeToggleGrp, Toggle literalToggle, Toggle simpleToggle, Toggle regexToggle, TextField patternTxtFld,
	    Label patternErrorLbl)
    {
	return new ObjectBinding<UserPattern>()
	{
	    {
		super.bind(patternModeToggleGrp.selectedToggleProperty(), patternTxtFld.textProperty());
	    }

	    @Override
	    protected UserPattern computeValue()
	    {
		String pattern = patternTxtFld.getText();
		if (StringUtils.isBlank(pattern))
		{
		    patternErrorLbl.setText("");
		    return null;
		}
		Mode mode;
		if (patternModeToggleGrp.getSelectedToggle() == simpleToggle)
		{
		    mode = Mode.SIMPLE;
		}
		else if (patternModeToggleGrp.getSelectedToggle() == regexToggle)
		{
		    mode = Mode.REGEX;
		}
		else
		{
		    mode = Mode.LITERAL;
		}
		try
		{
		    UserPattern p = new UserPattern(pattern, mode);
		    // try to convert -> may throw an exception
		    p.toPattern();
		    patternErrorLbl.setText("");
		    return p;
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		    patternErrorLbl.setText("Invalid pattern: " + e.getMessage().split("\\n", 2)[0]);
		    return null;
		}
	    }
	};
    }

    public static StringBinding constantStringBinding(final String value)
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

    public static <T> Binding<T> constantBinding(final T value)
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

    public static ObservableList<Locale> createListOfAvailableLocales(boolean includeEmptyLocale, boolean includeVariants, Comparator<Locale> sortOrder)
    {
	Locale[] allLocales = Locale.getAvailableLocales(); // ca. 160 (without variants 45)
	int estimatedSize = includeVariants ? allLocales.length : (int) (allLocales.length / 3.5f);
	ArrayList<Locale> filteredLocales = new ArrayList<>(estimatedSize);
	for (Locale l : allLocales)
	{
	    if ((includeEmptyLocale || !l.equals(Locale.ROOT)) && (includeVariants || l.getLanguage().equals(l.toString())))
	    {
		filteredLocales.add(l);
	    }
	}
	filteredLocales.trimToSize();
	if (sortOrder != null)
	{
	    filteredLocales.sort(sortOrder);
	}
	return FXCollections.observableList(filteredLocales);
    }

    public static Image loadImg(String img)
    {
	if (img == null)
	{
	    return null;
	}
	return new Image("img/" + img);
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
	loader.setLocation(FxUtil.class.getClassLoader().getResource("fxml/" + fxmlFilename));
	loader.setResources(resourceBaseName == null ? null : ResourceBundle.getBundle("i18n/" + resourceBaseName, locale));
	loader.setController(controller);
	return loader.load();
    }

    public static <E, F> void handleDistinctAdd(TableView<E> table, Optional<F> addDialogResult, Function<F, E> converter)
    {
	if (addDialogResult.isPresent())
	{
	    handleDistinctAdd(table, Optional.of(converter.apply(addDialogResult.get())));
	}
    }

    public static <E> void handleDistinctAdd(TableView<E> table, Optional<? extends E> addDialogResult)
    {
	if (addDialogResult.isPresent())
	{
	    E newItem = addDialogResult.get();
	    int newItemIndex = table.getItems().indexOf(newItem);
	    if (newItemIndex == -1)
	    {
		// if newItem not already exists
		table.getItems().add(newItem);
	    }
	    else
	    {
		// if newItem already exists
		table.getItems().set(newItemIndex, newItem);
	    }
	}
    }

    public static <E, F> void handleDistinctEdit(TableView<E> table, Optional<F> editDialogResult, Function<F, E> converter)
    {
	if (editDialogResult.isPresent())
	{
	    handleDistinctEdit(table, Optional.of(converter.apply(editDialogResult.get())));
	}
    }

    public static <E> void handleDistinctEdit(TableView<E> table, Optional<? extends E> editDialogResult)
    {
	if (editDialogResult.isPresent())
	{
	    E newItem = editDialogResult.get();
	    int newItemIndex = table.getItems().indexOf(newItem);
	    int selectionIndex = table.getSelectionModel().getSelectedIndex();
	    // if the updated item is not equal to any existing item or equal to the item which was opened for edit
	    if (newItemIndex == -1 || newItemIndex == selectionIndex)
	    {
		// replace
		table.getItems().set(selectionIndex, newItem);
	    }
	    else
	    {
		// if updatedItem already exists elsewhere
		// then replace the selected item with the updatedItem ...
		table.getItems().set(selectionIndex, newItem);
		// ... and remove the "old" item
		table.getItems().remove(newItemIndex);
		table.getSelectionModel().select(newItem);
	    }
	}
    }

    public static <E> void handleDelete(TableView<E> table, String elementType, StringConverter<E> elemToStringConverter)
    {
	E selectedElem = table.getSelectionModel().getSelectedItem();
	Alert alert = new Alert(AlertType.CONFIRMATION);
	alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
	alert.setResizable(true);
	alert.setTitle("Confirmation of removal of a " + elementType);
	alert.setHeaderText("Do you really want to remove this " + elementType + "?");
	alert.setContentText(elemToStringConverter.toString(selectedElem));

	Optional<ButtonType> result = alert.showAndWait();
	if (result.get() == ButtonType.YES)
	{
	    int selectedIndex = table.getSelectionModel().getSelectedIndex();
	    table.getItems().remove(selectedIndex);
	}
    }

    public static <T> TreeItem<T> findTreeItem(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate)
    {
	if (predicate.test(treeItem))
	{
	    return treeItem;
	}
	for (TreeItem<T> child : treeItem.getChildren())
	{
	    TreeItem<T> matchingItem = findTreeItem(child, predicate);
	    if (matchingItem != null)
	    {
		return matchingItem;
	    }
	}
	return null;
    }

    public static class ToggleEnumBinding<E extends Enum<E>>
    {
	private final ToggleGroup	     toggleGroup;
	private final Property<E>	     enumProp;
	private final Map<Toggle, E>	     mapping;
	private final ChangeListener<Toggle> toggleListener;
	private final ChangeListener<E>	     enumListener;
	private boolean			     updating;

	public ToggleEnumBinding(ToggleGroup toggleGroup, Property<E> enumProp, Map<Toggle, E> mapping)
	{
	    this.toggleGroup = toggleGroup;
	    this.enumProp = enumProp;
	    this.mapping = mapping;

	    // set initial value
	    selectToggle(this.enumProp.getValue());

	    // init listeners and add them
	    this.toggleListener = initToggleListener();
	    this.enumListener = initEnumListener();
	    this.toggleGroup.selectedToggleProperty().addListener(toggleListener);
	    this.enumProp.addListener(enumListener);
	}

	public void unbind()
	{
	    toggleGroup.selectedToggleProperty().removeListener(toggleListener);
	    enumProp.removeListener(enumListener);
	}

	private ChangeListener<Toggle> initToggleListener()
	{
	    return new ChangeListener<Toggle>()
	    {
		@Override
		public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
		{
		    if (!updating)
		    {
			try
			{
			    updating = true;
			    enumProp.setValue(mapping.get(newValue));
			}
			finally
			{
			    updating = false;
			}
		    }
		}
	    };
	}

	private ChangeListener<E> initEnumListener()
	{
	    return new ChangeListener<E>()
	    {
		@Override
		public void changed(ObservableValue<? extends E> observable, E oldValue, E newValue)
		{
		    if (!updating)
		    {
			try
			{
			    updating = true;
			    selectToggle(newValue);
			}
			finally
			{
			    updating = false;
			}
		    }
		}
	    };
	}

	private void selectToggle(E enumValue)
	{
	    for (Map.Entry<Toggle, E> entry : mapping.entrySet())
	    {
		if (enumValue == entry.getValue())
		{
		    toggleGroup.selectToggle(entry.getKey());
		}
	    }
	}
    }

    public static <E> Observable observeBeans(ObservableList<E> beans, Function<E, Observable[]> propertiesExtractor)
    {
	ObservableObject obsv = new ObservableObject();
	// Observe the list itself
	obsv.getDependencies().add(beans);
	// Observe the properties of the current list content
	for (E bean : beans)
	{
	    for (Observable o : propertiesExtractor.apply(bean))
	    {
		obsv.getDependencies().add(o);
	    }
	}
	// React on list changes
	beans.addListener(new ListChangeListener<E>()
	{
	    @Override
	    public void onChanged(ListChangeListener.Change<? extends E> c)
	    {
		while (c.next())
		{
		    if (c.wasRemoved())
		    {
			for (E bean : c.getRemoved())
			{
			    // remove listener for properties
			    for (Observable o : propertiesExtractor.apply(bean))
			    {
				obsv.getDependencies().remove(o);
			    }
			}
		    }
		    if (c.wasAdded())
		    {
			for (E bean : c.getAddedSubList())
			{
			    // add listener for properties
			    for (Observable o : propertiesExtractor.apply(bean))
			    {
				obsv.getDependencies().add(o);
			    }
			}
		    }
		}
	    }
	});
	return obsv;
    }

    public static <E> Observable observeBean(ReadOnlyProperty<E> bean, Function<E, Observable[]> propertiesExtractor)
    {
	ObservableObject obsv = new ObservableObject();
	// Observe the bean itself
	obsv.getDependencies().add(bean);
	// Observe the properties of the current bean
	if (bean.getValue() != null)
	{
	    for (Observable o : propertiesExtractor.apply(bean.getValue()))
	    {
		obsv.getDependencies().add(o);
	    }
	}
	// React on changes
	bean.addListener(new ChangeListener<E>()
	{
	    @Override
	    public void changed(ObservableValue<? extends E> observable, E oldValue, E newValue)
	    {
		if (oldValue != null)
		{
		    for (Observable o : propertiesExtractor.apply(oldValue))
		    {
			obsv.getDependencies().remove(o);
		    }
		}
		if (newValue != null)
		{
		    for (Observable o : propertiesExtractor.apply(newValue))
		    {
			obsv.getDependencies().add(o);
		    }
		}
	    }
	});
	return obsv;
    }

    private FxUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
