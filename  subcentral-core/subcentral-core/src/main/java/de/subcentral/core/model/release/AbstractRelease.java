package de.subcentral.core.model.release;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.util.Settings;

public abstract class AbstractRelease<M> implements Release<M>
{
	protected String	name;
	protected List<M>	materials	= new ArrayList<>(1);
	protected Temporal	date;
	protected long		size;
	protected String	nukeReason;

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
	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		this.date = date;
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

	// Convenience
	public void setMaterial(M material)
	{
		this.materials = new ArrayList<>(1);
		if (material != null)
		{
			this.materials.add(material);
		}
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
		if (this == obj)
		{
			return true;
		}
		if (obj != null && this.getClass().equals(obj.getClass()))
		{
			Release<?> o = (Release<?>) obj;
			return Objects.equal(name, o.getName());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(45, 3).append(getClass()).append(name).toHashCode();
	}

	@Override
	public int compareTo(Release<?> o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(getClass().getName(), o.getClass().getName(), Settings.STRING_ORDERING)
				.compare(name, o.getName(), Settings.STRING_ORDERING)
				.result();
	}
}
