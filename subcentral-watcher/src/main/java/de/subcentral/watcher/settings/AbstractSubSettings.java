package de.subcentral.watcher.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.watcher.model.ObservableObject;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * 
 * package private
 *
 */
abstract class AbstractSubSettings extends ObservableObject
{
    private BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);

    AbstractSubSettings()
    {
	addListener((Observable o) -> changed.set(true));
    }

    public abstract String getKey();

    public BooleanProperty changedProperty()
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
	doLoad(cfg);
	changed.set(false);
    }

    protected abstract void doLoad(XMLConfiguration cfg);

    public final void save(XMLConfiguration cfg)
    {
	doSave(cfg);
	changed.set(false);
    }

    protected abstract void doSave(XMLConfiguration cfg);
}
