package de.subcentral.core.sms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.ContributionUtil;
import de.subcentral.core.metadata.Work;
import de.subcentral.core.metadata.media.AvMedia;
import de.subcentral.core.metadata.subtitle.Subtitle;

public class Subtitling implements Work
{
	private AvMedia						media;
	private String						language;
	private String						info;
	private final List<Contribution>	contributions	= new ArrayList<>(4);

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

	public void setContributions(Collection<Contribution> contributions)
	{
		this.contributions.clear();
		this.contributions.addAll(contributions);
	}

	public double getProgress(String contributionType)
	{
		return ContributionUtil.calcProgress(contributions, contributionType);
	}

	public Map<String, Float> getProgresses()
	{
		return ContributionUtil.calcProgresses(contributions);
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
