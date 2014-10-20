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
import de.subcentral.core.model.media.SingleMedia;
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
	public static final String						PATTERN_MEDIA_NAME			= "((.*?)(?:\\s+\\((?:(\\d{4})|(\\p{Upper}{2}))\\))?)";

	private static final EpisodeMapper				EPISODE_MAPPER				= new EpisodeMapper();
	private static final MultiEpisodeMapper			MULTI_EPISODE_MAPPER		= new MultiEpisodeMapper(EPISODE_MAPPER);
	private static final SingleMediaMapper			SINGLE_MEDIA_MAPPER			= new SingleMediaMapper();
	private static final ReleaseMapper				RELEASE_MAPPER				= new ReleaseMapper();
	private static final SubtitleMapper				SUBTITLE_MAPPER				= new SubtitleMapper();
	private static final SubtitleAdjustmentMapper	SUBTITLE_ADJUSTMENT_MAPPER	= new SubtitleAdjustmentMapper();
	private static final ReleaseParser				RELEASE_PARSER				= new ReleaseParser("default");

	public static final <E> Mapper<List<E>> createSingletonListMapper(Mapper<? extends E> elementMapper)
	{
		return (props, pps) -> ImmutableList.of(elementMapper.map(props, pps));
	}

	public static final Mapper<Episode> getDefaultEpisodeMapper()
	{
		return EPISODE_MAPPER;
	}

	public static MultiEpisodeMapper getDefaultMultiEpisodeMapper()
	{
		return MULTI_EPISODE_MAPPER;
	}

	public static final Mapper<SingleMedia> getDefaultSingleMediaMapper()
	{
		return SINGLE_MEDIA_MAPPER;
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

	public static final void requireTextNotBlank(String text) throws ParsingException
	{
		if (StringUtils.isBlank(text))
		{
			throw new ParsingException("Text is blank", text, null);
		}
	}

	public static final <T> void reflectiveMapping(T entity, Map<SimplePropDescriptor, String> props, PropParsingService pps)
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
						List<?> value = pps.parseList(p.getValue(), simplePropDescr, itemClass);
						if (Set.class.isAssignableFrom(type.getRawType()))
						{
							simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(entity, ImmutableSet.copyOf(value));
						}
						else
						{
							simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(value);
						}
					}
					else
					{
						simplePropDescr.toPropertyDescriptor()
								.getWriteMethod()
								.invoke(entity, pps.parse(p.getValue(), simplePropDescr, type.wrap().getRawType()));
					}

				}
				catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParsingException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static final <T> T mapConditionally(List<ConditionalMapper<T>> conditionalMappers, Map<SimplePropDescriptor, String> props,
			PropParsingService propParsingService) throws MappingException
	{
		for (ConditionalMapper<T> m : conditionalMappers)
		{
			T result = m.map(props, propParsingService);
			if (result != null)
			{
				return result;
			}
		}
		throw new MappingException("No conditional mapper could map", props, null);
	}

	private Parsings()
	{
		// util class
	}

}
