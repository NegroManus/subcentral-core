package de.subcentral.core.impl.com.addic7ed;

import com.google.common.base.Joiner;

import de.subcentral.core.naming.Namer;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.subtitle.SubtitleRelease;

public class Addic7edSubtitleReleaseNamer implements Namer<SubtitleRelease>
{
	// Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com

	@Override
	public Class<SubtitleRelease> getType()
	{
		return SubtitleRelease.class;
	}

	@Override
	public String name(SubtitleRelease obj, NamingService namingService)
	{
		return new StringBuilder().append(namingService.name(obj.getFirstCompatibleMediaRelease()))
				.append('.')
				.append(obj.getFirstMaterial().getLanguage())
				.append('.')
				.append(Joiner.on('.').join(obj.getTags()))
				.append('.')
				.append("Addic7ed.com")
				.toString();
	}
}
