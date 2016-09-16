package de.subcentral.core.correct;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;

public class ReleaseTagsCorrector extends SinglePropertyCorrector<Release, List<Tag>> {
	public ReleaseTagsCorrector(TagsReplacer replacer) {
		super(replacer);
	}

	@Override
	public TagsReplacer getReplacer() {
		return (TagsReplacer) super.getReplacer();
	}

	@Override
	public String getPropertyName() {
		return Release.PROP_TAGS.getPropName();
	}

	@Override
	protected List<Tag> getValue(Release bean) {
		return bean.getTags();
	}

	@Override
	protected void setValue(Release bean, List<Tag> value) {
		bean.setTags(value);
	}

	@Override
	protected List<Tag> cloneValue(List<Tag> value) {
		return ImmutableList.copyOf(value);
	}
}
