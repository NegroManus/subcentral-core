package de.subcentral.core.metadata.media;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNamedMedia extends AbstractMedia
{
	protected String		name;
	protected List<String>	aliasNames	= new ArrayList<>(0);

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public List<String> getAliasNames()
	{
		return aliasNames;
	}

	public void setAliasNames(List<String> aliasNames)
	{
		this.aliasNames.clear();
		this.aliasNames.addAll(aliasNames);
	}
}
