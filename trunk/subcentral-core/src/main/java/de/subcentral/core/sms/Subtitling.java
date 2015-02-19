package de.subcentral.core.sms;

import java.util.List;
import java.util.Map;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.ContributionUtils;
import de.subcentral.core.metadata.Work;
import de.subcentral.core.metadata.media.AvMedia;
import de.subcentral.core.metadata.subtitle.Subtitle;

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

	public double getProgress(String contributionType)
	{
		return ContributionUtils.calcProgress(contributions, contributionType);
	}

	public Map<String, Float> getProgresses()
	{
		return ContributionUtils.calcProgresses(contributions);
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
