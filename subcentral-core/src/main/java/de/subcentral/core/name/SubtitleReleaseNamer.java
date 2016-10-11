package de.subcentral.core.name;

import java.util.Objects;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.util.Context;

public class SubtitleReleaseNamer extends AbstractPropertySequenceNamer<SubtitleRelease> {
    /**
     * The name of the parameter "preferName" of type {@link Boolean}. If set to {@code true} and the {@link SubtitleRelease#getName() subtitle adjustment's name} is not {@code null}, that name is
     * returned, otherwise the computed name is returned. The default value is {@code false}.
     */
    public static final String   PARAM_PREFER_NAME    = SubtitleReleaseNamer.class.getName() + ".preferName";

    /**
     * The name of the parameter "release" of type {@link Release}. The specified release is used for naming the subtitle adjustment. The default value is the return value of
     * {@link SubtitleRelease#getFirstMatchingRelease()}.
     */
    public static final String   PARAM_RELEASE        = SubtitleReleaseNamer.class.getName() + ".release";

    /**
     * Shortcut to {@link SubtitleNamer#PARAM_INCLUDE_GROUP}.
     */
    public static final String   PARAM_INCLUDE_GROUP  = SubtitleNamer.PARAM_INCLUDE_GROUP;

    /**
     * Shortcut to {@link SubtitleNamer#PARAM_INCLUDE_SOURCE}.
     */
    public static final String   PARAM_INCLUDE_SOURCE = SubtitleNamer.PARAM_INCLUDE_SOURCE;

    private final Namer<Release> releaseNamer;

    public SubtitleReleaseNamer(PropSequenceNameBuilder.Config config, Namer<Release> releaseNamer) {
        super(config);
        this.releaseNamer = Objects.requireNonNull(releaseNamer, "releaseNamer");
    }

    public Namer<Release> getReleaseNamer() {
        return releaseNamer;
    }

    @Override
    protected void appendName(PropSequenceNameBuilder b, SubtitleRelease subRls, Context ctx) {
        // read useName parameter
        if (subRls.getName() != null && ctx.getBoolean(PARAM_PREFER_NAME, Boolean.FALSE)) {
            b.append(SubtitleRelease.PROP_NAME, subRls.getName());
            return;
        }

        // read other naming parameters
        Release rls = ctx.get(PARAM_RELEASE, Release.class, subRls.getFirstMatchingRelease());
        b.appendRaw(SubtitleRelease.PROP_MATCHING_RELEASES, releaseNamer.name(rls, ctx));

        Subtitle sub = subRls.getFirstSubtitle();
        if (sub != null) {
            b.appendIfNotNull(Subtitle.PROP_LANGUAGE, sub.getLanguage());
        }
        b.append(SubtitleRelease.PROP_TAGS, subRls.getTags());
        b.appendIfNotNull(SubtitleRelease.PROP_VERSION, subRls.getVersion());
        if (sub != null) {
            if (sub.getGroup() != null && ctx.getBoolean(PARAM_INCLUDE_GROUP, Boolean.TRUE)) {
                b.append(Subtitle.PROP_GROUP, sub.getGroup());
            }
            if (sub.getSource() != null && ctx.getBoolean(PARAM_INCLUDE_SOURCE, Boolean.FALSE)) {
                b.append(Subtitle.PROP_SOURCE, sub.getSource());
            }
        }
    }
}
