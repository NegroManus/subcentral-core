package de.subcentral.core.metadata.release;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import de.subcentral.core.util.CollectionUtil;
import de.subcentral.core.util.IterableComparator;
import de.subcentral.core.util.StringUtil;

public class Tags {
	public static final Comparator<Iterable<Tag>> COMPARATOR = IterableComparator.create();

	private Tags() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static List<Tag> of(String... tagNames) {
		return of(Arrays.stream(tagNames));
	}

	public static List<Tag> of(Collection<String> tagNames) {
		return of(tagNames.stream());
	}

	public static List<Tag> of(Iterable<String> tagNames) {
		return of(StreamSupport.stream(tagNames.spliterator(), false));
	}

	public static List<Tag> of(Stream<String> tagNames) {
		return tagNames.map(Tag::of).collect(CollectionUtil.collectToImmutableList());
	}

	public static List<Tag> split(String tagNames) {
		return split(tagNames, StringUtil.COMMA_SPLITTER);
	}

	public static List<Tag> split(String tagNames, Splitter splitter) {
		return Tags.of(splitter.split(tagNames));
	}

	public static String join(List<Tag> tags) {
		return join(tags, StringUtil.COMMA_JOINER);
	}

	public static String join(List<Tag> tags, Joiner joiner) {
		return joiner.join(tags.stream().map(Tag::getName).iterator());
	}
}
