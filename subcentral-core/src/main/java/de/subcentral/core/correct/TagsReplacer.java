package de.subcentral.core.correct;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.CollectionUtil;
import de.subcentral.core.util.CollectionUtil.ReplaceMode;
import de.subcentral.core.util.CollectionUtil.SearchMode;

public class TagsReplacer implements UnaryOperator<List<Tag>> {
	private final List<Tag>		searchTags;
	private final SearchMode	searchMode;
	private final List<Tag>		replacement;
	private final ReplaceMode	replaceMode;
	private final boolean		ignoreOrder;

	public TagsReplacer(List<Tag> searchTags, List<Tag> replacement) {
		this(searchTags, replacement, SearchMode.CONTAIN, ReplaceMode.MATCHED_SEQUENCE, false);
	}

	public TagsReplacer(List<Tag> searchTags, List<Tag> replacement, SearchMode searchMode, ReplaceMode replaceMode, boolean ignoreOrder) {
		this.searchTags = ImmutableList.copyOf(searchTags);
		this.searchMode = Objects.requireNonNull(searchMode, "searchMode");
		this.replacement = ImmutableList.copyOf(replacement);
		this.replaceMode = Objects.requireNonNull(replaceMode, "replaceMode");
		this.ignoreOrder = ignoreOrder;
	}

	public List<Tag> getSearchTags() {
		return searchTags;
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}

	public List<Tag> getReplacement() {
		return replacement;
	}

	public ReplaceMode getReplaceMode() {
		return replaceMode;
	}

	public boolean getIgnoreOrder() {
		return ignoreOrder;
	}

	@Override
	public List<Tag> apply(List<Tag> tags) {
		switch (searchMode) {
			case CONTAIN:
				return replaceContain(tags);
			case EQUAL:
				return replaceEqual(tags);
			default:
				throw new AssertionError();
		}
	}

	private List<Tag> replaceContain(List<Tag> tags) {
		switch (replaceMode) {
			case COMPLETE_LIST:
				return replaceContainComplete(tags);
			case MATCHED_SEQUENCE:
				return replaceContainSequences(tags);
			default:
				throw new AssertionError();
		}
	}

	private List<Tag> replaceContainComplete(List<Tag> tags) {
		if (ignoreOrder) {
			if (tags.containsAll(searchTags)) {
				return replacement;
			}
		}
		else {
			if (CollectionUtil.containsSequence(tags, searchTags)) {
				return replacement;
			}
		}
		return tags;
	}

	private List<Tag> replaceEqual(List<Tag> tags) {
		if (ignoreOrder) {
			if (CollectionUtil.equalsIgnoreOrder(tags, searchTags)) {
				return replacement;
			}
		}
		else {
			if (tags.equals(searchTags)) {
				return replacement;
			}
		}
		return tags;
	}

	private List<Tag> replaceContainSequences(List<Tag> tags) {
		List<Tag> result = tags;
		for (int i = 0; i < result.size() && i + searchTags.size() <= result.size(); i++) {
			List<Tag> sublist = result.subList(i, i + searchTags.size());
			if (ignoreOrder ? CollectionUtil.equalsIgnoreOrder(sublist, searchTags) : sublist.equals(searchTags)) {
				if (result == tags) {
					result = new ArrayList<>(tags);
					result.subList(i, i + searchTags.size()).clear();
				}
				else {
					sublist.clear();
				}
				result.addAll(i, replacement);
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof TagsReplacer) {
			TagsReplacer o = (TagsReplacer) obj;
			return searchTags.equals(o.searchTags) && searchMode == o.searchMode && replacement.equals(o.replacement) && replaceMode == o.replaceMode && ignoreOrder == o.ignoreOrder;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(5643, 121).append(searchTags).append(searchMode).append(replacement).append(replaceMode).append(ignoreOrder).toHashCode();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(TagsReplacer.class)
				.add("searchTags", searchTags)
				.add("searchMode", searchMode)
				.add("replacement", replacement)
				.add("replaceMode", replaceMode)
				.add("ignoreOrder", ignoreOrder)
				.toString();
	}
}