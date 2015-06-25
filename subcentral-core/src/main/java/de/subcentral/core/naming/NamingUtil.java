package de.subcentral.core.naming;

import java.io.Serializable;
import java.util.Comparator;
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

    public static <T> Predicate<T> filterByName(T obj, NamingService mediaNamingService, Map<String, Object> namingParams)
    {
	String requiredName = mediaNamingService.name(obj, namingParams);
	return filterByName(requiredName, mediaNamingService, namingParams);
    }

    public static <T> Predicate<T> filterByName(String requiredName, NamingService mediaNamingService, Map<String, Object> namingParams)
    {
	return (T obj) -> {
	    String nameOfCandidate = mediaNamingService.name(obj, namingParams);
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

    public static <T, U> Predicate<T> filterByNestedName(T obj, NamingService mediaNamingService, Map<String, Object> namingParams, Function<T, U> nestedObjRetriever)
    {
	String requiredMediaName = mediaNamingService.name(nestedObjRetriever.apply(obj), namingParams);
	return filterByNestedName(requiredMediaName, mediaNamingService, namingParams, nestedObjRetriever);
    }

    public static <T, U> Predicate<T> filterByNestedName(String requiredName, NamingService mediaNamingService, Map<String, Object> namingParams, Function<T, U> nestedObjRetriever)
    {
	return (T obj) -> {
	    String nameOfCandidate = mediaNamingService.name(nestedObjRetriever.apply(obj), namingParams);
	    boolean accepted = requiredName.isEmpty() ? true : requiredName.equals(nameOfCandidate);
	    return accepted;
	};
    }

    private NamingUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
