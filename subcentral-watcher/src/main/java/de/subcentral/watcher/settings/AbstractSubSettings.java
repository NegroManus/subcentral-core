package de.subcentral.watcher.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.watcher.model.ObservableObject;

/**
 * 
 * package private
 *
 */
abstract class AbstractSubSettings extends ObservableObject
{
    protected abstract String getKey();

    protected abstract void load(XMLConfiguration cfg);

    protected abstract void save(XMLConfiguration cfg);
}
