package de.subcentral.watcher.settings;

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
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.AssumeExistence;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil.QueryMode;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
import de.subcentral.core.standardizing.SeriesNameStandardizer;
import de.subcentral.core.standardizing.TagsReplacer;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.orlydbcom.OrlyDbComReleaseDb;
import de.subcentral.support.predbme.PreDbMeReleaseDb;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.xrelto.XRelToReleaseDb;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

// package private
class ConfigurationHelper
{
    // GETTER
    // Static config getter
    static Path getPath(Configuration cfg, String key)
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

    static ObservableList<ParsingServiceSettingEntry> getParsingServices(HierarchicalConfiguration<ImmutableNode> cfg, String key)
    {
	ArrayList<ParsingServiceSettingEntry> services = new ArrayList<>(4);
	List<HierarchicalConfiguration<ImmutableNode>> parsingServiceCfgs = cfg.configurationsAt(key + ".parsingService");
	for (HierarchicalConfiguration<ImmutableNode> parsingServiceCfg : parsingServiceCfgs)
	{
	    String domain = parsingServiceCfg.getString("");
	    boolean enabled = parsingServiceCfg.getBoolean("[@enabled]");
	    switch (domain)
	    {
		case Addic7edCom.DOMAIN:
		    services.add(new ParsingServiceSettingEntry(Addic7edCom.getParsingService(), enabled));
		    break;
		case ItalianSubsNet.DOMAIN:
		    services.add(new ParsingServiceSettingEntry(ItalianSubsNet.getParsingService(), enabled));
		    break;
		case ReleaseScene.DOMAIN:
		    services.add(new ParsingServiceSettingEntry(ReleaseScene.getParsingService(), enabled));
		    break;
		case SubCentralDe.DOMAIN:
		    services.add(new ParsingServiceSettingEntry(SubCentralDe.getParsingService(), enabled));
		    break;
		default:
		    throw new IllegalArgumentException("Unknown parsing service. domain=" + domain);
	    }
	}
	services.trimToSize();
	return FXCollections.observableList(services);
    }

    static ObservableList<StandardizerSettingEntry<?, ?>> getStandardizers(HierarchicalConfiguration<ImmutableNode> cfg, String key)
    {
	ArrayList<StandardizerSettingEntry<?, ?>> stdzers = new ArrayList<>();
	List<HierarchicalConfiguration<ImmutableNode>> seriesStdzerCfgs = cfg.configurationsAt(key + ".seriesNameStandardizer");
	for (HierarchicalConfiguration<ImmutableNode> seriesStdzerCfg : seriesStdzerCfgs)
	{
	    boolean enabled = seriesStdzerCfg.getBoolean("[@enabled]");
	    String namePatternStr = seriesStdzerCfg.getString("[@namePattern]");
	    Mode namePatternMode = Mode.valueOf(seriesStdzerCfg.getString("[@namePatternMode]"));
	    UserPattern nameUiPattern = new UserPattern(namePatternStr, namePatternMode);
	    String nameReplacement = seriesStdzerCfg.getString("[@nameReplacement]");
	    stdzers.add(new SeriesNameStandardizerSettingEntry(nameUiPattern, nameReplacement, enabled));
	}
	List<HierarchicalConfiguration<ImmutableNode>> rlsTagsStdzerCfgs = cfg.configurationsAt(key + ".releaseTagsStandardizer");
	for (HierarchicalConfiguration<ImmutableNode> rlsTagsStdzerCfg : rlsTagsStdzerCfgs)
	{
	    boolean enabled = rlsTagsStdzerCfg.getBoolean("[@enabled]");
	    List<Tag> queryTags = Tag.parseList(rlsTagsStdzerCfg.getString("[@queryTags]"));
	    List<Tag> replacement = Tag.parseList(rlsTagsStdzerCfg.getString("[@replacement]"));
	    QueryMode queryMode = de.subcentral.core.metadata.release.TagUtil.QueryMode.valueOf(rlsTagsStdzerCfg.getString("[@queryMode]"));
	    ReplaceMode replaceMode = de.subcentral.core.metadata.release.TagUtil.ReplaceMode.valueOf(rlsTagsStdzerCfg.getString("[@replaceMode]"));
	    boolean ignoreOrder = rlsTagsStdzerCfg.getBoolean("[@ignoreOrder]", false);
	    ReleaseTagsStandardizer stdzer = new ReleaseTagsStandardizer(new TagsReplacer(queryTags, replacement, queryMode, replaceMode, ignoreOrder));
	    stdzers.add(new ReleaseTagsStandardizerSettingEntry(stdzer, enabled));
	}
	stdzers.trimToSize();
	return FXCollections.observableList(stdzers);
    }

