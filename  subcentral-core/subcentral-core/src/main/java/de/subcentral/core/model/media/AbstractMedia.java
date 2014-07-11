package de.subcentral.core.model.media;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

import de.subcentral.core.model.Contribution;

public abstract class AbstractMedia implements Media
{
	protected String				title;
	protected String				description;
	protected String				coverUrl;
	protected String				contentAdvisory;
	protected List<Contribution>	contributions			= new ArrayList<>();
	protected List<String>			furtherInformationUrls	= new ArrayList<>(3);

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
	public String getCoverUrl()
	{
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl)
	{
		this.coverUrl = coverUrl;
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
		this.contributions = contributions;
	}

	@Override
	public List<String> getFurtherInformationUrls()
	{
		return furtherInformationUrls;
	}

	public void setFurtherInformationUrls(List<String> furtherInformationUrls)
	{
		Validate.notNull(furtherInformationUrls, "furtherInformationUrls cannot be null");
		this.furtherInformationUrls = furtherInformationUrls;
	}

	// Convenience / Complex
	@Override
	public boolean isTitled()
	{
		return title != null;
	}
}
