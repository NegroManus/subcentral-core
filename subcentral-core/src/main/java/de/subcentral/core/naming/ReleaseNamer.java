package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.release.Release;

public class ReleaseNamer extends AbstractPropertySequenceNamer<Release>
{
    /**
     * The name of the parameter "preferName" of type {@link Boolean}. If set to {@code true} and the {@link Release#getName() release's name} is not {@code null}, that name is returned, otherwise the
     * computed name is returned. The default value is {@code false}.
     */
    public static final String PARAM_PREFER_NAME = ReleaseNamer.class.getName() + ".preferName";

    private final NamingService mediaNamingService;

    public ReleaseNamer(PropSequenceNameBuilder.Config config, NamingService mediaNamingService)
    {
	super(config);
	this.mediaNamingService = Objects.requireNonNull(mediaNamingService, "mediaNamingService");
    }

    public NamingService getMediaNamingService()
    {
	return mediaNamingService;
    }

    @Override
    public void buildName(PropSequenceNameBuilder b, Release rls, Map<String, Object> params)
    {
	// read naming parameters
	boolean preferName = NamingUtil.readParameter(params, PARAM_PREFER_NAME, Boolean.class, Boolean.FALSE);
	if (preferName && rls.getName() != null)
	{
	    b.append(Release.PROP_NAME, rls.getName());
	    return;
	}
	b.appendIfNotBlank(Release.PROP_MEDIA, mediaNamingService.name(rls.getMedia(), params));
	b.appendAll(Release.PROP_TAGS, rls.getTags());
	b.appendIfNotNull(Release.PROP_GROUP, rls.getGroup());
    }
}
