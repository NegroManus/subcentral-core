package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.util.Separation;

/**
 * Possible naming combinations:
 * <p>
 * <b>Series, Season, Episode</b>
 * <ul>
 * <li>series seasonnum seasontitle epinum epititle</li>
 * <li>series seasonnum seasontitle epinum</li>
 * <li>series seasonnum seasontitle epititle</li>
 * <li>series seasonnum seasontitle Exx</li>
 * <li>series {seasonnum epinum} epititle</li>
 * <li>series {seasonnum epinum}</li>
 * <li>series seasonnum epititle</li>
 * <li>series seasonnum Exx</li>
 * <li>series seasontitle epinum epititle</li>
 * <li>series seasontitle epinum</li>
 * <li>series seasontitle epititle</li>
 * <li>series seasontitle Exx</li>
 * <li>series Sxx epinum epititle</li>
 * <li>series Sxx epinum</li>
 * <li>series Sxx epititle</li>
 * <li>series Sxx Exx</li>
 * <li>series epinum epititle</li>
 * <li>series epinum</li>
 * <li>series epititle</li>
 * <li>series Exx</li>
 * </ul>
 * 
 * <b>Season, Episode</b>
 * <ul>
 * <li>seasonnum seasontitle epinum epititle</li>
 * <li>seasonnum seasontitle epinum</li>
 * <li>seasonnum seasontitle epititle</li>
 * <li>seasonnum seasontitle Exx</li>
 * <li>{seasonnum epinum} epititle</li>
 * <li>{seasonnum epinum}</li>
 * <li>seasonnum epititle</li>
 * <li>seasonnum Exx</li>
 * <li>seasontitle epinum epititle</li>
 * <li>seasontitle epinum</li>
 * <li>seasontitle epititle</li>
 * <li>seasontitle Exx</li>
 * <li>Sxx epinum epititle</li>
 * <li>Sxx epinum</li>
 * <li>Sxx epititle</li>
 * <li>Sxx Exx</li>
 * <li>epinum epititle</li>
 * <li>epinum</li>
 * <li>epititle</li>
 * <li>Exx</li>
 * </ul>
 * 
 * <b>Episode</b>
 * <ul>
 * <li>epinum</li>
 * <li>epinum epititle</li>
 * <li>epititle</li>
 * <li>Exx</li>
 * </ul>
 * </p>
 * 
 * The following separators are needed:
 * 
 * <pre>
 * series-seasonnum x
 * series-seasontitle x
 * series-sxx x
 * series-epinum x
 * series-epititle x
 * series-exx x
 * -> series-anything (all)
 * 
 * seasonnum-seasontitle
 * seasonnum-epinum
 * seasonnum-epititle
 * seasonnum-exx
 * -> seasonnum-seasontitle
 * -> seasonnum-epinum
 * -> season-epi
 * -> seasonnum-epinum (duplicate)
 *  
 * seasontitle-epinum
 * seasontitle-epititle
 * seasontitle-exx
 * -> season-epi (all, duplicate)
 * 
 * epinum-epititle
 * -> epinum-epititle
 * </pre>
 * 
 * 
 * @author mhertram
 *
 */
public class SeasonedEpisodeNamer extends AbstractEpisodeNamer
{
	/**
	 * The name of the parameter "includeSeason" of type {@link Boolean}. If set to {@code true}, the episode's season is included in the name,
	 * otherwise it is excluded. The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SEASON				= SeasonedEpisodeNamer.class.getName() + ".includeSeason";

	/**
	 * The name of the parameter "alwaysIncludeSeasonTitle" of type {@link Boolean}. If set to {@code true}, the title of the episode's season is
	 * always included in the name, otherwise only if the season is not numbered. The default value is {@code false}.
	 */
	public static final String	PARAM_ALWAYS_INCLUDE_SEASON_TITLE	= SeasonedEpisodeNamer.class.getName() + ".alwaysIncludeSeasonTitle";

	protected SeasonedEpisodeNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Episode epi, Map<String, Object> params)
	{
		// read general naming parameters that are needed in any case
		boolean includeSeries = NamingUtils.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, PARAM_INCLUDE_SERIES_DEFAULT);
		boolean alwaysIncludeEpisodeTitle = NamingUtils.readParameter(params,
				PARAM_ALWAYS_INCLUDE_TITLE,
				Boolean.class,
				PARAM_ALWAYS_INCLUDE_TITLE_DEFAULT);

		// add series
		if (includeSeries && epi.getSeries() != null)
		{
			boolean useSeriesTitle = NamingUtils.readParameter(params, PARAM_USE_SERIES_TITLE, Boolean.class, PARAM_USE_SERIES_TITLE_DEFAULT);
			b.appendIfNotNull(Episode.PROP_SERIES, useSeriesTitle ? epi.getSeries().getTitleOrName() : epi.getSeries().getName());
		}

		// add season
		boolean includeSeason = NamingUtils.readParameter(params, PARAM_INCLUDE_SEASON, Boolean.class, Boolean.TRUE);
		if (includeSeason && epi.isPartOfSeason())
		{
			Season season = epi.getSeason();
			if (season.isNumbered())
			{
				b.append(Season.PROP_NUMBER, season.getNumber());
				boolean alwaysIncludeSeasonTitle = NamingUtils.readParameter(params, PARAM_ALWAYS_INCLUDE_SEASON_TITLE, Boolean.class, Boolean.FALSE);
				b.appendIf(Season.PROP_TITLE, season.getTitle(), alwaysIncludeSeasonTitle && season.isTitled());
			}
			else
			{
				b.appendIfNotNull(Season.PROP_TITLE, season.getTitle());
			}
		}

		// add episode
		if (epi.isPartOfSeason())
		{
			if (epi.isNumberedInSeason())
			{
				b.append(Episode.PROP_NUMBER_IN_SEASON, epi.getNumberInSeason());
				b.appendIf(Episode.PROP_TITLE, epi.getTitle(), alwaysIncludeEpisodeTitle && epi.isTitled());
			}
			else
			{
				b.appendIfNotNull(Episode.PROP_TITLE, epi.getTitle());
			}
		}
		else
		{
			if (epi.isNumberedInSeries())
			{
				b.append(Episode.PROP_NUMBER_IN_SERIES, epi.getNumberInSeries());
				b.appendIf(Episode.PROP_TITLE, epi.getTitle(), alwaysIncludeEpisodeTitle && epi.isTitled());
			}
			else
			{
				b.appendIfNotNull(Episode.PROP_TITLE, epi.getTitle());
			}
		}
	}
}
