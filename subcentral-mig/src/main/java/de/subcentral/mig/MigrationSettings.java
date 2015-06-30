package de.subcentral.mig;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.standardizing.PatternStringReplacer;
import de.subcentral.core.standardizing.PatternStringReplacer.Mode;
import de.subcentral.core.standardizing.ReflectiveStandardizer;
import de.subcentral.core.standardizing.SeriesNameStandardizer;
import de.subcentral.core.standardizing.StandardizingService;
import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.fx.UserPattern;
import de.subcentral.mig.ContributionParser.ContributionTypePattern;
import de.subcentral.support.thetvdbcom.TheTvDbApi;
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
    public static final MigrationSettings INSTANCE = new MigrationSettings();
    private static final Logger		  log	   = LogManager.getLogger(MigrationSettings.class);

    private final ListProperty<Series>			series			 = new SimpleListProperty<>(this, "series", FXCollections.observableArrayList());
    private final ListProperty<ContributionTypePattern>	contributionTypePatterns = new SimpleListProperty<>(this, "contributionTypePatterns", FXCollections.observableArrayList());
    private final ListProperty<Pattern>			knownContributors	 = new SimpleListProperty<>(this, "knownContributors", FXCollections.observableArrayList());
    private final ListProperty<Pattern>			knownNonContributors	 = new SimpleListProperty<>(this, "knownNonContributors", FXCollections.observableArrayList());
    private final Property<StandardizingService>	standardizingService	 = new SimpleObjectProperty<>(this, "standardizingService");
    private final Map<String, Object>			namingParams		 = new HashMap<>();

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

    private void load(XMLConfiguration cfg)
    {
	series.setAll(loadSeries(cfg, "series.series"));
	contributionTypePatterns.setAll(loadContributionTypePatterns(cfg, "subtitles.contributionTypes.contributionType"));
	knownContributors.setAll(loadUserPatterns(cfg, "subtitles.knownContributors.knownContributor"));
	knownNonContributors.setAll(loadUserPatterns(cfg, "subtitles.knownNonContributors.knownNonContributor"));
	standardizingService.setValue(loadStandardizingService(cfg));

    }

    private static List<Series> loadSeries(XMLConfiguration cfg, String key)
    {
	ArrayList<Series> seriesList = new ArrayList<>();
	List<HierarchicalConfiguration<ImmutableNode>> seriesCfgs = cfg.configurationsAt(key);
	for (HierarchicalConfiguration<ImmutableNode> seriesCfg : seriesCfgs)
	{
	    String name = seriesCfg.getString("[@name]");
	    int thetvdbId = seriesCfg.getInt("[@thetvdbId]");
	    Series series = new Series(name);
	    series.getAttributes().put(TheTvDbApi.ATTRIBUTE_THETVDB_ID, thetvdbId);
	    seriesList.add(series);
	}
	seriesList.trimToSize();
	return seriesList;
    }

    private static List<ContributionTypePattern> loadContributionTypePatterns(XMLConfiguration cfg, String key)
    {
	ArrayList<ContributionTypePattern> patterns = new ArrayList<>();
	List<HierarchicalConfiguration<ImmutableNode>> patternCfgs = cfg.configurationsAt(key);
	for (HierarchicalConfiguration<ImmutableNode> patternCfg : patternCfgs)
	{
	    String contributionType = patternCfg.getString("[@type]");
	    Pattern pattern = Pattern.compile(patternCfg.getString("[@pattern]"), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	    patterns.add(new ContributionTypePattern(pattern, contributionType));
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

    private static StandardizingService loadStandardizingService(XMLConfiguration cfg)
    {
	TypeStandardizingService service = new TypeStandardizingService("migration");
	for (PatternStringReplacer seriesNameReplacer : loadPatternStringReplacers(cfg, "series.seriesNameReplacers.seriesNameReplacer"))
	{
	    service.registerStandardizer(Series.class, new SeriesNameStandardizer(seriesNameReplacer.getPattern(), seriesNameReplacer.getReplacement()));
	}
	for (PatternStringReplacer contributorNameReplacer : loadPatternStringReplacers(cfg, "subtitles.contributorNameReplacers.contributorNameReplacer"))
	{
	    service.registerStandardizer(Subber.class, new ReflectiveStandardizer<Subber, String>(Subber.class, "name", contributorNameReplacer));
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

    public ListProperty<ContributionTypePattern> contributionTypePatternsProperty()
    {
	return contributionTypePatterns;
    }

    public ListProperty<Pattern> knownContributorsProperty()
    {
	return knownContributors;
    }

    public ListProperty<Pattern> knownNonContributorsProperty()
    {
	return knownNonContributors;
    }

    public StandardizingService getStandardizingService()
    {
	return standardizingService.getValue();
    }

    public Map<String, Object> getNamingParams()
    {
	return namingParams;
    }

}
