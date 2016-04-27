package de.subcentral.fx.action;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.subcentral.core.util.CollectionUtil;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableView;

public class ActionList<E>
{
	// required properties
	private final ObservableList<E>				list;
	private final TransformationList<E, E>		displayList;
	private final SelectionModel<E>				selectionModel;

	// optional properties
	private Supplier<Optional<E>>				newItemSupplier;
	private Function<E, Optional<E>>			itemEditer;
	private BiPredicate<? super E, ? super E>	distincter;
	private Comparator<? super E>				sorter;
	private Consumer<? super E>					alreadyContainedInformer;
	private Predicate<? super E>				removeConfirmer;

	// internal variables
	private BooleanBinding						noIndexSelected;

	// constructors
	public ActionList(ComboBox<E> comboBox)
	{
		this(comboBox.getItems(), comboBox.getSelectionModel(), null);
	}

	public ActionList(ListView<E> listView)
	{
		this(listView.getItems(), listView.getSelectionModel(), null);
	}

	public ActionList(TableView<E> tableView)
	{
		this(tableView.getItems(), tableView.getSelectionModel(), null);
	}

	public ActionList(ObservableList<E> list, SelectionModel<E> selectionModel, TransformationList<E, E> displayList)
	{
		this.list = Objects.requireNonNull(list, "list");
		this.selectionModel = Objects.requireNonNull(selectionModel, "selectionModel");
		this.displayList = displayList;
	}

	// properties
	public ObservableList<E> getList()
	{
		return list;
	}

	public TransformationList<E, E> getDisplayList()
	{
		return displayList;
	}

	public SelectionModel<E> getSelectionModel()
	{
		return selectionModel;
	}

	public Supplier<Optional<E>> getNewItemSupplier()
	{
		return newItemSupplier;
	}

	public void setNewItemSupplier(Supplier<Optional<E>> newItemSupplier)
	{
		this.newItemSupplier = newItemSupplier;
	}

	public Function<E, Optional<E>> getItemEditer()
	{
		return itemEditer;
	}

	public void setItemEditer(Function<E, Optional<E>> itemEditer)
	{
		this.itemEditer = itemEditer;
	}

	public BiPredicate<? super E, ? super E> getDistincter()
	{
		return distincter;
	}

	public void setDistincter(BiPredicate<? super E, ? super E> distincter)
	{
		this.distincter = distincter;
	}

	public Comparator<? super E> getSorter()
	{
		return sorter;
	}

	public void setSorter(Comparator<? super E> sorter)
	{
		this.sorter = sorter;
	}

	public Consumer<? super E> getAlreadyContainedInformer()
	{
		return alreadyContainedInformer;
	}

	public void setAlreadyContainedInformer(Consumer<? super E> alreadyContainedInformer)
	{
		this.alreadyContainedInformer = alreadyContainedInformer;
	}

	public Predicate<? super E> getRemoveConfirmer()
	{
		return removeConfirmer;
	}

	public void setRemoveConfirmer(Predicate<? super E> removeConfirmer)
	{
		this.removeConfirmer = removeConfirmer;
	}

	// functions
	// binding
	public void bindAddButton(ButtonBase addBtn)
	{
		addBtn.setOnAction((ActionEvent evt) -> add());
	}

	public void bindEditButton(ButtonBase editBtn)
	{
		editBtn.disableProperty().bind(getNoIndexSelectedBinding());
		editBtn.setOnAction((ActionEvent evt) -> editSelected());
	}

	public void bindRemoveButton(ButtonBase removeBtn)
	{
		removeBtn.disableProperty().bind(getNoIndexSelectedBinding());
		removeBtn.setOnAction((ActionEvent evt) -> removeSelected());
	}

	public void bindMoveButtons(ButtonBase moveUpBtn, ButtonBase moveDownBtn)
	{
		FxActions.bindMoveButtons(list, selectionModel, moveUpBtn, moveDownBtn);
	}

	// access
	private int getSourceIndex(int index)
	{
		return displayList == null ? index : displayList.getSourceIndexFor(list, index);
	}

	private int getSelectedSourceIndex()
	{
		return getSourceIndex(selectionModel.getSelectedIndex());
	}

	private ObservableList<E> getSelectableList()
	{
		return displayList == null ? list : displayList;
	}

	// modify
	public boolean add()
	{
		if (newItemSupplier == null)
		{
			throw new UnsupportedOperationException("No newItemSupplier provided");
		}
		Optional<E> itemOpt = newItemSupplier.get();
		if (itemOpt.isPresent())
		{
			if (sorter == null)
			{
				if (distincter == null || !CollectionUtil.contains(list, itemOpt.get(), distincter))
				{
					int addIndex = selectionModel.getSelectedIndex() >= 0 ? selectionModel.getSelectedIndex() + 1 : list.size();
					list.add(getSourceIndex(addIndex), itemOpt.get());
					selectionModel.select(addIndex);
					return true;
				}
			}
			else
			{
				if (CollectionUtil.addToSortedList(list, itemOpt.get(), sorter, distincter))
				{
					selectionModel.select(itemOpt.get());
					return true;
				}
			}
			if (alreadyContainedInformer != null)
			{
				alreadyContainedInformer.accept(itemOpt.get());
			}
		}
		return false;
	}

	public E editSelected()
	{
		if (itemEditer == null)
		{
			throw new UnsupportedOperationException("No itemEditer provided");
		}
		if (selectionModel.getSelectedIndex() < 0)
		{
			return null;
		}
		Optional<E> itemOpt = itemEditer.apply(selectionModel.getSelectedItem());
		if (itemOpt.isPresent())
		{
			int selectedSourceIndex = getSelectedSourceIndex();
			if (sorter == null)
			{
				if (distincter != null)
				{
					int sourceIndexOfExisting = CollectionUtil.indexOf(list, itemOpt.get(), distincter);
					if (sourceIndexOfExisting < 0 || sourceIndexOfExisting == selectedSourceIndex)
					{
						return list.set(selectedSourceIndex, itemOpt.get());
					}
				}
				else
				{
					return list.set(selectedSourceIndex, itemOpt.get());
				}
			}
			else
			{
				E previousItem = CollectionUtil.setInSortedList(list, selectedSourceIndex, itemOpt.get(), sorter, distincter);
				if (previousItem != null)
				{
					// select the new item if the edit was successfull
					selectionModel.select(itemOpt.get());
					return previousItem;
				}
			}
			if (alreadyContainedInformer != null)
			{
				alreadyContainedInformer.accept(itemOpt.get());
			}
		}
		return null;
	}

	public E removeSelected()
	{
		if (selectionModel.getSelectedIndex() < 0)
		{
			return null;
		}
		if (removeConfirmer == null || removeConfirmer.test(selectionModel.getSelectedItem()))
		{
			int selectedIndex = selectionModel.getSelectedIndex();
			E removedItem = list.remove(getSelectedSourceIndex());
			// Reselect the selectedIndex (standard behavior selects selectedIndex-1)
			if (!getSelectableList().isEmpty() && selectedIndex < getSelectableList().size())
			{
				selectionModel.select(selectedIndex);
			}
			return removedItem;
		}
		return null;
	}

	// internal
	private BooleanBinding getNoIndexSelectedBinding()
	{
		// only build this binding if it is needed
		if (noIndexSelected == null)
		{
			noIndexSelected = selectionModel.selectedIndexProperty().lessThan(0);
		}
		return noIndexSelected;
	}
}
