package de.subcentral.mig;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;
import de.subcentral.core.file.subtitle.SubtitleContent;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;

public class SubFile implements Comparable<SubFile>
{
	private final SubtitleAdjustment	subtitleMetadata;
	private SubtitleContent				subtitleData;
	private final Set<Path>				files	= new HashSet<>(4);

	public SubFile(SubtitleAdjustment subtitleMetadata, Path file)
	{
		this.subtitleMetadata = subtitleMetadata;
		files.add(file);
	}

	public SubFile updateWithContributions(List<Contribution> contributions)
	{
		for (Contribution c : contributions)
		{
			if (SubtitleAdjustment.CONTRIBUTION_TYPE_ADJUSTMENT.equals(c.getType()))
			{
				subtitleMetadata.getContributions().add(c);
			}
			else
			{
				for (Subtitle sub : subtitleMetadata.getSubtitles())
				{
					sub.getContributions().add(c);
				}
			}
		}
		return this;
	}

	public SubFile updateWithMatchingRelease(SubFile other)
	{
		subtitleMetadata.getMatchingReleases().addAll(other.subtitleMetadata.getMatchingReleases());
		files.addAll(other.files);
		return this;
	}

	public SubtitleAdjustment getSubtitleMetadata()
	{
		return subtitleMetadata;
	}

	public SubtitleContent getSubtitleData()
	{
		return subtitleData;
	}

	public void updateWithData(SubtitleContent subtitleData)
	{
		this.subtitleData = subtitleData;
	}

	public Set<Path> getFiles()
	{
		return files;
	}

	@Override
	public int compareTo(SubFile o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start().compare(subtitleMetadata, o.subtitleMetadata, Settings.createDefaultOrdering()).result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SubFile.class)
				.add("subtitleMetadata", subtitleMetadata)
				.add("subtitleData.items.size", subtitleData.getItems().size())
				.omitNullValues()
				.add("files", files)
				.toString();
	}

}