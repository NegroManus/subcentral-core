package de.subcentral.core.standardizing;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;

public class ReleaseTagsStandardizer extends SinglePropertyStandardizer<Release, List<Tag>>
{
	public ReleaseTagsStandardizer(TagsReplacer replacer)
	{
		super(replacer);
	}

	@Override
	public Class<Release> getBeanType()
	{
		return Release.class;
	}

	@Override
	public String getPropertyName()
	{
		return Release.PROP_TAGS.getPropName();
	}

	@Override
	protected List<Tag> getValue(Release bean)
	{
		return ImmutableList.copyOf(bean.getTags());
	}

	@Override
	protected void setValue(Release bean, List<Tag> value)
	{
		bean.setTags(value);
	}

	@Override
	public TagsReplacer getReplacer()
	{
		return (TagsReplacer) super.getReplacer();
	}

}
