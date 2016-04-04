package de.subcentral.settings;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.metadata.release.TagUtil.SearchMode;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.orlydbcom.OrlyDbCom;
import de.subcentral.support.orlydbcom.OrlyDbComMetadataDb;
import de.subcentral.support.predbme.PreDbMe;
import de.subcentral.support.predbme.PreDbMeMetadataDb;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.xrelto.XRelTo;
import de.subcentral.support.xrelto.XRelToMetadataDb;
import de.subcentral.watcher.settings.CompatibilitySettingEntry;
import de.subcentral.watcher.settings.CorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.MetadataDbSettingEntry;
import de.subcentral.watcher.settings.ParsingServiceSettingEntry;
import de.subcentral.watcher.settings.ReleaseTagsCorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.SeriesNameCorrectionRuleSettingEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class ConfigurationHelper
{
	public static void save(XMLConfiguration cfg, Path file) throws ConfigurationException
	{
		try
		{
			FileHandler cfgFileHandler = new FileHandler(cfg);
			cfgFileHandler.save(Files.newOutputStream(file), Charset.forName("UTF-8").name());
		}
		catch (IOException e)
		{
			throw new ConfigurationException(e);
		}
	}

	public static XMLConfiguration load(URL file) throws ConfigurationException
	{
		try
		{
			XMLConfiguration cfg = new XMLConfiguration();
			FileHandler cfgFileHandler = new FileHandler(cfg);
			cfgFileHandler.load(file.openStream(), Charset.forName("UTF-8").name());
			return cfg;
		}
		catch (IOException e)
		{
			throw new ConfigurationException(e);
		}
	}

	public static XMLConfiguration load(Path file) throws ConfigurationException
	{
		try
		{
			XMLConfiguration cfg = new XMLConfiguration();
			FileHandler cfgFileHandler = new FileHandler(cfg);
			cfgFileHandler.load(Files.newInputStream(file), Charset.forName("UTF-8").name());
			return cfg;
		}
		catch (IOException e)
		{
			throw new ConfigurationException(e);
		}
	}

	// GETTER
	// Static config getter
	public static Path getPath(Configuration cfg, String key)
	{
		String path = cfg.getString(key);
		if (path.isEmpty())
		{
			return null;
		}
		else
		{
			return Paths.get(path);
		}
	}

	public static ObservableList<ParsingServiceSettingEntry> getParsingServices(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<ParsingServiceSettingEntry> services = new ArrayList<>(4);
		List<HierarchicalConfiguration<ImmutableNode>> parsingServiceCfgs = cfg.configurationsAt(key + ".parsingService");
		for (HierarchicalConfiguration<ImmutableNode> parsingServiceCfg : parsingServiceCfgs)
		{
			String domain = parsingServiceCfg.getString("");
			boolean enabled = parsingServiceCfg.getBoolean("[@enabled]");
			if (Addic7edCom.getParsingService().getDomain().equals(domain))
			{
				services.add(new ParsingServiceSettingEntry(Addic7edCom.getParsingService(), enabled));
			}
			else if (ItalianSubsNet.getParsingService().getDomain().equals(domain))
			{
				services.add(new ParsingServiceSettingEntry(ItalianSubsNet.getParsingService(), enabled));
			}
			else if (ReleaseScene.getParsingService().getDomain().equals(domain))
			{
				services.add(new ParsingServiceSettingEntry(ReleaseScene.getParsingService(), enabled));
			}
			else if (SubCentralDe.getParsingService().getDomain().equals(domain))
			{
				services.add(new ParsingServiceSettingEntry(SubCentralDe.getParsingService(), enabled));
			}
			else
			{
				throw new IllegalArgumentException("Unknown parsing service domain: " + domain);
			}
		}
		services.trimToSize();
		return FXCollections.observableList(services);
	}

	public static ObservableList<CorrectionRuleSettingEntry<?, ?>> getCorrectionRules(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<CorrectionRuleSettingEntry<?, ?>> stdzers = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> seriesStdzerCfgs = cfg.configurationsAt(key + ".seriesNameCorrectionRule");
		int seriesNameIndex = 0;
		for (HierarchicalConfiguration<ImmutableNode> stdzerCfg : seriesStdzerCfgs)
		{
			String namePatternStr = stdzerCfg.getString("[@namePattern]");
			Mode namePatternMode = Mode.valueOf(stdzerCfg.getString("[@namePatternMode]"));
			UserPattern nameUiPattern = new UserPattern(namePatternStr, namePatternMode);
			String nameReplacement = stdzerCfg.getString("[@nameReplacement]");
			List<HierarchicalConfiguration<ImmutableNode>> aliasNameCfgs = cfg.configurationsAt(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ").aliasNames.aliasName");
			List<String> aliasNameReplacements = new ArrayList<>(aliasNameCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> aliasNameCfg : aliasNameCfgs)
			{
				aliasNameReplacements.add(aliasNameCfg.getString(""));
			}
			boolean enabledPreMetadataDb = stdzerCfg.getBoolean("[@beforeQuerying]");
			boolean enabledPostMetadataDb = stdzerCfg.getBoolean("[@afterQuerying]");
			stdzers.add(new SeriesNameCorrectionRuleSettingEntry(nameUiPattern, nameReplacement, aliasNameReplacements, enabledPreMetadataDb, enabledPostMetadataDb));
			seriesNameIndex++;
		}
		List<HierarchicalConfiguration<ImmutableNode>> rlsTagsStdzerCfgs = cfg.configurationsAt(key + ".releaseTagsCorrectionRule");
		for (HierarchicalConfiguration<ImmutableNode> stdzerCfg : rlsTagsStdzerCfgs)
		{
			List<Tag> queryTags = Tag.parseList(stdzerCfg.getString("[@searchTags]"));
			List<Tag> replacement = Tag.parseList(stdzerCfg.getString("[@replacement]"));
			SearchMode queryMode = SearchMode.valueOf(stdzerCfg.getString("[@searchMode]"));
			ReplaceMode replaceMode = ReplaceMode.valueOf(stdzerCfg.getString("[@replaceMode]"));
			boolean ignoreOrder = stdzerCfg.getBoolean("[@ignoreOrder]", false);
			boolean beforeQuerying = stdzerCfg.getBoolean("[@beforeQuerying]");
			boolean afterQuerying = stdzerCfg.getBoolean("[@afterQuerying]");
			ReleaseTagsCorrector stdzer = new ReleaseTagsCorrector(new TagsReplacer(queryTags, replacement, queryMode, replaceMode, ignoreOrder));
			stdzers.add(new ReleaseTagsCorrectionRuleSettingEntry(stdzer, beforeQuerying, afterQuerying));
		}
		stdzers.trimToSize();
		return FXCollections.observableList(stdzers);
	}

	public static ObservableList<MetadataDbSettingEntry<Release>> getReleaseDbs(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<MetadataDbSettingEntry<Release>> dbs = new ArrayList<>(3);
		List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key + ".db");
		for (HierarchicalConfiguration<ImmutableNode> rlsDbCfg : rlsDbCfgs)
		{
			String name = rlsDbCfg.getString("");
			boolean enabled = rlsDbCfg.getBoolean("[@enabled]");
			if (PreDbMe.SITE.getName().equals(name))
			{
				dbs.add(new MetadataDbSettingEntry<>(new PreDbMeMetadataDb(), enabled));
			}
			else if (XRelTo.SITE.getName().equals(name))
			{
				dbs.add(new MetadataDbSettingEntry<>(new XRelToMetadataDb(), enabled));
			}
			else if (OrlyDbCom.SITE.getName().equals(name))
			{
				dbs.add(new MetadataDbSettingEntry<>(new OrlyDbComMetadataDb(), enabled));
			}
			else
			{
				throw new IllegalArgumentException("Unknown metadata database: " + name);
			}
		}
		dbs.trimToSize();
		return FXCollections.observableList(dbs);
	}

	public static ObservableList<Tag> getTags(Configuration cfg, String key)
	{
		ArrayList<Tag> tags = new ArrayList<>();
		for (String tagName : cfg.getList(String.class, key + ".tag"))
		{
			tags.add(new Tag(tagName));
		}
		tags.trimToSize();
		return FXCollections.observableList(tags);
	}

	public static ObservableList<StandardRelease> getStandardReleases(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<StandardRelease> rlss = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".standardRelease");
		for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
		{
			List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
			Group group = Group.from(rlsCfg.getString("[@group]"));
			Scope scope = Scope.valueOf(rlsCfg.getString("[@scope]"));
			rlss.add(new StandardRelease(tags, group, scope));
		}
		rlss.trimToSize();
		return FXCollections.observableList(rlss);
	}

	public static ObservableList<Release> getReleases(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<Release> rlss = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".release");
		for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
		{
			List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
			Group group = Group.from(rlsCfg.getString("[@group]"));
			rlss.add(new Release(tags, group));
		}
		rlss.trimToSize();
		return FXCollections.observableList(rlss);
	}

	public static ObservableList<CompatibilitySettingEntry> getCompatibilities(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		Set<CompatibilitySettingEntry> compatibilities = new LinkedHashSet<>();
		// read GroupsCompatibilities
		List<HierarchicalConfiguration<ImmutableNode>> groupsCompCfgs = cfg.configurationsAt(key + ".crossGroupCompatibility");
		for (HierarchicalConfiguration<ImmutableNode> groupsCompCfg : groupsCompCfgs)
		{
			boolean enabled = groupsCompCfg.getBoolean("[@enabled]");
			Group sourceGroup = Group.from(groupsCompCfg.getString("[@sourceGroup]"));
			Group compatibleGroup = Group.from(groupsCompCfg.getString("[@compatibleGroup]"));
			boolean symmetric = groupsCompCfg.getBoolean("[@symmetric]", false);
			compatibilities.add(new CompatibilitySettingEntry(new CrossGroupCompatibility(sourceGroup, compatibleGroup, symmetric), enabled));
		}
		return FXCollections.observableArrayList(compatibilities);
	}

	public static ObservableMap<String, Object> getNamingParameters(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		Map<String, Object> params = new LinkedHashMap<>(3);
		// read actual values
		List<HierarchicalConfiguration<ImmutableNode>> paramCfgs = cfg.configurationsAt(key + ".param");
		for (HierarchicalConfiguration<ImmutableNode> paramCfg : paramCfgs)
		{
			String paramKey = paramCfg.getString("[@key]");
			boolean paramValue = paramCfg.getBoolean("[@value]");
			params.put(paramKey, paramValue);
		}
		return FXCollections.observableMap(params);
	}

	// SETTER
	public static void addParsingServices(XMLConfiguration cfg, String key, List<ParsingServiceSettingEntry> parsingServices)
	{
		for (int i = 0; i < parsingServices.size(); i++)
		{
			ParsingServiceSettingEntry ps = parsingServices.get(i);
			cfg.addProperty(key + ".parsingService(" + i + ")", ps.getValue().getDomain());
			cfg.addProperty(key + ".parsingService(" + i + ")[@enabled]", ps.isEnabled());
		}
	}

	public static void addCorrectionRules(XMLConfiguration cfg, String key, List<CorrectionRuleSettingEntry<?, ?>> rules)
	{
		// one index for each element name
		int seriesNameIndex = 0;
		int releaseTagsIndex = 0;
		for (CorrectionRuleSettingEntry<?, ?> genericEntry : rules)
		{
			if (genericEntry instanceof SeriesNameCorrectionRuleSettingEntry)
			{
				SeriesNameCorrectionRuleSettingEntry entry = (SeriesNameCorrectionRuleSettingEntry) genericEntry;
				SeriesNameCorrector corrector = entry.getValue();
				UserPattern namePattern = entry.getNameUserPattern();

				cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePattern]", namePattern.getPattern());
				cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePatternMode]", namePattern.getMode());
				cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@nameReplacement]", corrector.getNameReplacement());
				cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
				cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@afterQuerying]", entry.isAfterQuerying());
				for (String aliasName : corrector.getAliasNamesReplacement())
				{
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ").aliasNames.aliasName", aliasName);
				}
				seriesNameIndex++;
			}
			else if (genericEntry instanceof ReleaseTagsCorrectionRuleSettingEntry)
			{
				ReleaseTagsCorrectionRuleSettingEntry entry = (ReleaseTagsCorrectionRuleSettingEntry) genericEntry;
				TagsReplacer replacer = (TagsReplacer) entry.getValue().getReplacer();

				cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchTags]", Tag.formatList(replacer.getSearchTags()));
				cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replacement]", Tag.formatList(replacer.getReplacement()));
				cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchMode]", replacer.getSearchMode());
				cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replaceMode]", replacer.getReplaceMode());
				cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@ignoreOrder]", replacer.getIgnoreOrder());
				cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
				cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@afterQuerying]", entry.isAfterQuerying());
				releaseTagsIndex++;
			}
			else
			{
				throw new IllegalArgumentException("Unknown standardizer: " + genericEntry);
			}
		}
	}

	public static void addPath(Configuration cfg, String key, Path path)
	{
		// WARNING: Need to use path.toString() because path implements iterable
		// and results in an endless loop when Commons-Configuration tries to print it
		cfg.addProperty(key, path == null ? "" : path.toString());
	}
}
