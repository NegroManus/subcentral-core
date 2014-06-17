package de.subcentral.core.release;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ComparisonChain;

import de.subcentral.core.util.Settings;

public abstract class AbstractRelease<M> implements Release<M>
{
	protected String	name;
	protected List<M>	materials	= new ArrayList<>(1);
	protected Group		group;
	protected List<Tag>	tags		= new ArrayList<>(4);
	protected Temporal	date;
	protected String	nukeReason;
	protected String	section;
	protected long		size;
	protected String	info;
	protected String	infoUrl;

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
	public String getNukeReason()
	{
		return nukeReason;
	}

	public void setNukeReason(String nukeReason)
	{
		this.nukeReason = nukeReason;
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
		return !materials.isEmpty() ? materials.get(0) : null;
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
		if (this.getClass() != obj.getClass())
		{
			return false;
		}
		AbstractRelease<?> other = (AbstractRelease<?>) obj;
		return new EqualsBuilder().append(getMaterials(), other.getMaterials()).append(group, other.group).append(tags, other.tags).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(23, 27).append(getMaterials()).append(group).append(tags).toHashCode();
	}

	@Override
	public int compareTo(Release<?> o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
		// TODO Comparator for Materials
		// .compare(getMaterials(), o.getMaterials(), Settings.STRING_ORDERING)
				.compare(group.getName(), o.getGroup().getName(), Settings.STRING_ORDERING)
				.compare(tags, o.getTags(), Releases.MEDIA_NAME_COMPARATOR)
				.result();
	}
}
