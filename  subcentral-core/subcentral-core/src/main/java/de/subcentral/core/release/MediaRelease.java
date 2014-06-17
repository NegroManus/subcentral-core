package de.subcentral.core.release;

import com.google.common.base.Objects;

import de.subcentral.core.media.Media;

public class MediaRelease extends AbstractRelease<Media>
{
	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("materials", materials)
				.add("group", group)
				.add("tags", tags)
				.add("date", date)
				.add("nukeReason", nukeReason)
				.add("section", section)
				.add("info", info)
				.add("infoUrl", infoUrl)
				.toString();
	}
}
