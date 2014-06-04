package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.subcentral.core.contribution.Contribution;

public abstract class AbstractMedia implements Media
{
	protected String				name;
	protected String				title;
	protected Temporal				date;
	protected Set<String>			genres			= new HashSet<>(4);
	protected String				description;
	protected String				coverUrl;
	protected List<Contribution>	contributions	= new ArrayList<>();

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
	public String computeName()
	{
		return title;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
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
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		this.genres = genres;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String getCoverUrl()
	{
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl)
	{
		this.coverUrl = coverUrl;
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		this.contributions = contributions;
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
		AbstractMedia other = (AbstractMedia) obj;
		String thisName = getNameOrCompute();
		String otherName = other.getNameOrCompute();
		return thisName != null ? thisName.equals(otherName) : otherName == null;
	}

	@Override
	public int hashCode()
	{
		String name = getNameOrCompute();
		return name == null ? 0 : name.hashCode();
	}
}
