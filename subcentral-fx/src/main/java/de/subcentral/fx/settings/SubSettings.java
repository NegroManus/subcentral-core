package de.subcentral.fx.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.ObservableHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class SubSettings implements Observable
{
	protected ObservableHelper	observableHelper	= new ObservableHelper(this);
	private BooleanProperty		changed				= new SimpleBooleanProperty(this, "changed", false);

	public SubSettings()
	{
		observableHelper.addListener((Observable o) -> changed.set(true));
	}

	@Override
	public void addListener(InvalidationListener listener)
	{
		observableHelper.addListener(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener)
	{
		observableHelper.removeListener(listener);
	};

	public abstract String getKey();

	public ReadOnlyBooleanProperty changedProperty()
	{
		return changed;
	}

	/**
	 * Whether the settings changed since initial load
	 */
	public boolean getChanged()
	{
		return changed.get();
	}

	public final void load(XMLConfiguration cfg)
	{
		FxUtil.requireFxApplicationThread();
		doLoad(cfg);
		changed.set(false);
	}

	protected abstract void doLoad(XMLConfiguration cfg);

	public final void save(XMLConfiguration cfg)
	{
		FxUtil.requireFxApplicationThread();
		doSave(cfg);
		changed.set(false);
	}

	protected abstract void doSave(XMLConfiguration cfg);
}
