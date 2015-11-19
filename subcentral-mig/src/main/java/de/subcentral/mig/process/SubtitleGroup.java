package de.subcentral.mig.process;

import com.google.common.base.MoreObjects;

public class SubtitleGroup extends AbstractContributor
{
	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SubtitleGroup.class).omitNullValues().add("name", name).toString();
	}
}