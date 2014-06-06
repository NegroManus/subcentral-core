package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.subcentral.core.contribution.Contribution;

public abstract class AbstractMedia implements Media
{
	protected String				title;
	protected Temporal				date;
	protected Set<String>			genres			= new HashSet<>(4);
	protected String				description;
	protected String				coverUrl;
	protected String				contentRating;
	protected List<Contribution>	contributions	= new ArrayList<>();

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
	public String getContentRating()
	{
		return contentRating;
	}

	public void setContentRating(String contentRating)
	{
		this.contentRating = contentRating;
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
}
