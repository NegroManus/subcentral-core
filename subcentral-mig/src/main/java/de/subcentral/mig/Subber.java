package de.subcentral.mig;

import com.google.common.base.MoreObjects;

public class Subber extends AbstractContributor
{
	private int id;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Subber.class).omitNullValues().add("id", id).add("name", name).toString();
	}
}