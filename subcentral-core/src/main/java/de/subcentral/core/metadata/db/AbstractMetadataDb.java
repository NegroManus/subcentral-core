package de.subcentral.core.metadata.db;

public abstract class AbstractMetadataDb<T> implements MetadataDb<T>
{
    @Override
    public String toString()
    {
	return getName();
    }
}
