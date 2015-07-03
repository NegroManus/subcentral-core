package de.subcentral.mig;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.metadata.Contributor;

public class AbstractContributor implements Contributor
{
    protected String name;

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
    public boolean equals(Object obj)
    {
	if (this == obj)
	{
	    return true;
	}
	if (obj instanceof Subber)
	{
	    return Objects.equals(name, ((Subber) obj).name);
	}
	return false;
    }

    @Override
    public int hashCode()
    {
	return new HashCodeBuilder(973, 59).append(name).toHashCode();
    }
}