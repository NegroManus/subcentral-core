package de.subcentral.core.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.release.Tag;

public class MediaReleaseNamer extends AbstractReleaseNamer<MediaRelease, Media>
{
	@Override
	public Class<MediaRelease> getType()
	{
		return MediaRelease.class;
	}

	@Override
	public String doName(MediaRelease rls, NamingService namingService, Map<String, Object> namingSettings)
	{
		StringBuilder sb = new StringBuilder();
		String mediaName = Medias.name(rls.getMaterials(), namingService, materialsSeparator);
		sb.append(formatMaterials(mediaName));
		if (!rls.getTags().isEmpty())
		{
			sb.append(materialsAndTagsSeparator);
			List<String> formattedTags = new ArrayList<>();
			for (Tag tag : rls.getTags())
			{
				formattedTags.add(formatTag(tag.getName()));
			}
			sb.append(Joiner.on(tagsSeparator).join(rls.getTags()));
		}
		if (rls.getGroup() != null)
		{
			sb.append(tagsAndGroupSeparator);
			sb.append(formatGroup(rls.getGroup().getName()));
		}
		return sb.toString();
	}
}
