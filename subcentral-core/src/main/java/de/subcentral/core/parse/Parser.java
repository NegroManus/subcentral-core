package de.subcentral.core.parse;

import java.util.function.Function;

public interface Parser<T> extends Function<String, T> {
	public T parse(String text);

	@Override
	public default T apply(String text) {
		return parse(text);
	}
}
