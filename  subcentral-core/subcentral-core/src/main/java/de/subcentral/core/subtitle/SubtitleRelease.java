package de.subcentral.core.subtitle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.contribution.Work;
import de.subcentral.core.release.AbstractRelease;
import de.subcentral.core.release.MediaRelease;

public class SubtitleRelease extends AbstractRelease<Subtitle> implements Work
{
	public static final String	CONTRIBUTION_TYPE_ADJUSTMENT	= "ADJUSTMENT";
	public static final String	CONTRIBUTION_TYPE_CUSTOMIZATION	= "CUSTOMIZATION";

	private Set<MediaRelease>	compatibleMediaReleases			= new HashSet<>(2);
	private List<Contribution>	contributions					= new ArrayList<>(2);

	public Set<MediaRelease> getCompatibleMediaReleases()
	{
		return compatibleMediaReleases;
	}

	public MediaRelease getFirstCompatibleMediaRelease()
	{
		return compatibleMediaReleases.isEmpty() ? null : compatibleMediaReleases.iterator().next();
	}

	public void setCompatibleMediaReleases(Set<MediaRelease> compatibleMediaReleases)
	{
		this.compatibleMediaReleases = compatibleMediaReleases;
	}

	public void setCompatibleMediaRelease(MediaRelease compatibleMediaRelease)
	{
		this.compatibleMediaReleases = new HashSet<>(1);
		this.compatibleMediaReleases.add(compatibleMediaRelease);
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

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("materials", materials)
				.add("compatibleMediaReleases", compatibleMediaReleases)
				.add("group", group)
				.add("tags", tags)
				.add("date", date)
				.add("nukeReason", nukeReason)
				.add("section", section)
				.add("info", info)
				.add("infoUrl", infoUrl)
				.add("contributions", contributions)
				.toString();
	}
}
