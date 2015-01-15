package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.ModelUtils;

public abstract class AbstractMedia implements Media
{
	protected String								title;
	protected Temporal								date;
	protected String								description;
	protected final List<String>					coverLinks			= new ArrayList<>(1);
	protected String								contentRating;
	protected final List<Contribution>				contributions		= new ArrayList<>(0);
	protected final List<String>					furtherInfoLinks	= new ArrayList<>(4);
	protected final ListMultimap<String, Object>	attributes			= LinkedListMultimap.create(0);

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

	public void setDate(Temporal date) throws IllegalArgumentException
	{
		this.date = ModelUtils.validateTemporalClass(date);
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
	public List<String> getCoverLinks()
	{
		return coverLinks;
	}

	public void setCoverLinks(Collection<String> coverLinks)
	{
		this.coverLinks.clear();
		this.coverLinks.addAll(coverLinks);
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

	public void setContributions(Collection<Contribution> contributions)
	{
		this.contributions.clear();
		this.contributions.addAll(contributions);
	}

	@Override
	public List<String> getFurtherInfoLinks()
	{
		return furtherInfoLinks;
	}

	public void setFurtherInfoLinks(Collection<String> furtherInfoLinks)
	{
		this.furtherInfoLinks.clear();
		this.furtherInfoLinks.addAll(furtherInfoLinks);
	}

	@Override
	public ListMultimap<String, Object> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(ListMultimap<String, Object> attributes)
	{
		this.attributes.clear();
		this.attributes.putAll(attributes);
	}
}
