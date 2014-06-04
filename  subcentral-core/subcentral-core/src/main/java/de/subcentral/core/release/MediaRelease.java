package de.subcentral.core.release;

import de.subcentral.core.media.Media;
import de.subcentral.core.naming.NamingStandards;

public class MediaRelease extends AbstractRelease<Media>
{
	@Override
	public String computeName()
	{
		return NamingStandards.MEDIA_RELEASE_NAMER.name(this);
	}
}
