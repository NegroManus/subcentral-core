package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class SubtitleNamer extends AbstractPropertySequenceNamer<Subtitle>
{
    private final NamingService mediaNamingService;

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
