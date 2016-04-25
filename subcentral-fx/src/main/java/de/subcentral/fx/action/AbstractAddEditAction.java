package de.subcentral.fx.action;

import java.util.Comparator;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.scene.control.SelectionModel;

public abstract class AbstractAddEditAction<E> extends AbstractItemAction<E>
{
	protected Comparator<? super E>	comparator;
	protected boolean				distinct;
	protected Consumer<? super E>	alreadyExistedInformer;

	public AbstractAddEditAction(ObservableList<E> items, SelectionModel<E> selectionModel)
	{
		super(items, selectionModel);
	}

	public Comparator<? super E> getComparator()
	{
		return comparator;
	}

	public void setComparator(Comparator<? super E> comparator)
	{
		this.comparator = comparator;
	}

	public boolean isDistinct()
	{
		return distinct;
	}

	public void setDistinct(boolean distinct)
	{
		this.distinct = distinct;
	}

	public Consumer<? super E> getAlreadyExistedInformer()
	{
		return alreadyExistedInformer;
	}

	public void setAlreadyExistedInformer(Consumer<? super E> alreadyExistedInformer)
	{
		this.alreadyExistedInformer = alreadyExistedInformer;
	}
}
