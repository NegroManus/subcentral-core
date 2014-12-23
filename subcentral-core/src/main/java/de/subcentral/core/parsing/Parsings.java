package de.subcentral.core.parsing;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.RegularAvMedia;
import de.subcentral.core.model.media.RegularMedia;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.SimplePropDescriptor;

public class Parsings
{
	/**
	 * Pattern for media names like "The Lord of the Rings (2003)", "The Office (UK)".<br/>
	 * Groups
	 * <ol>
	 * <li>name</li>
	 * <li>title (may be equal to name)</li>
	 * <li>year (or null)</li>
	 * <li>country code (or null)</li>
	 * </ol>
	 */
	public static final String							PATTERN_MEDIA_NAME						= "((.*?)(?:\\s+\\((?:(\\d{4})|(\\p{Upper}{2}))\\))?)";

	private static final EpisodeMapper					EPISODE_MAPPER							= new EpisodeMapper();
	private static final Mapper<List<Episode>>			SINGLETON_LIST_EPISODE_MAPPER			= createSingletonListMapper(EPISODE_MAPPER);
	private static final MultiEpisodeMapper				MULTI_EPISODE_MAPPER					= new MultiEpisodeMapper(EPISODE_MAPPER);
	private static final RegularMediaMapper				REGULAR_MEDIA_MAPPER					= new RegularMediaMapper();
	private static final Mapper<List<RegularMedia>>		SINGLETON_LIST_REGULAR_MEDIA_MAPPER		= createSingletonListMapper(REGULAR_MEDIA_MAPPER);
	private static final RegularAvMediaMapper			REGULAR_AV_MEDIA_MAPPER					= new RegularAvMediaMapper();
	private static final Mapper<List<RegularAvMedia>>	SINGLETON_LIST_REGULAR_AV_MEDIA_MAPPER	= createSingletonListMapper(REGULAR_AV_MEDIA_MAPPER);
	private static final ReleaseMapper					RELEASE_MAPPER							= new ReleaseMapper();
	private static final SubtitleMapper					SUBTITLE_MAPPER							= new SubtitleMapper();
	private static final SubtitleAdjustmentMapper		SUBTITLE_ADJUSTMENT_MAPPER				= new SubtitleAdjustmentMapper();

	private static final <E> Mapper<List<E>> createSingletonListMapper(Mapper<E> elementMapper)
	{
		return (props, pps) -> ImmutableList.of(elementMapper.map(props, pps));
	}

	public static Mapper<Episode> getDefaultEpisodeMapper()
	{
		return EPISODE_MAPPER;
	}

	public static Mapper<List<Episode>> getDefaultSingletonListEpisodeMapper()
	{
		return SINGLETON_LIST_EPISODE_MAPPER;
	}

	public static Mapper<List<Episode>> getDefaultMultiEpisodeMapper()
	{
		return MULTI_EPISODE_MAPPER;
	}

	public static final Mapper<RegularMedia> getDefaultRegularMediaMapper()
	{
		return REGULAR_MEDIA_MAPPER;
	}

	public static Mapper<List<RegularMedia>> getDefaultSingletonListRegularMediaMapper()
	{
		return SINGLETON_LIST_REGULAR_MEDIA_MAPPER;
	}

	public static Mapper<RegularAvMedia> getDefaultRegularAvMediaMapper()
	{
		return REGULAR_AV_MEDIA_MAPPER;
	}

	public static Mapper<List<RegularAvMedia>> getDefaultSingletonListRegularAvMediaMapper()
	{
		return SINGLETON_LIST_REGULAR_AV_MEDIA_MAPPER;
	}

	public static final Mapper<Release> getDefaultReleaseMapper()
	{
		return RELEASE_MAPPER;
	}

	public static final Mapper<Subtitle> getDefaultSubtitleMapper()
	{
		return SUBTITLE_MAPPER;
	}

	public static final Mapper<SubtitleAdjustment> getDefaultSubtitleAdjustmentMapper()
	{
		return SUBTITLE_ADJUSTMENT_MAPPER;
	}

	public static void requireNotBlank(String text, Class<?> targetClass) throws NoMatchException
	{
		if (StringUtils.isBlank(text))
		{
			throw new NoMatchException(text, targetClass, "text is blank");
		}
	}

	public static final <T> void reflectiveMapping(T entity, Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		Objects.requireNonNull(entity, "entity");
		for (Map.Entry<SimplePropDescriptor, String> p : props.entrySet())
		{
			SimplePropDescriptor simplePropDescr = p.getKey();
			if (entity.getClass().equals(simplePropDescr.getBeanClass()))
			{
				try
				{
					PropertyDescriptor propDescr = simplePropDescr.toPropertyDescriptor();
					TypeToken<?> type = TypeToken.of(propDescr.getReadMethod().getGenericParameterTypes()[0]);
					if (Collection.class.isAssignableFrom(type.getRawType()))
					{
						ParameterizedType genericType = (ParameterizedType) type.getType();
						Class<?> itemClass = ((Class<?>) genericType.getActualTypeArguments()[0]);
						List<?> value = propFromStringService.parseList(p.getValue(), simplePropDescr, itemClass);
						if (Set.class.isAssignableFrom(type.getRawType()))
						{
							simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(entity, ImmutableSet.copyOf(value));
						}
						else
						{
							simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(entity, value);
						}
					}
					else
					{
						simplePropDescr.toPropertyDescriptor()
								.getWriteMethod()
								.invoke(entity, propFromStringService.parse(p.getValue(), simplePropDescr, type.wrap().getRawType()));
					}

				}
				catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParsingException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> T parse(String text, Class<T> targetClass, List<ParsingService> parsingServices) throws NoMatchException, ParsingException
	{
		for (ParsingService ps : parsingServices)
		{
			try
			{
				return ps.parse(text, targetClass);
			}
			catch (NoMatchException nme)
			{
				continue;
			}
		}
		throw new NoMatchException(text, targetClass, "No ParsingService could parse the text");
	}

	public static Object parse(String text, List<ParsingService> parsingServices) throws NoMatchException, ParsingException
	{
		for (ParsingService ps : parsingServices)
		{
			try
			{
				return ps.parse(text);
			}
			catch (NoMatchException nme)
			{
				continue;
			}
		}
		throw new NoMatchException(text, null, "No ParsingService could parse the text");
	}

	private Parsings()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
