package de.subcentral.core.release;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.util.Settings;

public abstract class AbstractRelease<M> implements Release<M>
{
	protected String	name;
	protected List<M>	materials	= new ArrayList<>(1);
	protected Group		group;
	protected List<Tag>	tags		= new ArrayList<>(4);
	protected Temporal	date;
	protected String	section;
	protected long		size;
	protected String	nukeReason;
	protected String	info;
	protected String	infoUrl;
	protected String	source;
	protected String	sourceUrl;

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public List<M> getMaterials()
	{
		return materials;
	}

	public void setMaterials(List<M> materials)
	{
		Validate.notNull(materials, "materials cannot be null");
		this.materials = materials;
	}

	@Override
	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	@Override
	public List<Tag> getTags()
	{
		return tags;
	}

	public void setTags(List<Tag> tags)
	{
		Validate.notNull(tags, "tags cannot be null");
		this.tags = tags;
	}

	@Override
	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		this.date = date;
	}

	@Override
	public String getSection()
	{
		return section;
	}

	public void setSection(String section)
	{
		this.section = section;
	}

	@Override
	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	@Override
	public String getNukeReason()
	{
		return nukeReason;
	}

	public void setNukeReason(String nukeReason)
	{
		this.nukeReason = nukeReason;
	}

	@Override
	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	@Override
	public String getInfoUrl()
	{
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl)
	{
		this.infoUrl = infoUrl;
	}

	@Override
	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	@Override
	public String getSourceUrl()
	{
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl)
	{
		this.sourceUrl = sourceUrl;
	}

	// Convenience
	public void setMaterial(M material)
	{
		this.materials = new ArrayList<>(1);
		this.materials.add(material);
	}

	@Override
	public boolean containsSingleMaterial()
	{
		return materials.size() == 1;
	}

	@Override
	public M getFirstMaterial()
	{
		return materials.isEmpty() ? null : materials.get(0);
	}

	@Override
	public boolean isNuked()
	{
		return nukeReason != null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Release)
		{
			return false;
		}
		Release<?> o = (Release<?>) obj;
		return Objects.equal(name, o.getName());
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(45, 3).append(name).toHashCode();
	}

	@Override
	public int compareTo(Release<?> o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.getName());
	}
}
