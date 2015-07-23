package de.subcentral.core.naming;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import de.subcentral.core.Settings;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.util.IterableComparator;

public class NamingUtil
{
    public static final Comparator<Media>	    DEFAULT_MEDIA_NAME_COMPARATOR	   = new MediaNameComparator(NamingDefaults.getDefaultNamingService());
    public static final Comparator<Iterable<Media>> DEFAULT_MEDIA_ITERABLE_NAME_COMPARATOR = IterableComparator.create(DEFAULT_MEDIA_NAME_COMPARATOR);

    public static final class MediaNameComparator implements Comparator<Media>, Serializable
    {
	// Comparators should be Serializable
	private static final long serialVersionUID = -3197188465533525469L;

	private final NamingService namingService;

	public MediaNameComparator(NamingService namingService)
	{
	    this.namingService = Objects.requireNonNull(namingService, "namingService");
	}

	@Override
	public int compare(Media o1, Media o2)
	{
	    // nulls first as naming of null results in an empty string "" and an empty string always comes first
	    return Settings.STRING_ORDERING.compare(namingService.name(o1), namingService.name(o2));
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
	return (T obj) -> {
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

    public static <T, U> Predicate<T> filterByNestedName(T obj, NamingService namingService, Map<String, Object> parameters, Function<T, U> nestedObjRetriever)
    {
	String requiredMediaName = namingService.name(nestedObjRetriever.apply(obj), parameters);
	return filterByNestedName(requiredMediaName, namingService, parameters, nestedObjRetriever);
    }

    public static <T, U> Predicate<T> filterByNestedName(String requiredName, NamingService namingService, Map<String, Object> parameters, Function<T, U> nestedObjRetriever)
    {
	return (T obj) -> {
	    String nameOfCandidate = namingService.name(nestedObjRetriever.apply(obj), parameters);
	    boolean accepted = requiredName.isEmpty() ? true : requiredName.equals(nameOfCandidate);
	    return accepted;
	};
    }

    public static List<String> generateNames(Object obj, NamingService namingService, List<Map<String, Object>> parametersList)
    {
	List<String> names = new ArrayList<>(parametersList.size());
	for (Map<String, Object> parameters : parametersList)
	{
	    names.add(namingService.name(obj, parameters));
	}
	return names;
    }

    private NamingUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
