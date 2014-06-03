package de.subcentral.core.subtitle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.contribution.Work;
import de.subcentral.core.release.AbstractRelease;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Tag;
import de.subcentral.core.util.StringUtil;

public class SubtitleRelease extends AbstractRelease<Subtitle> implements Work
{
	public static final String	CONTRIBUTION_TYPE_ADJUSTMENT	= "ADJUSTMENT";
	public static final String	CONTRIBUTION_TYPE_CUSTOMIZATION	= "CUSTOMIZATION";

	private String				format;
	private Set<MediaRelease>	compatibleMediaReleases			= new HashSet<>(2);
	private List<Contribution>	contributions					= new ArrayList<>(2);

	public String getFormat()
	{
		return format;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

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
	public String getImplicitName()
	{
		StringBuilder sb = new StringBuilder();
		MediaRelease firstCompatibleRls = getFirstCompatibleMediaRelease();
		if (firstCompatibleRls != null)
		{
			sb.append(firstCompatibleRls.getName());
		}
		Subtitle sub = getFirstMaterial();
		if (sub != null)
		{
			StringUtil.append(sb);
			sb.append(sub.getImplicitName(false));
		}

		if (tags != null && !tags.isEmpty())
		{
			for (Tag tag : tags)
			{
				StringUtil.append(sb);
				sb.append(tag.getName());
			}
		}
		if (group != null)
		{
			StringUtil.append(sb);
			sb.append(group.getName());
		}
		return sb.toString();
	}
}
