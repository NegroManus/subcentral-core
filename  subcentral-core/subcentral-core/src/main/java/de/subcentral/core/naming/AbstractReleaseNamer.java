package de.subcentral.core.naming;

import de.subcentral.core.release.Release;

public abstract class AbstractReleaseNamer<M, R extends Release<M>> implements Namer<R>
{
	protected String	tagsSeparator	= ".";
	protected String	tagsFormat		= ".%s";
	protected String	groupFormat		= "-%s";

	public String getTagsSeparator()
	{
		return tagsSeparator;
	}

	public void setTagsSeparator(String tagsSeparator)
	{
		this.tagsSeparator = tagsSeparator;
	}

	public String getTagsFormat()
	{
		return tagsFormat;
	}

	public void setTagsFormat(String tagsFormat)
	{
		this.tagsFormat = tagsFormat;
	}

	public String getGroupFormat()
	{
		return groupFormat;
	}

	public void setGroupFormat(String groupFormat)
	{
		this.groupFormat = groupFormat;
	}
}
