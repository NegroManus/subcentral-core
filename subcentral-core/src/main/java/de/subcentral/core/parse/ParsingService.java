package de.subcentral.core.parse;

import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.util.Service;

public interface ParsingService extends Service, Function<String, Object> {
	public Set<Class<?>> getSupportedTargetTypes();

	/**
	 * 
	 * @param text
	 *            the text which should be parsed to an object
	 * @return the parsed object or null if the text could not be parsed
	 */
	public Object parse(String text);

	/**
	 * 
	 * @param text
	 *            the text which should be parsed to an object
	 * @param targetType
	 *            the type of the parsed object
	 * @return the parsed object or null if the text could not be parsed
	 */
	public <T> T parse(String text, Class<T> targetType);

	/**
	 * 
	 * @param text
	 *            the text which should be parsed to an object
	 * @param targetTypes
	 *            the allowed types of the parsed object
	 * @return the parsed object or null if the text could not be parsed
	 */
	public Object parse(String text, Set<Class<?>> targetTypes);

	@Override
	public default Object apply(String text) {
		return parse(text);
	}
}
