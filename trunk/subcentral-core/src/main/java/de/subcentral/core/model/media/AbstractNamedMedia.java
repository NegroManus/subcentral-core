package de.subcentral.core.model.media;

public abstract class AbstractNamedMedia extends AbstractMedia implements NamedMedia
{
	protected String	name;

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
