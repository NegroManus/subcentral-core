package de.subcentral.core.standardizing;

import java.util.List;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;

public class ReleaseTagsStandardizer extends SinglePropertyStandardizer<Release, List<Tag>, TagsReplacer>
{
	public ReleaseTagsStandardizer(TagsReplacer replacer)
	{
		super(Release.class, Release.PROP_TAGS.getPropName(), replacer);
	}

	@Override
	protected List<Tag> getValue(Release bean)
	{
		return bean.getTags();
	}

	@Override
	protected void setValue(Release bean, List<Tag> value)
	{
		bean.setTags(value);
	}
}
