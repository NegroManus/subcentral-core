package de.subcentral.core.metadata.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class NamedMediaBase extends MediaBase implements NamedMedia
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

	public void setAliasNames(Collection<? extends String> aliasNames)
	{
		this.aliasNames.clear();
		this.aliasNames.addAll(aliasNames);
	}

	@Override
	public List<String> getAllNames()
	{
		ImmutableList.Builder<String> names = ImmutableList.builder();
		if (getName() != null)
		{
			names.add(getName());
		}
		names.addAll(getAliasNames());
		return names.build();
	}
}
