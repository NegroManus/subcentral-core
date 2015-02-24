package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class SubtitleNamer extends AbstractPropertySequenceNamer<Subtitle>
{
	private final NamingService	mediaNamingService;

	public SubtitleNamer(PropSequenceNameBuilder.Config config, NamingService mediaNamingService)
	{
		super(config);
		this.mediaNamingService = Objects.requireNonNull(mediaNamingService, "mediaNamingService");
	}

	public NamingService getMediaNamingService()
	{
		return mediaNamingService;
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Subtitle sub, Map<String, Object> params)
	{
		b.appendIfNotBlank(Subtitle.PROP_MEDIA, mediaNamingService.name(sub.getMedia(), params));
		b.appendIfNotNull(Subtitle.PROP_LANGUAGE, sub.getLanguage());
		b.append(Subtitle.PROP_HEARING_IMPAIRED, sub.isHearingImpaired());
		b.append(Subtitle.PROP_FOREIGN_PARTS, sub.getForeignParts());
		b.appendAll(Subtitle.PROP_TAGS, sub.getTags());
		b.appendIfNotNull(Subtitle.PROP_VERSION, sub.getVersion());
		if (sub.getGroup() != null)
		{
			b.append(Subtitle.PROP_GROUP, sub.getGroup());
		}
		else
		{
			b.appendIfNotNull(Subtitle.PROP_SOURCE, sub.getSource());
		}
	}
}
