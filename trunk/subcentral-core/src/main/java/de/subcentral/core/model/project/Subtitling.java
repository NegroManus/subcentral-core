package de.subcentral.core.model.project;

import java.util.List;
import java.util.Map;

import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Contributions;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMedia;
import de.subcentral.core.model.subtitle.Subtitle;

public class Subtitling implements Work
{
	private AvMedia				media;
	private String				language;
	private String				info;
	private List<Contribution>	contributions;

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

	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
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

	public double getProgress()
	{
		return Contributions.calcProgress(contributions);
	}

	public double getProgress(String contributionType)
	{
		return Contributions.calcProgress(contributions, contributionType);
	}

	public Map<String, Double> getProgresses()
	{
		return Contributions.calcProgresses(contributions);
	}

	public Subtitle toSubtitle()
	{
		Subtitle sub = new Subtitle();
		sub.setMedia(media);
		sub.setLanguage(language);
		sub.setContributions(contributions);
		return sub;
	}

}
