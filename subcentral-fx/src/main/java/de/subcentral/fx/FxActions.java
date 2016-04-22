package de.subcentral.fx;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.SystemUtils;

import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.IOUtil.ProcessResult;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
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
import javafx.stage.Window;
import javafx.util.StringConverter;

public class FxActions
{
	private static final TimeUnit	IO_TIMEOUT_UNIT		= TimeUnit.MINUTES;
	private static final long		IO_TIMEOUT_VALUE	= 1;

	private FxActions()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static void chooseDirectory(TextFormatter<Path> textFormatter, Window window, String title)
	{
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(title);
		Path currentVal = textFormatter.getValue();
		if (currentVal != null && Files.isDirectory(currentVal, LinkOption.NOFOLLOW_LINKS))
		{
			dirChooser.setInitialDirectory(currentVal.toFile());
		}
		File selectedDir = dirChooser.showDialog(window);
		if (selectedDir != null)
		{
			textFormatter.setValue(selectedDir.toPath());
		}
	}

	public static void chooseFile(TextFormatter<Path> textFormatter, Window owner, String title, ExtensionFilter... extensionFilters)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		Path currentVal = textFormatter.getValue();
		if (currentVal != null)
		{
			Path potentialParentDir = currentVal.getParent();
			if (potentialParentDir != null && Files.isDirectory(potentialParentDir, LinkOption.NOFOLLOW_LINKS))
			{
				fileChooser.setInitialDirectory(potentialParentDir.toFile());
			}
		}
		if (extensionFilters.length > 0)
		{
			fileChooser.getExtensionFilters().addAll(extensionFilters);
		}

