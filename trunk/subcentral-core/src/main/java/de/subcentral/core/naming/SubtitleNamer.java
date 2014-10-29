package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.util.Separation;

public class SubtitleNamer extends AbstractPropertySequenceNamer<Subtitle>
{
	private final NamingService	mediaNamingService;

	protected SubtitleNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter, NamingService mediaNamingService)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
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
		b.appendIf(Subtitle.PROP_HEARING_IMPAIRED, Subtitle.TAG_HEARING_IMPAIRED.getName(), sub.isHearingImpaired());
		switch (sub.getForeignParts())
		{
			case NONE:
				break;
			case INCLUDED:
				b.append(Subtitle.PROP_FOREIGN_PARTS, "FOREIGN PARTS INCL");
				break;
			case EXCLUDED:
				b.append(Subtitle.PROP_FOREIGN_PARTS, "FOREIGN PARTS EXCL");
				break;
			case ONLY:
				b.append(Subtitle.PROP_FOREIGN_PARTS, "FOREIGN PARTS ONLY");
				break;
			default:
				break;
		}
		b.appendAll(Subtitle.PROP_TAGS, sub.getTags());
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
