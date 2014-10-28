package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Models;

public abstract class AbstractMedia implements Media
{
	protected String					title;
	protected Temporal					date;
	protected String					description;
	protected final List<String>		coverUrls		= new ArrayList<>(1);
	protected String					contentAdvisory;
	protected final List<Contribution>	contributions	= new ArrayList<>();
	// HashMap / HashSet initial capacities should be a power of 2
	protected final Set<String>			furtherInfoUrls	= new HashSet<>(4);

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
		this.date = Models.validateTemporalClass(date);
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
	public List<String> getCoverUrls()
	{
		return coverUrls;
	}

	public void setCoverUrls(List<String> coverUrls)
	{
		this.coverUrls.clear();
		this.coverUrls.addAll(coverUrls);
	}

	@Override
	public String getContentAdvisory()
	{
		return contentAdvisory;
	}

	public void setContentAdvisory(String contentAdvisory)
	{
		this.contentAdvisory = contentAdvisory;
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		this.contributions.clear();
		this.contributions.addAll(contributions);
	}

	@Override
	public Set<String> getFurtherInfoUrls()
	{
		return furtherInfoUrls;
	}

	public void setFurtherInfoUrls(Set<String> furtherInfoUrls)
	{
		this.furtherInfoUrls.clear();
		this.furtherInfoUrls.addAll(furtherInfoUrls);
	}
}
