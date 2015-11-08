package de.subcentral.mig;

import java.io.Serializable;

import com.google.common.base.MoreObjects;

public class Subber extends AbstractContributor implements Serializable
{
	private static final long	serialVersionUID	= 5795083532465172235L;

	private int					id;

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