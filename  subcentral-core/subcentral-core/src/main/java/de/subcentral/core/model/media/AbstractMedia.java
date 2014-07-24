package de.subcentral.core.model.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import de.subcentral.core.model.Contribution;

public abstract class AbstractMedia implements Media
{
	protected String				title;
	protected String				description;
	protected List<String>			coverUrls		= new ArrayList<>(1);
	protected String				contentAdvisory;
	protected List<Contribution>	contributions	= new ArrayList<>();
	protected Set<String>			furtherInfoUrls	= new HashSet<>(4);

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
		Validate.notNull(coverUrls, "coverUrls cannot be null");
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
		Validate.notNull(contributions, "contributions cannot be null");
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
		Validate.notNull(furtherInfoUrls, "furtherInfoUrls cannot be null");
		this.furtherInfoUrls = furtherInfoUrls;
	}

	// Convenience / Complex
	@Override
	public boolean isTitled()
	{
		return title != null;
	}
}
