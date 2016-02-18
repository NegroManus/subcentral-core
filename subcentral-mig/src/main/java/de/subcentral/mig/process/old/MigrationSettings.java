package de.subcentral.mig.process.old;

import java.beans.IntrospectionException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.correct.CorrectionService;
import de.subcentral.core.correct.PatternStringReplacer;
import de.subcentral.core.correct.ReflectiveCorrector;
import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.correct.TypeBasedCorrectionService;
import de.subcentral.core.correct.PatternStringReplacer.Mode;
import de.subcentral.core.metadata.Contributor;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.fx.UserPattern;
import de.subcentral.mig.process.Subber;
import de.subcentral.mig.process.SubtitleGroup;
import de.subcentral.support.thetvdbcom.TheTvDbCom;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

/**
 * scUsername = "NegroManus"; scPassword = "xxx";
 * 
 * tvdbApiKey = "A3ACA9D28A27792D";
 * 
 * @author mhertram
 *
 */
public class MigrationSettings
{
	public static final MigrationSettings				INSTANCE					= new MigrationSettings();
	private static final Logger							log							= LogManager.getLogger(MigrationSettings.class);

	private final ListProperty<Series>					series						= new SimpleListProperty<>(this, "series", FXCollections.observableArrayList());
	private final ListProperty<ContributionPattern>		contributionPatterns		= new SimpleListProperty<>(this, "contributionPatterns", FXCollections.observableArrayList());
	private final ListProperty<ContributionTypePattern>	contributionTypePatterns	= new SimpleListProperty<>(this, "contributionTypePatterns", FXCollections.observableArrayList());
	private final ListProperty<ContributorPattern>		contributorPatterns			= new SimpleListProperty<>(this, "contributorPatterns", FXCollections.observableArrayList());
	private final ListProperty<Pattern>					irrelevantPatterns			= new SimpleListProperty<>(this, "irrelevantPatterns", FXCollections.observableArrayList());
	private final Property<CorrectionService>			standardizingService		= new SimpleObjectProperty<>(this, "standardizingService");
	private final Map<String, Object>					namingParams				= new HashMap<>();

	private MigrationSettings()
	{

	}

	public void load(URL file) throws ConfigurationException
	{
		log.info("Loading settings from {}", file);

		XMLConfiguration cfg = new XMLConfiguration();
		// cfg.addEventListener(Event.ANY, (Event event) -> {
		// System.out.println(event);
		// });

		FileHandler cfgFileHandler = new FileHandler(cfg);
		cfgFileHandler.load(file);

		load(cfg);
	}

	private void load(XMLConfiguration cfg) throws ConfigurationException
	{
		series.setAll(loadSeries(cfg, "series.series"));
		contributionPatterns.setAll(loadContributionPatterns(cfg, "subtitles.contributionPatterns.contributionPattern"));
		contributionTypePatterns.setAll(loadContributionTypePatterns(cfg, "subtitles.contributionTypePatterns.contributionTypePattern"));
		contributorPatterns.setAll(loadContributorPatterns(cfg, "subtitles.contributorPatterns.contributorPattern"));
		irrelevantPatterns.setAll(loadUserPatterns(cfg, "subtitles.irrelevantPatterns.irrelevantPattern"));
		standardizingService.setValue(loadStandardizingService(cfg));
	}

	private static List<Series> loadSeries(XMLConfiguration cfg, String key)
	{
		ArrayList<Series> seriesList = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> seriesCfgs = cfg.configurationsAt(key);
		for (HierarchicalConfiguration<ImmutableNode> seriesCfg : seriesCfgs)
		{
			String name = seriesCfg.getString("[@name]");
			String thetvdbId = seriesCfg.getString("[@thetvdbId]");
			Series series = new Series(name);
			series.getIds().put(TheTvDbCom.SITE_ID, thetvdbId);
			seriesList.add(series);
		}
		seriesList.trimToSize();
		return seriesList;
	}

