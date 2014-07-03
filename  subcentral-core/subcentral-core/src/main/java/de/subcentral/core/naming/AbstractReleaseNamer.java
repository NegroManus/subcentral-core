package de.subcentral.core.naming;

import de.subcentral.core.release.Release;
import de.subcentral.core.util.Replacer;

public abstract class AbstractReleaseNamer<R extends Release<M>, M> extends AbstractNamer<R>
{
	protected Replacer	materialsReplacer			= NamingStandards.STANDARD_REPLACER;
	protected String	materialsFormat				= "%s";
	protected Replacer	tagReplacer					= NamingStandards.STANDARD_REPLACER;
	protected String	tagFormat					= "%s";
	protected Replacer	groupReplacer				= NamingStandards.STANDARD_REPLACER;
	protected String	groupFormat					= "%s";

	protected String	materialsSeparator			= ".";
	protected String	materialsAndTagsSeparator	= ".";
	protected String	tagsSeparator				= ".";
	protected String	tagsAndGroupSeparator		= "-";

	// replacer and formats
	public Replacer getMaterialsReplacer()
	{
		return materialsReplacer;
	}

	public void setMaterialsReplacer(Replacer materialsReplacer)
	{
		this.materialsReplacer = materialsReplacer;
	}

	public String getMaterialsFormat()
	{
		return materialsFormat;
	}

	public void setMaterialsFormat(String materialsFormat)
	{
		this.materialsFormat = materialsFormat;
	}

	public Replacer getTagReplacer()
	{
		return tagReplacer;
	}

	public void setTagReplacer(Replacer tagReplacer)
	{
		this.tagReplacer = tagReplacer;
	}

	public String getTagFormat()
	{
		return tagFormat;
	}

	public void setTagFormat(String tagFormat)
	{
		this.tagFormat = tagFormat;
	}

	public Replacer getGroupReplacer()
	{
		return groupReplacer;
	}

	public void setGroupReplacer(Replacer groupReplacer)
	{
		this.groupReplacer = groupReplacer;
	}

	public String getGroupFormat()
	{
		return groupFormat;
	}

	public void setGroupFormat(String groupFormat)
	{
		this.groupFormat = groupFormat;
	}

	// separators
	public String getMaterialsSeparator()
	{
		return materialsSeparator;
	}

	public void setMaterialsSeparator(String materialsSeparator)
	{
		this.materialsSeparator = materialsSeparator;
	}

	public String getMaterialsAndTagsSeparator()
	{
		return materialsAndTagsSeparator;
	}

	public void setMaterialsAndTagsSeparator(String materialsAndTagsSeparator)
	{
		this.materialsAndTagsSeparator = materialsAndTagsSeparator;
	}

	public String getTagsSeparator()
	{
		return tagsSeparator;
	}

	public void setTagsSeparator(String tagsSeparator)
	{
		this.tagsSeparator = tagsSeparator;
	}

	public String getTagsAndGroupSeparator()
	{
		return tagsAndGroupSeparator;
	}

	public void setTagsAndGroupSeparator(String tagsAndGroupSeparator)
	{
		this.tagsAndGroupSeparator = tagsAndGroupSeparator;
	}

	// format methods
	public String formatMaterials(String materials)
	{
		return String.format(materialsFormat, Replacer.replace(materials, materialsReplacer));
	}

	public String formatTag(String tag)
	{
		return String.format(tagFormat, Replacer.replace(tag, tagReplacer));
	}

	public String formatGroup(String group)
	{
		return String.format(groupFormat, Replacer.replace(group, groupReplacer));
	}
}
