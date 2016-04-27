package de.subcentral.core.name;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.util.Context;
import de.subcentral.core.util.IterableComparator;
import de.subcentral.core.util.ObjectUtil;

public class NamingUtil
{
	public static final Comparator<Media>			DEFAULT_MEDIA_NAME_COMPARATOR			= new MediaNameComparator(NamingDefaults.getDefaultNamingService());
	public static final Comparator<Iterable<Media>>	DEFAULT_MEDIA_ITERABLE_NAME_COMPARATOR	= IterableComparator.create(DEFAULT_MEDIA_NAME_COMPARATOR);
	private static final Context					MEDIA_NAME_COMPARATOR_CTX				= Context.of(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE);

	private NamingUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static <T> Predicate<T> filterByName(T obj, NamingService namingService, Context ctx)
	{
		String requiredName = namingService.name(obj, ctx);
		return filterByName(requiredName, namingService, ctx);
	}

	public static <T> Predicate<T> filterByName(String requiredName, NamingService namingService, Context ctx)
	{
		return (T obj) -> requiredName.isEmpty() ? true : requiredName.equals(namingService.name(obj, ctx));
	}

	public static <T, U> Predicate<T> filterByNestedName(T obj, Function<T, U> nestedObjRetriever, NamingService namingService, Function<U, ? extends Iterable<Context>> ctxGenerator)
	{
		// acceptedNames can be empty. Then no restriction is made and all names are valid
		U nestedObj = nestedObjRetriever.apply(obj);
		Set<String> requiredNames = generateNames(nestedObj, ImmutableList.of(namingService), ctxGenerator.apply(nestedObj));
		return filterByNestedName(requiredNames, nestedObjRetriever, namingService, ctxGenerator);
	}

	public static <T, U> Predicate<T> filterByNestedName(Set<String> requiredNames,
			Function<T, U> nestedObjRetriever,
			NamingService namingServices,
			Function<U, ? extends Iterable<Context>> ctxGenerator)
	{
		return (T obj) ->
		{
			U nestedObj = nestedObjRetriever.apply(obj);
			Set<String> candidateNames = generateNames(nestedObj, ImmutableList.of(namingServices), ctxGenerator.apply(nestedObj));
			// If requiredNames is empty, all names are accepted. Otherwise return true, if any of the candidate's names matches a required name
			return requiredNames.isEmpty() ? true : !Collections.disjoint(requiredNames, candidateNames);
		};
	}

	public static Set<String> generateNames(Object obj, Iterable<NamingService> namingServices, Iterable<Context> contexts)
	{
		// LinkedHashSet to keep insertion order
		Set<String> names = new LinkedHashSet<>();
		for (NamingService ns : namingServices)
		{
			for (Context ctx : contexts)
			{
				String name = ns.name(obj, ctx);
				if (StringUtils.isNotEmpty(name))
				{
					names.add(name);
				}
			}
		}
		return names;
	}

	public static <T> Function<T, List<Context>> getDefaultParameterGenerator()
	{
		return (T obj) -> ImmutableList.of(Context.EMPTY);
	}

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
			return ObjectUtil.getDefaultStringOrdering().compare(namingService.name(o1, MEDIA_NAME_COMPARATOR_CTX), namingService.name(o2, MEDIA_NAME_COMPARATOR_CTX));
		}
	}
}