	private static List<ContributionPattern> loadContributionPatterns(XMLConfiguration cfg, String key)
	{
		ArrayList<ContributionPattern> patterns = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> patternCfgs = cfg.configurationsAt(key);
		for (HierarchicalConfiguration<ImmutableNode> patternCfg : patternCfgs)
		{
			Pattern pattern = Pattern.compile(patternCfg.getString("[@pattern]"), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			int confidence = patternCfg.getInt("[@confidence]", 2);
			int contributionTypeGroup = patternCfg.getInt("[@contributionTypeGroup]");
			int contributorGroup = patternCfg.getInt("[@contributorGroup]");
			patterns.add(new ContributionPattern(pattern, contributionTypeGroup, contributorGroup, confidence));
		}
		patterns.trimToSize();
		return patterns;
	}

	private static List<ContributionTypePattern> loadContributionTypePatterns(XMLConfiguration cfg, String key)
	{
		ArrayList<ContributionTypePattern> patterns = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> patternCfgs = cfg.configurationsAt(key);
		for (HierarchicalConfiguration<ImmutableNode> patternCfg : patternCfgs)
		{
			Pattern pattern = Pattern.compile(patternCfg.getString("[@pattern]"), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			int confidence = patternCfg.getInt("[@confidence]", 3);
			String contributionType = StringUtils.trimToNull(patternCfg.getString("[@type]"));
			patterns.add(new ContributionTypePattern(pattern, confidence, contributionType));
		}
		patterns.trimToSize();
		return patterns;
	}

	private static List<ContributorPattern> loadContributorPatterns(XMLConfiguration cfg, String key) throws ConfigurationException
	{
		ArrayList<ContributorPattern> patterns = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> patternCfgs = cfg.configurationsAt(key);
		for (HierarchicalConfiguration<ImmutableNode> patternCfg : patternCfgs)
		{
			String pattern = patternCfg.getString("[@pattern]");
			UserPattern.Mode mode = UserPattern.Mode.valueOf(patternCfg.getString("[@patternMode]"));
			UserPattern userPattern = new UserPattern(pattern, mode);

			int confidence = patternCfg.getInt("[@confidence]", 2);

			Contributor contributor = null;
			String name = patternCfg.getString("[@name]");
			String type = patternCfg.getString("[@type]", "SUBBER");
			switch (type)
			{
				case "SUBBER":
					Subber subber = new Subber();
					subber.setName(name);
					int scUserId = patternCfg.getInt("[@scUserId]", 0);
					subber.setId(scUserId);
					contributor = subber;
					break;
				case "GROUP":
					SubtitleGroup group = new SubtitleGroup();
					group.setName(name);
					contributor = group;
					break;
				default:
					throw new ConfigurationException("Illegal type: " + type);

			}

			patterns.add(new ContributorPattern(userPattern.toPattern(), confidence, contributor));
		}
		patterns.trimToSize();
		return patterns;
	}

	private static List<Pattern> loadUserPatterns(XMLConfiguration cfg, String key)
	{
		ArrayList<Pattern> patterns = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> patternCfgs = cfg.configurationsAt(key);
		for (HierarchicalConfiguration<ImmutableNode> patternCfg : patternCfgs)
		{
			String pattern = patternCfg.getString("[@pattern]");
			UserPattern.Mode mode = UserPattern.Mode.valueOf(patternCfg.getString("[@patternMode]"));
			UserPattern userPattern = new UserPattern(pattern, mode);
			patterns.add(userPattern.toPattern());
		}
		patterns.trimToSize();
		return patterns;
	}

	private static CorrectionService loadStandardizingService(XMLConfiguration cfg)
	{
		TypeBasedCorrectionService service = new TypeBasedCorrectionService("migration");
		for (PatternStringReplacer seriesNameReplacer : loadPatternStringReplacers(cfg, "series.seriesNameReplacers.replacer"))
		{
			service.registerCorrector(Series.class, new SeriesNameCorrector(seriesNameReplacer.getPattern(), seriesNameReplacer.getReplacement()));
		}
		for (PatternStringReplacer contributorNameReplacer : loadPatternStringReplacers(cfg, "subtitles.contributorNameReplacers.replacer"))
		{
			try
			{
				service.registerCorrector(Subber.class, new ReflectiveCorrector<>(Subber.class, "name", contributorNameReplacer, Function.identity()));
			}
			catch (IntrospectionException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return service;
	}

	private static List<PatternStringReplacer> loadPatternStringReplacers(XMLConfiguration cfg, String key)
	{
		ArrayList<PatternStringReplacer> replacers = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> replacerCfgs = cfg.configurationsAt(key);
		for (HierarchicalConfiguration<ImmutableNode> replacerCfg : replacerCfgs)
		{
			String pattern = replacerCfg.getString("[@pattern]");
			UserPattern.Mode mode = UserPattern.Mode.valueOf(replacerCfg.getString("[@patternMode]"));
			String replacement = replacerCfg.getString("[@replacement]");
			UserPattern userPattern = new UserPattern(pattern, mode);
			replacers.add(new PatternStringReplacer(userPattern.toPattern(), replacement, Mode.REPLACE_COMPLETE));
		}
		replacers.trimToSize();
		return replacers;
	}

	public ListProperty<ContributionPattern> contributionPatternsProperty()
	{
		return contributionPatterns;
	}

	public ListProperty<ContributionTypePattern> contributionTypePatternsProperty()
	{
		return contributionTypePatterns;
	}

	public ListProperty<ContributorPattern> contributorPatternsProperty()
	{
		return contributorPatterns;
	}

	public ListProperty<Pattern> irrelevantPatternsProperty()
	{
		return irrelevantPatterns;
	}

	public CorrectionService getStandardizingService()
	{
		return standardizingService.getValue();
	}

	public Map<String, Object> getNamingParams()
	{
		return namingParams;
	}

}
