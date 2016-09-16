package de.subcentral.core.name;

import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.MoreObjects;

import de.subcentral.core.util.Context;

public class DecoratingNamingService implements NamingService {
	private final String					name;
	private final NamingService				original;
	private final Function<String, String>	finalFormatter;

	public DecoratingNamingService(String name, NamingService original, Function<String, String> finalFormatter) {
		this.name = Objects.requireNonNull(name, "name");
		this.original = Objects.requireNonNull(original, "original");
		this.finalFormatter = Objects.requireNonNull(finalFormatter, "finalFormatter");
	}

	@Override
	public String getName() {
		return name;
	}

	public NamingService getOriginal() {
		return original;
	}

	public Function<String, String> getFinalFormatter() {
		return finalFormatter;
	}

	@Override
	public String name(Object obj, Context ctx) {
		String objName = original.name(obj, ctx);
		if (objName != null) {
			return finalFormatter.apply(objName);
		}
		return null;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(DecoratingNamingService.class).add("name", name).add("original", original).toString();
	}
}