		File selectedFile = fileChooser.showOpenDialog(owner);
		if (selectedFile != null)
		{
			textFormatter.setValue(selectedFile.toPath());
		}
	}

	public static void browse(URI uri, Executor executor)
	{
		browse(uri, Function.identity(), executor);
	}

	public static void browse(String uri, Executor executor)
	{
		browse(uri, (String u) ->
		{
			try
			{
				return new URI(u);
			}
			catch (URISyntaxException e)
			{
				throw new RuntimeException(e);
			}
		}, executor);
	}

	public static void browse(URL url, Executor executor)
	{
		browse(url, (URL u) ->
		{
			try
			{
				return u.toURI();
			}
			catch (URISyntaxException e)
			{
				throw new RuntimeException(e);
			}
		}, executor);
	}

	public static void browse(Path path, Executor executor)
	{
		browse(path, Path::toUri, executor);
	}

	public static void browseParent(Path path, Executor executor)
	{
		Path parent = path.getParent();
		if (parent != null)
		{
			browse(parent, Path::toUri, executor);
		}
		else
		{
			browse(path, Path::toUri, executor);
		}
	}

	public static <T> void browse(T uri, Function<T, URI> uriConverter, Executor executor)
	{
		Task<Void> task = new Task<Void>()
		{
			{
				updateTitle("Browsing " + uri);
			}

			@Override
			protected Void call() throws IOException
			{
				java.awt.Desktop.getDesktop().browse(uriConverter.apply(uri));
				return null;
			}
		};
		executor.execute(task);
	}

	public static void showInDirectory(Path path, Executor executor)
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			showInWindowsExplorer(path, executor);
		}
		else
		{
			browseParent(path, executor);
		}
	}

	public static void showInWindowsExplorer(Path path, Executor executor)
	{
		Task<ProcessResult> task = new Task<ProcessResult>()
		{
			{
				updateTitle("Showing in windows explorer " + path);
			}

			@Override
			protected ProcessResult call() throws IOException, InterruptedException, TimeoutException
			{
				List<String> command = Arrays.asList("explorer.exe", "/select,", path.toString());
				return IOUtil.executeProcess(command, IO_TIMEOUT_VALUE, IO_TIMEOUT_UNIT);
			}
		};
		executor.execute(task);
	}

	public static <E> E remove(ListView<E> list)
	{
		return remove(list.getItems(), list.getSelectionModel());
	}

	public static <E> E remove(ComboBox<E> comboBox)
	{
		return remove(comboBox.getItems(), comboBox.getSelectionModel());
	}

	public static <E> E remove(ObservableList<E> items, SelectionModel<E> selectionModel)
	{
		int selectedIndex = selectionModel.getSelectedIndex();
		E removed = items.remove(selectedIndex);
		// Reselect the selectedIndex (standard behavior selects selectedIndex-1)
		if (!items.isEmpty() && selectedIndex < items.size())
		{
			selectionModel.select(selectedIndex);
		}
		return removed;
	}

	public static <E> E removeConfirmed(TableView<E> table, String elementType, StringConverter<E> elemToStringConverter)
	{
		return removeConfirmed(table.getItems(), table.getSelectionModel(), elementType, elemToStringConverter);
	}

	public static <E> E removeConfirmed(ObservableList<E> items, SelectionModel<E> selectionModel, String elementType, StringConverter<E> elemToStringConverter)
	{
		E selectedElem = selectionModel.getSelectedItem();
		if (selectedElem == null)
		{
			return null;
		}
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
		alert.setResizable(true);
		alert.setTitle("Remove this " + elementType + "?");
		alert.setHeaderText("Do you really want to remove this " + elementType + "?");
		alert.setContentText(elemToStringConverter.toString(selectedElem));

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.YES)
		{
			return remove(items, selectionModel);
		}
		return null;
	}

	public static <E, F> void addDistinct(ListView<E> list, Optional<F> addDialogResult, Function<F, E> converter)
	{
		addDistinct(list.getItems(), list.getSelectionModel(), addDialogResult, converter);
	}

	public static <E, F> void addDistinct(TableView<E> table, Optional<F> addDialogResult, Function<F, E> converter)
	{
		addDistinct(table.getItems(), table.getSelectionModel(), addDialogResult, converter);
	}

	public static <E> void addDistinct(ListView<E> list, Optional<? extends E> addDialogResult)
	{
		addDistinct(list.getItems(), list.getSelectionModel(), addDialogResult);
	}

	public static <E> void addDistinct(TableView<E> table, Optional<? extends E> addDialogResult)
	{
		addDistinct(table.getItems(), table.getSelectionModel(), addDialogResult);
	}

	public static <E, F> void addDistinct(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<F> addDialogResult, Function<F, E> converter)
	{
		if (addDialogResult.isPresent())
		{
			addDistinct(items, selectionModel, Optional.of(converter.apply(addDialogResult.get())));
		}
	}

	public static <E> void addDistinct(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<? extends E> addDialogResult)
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

	public static <E, F> void editDistinct(ListView<E> list, Optional<F> editDialogResult, Function<F, E> converter)
	{
		editDistinct(list.getItems(), list.getSelectionModel(), editDialogResult, converter);
	}

	public static <E, F> void editDistinct(TableView<E> table, Optional<F> editDialogResult, Function<F, E> converter)
	{
		editDistinct(table.getItems(), table.getSelectionModel(), editDialogResult, converter);
	}

	public static <E, F> void editDistinct(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<F> editDialogResult, Function<F, E> converter)
	{
		if (editDialogResult.isPresent())
		{
			editDistinct(items, selectionModel, Optional.of(converter.apply(editDialogResult.get())));
		}
	}

	public static <E> void editDistinct(ListView<E> list, Optional<? extends E> editDialogResult)
	{
		editDistinct(list.getItems(), list.getSelectionModel(), editDialogResult);
	}

	public static <E> void editDistinct(TableView<E> table, Optional<? extends E> editDialogResult)
	{
		editDistinct(table.getItems(), table.getSelectionModel(), editDialogResult);
	}

	public static <E> void editDistinct(ObservableList<E> items, SelectionModel<E> selectionModel, Optional<? extends E> editDialogResult)
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

	/**
	 * For a list of atomic, uneditable elements.
	 * 
	 * @param list
	 * @param addButton
	 * @param removeButton
	 */
	public static void setStandardMouseAndKeyboardSupport(ListView<?> list, ButtonBase addButton, ButtonBase removeButton)
	{
		setStandardMouseAndKeyboardSupport(list, addButton, null, removeButton, false, null);
	}

	public static void setStandardMouseAndKeyboardSupport(ListView<?> list, ButtonBase addButton, ButtonBase editButton, ButtonBase removeButton, boolean cellEditable)
	{
		setStandardMouseAndKeyboardSupport(list, addButton, editButton, removeButton, cellEditable, cellEditable ? ((ListView<?> lv) -> lv.getEditingIndex() != 0) : null);
	}

	public static void setStandardMouseAndKeyboardSupport(TableView<?> table, ButtonBase addButton, ButtonBase editButton, ButtonBase removeButton)
	{
		setStandardMouseAndKeyboardSupport(table, addButton, editButton, removeButton, false, null);
	}

	private static <T extends Control> void setStandardMouseAndKeyboardSupport(T control,
			ButtonBase addButton,
			ButtonBase editButton,
			ButtonBase removeButton,
			boolean cellEditable,
			Predicate<T> isCellEditing)
	{
		// Mouse left double-click is already handled by the control if it is editable
		if (editButton != null && !cellEditable)
		{
			control.setOnMouseClicked((MouseEvent evt) ->
			{
				if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2)
				{
					editButton.fire();
				}
			});
		}
		control.setOnKeyPressed((KeyEvent evt) ->
		{
			switch (evt.getCode())
			{
				case INSERT:
					if (addButton != null)
					{
						addButton.fire();
					}
					break;
				case ENTER:
					// ENTER is already handled by the control if it is editable
					if (!cellEditable && editButton != null)
					{
						editButton.fire();
					}
					break;
				case DELETE:
					if (removeButton != null)
					{
						removeButton.fire();
					}
					break;
				// FIXFOR: Pressing the escape key when editing a cell cancels the edit but also closes the dialog.
				// The KeyEvent sadly never reaches the KeyEvent handler in javafx.scene.control.cell.CellUtils.createTextField(Cell<T>, StringConverter<T>)
				// so we need to take actions ourselves
				// -> If a cell is currently edited and the ESCAPE key is pressed, we consume the ESCAPE event
				case ESCAPE:
					if (cellEditable && isCellEditing.test(control))
					{
						evt.consume();
					}
					break;
				default:
					break;
			}
		});
	}

	public static void bindMoveButtons(ListView<?> list, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		bindMoveButtons(list.getItems(), list.getSelectionModel(), moveUpBtn, moveDownBtn);
	}

	public static void bindMoveButtons(TableView<?> table, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		bindMoveButtons(table.getItems(), table.getSelectionModel(), moveUpBtn, moveDownBtn);
	}

	public static void bindMoveButtons(ObservableList<?> items, SelectionModel<?> selectionModel, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
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

	private static void updateMoveBtnsDisabilityForSingleSelection(ObservableList<?> items, SelectionModel<?> selectionModel, ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		int selectedIndex = selectionModel.getSelectedIndex();
		moveUpBtn.setDisable(selectedIndex < 1);
		moveDownBtn.setDisable(selectedIndex >= items.size() - 1 || selectedIndex < 0);
	}
}
