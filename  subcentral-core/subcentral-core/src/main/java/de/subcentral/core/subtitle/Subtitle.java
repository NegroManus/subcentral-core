package de.subcentral.core.subtitle;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.contribution.Work;
import de.subcentral.core.media.AvMedia;

public class Subtitle implements Work
{
	public static final String	CONTRIBUTION_TYPE_TRANSCRIPT	= "TRANSCRIPT";
	public static final String	CONTRIBUTION_TYPE_TIMINGS		= "TIMINGS";
	public static final String	CONTRIBUTION_TYPE_TRANSLATION	= "TRANSLATION";
	public static final String	CONTRIBUTION_TYPE_REVISION		= "REVISION";

	private AvMedia				media;
	private String				language;
	private Temporal			date;
	private String				productionType;
	private String				description;
	private List<Contribution>	contributions					= new ArrayList<>();

	public String computeName()
	{

	}

	public AvMedia getMedia()
	{
		return media;
	}

	public void setMedia(AvMedia media)
	{
		this.media = media;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		this.date = date;
	}

	public String getProductionType()
	{
		return productionType;
	}

	public void setProductionType(String productionType)
	{
		this.productionType = productionType;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isTranslation()
	{
		return !language.equals(media.getOriginalLanguage());
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
