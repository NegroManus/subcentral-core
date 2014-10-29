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
	 * The parameter key for the Boolean value "includeSeason". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SEASON_KEY					= "includeSeason";

	/**
	 * The parameter key for the Boolean value "alwaysIncludeSeasonTitle".
	 */
	public static final String	PARAM_ALWAYS_INCLUDE_SEASON_TITLE_KEY		= "alwaysIncludeSeasonTitle";
	public static final Boolean	PARAM_ALWAYS_INCLUDE_SEASON_TITLE_DEFAULT	= Boolean.FALSE;

	protected SeasonedEpisodeNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Episode epi, Map<String, Object> params)
	{
		// settings
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES_KEY, Boolean.class, Boolean.TRUE);
		boolean includeSeason = Namings.readParameter(params, PARAM_INCLUDE_SEASON_KEY, Boolean.class, Boolean.TRUE);
		boolean alwaysIncludeSeasonTitle = Namings.readParameter(params,
				PARAM_ALWAYS_INCLUDE_SEASON_TITLE_KEY,
				Boolean.class,
				PARAM_ALWAYS_INCLUDE_SEASON_TITLE_DEFAULT);
		boolean alwaysIncludeEpisodeTitle = Namings.readParameter(params,
				PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY,
				Boolean.class,
				PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_DEFAULT);

		// add series
		if (includeSeries && epi.getSeries() != null)
		{
			b.appendIfNotNull(Episode.PROP_SERIES, epi.getSeries().getName());
		}

		// add season
		if (includeSeason && epi.isPartOfSeason())
		{
			Season season = epi.getSeason();
			if (season.isNumbered())
			{
				b.append(Season.PROP_NUMBER, season.getNumber());
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
