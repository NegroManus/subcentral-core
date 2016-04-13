package de.subcentral.fx.settings;

import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import de.subcentral.fx.ObservableHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class SettableBase implements Settable
{
	protected final ObservableHelper	helper	= new ObservableHelper(this);
	protected final BooleanProperty		changed	= new SimpleBooleanProperty(this, "changed");

	@Override
	public void addListener(InvalidationListener listener)
	{
		helper.addListener(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener)
	{
		helper.removeListener(listener);
	}

	@Override
	public ReadOnlyBooleanProperty changedProperty()
	{
		return changed;
	}

	@Override
	public boolean changed()
	{
		return changed.get();
	}

	@Override
	public void load(URL file) throws ConfigurationException
	{
		XMLConfiguration cfg = ConfigurationHelper.load(file);
		load(cfg);
	}

	@Override
	public void load(Path file) throws ConfigurationException
	{
		XMLConfiguration cfg = ConfigurationHelper.load(file);
		load(cfg);
	}

	@Override
	public void save(Path file) throws ConfigurationException
	{
		XMLConfiguration cfg = new XMLConfiguration();
		save(cfg);
		ConfigurationHelper.save(cfg, file);
	}
}
