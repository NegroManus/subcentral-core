package de.subcentral.core.naming;

import java.util.Map;

import org.jsoup.helper.Validate;

import de.subcentral.core.model.subtitle.Subtitle;

public class SubtitleNamer extends AbstractPropertySequenceNamer<Subtitle>
{
	@Override
	public Class<Subtitle> getType()
	{
		return Subtitle.class;
	}

	@Override
	public String doName(Subtitle sub, NamingService namingService, Map<String, Object> params)
	{
		Validate.notNull(namingService, "namingService cannot be null");
		Builder b = new Builder();
		b.appendString(Subtitle.PROP_MEDIA_ITEM, namingService.name(sub.getMediaItem(), params));
		b.append(Subtitle.PROP_LANGUAGE, sub.getLanguage());
		return b.build();
	}
}
