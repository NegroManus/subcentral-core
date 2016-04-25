package de.subcentral.fx.action;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import de.subcentral.core.util.CollectionUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableView;

public class EditAction<E> extends AbstractAddEditAction<E>
{
	private final Function<E, Optional<E>> itemEditer;

	public EditAction(ListView<E> listView, Function<E, Optional<E>> itemEditer)
	{
		this(listView.getItems(), listView.getSelectionModel(), itemEditer);
	}

	public EditAction(TableView<E> tableView, Function<E, Optional<E>> itemEditer)
	{
		this(tableView.getItems(), tableView.getSelectionModel(), itemEditer);
	}

	public EditAction(ObservableList<E> items, SelectionModel<E> selectionModel, Function<E, Optional<E>> itemEditer)
	{
		super(items, selectionModel);
		this.itemEditer = Objects.requireNonNull(itemEditer, "itemEditer");
	}

	@Override
	public void handle(ActionEvent event)
	{
		if (selectionModel.getSelectedItem() == null)
		{
			return;
		}
		Optional<E> itemOpt = itemEditer.apply(selectionModel.getSelectedItem());
		if (itemOpt.isPresent())
		{
			if (comparator == null)
			{
				if (distinct)
				{
					int indexOfExisting = items.indexOf(itemOpt.get());
					if (indexOfExisting < 0 || indexOfExisting == selectionModel.getSelectedIndex())
					{
						items.set(selectionModel.getSelectedIndex(), itemOpt.get());
						return;
					}
				}
				else
				{
					items.set(selectionModel.getSelectedIndex(), itemOpt.get());
					return;
				}
			}
			else
			{
				if (CollectionUtil.setInSortedList(items, selectionModel.getSelectedIndex(), itemOpt.get(), comparator, distinct) != null)
				{
					return;
				}
			}

			if (alreadyExistedInformer != null)
			{
				alreadyExistedInformer.accept(itemOpt.get());
			}
		}
	}
}
