package de.subcentral.core.parse;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public class MultiParsingService implements ParsingService {
	private final String				name;
	private final List<ParsingService>	services;

	public MultiParsingService(String name, ParsingService... services) {
		this(name, ImmutableList.copyOf(services));
	}

	public MultiParsingService(String domain, Iterable<? extends ParsingService> services) {
		this.name = Objects.requireNonNull(domain, "name");
		this.services = ImmutableList.copyOf(services);
	}

	@Override
	public String getName() {
		return name;
	}

	public List<ParsingService> getServices() {
		return services;
	}

	@Override
	public Set<Class<?>> getSupportedTargetTypes() {
		return services.stream().flatMap((ParsingService ps) -> ps.getSupportedTargetTypes().stream()).collect(Collectors.toSet());
	}

	@Override
	public Object parse(String text) {
		for (ParsingService ps : services) {
			Object parsedObj = ps.parse(text);
			if (parsedObj != null) {
				return parsedObj;
			}
		}
		return null;
	}

	@Override
	public <T> T parse(String text, Class<T> targetType) {
		for (ParsingService ps : services) {
			T parsedObj = ps.parse(text, targetType);
			if (parsedObj != null) {
				return parsedObj;
			}
		}
		return null;
	}

	@Override
	public Object parse(String text, Set<Class<?>> targetTypes) {
		for (ParsingService ps : services) {
			Object parsedObj = ps.parse(text, targetTypes);
			if (parsedObj != null) {
				return parsedObj;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(MultiParsingService.class).add("name", name).add("services", services).toString();
	}
}
