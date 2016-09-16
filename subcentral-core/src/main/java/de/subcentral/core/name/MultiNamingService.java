package de.subcentral.core.name;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.Context;

public class MultiNamingService implements NamingService {
	private final String				name;
	private final List<NamingService>	services;

	public MultiNamingService(String name, NamingService... services) {
		this(name, ImmutableList.copyOf(services));
	}

	public MultiNamingService(String name, Iterable<? extends NamingService> services) {
		this.name = Objects.requireNonNull(name, "name");
		this.services = ImmutableList.copyOf(services);
	}

	@Override
	public String getName() {
		return name;
	}

	public List<NamingService> getServices() {
		return services;
	}

	@Override
	public String name(Object obj, Context ctx) {
		for (NamingService service : services) {
			String objName = service.name(obj, ctx);
			if (objName != null) {
				return objName;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(DecoratingNamingService.class).add("name", name).add("services", services).toString();
	}
}
