package de.subcentral.core.name;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.util.Context;
import de.subcentral.core.util.Service;

public interface NamingService extends Service, Function<Object, String> {
	public static String DEFAULT_SEPARATOR = " ";

	public default String name(Object obj) {
		return name(obj, Context.EMPTY);
	}

	/**
	 * 
	 * @param obj
	 *            the object to name (can be null)
	 * @param ctx
	 *            the context in which the object should be named
	 * @return the generated name of the candidate, an empty string {@code ""} if the candidate was {@code null} or {@code null} if the candidate could not be named
	 */
	public String name(Object obj, Context ctx);

	public default String nameAll(Iterable<?> objects) {
		return nameAll(objects, DEFAULT_SEPARATOR, Context.EMPTY);
	}

	public default String nameAll(Iterable<?> objects, String separator) {
		return nameAll(objects, separator, Context.EMPTY);
	}

	public default String nameAll(Iterable<?> objects, Context ctx) {
		return nameAll(objects, DEFAULT_SEPARATOR, ctx);
	}

	public default String nameAll(Iterable<?> objects, String separator, Context ctx) {
		StringBuilder fullName = new StringBuilder();
		Iterator<?> iter = objects.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			String name = name(obj, ctx);
			if (StringUtils.isNotEmpty(name)) {
				fullName.append(name);
				if (iter.hasNext()) {
					fullName.append(separator);
				}
			}
		}
		return fullName.toString();
	}

	@Override
	public default String apply(Object obj) {
		return name(obj);
	}
}
