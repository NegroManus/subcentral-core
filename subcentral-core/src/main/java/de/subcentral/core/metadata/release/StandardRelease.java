package de.subcentral.core.metadata.release;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

public class StandardRelease
{
    public enum AssumeExistence
    {
	ALWAYS, IF_NONE_FOUND
    };

    // private final Predicate<List<Media>> mediaFilter;
    private final Release	  release;
    private final AssumeExistence assumeExistence;

    public StandardRelease(Release release, AssumeExistence assumeExistence)
    {
	this(release.getTags(), release.getGroup(), assumeExistence);
    }

    public StandardRelease(List<Tag> tags, Group group, AssumeExistence assumeExistence)
    {
	this.release = new Release(tags, group);
	this.assumeExistence = Objects.requireNonNull(assumeExistence, "assumeExistence");
    }

    /**
     * Only stores tags and group.
     * 
     * @return the standard release
     */
    public Release getRelease()
    {
	return release;
    }

    public AssumeExistence getAssumeExistence()
    {
	return assumeExistence;
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	{
	    return true;
	}
	if (obj instanceof StandardRelease)
	{
	    return release.equals(((StandardRelease) obj).release);
	}
	return false;
    }

    @Override
    public int hashCode()
    {
	return new HashCodeBuilder(53, 21).append(release).toHashCode();
    }

    @Override
    public String toString()
    {
	return MoreObjects.toStringHelper(StandardRelease.class).add("release", release).add("assumeExistence", assumeExistence).toString();
    }
}
