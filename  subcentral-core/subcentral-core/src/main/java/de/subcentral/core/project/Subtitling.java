package de.subcentral.core.project;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.contribution.Contributions;
import de.subcentral.core.contribution.Work;
import de.subcentral.core.media.AvMedia;
import de.subcentral.core.subtitle.Subtitle;

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
		sub.setDate(ZonedDateTime.now());
		sub.setContributions(contributions);
		return sub;
	}

}
