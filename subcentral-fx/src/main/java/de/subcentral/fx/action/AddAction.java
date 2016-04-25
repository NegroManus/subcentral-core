package de.subcentral.fx.action;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import de.subcentral.core.util.CollectionUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableView;

public class AddAction<E> extends AbstractAddEditAction<E>
{
	private final Supplier<Optional<E>> itemSupplier;

	public AddAction(ListView<E> listView, Supplier<Optional<E>> itemSupplier)
	{
		this(listView.getItems(), listView.getSelectionModel(), itemSupplier);
	}

	public AddAction(TableView<E> tableView, Supplier<Optional<E>> itemSupplier)
	{
		this(tableView.getItems(), tableView.getSelectionModel(), itemSupplier);
	}

	public AddAction(ObservableList<E> items, SelectionModel<E> selectionModel, Supplier<Optional<E>> itemSupplier)
	{
		super(items, selectionModel);
		this.itemSupplier = Objects.requireNonNull(itemSupplier, "itemSupplier");
	}

	@Override
	public void handle(ActionEvent event)
	{
		Optional<E> itemOpt = itemSupplier.get();
		if (itemOpt.isPresent())
		{
			if (comparator == null)
			{
				if ((!distinct || !items.contains(itemOpt.get())))
				{
					int addIndex = selectionModel.getSelectedIndex() >= 0 ? selectionModel.getSelectedIndex() + 1 : items.size();
					items.add(addIndex, itemOpt.get());
					return;
				}
			}
			else
			{
				if (CollectionUtil.addToSortedList(items, itemOpt.get(), comparator, distinct))
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
