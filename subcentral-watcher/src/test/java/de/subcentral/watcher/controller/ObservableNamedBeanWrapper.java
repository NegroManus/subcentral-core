package de.subcentral.watcher.controller;

import java.util.Objects;

import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.util.TimeUtil;

public class ObservableNamedBeanWrapper<T> extends ObservableBeanWrapper<T>
{
    protected final NamingService		  namingService;
    protected final ObservableMap<String, Object> namingParameters = FXCollections.observableHashMap();
    protected StringBinding			  computedName;

    public ObservableNamedBeanWrapper(T bean, NamingService namingService)
    {
	super(bean);
	this.namingService = Objects.requireNonNull(namingService, "namingService");
	computedName = new StringBinding()
	{
	    {
		super.bind(ObservableNamedBeanWrapper.this, namingParameters);
	    }

	    @Override
	    protected String computeValue()
	    {
		long start = System.nanoTime();
		String name = namingService.name(bean, namingParameters);
		TimeUtil.printDurationMillis("NamedBean.computedName.computeValue(): " + name, start);
		return name;
	    }
	};
    }

    public String getName()
    {
	return computedName.get();
    }

    public StringBinding nameBinding()
    {
	return computedName;
    }

    public String getComputedName()
    {
	return computedName.get();
    }

    public StringBinding computedNameBinding()
    {
	return computedName;
    }

    public NamingService getNamingService()
    {
	return namingService;
    }

    public ObservableMap<String, Object> getNamingParameters()
    {
	return namingParameters;
    }
}
