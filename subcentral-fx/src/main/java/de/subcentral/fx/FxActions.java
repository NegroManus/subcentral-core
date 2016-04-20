package de.subcentral.fx;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class FxActions
{
	private static final Logger							log							= LogManager.getLogger(FxActions.class);

	public static final EventHandler<WorkerStateEvent>	DEFAULT_TASK_FAILED_HANDLER	= FxActions.initDefaultTaskFailedHandler();

	private FxActions()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	private static EventHandler<WorkerStateEvent> initDefaultTaskFailedHandler()
	{
		return (WorkerStateEvent evt) ->
		{
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
			Alert alert = FxUtil.createExceptionAlert(null, msg, msg, exc);
			alert.show();
		};
	}

	public static void chooseDirectory(TextFormatter<Path> textFormatter, Stage stage, String title)
	{
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
	}

	public static void chooseFile(TextFormatter<Path> textFormatter, Window owner, String title, ExtensionFilter... extensionFilters)
	{
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
		if (extensionFilters.length > 0)
		{
			fileChooser.getExtensionFilters().addAll(extensionFilters);
			fileChooser.setSelectedExtensionFilter(extensionFilters[0]);
		}

		File selectedFile = fileChooser.showOpenDialog(owner);
		if (selectedFile != null)
		{
			textFormatter.setValue(selectedFile.toPath());
		}
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
				java.awt.Desktop.getDesktop().browse(new URI(uri));
				// log.debug("After browsing");
				return null;
			}
		};
		browseTask.setOnFailed(onFailedHandler);
		executor.submit(browseTask);
	}

	public static <E> E handleRemove(ObservableList<E> items, SelectionModel<E> selectionModel)
	{
		int selectedIndex = selectionModel.getSelectedIndex();
		return items.remove(selectedIndex);
	}

	public static <E> E handleRemove(ListView<E> list)
	{
		return handleRemove(list.getItems(), list.getSelectionModel());
	}

	public static <E> E handleRemove(ComboBox<E> comboBox)
	{
		return handleRemove(comboBox.getItems(), comboBox.getSelectionModel());
	}

	public static <E> E handleConfirmedRemove(ObservableList<E> items, SelectionModel<E> selectionModel, String elementType, StringConverter<E> elemToStringConverter)
	{
		E selectedElem = selectionModel.getSelectedItem();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		alert.setResizable(true);
		alert.setTitle("Confirmation of removal of a " + elementType);
		alert.setHeaderText("Do you really want to remove this " + elementType + "?");
		alert.setContentText(elemToStringConverter.toString(selectedElem));

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.YES)
		{
			return handleRemove(items, selectionModel);
		}
		return null;
	}

	public static <E> E handleConfirmedRemove(TableView<E> table, String elementType, StringConverter<E> elemToStringConverter)
	{
		return handleConfirmedRemove(table.getItems(), table.getSelectionModel(), elementType, elemToStringConverter);
	}

	public static <E, F> void handleDistinctAdd(ListView<E> list, Optional<F> addDialogResult, Function<F, E> converter)
	{
		handleDistinctAdd(list.getItems(), list.getSelectionModel(), addDialogResult, converter);
	}

	public static <E, F> void handleDistinctAdd(TableView<E> table, Optional<F> addDialogResult, Function<F, E> converter)
	{
		handleDistinctAdd(table.getItems(), table.getSelectionModel(), addDialogResult, converter);
	}

	public static <E, F> void handleDistinctAdd(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<F> addDialogResult, Function<F, E> converter)
	{
		if (addDialogResult.isPresent())
		{
			handleDistinctAdd(items, selectionModel, Optional.of(converter.apply(addDialogResult.get())));
		}
	}

	public static <E> void handleDistinctAdd(ListView<E> list, Optional<? extends E> addDialogResult)
	{
		handleDistinctAdd(list.getItems(), list.getSelectionModel(), addDialogResult);
	}

	public static <E> void handleDistinctAdd(TableView<E> table, Optional<? extends E> addDialogResult)
	{
		handleDistinctAdd(table.getItems(), table.getSelectionModel(), addDialogResult);
	}

	public static <E> void handleDistinctAdd(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<? extends E> addDialogResult)
	{
		if (addDialogResult.isPresent())
		{
			E newItem = addDialogResult.get();
			int existingItemIndex = items.indexOf(newItem);
			if (existingItemIndex == -1)
			{
				// if newItem not already exists
				int newItemIndex = selectionModel.getSelectedIndex() + 1;
				items.add(newItemIndex, newItem);
				// select the newly added item
				selectionModel.select(newItemIndex);
			}
			else
			{
				// if newItem already exists, replace it
				items.set(existingItemIndex, newItem);
				// select the newly added item
				selectionModel.select(existingItemIndex);
			}
		}
	}

	public static <E, F> void handleDistinctEdit(ListView<E> list, Optional<F> editDialogResult, Function<F, E> converter)
	{
		handleDistinctEdit(list.getItems(), list.getSelectionModel(), editDialogResult, converter);
	}

	public static <E, F> void handleDistinctEdit(TableView<E> table, Optional<F> editDialogResult, Function<F, E> converter)
	{
		handleDistinctEdit(table.getItems(), table.getSelectionModel(), editDialogResult, converter);
	}

	public static <E, F> void handleDistinctEdit(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<F> editDialogResult, Function<F, E> converter)
	{
		if (editDialogResult.isPresent())
		{
			handleDistinctEdit(items, selectionModel, Optional.of(converter.apply(editDialogResult.get())));
		}
	}

	public static <E> void handleDistinctEdit(ListView<E> list, Optional<? extends E> editDialogResult)
	{
		handleDistinctEdit(list.getItems(), list.getSelectionModel(), editDialogResult);
	}

	public static <E> void handleDistinctEdit(TableView<E> table, Optional<? extends E> editDialogResult)
	{
		handleDistinctEdit(table.getItems(), table.getSelectionModel(), editDialogResult);
	}

	public static <E> void handleDistinctEdit(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<? extends E> editDialogResult)
	{
		if (editDialogResult.isPresent())
		{
			E newItem = editDialogResult.get();
			int existingItemIndex = items.indexOf(newItem);
			int selectionIndex = selectionModel.getSelectedIndex();
			items.set(selectionIndex, newItem);
			// if the updated item is equal to any existing item and is not equal to the item which was opened for edit
			if (existingItemIndex != -1 && existingItemIndex != selectionIndex)
			{
				// if updatedItem already exists elsewhere
				// remove the "old" item
				items.remove(existingItemIndex);
				if (existingItemIndex < selectionIndex)
				{
					selectionIndex--;
				}
			}
			// select the edited item
			selectionModel.select(selectionIndex);
		}
	}

	public static void setStandardMouseAndKeyboardSupport(final ListView<?> list, final ButtonBase editButton, final ButtonBase removeButton)
	{
		list.setOnMouseClicked((MouseEvent evt) ->
		{
			if (evt.getClickCount() == 2 && (!list.getSelectionModel().isEmpty()))
			{
				editButton.fire();
			}
		});
		list.setOnKeyPressed((KeyEvent evt) ->
		{
			switch (evt.getCode())
			{
				case ENTER:
					if (!list.getSelectionModel().isEmpty())
					{
						editButton.fire();
					}
					break;
				// FIXFOR: Pressing the escape key when editing a cell cancels the edit but also closes the dialog.
				// The KeyEvent sadly never reaches the KeyEvent handler in javafx.scene.control.cell.CellUtils.createTextField(Cell<T>, StringConverter<T>)
				// so we need to take actions ourselves
				// -> If a cell is currently edited and the ESCAPE key is pressed, we consume the ESCAPE event
				case ESCAPE:
					if (list.getEditingIndex() >= 0)
					{
						evt.consume();
					}
					break;
				case DELETE:
					if (!list.getSelectionModel().isEmpty())
					{
						removeButton.fire();
					}
					break;
				default:
					break;
			}
		});
	}

	public static void setStandardMouseAndKeyboardSupport(final TableView<?> table, final ButtonBase editButton, final ButtonBase removeButton)
	{
		table.setOnMouseClicked((MouseEvent evt) ->
		{
			if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2 && (!table.getSelectionModel().isEmpty()))
			{
				editButton.fire();
			}
		});
		table.setOnKeyPressed((KeyEvent evt) ->
		{
			switch (evt.getCode())
			{
				case ENTER:
					if (!table.getSelectionModel().isEmpty())
					{
						editButton.fire();
					}
					break;
				case DELETE:
					if (!table.getSelectionModel().isEmpty())
					{
						removeButton.fire();
					}
					break;
				default:
					break;
			}
		});
	}

	public static void bindMoveButtonsForSingleSelection(ListView<?> list, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		bindMoveButtonsForSingleSelection(list.getItems(), list.getSelectionModel(), moveUpBtn, moveDownBtn);
	}

	public static void bindMoveButtonsForSingleSelection(TableView<?> table, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		bindMoveButtonsForSingleSelection(table.getItems(), table.getSelectionModel(), moveUpBtn, moveDownBtn);
	}

	public static void bindMoveButtonsForSingleSelection(ObservableList<?> items, SelectionModel<?> selectionModel, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		updateMoveBtnsDisabilityForSingleSelection(items, selectionModel, moveUpBtn, moveDownBtn);
		selectionModel.selectedIndexProperty().addListener((Observable observable) -> updateMoveBtnsDisabilityForSingleSelection(items, selectionModel, moveUpBtn, moveDownBtn));

		moveUpBtn.setOnAction((ActionEvent evt) ->
		{
			int selectedIndex = selectionModel.getSelectedIndex();
			if (selectedIndex < 1)
			{
				return;
			}
			Collections.swap(items, selectedIndex, selectedIndex - 1);
			selectionModel.select(selectedIndex - 1);
		});
		moveDownBtn.setOnAction((ActionEvent evt) ->
		{
			int selectedIndex = selectionModel.getSelectedIndex();
			if (selectedIndex >= items.size() - 1 || selectedIndex < 0)
			{
				return;
			}
			Collections.swap(items, selectedIndex, selectedIndex + 1);
			selectionModel.select(selectedIndex + 1);
		});
	}

	static void updateMoveBtnsDisabilityForSingleSelection(ObservableList<?> items, SelectionModel<?> selectionModel, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		int selectedIndex = selectionModel.getSelectedIndex();
		moveUpBtn.setDisable(selectedIndex < 1);
		moveDownBtn.setDisable(selectedIndex >= items.size() - 1 || selectedIndex < 0);
	}

	public static void setStandardMouseAndKeyboardSupportForEditable(final ListView<?> list, final ButtonBase removeButton)
	{
		list.setOnKeyPressed((KeyEvent evt) ->
		{
			switch (evt.getCode())
			{
				// FIXFOR: Pressing the escape key when editing a cell cancels the edit but also closes the dialog.
				// The KeyEvent sadly never reaches the KeyEvent handler in javafx.scene.control.cell.CellUtils.createTextField(Cell<T>, StringConverter<T>)
				// so we need to take actions ourselves
				// -> If a cell is currently edited and the ESCAPE key is pressed, we consume the ESCAPE event
				case ESCAPE:
					if (list.getEditingIndex() >= 0)
					{
						evt.consume();
					}
					break;
				case DELETE:
					if (!list.getSelectionModel().isEmpty())
					{
						removeButton.fire();
					}
					break;
				default:
					break;
			}
		});
	}
}
