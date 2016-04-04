package de.subcentral.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.ObservableObject;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractSubSettings extends ObservableObject
{
	private BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);

	public AbstractSubSettings()
	{
		addListener((Observable o) -> changed.set(true));
	}

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
		FxUtil.requireJavaFxApplicationThread();
		doLoad(cfg);
		changed.set(false);
	}

	protected abstract void doLoad(XMLConfiguration cfg);

	public final void save(XMLConfiguration cfg)
	{
		FxUtil.requireJavaFxApplicationThread();
		doSave(cfg);
		changed.set(false);
	}

	protected abstract void doSave(XMLConfiguration cfg);
}