    static ObservableList<MetadataDbSettingEntry<Release>> getReleaseDbs(HierarchicalConfiguration<ImmutableNode> cfg, String key)
    {
	ArrayList<MetadataDbSettingEntry<Release>> dbs = new ArrayList<>(3);
	List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key + ".db");
	for (HierarchicalConfiguration<ImmutableNode> rlsDbCfg : rlsDbCfgs)
	{
	    String domain = rlsDbCfg.getString("");
	    boolean enabled = rlsDbCfg.getBoolean("[@enabled]");
	    switch (domain)
	    {
		case OrlyDbComReleaseDb.DOMAIN:
		    dbs.add(new MetadataDbSettingEntry<>(new OrlyDbComReleaseDb(), enabled));
		    break;
		case PreDbMeReleaseDb.DOMAIN:
		    dbs.add(new MetadataDbSettingEntry<>(new PreDbMeReleaseDb(), enabled));
		    break;
		case XRelToReleaseDb.DOMAIN:
		    dbs.add(new MetadataDbSettingEntry<>(new XRelToReleaseDb(), enabled));
		    break;
		default:
		    throw new IllegalArgumentException("Unknown metadata database. domain=" + domain);
	    }
	}
	dbs.trimToSize();
	return FXCollections.observableList(dbs);
    }

    static ObservableList<Tag> getTags(Configuration cfg, String key)
    {
	ArrayList<Tag> tags = new ArrayList<>();
	for (String tagName : cfg.getList(String.class, key + ".tag"))
	{
	    tags.add(new Tag(tagName));
	}
	tags.trimToSize();
	return FXCollections.observableList(tags);
    }

    static ObservableList<StandardRelease> getStandardReleases(HierarchicalConfiguration<ImmutableNode> cfg, String key)
    {
	ArrayList<StandardRelease> rlss = new ArrayList<>();
	List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".standardRelease");
	for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
	{
	    List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
	    Group group = Group.parse(rlsCfg.getString("[@group]"));
	    AssumeExistence assumeExistence = AssumeExistence.valueOf(rlsCfg.getString("[@assumeExistence]"));
	    rlss.add(new StandardRelease(tags, group, assumeExistence));
	}
	rlss.trimToSize();
	return FXCollections.observableList(rlss);
    }

    static ObservableList<Release> getReleases(HierarchicalConfiguration<ImmutableNode> cfg, String key)
    {
	ArrayList<Release> rlss = new ArrayList<>();
	List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".release");
	for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
	{
	    List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
	    Group group = Group.parse(rlsCfg.getString("[@group]"));
	    rlss.add(new Release(tags, group));
	}
	rlss.trimToSize();
	return FXCollections.observableList(rlss);
    }

    static ObservableList<CompatibilitySettingEntry> getCompatibilities(HierarchicalConfiguration<ImmutableNode> cfg, String key)
    {
	Set<CompatibilitySettingEntry> compatibilities = new LinkedHashSet<>();
	// read GroupsCompatibilities
	List<HierarchicalConfiguration<ImmutableNode>> groupsCompCfgs = cfg.configurationsAt(key + ".crossGroupCompatibility");
	for (HierarchicalConfiguration<ImmutableNode> groupsCompCfg : groupsCompCfgs)
	{
	    boolean enabled = groupsCompCfg.getBoolean("[@enabled]");
	    Group sourceGroup = Group.parse(groupsCompCfg.getString("[@sourceGroup]"));
	    Group compatibleGroup = Group.parse(groupsCompCfg.getString("[@compatibleGroup]"));
	    boolean symmetric = groupsCompCfg.getBoolean("[@symmetric]", false);
	    compatibilities.add(new CompatibilitySettingEntry(new CrossGroupCompatibility(sourceGroup, compatibleGroup, symmetric), enabled));
	}
	return FXCollections.observableArrayList(compatibilities);
    }

    static ObservableMap<String, Object> getNamingParameters(HierarchicalConfiguration<ImmutableNode> cfg, String key)
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
    static void addParsingServices(XMLConfiguration cfg, String key, List<ParsingServiceSettingEntry> parsingServices)
    {
	for (int i = 0; i < parsingServices.size(); i++)
	{
	    ParsingServiceSettingEntry ps = parsingServices.get(i);
	    cfg.addProperty(key + ".parsingService(" + i + ")", ps.getValue().getDomain());
	    cfg.addProperty(key + ".parsingService(" + i + ")[@enabled]", ps.isEnabled());
	}
    }

    static void addStandardizers(XMLConfiguration cfg, String key, List<StandardizerSettingEntry<?, ?>> standardizers)
    {
	// one index for each element name
	int seriesNameIndex = 0;
	int releaseTagsIndex = 0;
	for (StandardizerSettingEntry<?, ?> genericEntry : standardizers)
	{
	    if (genericEntry instanceof SeriesNameStandardizerSettingEntry)
	    {
		SeriesNameStandardizerSettingEntry entry = (SeriesNameStandardizerSettingEntry) genericEntry;
		SeriesNameStandardizer stdzer = entry.getValue();
		UserPattern namePattern = entry.getNameUserPattern();

		cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@enabled]", entry.isEnabled());
		cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@namePattern]", namePattern.getPattern());
		cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@namePatternMode]", namePattern.getMode());
		cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@nameReplacement]", stdzer.getNameReplacement());
		seriesNameIndex++;
	    }
	    else if (genericEntry instanceof ReleaseTagsStandardizerSettingEntry)
	    {
		ReleaseTagsStandardizerSettingEntry entry = (ReleaseTagsStandardizerSettingEntry) genericEntry;
		TagsReplacer replacer = entry.getValue().getReplacer();

		cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@enabled]", entry.isEnabled());
		cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@queryTags]", Tag.listToString(replacer.getQueryTags()));
		cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@replacement]", Tag.listToString(replacer.getReplacement()));
		cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@queryMode]", replacer.getQueryMode());
		cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@replaceMode]", replacer.getReplaceMode());
		cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@ignoreOrder]", replacer.getIgnoreOrder());
		releaseTagsIndex++;
	    }
	    else
	    {
		throw new IllegalArgumentException("Unknown standardizer: " + genericEntry);
	    }
	}
    }

    static void addPath(Configuration cfg, String key, Path path)
    {
	// WARNING: Need to use path.toString() because path implements iterable
	// and results in an endless loop when Commons-Configuration tries to print it
	cfg.addProperty(key, path == null ? "" : path.toString());
    }
}