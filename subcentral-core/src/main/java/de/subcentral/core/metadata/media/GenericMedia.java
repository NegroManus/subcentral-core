package de.subcentral.core.metadata.media;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.subcentral.core.PropNames;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any media type that has no own class, like a movie, or if the media type could not be determined.
 *
 */
public class GenericMedia extends StandaloneMedia implements Comparable<GenericMedia> {
    private static final long                serialVersionUID        = 466506490140852772L;

    public static final SimplePropDescriptor PROP_NAME               = new SimplePropDescriptor(GenericMedia.class, PropNames.NAME);
    public static final SimplePropDescriptor PROP_ALIAS_NAMES        = new SimplePropDescriptor(GenericMedia.class, PropNames.ALIAS_NAMES);
    public static final SimplePropDescriptor PROP_TITLE              = new SimplePropDescriptor(GenericMedia.class, PropNames.TITLE);
    public static final SimplePropDescriptor PROP_MEDIA_CONTENT_TYPE = new SimplePropDescriptor(GenericMedia.class, PropNames.MEDIA_CONTENT_TYPE);
    public static final SimplePropDescriptor PROP_MEDIA_TYPE         = new SimplePropDescriptor(GenericMedia.class, PropNames.MEDIA_TYPE);
    public static final SimplePropDescriptor PROP_DATE               = new SimplePropDescriptor(GenericMedia.class, PropNames.DATE);
    public static final SimplePropDescriptor PROP_LANGUAGES          = new SimplePropDescriptor(GenericMedia.class, PropNames.LANGUAGES);
    public static final SimplePropDescriptor PROP_COUNTRIES          = new SimplePropDescriptor(GenericMedia.class, PropNames.COUNTRIES);
    public static final SimplePropDescriptor PROP_GENRES             = new SimplePropDescriptor(GenericMedia.class, PropNames.GENRES);
    public static final SimplePropDescriptor PROP_RUNNING_TIME       = new SimplePropDescriptor(GenericMedia.class, PropNames.RUNNING_TIME);
    public static final SimplePropDescriptor PROP_DESCRIPTION        = new SimplePropDescriptor(GenericMedia.class, PropNames.DESCRIPTION);
    public static final SimplePropDescriptor PROP_IMAGES             = new SimplePropDescriptor(GenericMedia.class, PropNames.IMAGES);
    public static final SimplePropDescriptor PROP_CONTENT_RATING     = new SimplePropDescriptor(GenericMedia.class, PropNames.CONTENT_RATING);
    public static final SimplePropDescriptor PROP_FURTHER_INFO_LINKS = new SimplePropDescriptor(GenericMedia.class, PropNames.FURTHER_INFO_LINKS);
    public static final SimplePropDescriptor PROP_IDS                = new SimplePropDescriptor(GenericMedia.class, PropNames.IDS);
    public static final SimplePropDescriptor PROP_ATTRIBUTES         = new SimplePropDescriptor(GenericMedia.class, PropNames.ATTRIBUTES);

    private String                           mediaType;
    private String                           mediaContentType;

    public GenericMedia() {
        // default constructor
    }

    public GenericMedia(String name) {
        this.name = name;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String getMediaContentType() {
        return mediaContentType;
    }

    public void setMediaContentType(String mediaContentType) {
        this.mediaContentType = mediaContentType;
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof GenericMedia) {
            return Objects.equals(name, ((GenericMedia) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(GenericMedia.class)
                .omitNullValues()
                .add("name", name)
                .add("aliasNames", ObjectUtil.nullIfEmpty(aliasNames))
                .add("title", title)
                .add("mediaType", mediaType)
                .add("mediaContentType", mediaContentType)
                .add("date", date)
                .add("languages", ObjectUtil.nullIfEmpty(languages))
                .add("countries", ObjectUtil.nullIfEmpty(countries))
                .add("runningTime", ObjectUtil.nullIfZero(runningTime))
                .add("genres", ObjectUtil.nullIfEmpty(genres))
                .add("description", description)
                .add("ratings", ObjectUtil.nullIfEmpty(ratings))
                .add("contentRating", contentRating)
                .add("images", ObjectUtil.nullIfEmpty(images))
                .add("furtherInfoLinks", ObjectUtil.nullIfEmpty(furtherInfoLinks))
                .add("ids", ids)
                .add("attributes", ObjectUtil.nullIfEmpty(attributes))
                .toString();
    }

    @Override
    public int compareTo(GenericMedia o) {
        if (this == o) {
            return 0;
        }
        // nulls first
        if (o == null) {
            return 1;
        }
        return ObjectUtil.getDefaultStringOrdering().compare(name, o.name);
    }
}
