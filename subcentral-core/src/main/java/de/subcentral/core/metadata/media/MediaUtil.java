package de.subcentral.core.metadata.media;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.name.AbstractNamedMediaNamer;
import de.subcentral.core.util.Context;

public class MediaUtil {
	private MediaUtil() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static boolean isMediaIterable(Object obj) {
		if (obj instanceof Iterable) {
			for (Object o : (Iterable<?>) obj) {
				if (!(o instanceof Media)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isSingletonMedia(Object obj) {
		return toSingletonMedia(obj) != null;
	}

	public static Media toSingletonMedia(Object obj) {
		if (obj instanceof Media) {
			return (Media) obj;
		}
		Iterator<?> iter;
		if (obj instanceof Iterable) {
			iter = ((Iterable<?>) obj).iterator();
		}
		else if (obj instanceof Stream) {
			iter = ((Stream<?>) obj).iterator();
		}
		else {
			iter = null;
		}
		if (iter != null && iter.hasNext()) {
			Object firstElem = iter.next();
			if (firstElem instanceof Media && !iter.hasNext()) {
				return (Media) firstElem;
			}
		}
		return null;
	}

	public static List<Context> generateNamingContextsForAllNames(Object obj) {
		Media singleMedia = toSingletonMedia(obj);
		if (singleMedia != null && obj instanceof NamedMedia) {
			NamedMedia namedMedia = (NamedMedia) obj;
			if (namedMedia.getName() != null) {
				return generateNamingContextsForMediaNames(namedMedia.getAllNames());
			}
		}
		MultiEpisodeHelper meHelper = MultiEpisodeHelper.of(obj);
		if (meHelper != null && meHelper.getCommonSeries() != null && meHelper.getCommonSeries().getName() != null) {
			return generateNamingContextsForMediaNames(meHelper.getCommonSeries().getAllNames());
		}

		// The list has to at least contain 1 entry (an empty map) because otherwise no names are generated
		return ImmutableList.of(Context.EMPTY);
	}

	public static List<Context> generateNamingContextsForMediaNames(List<String> names) {
		ImmutableList.Builder<Context> list = ImmutableList.builder();
		for (String name : names) {
			list.add(Context.of(AbstractNamedMediaNamer.PARAM_NAME, name));
		}
		return list.build();
	}
}
