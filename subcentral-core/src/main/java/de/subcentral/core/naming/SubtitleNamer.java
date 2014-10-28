package de.subcentral.core.naming;

import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.subtitle.Subtitle;

public class SubtitleNamer extends AbstractPropertySequenceNamer<Subtitle>
{
	private NamingService	mediaNamingService	= NamingStandards.getDefaultNamingService();

	public NamingService getMediaNamingService()
	{
		return mediaNamingService;
	}

	public void setMediaNamingService(NamingService mediaNamingService)
	{
		Validate.notNull(mediaNamingService, "mediaNamingService cannot be null");
		this.mediaNamingService = mediaNamingService;
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Subtitle sub, Map<String, Object> params)
	{
		b.append(Subtitle.PROP_MEDIA, mediaNamingService.name(sub.getMedia(), params));
		b.append(Subtitle.PROP_LANGUAGE, sub.getLanguage());
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
		b.appendIfNotNull(Subtitle.PROP_GROUP, sub.getGroup());
		b.appendIfNotNull(Subtitle.PROP_SOURCE, sub.getSource());
	}
}
