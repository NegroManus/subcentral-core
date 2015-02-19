package de.subcentral.core.metadata.release;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

public class CommonReleaseDefinition
{
	public enum AssumeExistence
	{
		ALWAYS, IF_GUESSING
	};

	// private final Predicate<List<Media>> mediaFilter;
	private final Release			commonRelease;
	private final AssumeExistence	assumeExistence;

	public CommonReleaseDefinition(Release partialRelease, AssumeExistence assumeExistence)
	{
		this(partialRelease.getTags(), partialRelease.getGroup(), assumeExistence);
	}

	public CommonReleaseDefinition(List<Tag> tags, Group group, AssumeExistence assumeExistence)
	{
		this.commonRelease = new Release(tags, group);
		this.assumeExistence = Objects.requireNonNull(assumeExistence, "assumeExistence");
	}

	/**
	 * Only stores tags and group.
	 * 
	 * @return the common release
	 */
	public Release getCommonRelease()
	{
		return commonRelease;
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
		if (obj instanceof CommonReleaseDefinition)
		{
			return commonRelease.equals(((CommonReleaseDefinition) obj).commonRelease);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(53, 21).append(commonRelease).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(CommonReleaseDefinition.class).add("commonRelease", commonRelease).add("assumeExistence", assumeExistence).toString();
	}
}