package de.subcentral.subman.mig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.ReleaseNamer;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;
import de.subcentral.core.standardizing.PatternStringReplacer;
import de.subcentral.core.standardizing.ReflectiveStandardizer;
import de.subcentral.core.standardizing.StandardizingDefaults;
import de.subcentral.core.standardizing.StringReplacer;
import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.subman.mig.ContributionParser.ContributionTypePattern;

public class Config
{
	public static final Config					INSTANCE				= new Config();

	private String								scUsername;
	private String								scPassword;
	private String								tvdbApiKey;
	private final List<ContributionTypePattern>	patterns				= new ArrayList<>();
	private final List<Pattern>					knownSubbers			= new ArrayList<>();
	private final List<Pattern>					knownNonSubbers			= new ArrayList<>();
	private final TypeStandardizingService		standardizingService	= new TypeStandardizingService("migration");
	private final Map<String, Object>			namingParams			= new HashMap<>();

	private Config()
	{
		scUsername = "NegroManus";
		scPassword = "sc-don13duck-";

		tvdbApiKey = "A3ACA9D28A27792D";

		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Übersetzung|Übersetzer|Übersetzt|Subbed by|Untertitel)\\b",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TRANSLATION));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Korrektur|Korrekturen|Korrekturleser|Korrigiert|Revised by|Re-revised by|Überarbeitung|Überarbeitet|Corrected)\\b",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
				Subtitle.CONTRIBUTION_TYPE_REVISION));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Anpassung|Anpasser|Angepasst|Adjusted by)\\b", Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE), SubtitleAdjustment.CONTRIBUTION_TYPE_ADJUSTMENT));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Timings|Timings|Sync|Synced|Synchro|Sync's|Syncs|Sync)\\b",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TIMINGS));
		// nicht nur "VO", weil das zu oft vorkommt. Daher "VO von"
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(VO von|VO by|Transcript|Subs)\\b", Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TRANSCRIPT));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Special Thx to)\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), null));

		knownSubbers.add(Pattern.compile("(Randall Flagg|The old Man|JW 301|-TiLT- aka smizz|smizz aka -TiLT-|Kami Cat|Iulius Monea)",
				Pattern.CASE_INSENSITIVE));

		knownNonSubbers.add(Pattern.compile("\\b(und|and|from|von|by)\\b", Pattern.CASE_INSENSITIVE));

		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(standardizingService);
		StandardizingDefaults.registerAllDefaultStandardizers(standardizingService);

		// Die Credits stehen manchmal in einem Satz. D.h. am Ende steht ein Punkt.
		// Dieser Punkt gehört dann nicht zum Subber-Namen und wird entfernt
		standardizingService.registerStandardizer(Subber.class, new ReflectiveStandardizer<Subber, String>(Subber.class,
				"name",
				new PatternStringReplacer(Pattern.compile("(.*)\\."), "$1", PatternStringReplacer.Mode.REPLACE_COMPLETE)));
		standardizingService.registerStandardizer(Subber.class, new ReflectiveStandardizer<Subber, String>(Subber.class,
				"name",
				new StringReplacer("Ic3m4n", "Ic3m4n™", StringReplacer.Mode.REPLACE_COMPLETE)));
		standardizingService.registerStandardizer(Subber.class, new ReflectiveStandardizer<Subber, String>(Subber.class,
				"name",
				new StringReplacer("smizz aka -TiLT-", "-TiLT- aka smizz", StringReplacer.Mode.REPLACE_COMPLETE)));

		namingParams.put(ReleaseNamer.PARAM_PREFER_NAME, Boolean.TRUE);
		namingParams.put(SubtitleAdjustmentNamer.PARAM_PREFER_NAME, Boolean.TRUE);
	}

	public String getScUsername()
	{
		return scUsername;
	}

	public void setScUsername(String scUsername)
	{
		this.scUsername = scUsername;
	}

	public String getScPassword()
	{
		return scPassword;
	}

	public void setScPassword(String scPassword)
	{
		this.scPassword = scPassword;
	}

	public String getTvdbApiKey()
	{
		return tvdbApiKey;
	}

	public void setTvdbApiKey(String tvdbApiKey)
	{
		this.tvdbApiKey = tvdbApiKey;
	}

	public List<ContributionTypePattern> getPatterns()
	{
		return patterns;
	}

	public List<Pattern> getKnownSubbers()
	{
		return knownSubbers;
	}

	public List<Pattern> getKnownNonSubbers()
	{
		return knownNonSubbers;
	}

	public TypeStandardizingService getStandardizingService()
	{
		return standardizingService;
	}

	public Map<String, Object> getNamingParams()
	{
		return namingParams;
	}
}
