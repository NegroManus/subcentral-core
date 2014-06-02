package de.subcentral.core.naming;

import com.google.common.base.Joiner;

import de.subcentral.core.release.MediaRelease;

public class Addic7edMediaReleaseNamer implements Namer<MediaRelease> {

	// Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com

	@Override
	public Class<MediaRelease> getType() {
		return MediaRelease.class;
	}

	@Override
	public String name(MediaRelease obj, NamingService namingService) {
		if (obj.isExplicitNameSet()) {
			return obj.getExplicitName();
		}
		return new StringBuilder(namingService.name(obj.getFirstMaterial()))
				.append('.').append(Joiner.on('.').join(obj.getTags()))
				.append(obj.getTags().isEmpty() ? "" : '-')
				.append(obj.getGroup()).toString();
	}

}
