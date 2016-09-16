package de.subcentral.core.name;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.subcentral.core.util.SimplePropDescriptor;

public class SimplePrintPropService implements PrintPropService {
	private final Map<Class<?>, Function<?, String>>				typePrinter	= new HashMap<>();
	private final Map<SimplePropDescriptor, Function<?, String>>	propPrinter	= new HashMap<>();

	public Map<Class<?>, Function<?, String>> getTypePrinter() {
		return typePrinter;
	}

	public Map<SimplePropDescriptor, Function<?, String>> getPropPrinter() {
		return propPrinter;
	}

	@Override
	public String print(SimplePropDescriptor propDescriptor, Object propValue) {
		return printTyped(propDescriptor, propValue);
	}

	@SuppressWarnings("unchecked")
	private <T> String printTyped(SimplePropDescriptor propDescriptor, T propValue) {
		if (propValue == null) {
			return "";
		}
		// Search for printer registered for property
		Function<?, String> printer = propPrinter.get(propDescriptor);
		if (printer != null) {
			return ((Function<T, String>) printer).apply(propValue);
		}
		// Search for printer registered for type
		printer = typePrinter.get(propValue.getClass());
		if (printer != null) {
			return ((Function<T, String>) printer).apply(propValue);
		}
		// Search for printer registered for super type
		for (Map.Entry<Class<?>, Function<?, String>> entry : typePrinter.entrySet()) {
			if (entry.getKey().isAssignableFrom(propValue.getClass())) {
				return ((Function<T, String>) entry.getValue()).apply(propValue);
			}
		}
		return propValue.toString();
	}
}
