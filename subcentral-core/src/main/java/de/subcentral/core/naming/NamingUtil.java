package de.subcentral.core.naming;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.Settings;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.util.IterableComparator;

public class NamingUtil
{
	public static final Comparator<Media>			DEFAULT_MEDIA_NAME_COMPARATOR			= new MediaNameComparator(NamingDefaults.getDefaultNamingService());
	public static final Comparator<Iterable<Media>>	DEFAULT_MEDIA_ITERABLE_NAME_COMPARATOR	= IterableComparator.create(DEFAULT_MEDIA_NAME_COMPARATOR);
	private static final Map<String, Object>		DEFAULT_MEDIA_NAME_PARAMS				= ImmutableMap.of(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE);

	public static final class MediaNameComparator implements Comparator<Media>, Serializable
	{
		// Comparators should be Serializable
		private static final long	serialVersionUID	= -3197188465533525469L;

		private final NamingService	namingService;

		public MediaNameComparator(NamingService namingService)
		{
			this.namingService = Objects.requireNonNull(namingService, "namingService");
		}

		@Override
		public int compare(Media o1, Media o2)
		{
			// nulls first as naming of null results in an empty string "" and an empty string always comes first
			return Settings.STRING_ORDERING.compare(namingService.name(o1, DEFAULT_MEDIA_NAME_PARAMS), namingService.name(o2, DEFAULT_MEDIA_NAME_PARAMS));
		}
	}

	public static final <T> T readParameter(Map<String, Object> parameters, String key, Class<T> valueClass, T defaultValue)
	{
		return valueClass.cast(parameters.getOrDefault(key, defaultValue));
	}

	public static <T> Predicate<T> filterByName(T obj, NamingService namingService, Map<String, Object> parameters)
	{
		String requiredName = namingService.name(obj, parameters);
		return filterByName(requiredName, namingService, parameters);
	}

	public static <T> Predicate<T> filterByName(String requiredName, NamingService namingService, Map<String, Object> parameters)
	{
		return (T obj) ->
		{
			String nameOfCandidate = namingService.name(obj, parameters);
			boolean accepted = requiredName.isEmpty() ? true : requiredName.equals(nameOfCandidate);
			if (accepted)
			{
				System.out.println("accepted " + nameOfCandidate);
			}
			else
			{
				System.out.println("denied " + nameOfCandidate);
			}

			return accepted;
		};
	}

	public static <T, U> Predicate<T> filterByNestedName(T obj, Function<T, U> nestedObjRetriever, NamingService namingService, Function<U, List<Map<String, Object>>> parameterGenerator)
	{
		// acceptedNames can be empty. Then no restriction is made and all names are valid
		U nestedObj = nestedObjRetriever.apply(obj);
		Set<String> requiredNames = generateNames(nestedObj, ImmutableList.of(namingService), parameterGenerator.apply(nestedObj));
		return filterByNestedName(requiredNames, nestedObjRetriever, namingService, parameterGenerator);
	}

	public static <T, U> Predicate<T> filterByNestedName(Set<String> requiredNames,
			Function<T, U> nestedObjRetriever,
			NamingService namingServices,
			Function<U, List<Map<String, Object>>> parameterGenerator)
	{
		return (T obj) ->
		{
			U nestedObj = nestedObjRetriever.apply(obj);
			Set<String> candidateNames = generateNames(nestedObj, ImmutableList.of(namingServices), parameterGenerator.apply(nestedObj));
			// If requiredNames is empty, all names are accepted. Otherwise return true, if any of the candidate's names matches a required name
			boolean accepted = requiredNames.isEmpty() ? true : !Collections.disjoint(requiredNames, candidateNames);
			return accepted;
		};
	}

	public static Set<String> generateNames(Object obj, List<NamingService> namingServices, List<Map<String, Object>> parameters)
	{
		// LinkedHashSet to keep insertion order
		Set<String> names = new LinkedHashSet<>();
		for (NamingService ns : namingServices)
		{
			try
			{
				for (Map<String, Object> params : parameters)
				{
					String name = ns.name(obj, params);
					if (!name.isEmpty())
					{
						names.add(name);
					}
				}
			}
			catch (NoNamerRegisteredException e)
			{
				// ignore
			}
		}
		return names;
	}

	public static <T> Function<T, List<Map<String, Object>>> getDefaultParameterGenerator()
	{
		return (T obj) -> ImmutableList.of(ImmutableMap.of());
	}

	private NamingUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
