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

	/**
	 * 
	 * @return the {@link #getTitle() title} if this media {@link #isTitled() is titled}, else the {@link #getName() name}
	 */
	@Override
	public String getTitleOrName()
	{
		return isTitled() ? title : name;
	}
}
