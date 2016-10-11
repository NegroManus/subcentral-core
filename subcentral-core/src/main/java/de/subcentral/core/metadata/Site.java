package de.subcentral.core.metadata;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.subcentral.core.PropNames;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.SimplePropDescriptor;

public class Site extends NamedMetadataBase implements Comparable<Site> {
    private static final long                serialVersionUID  = 1932132712639923592L;

    public static final SimplePropDescriptor PROP_NAME         = new SimplePropDescriptor(Site.class, PropNames.NAME);
    public static final SimplePropDescriptor PROP_DISPLAY_NAME = new SimplePropDescriptor(Site.class, PropNames.DISPLAY_NAME);
    public static final SimplePropDescriptor PROP_LINK         = new SimplePropDescriptor(Site.class, PropNames.LINK);
    public static final SimplePropDescriptor PROP_IDS          = new SimplePropDescriptor(Site.class, PropNames.IDS);

    private String                           displayName;
    private String                           link;

    public Site() {
        // default constructor
    }

    public Site(String name) {
        this(name, null, null);
    }

    public Site(String name, String displayName) {
        this(name, displayName, null);
    }

    public Site(String name, String displayName, String link) {
        this.name = name;
        this.displayName = displayName;
        this.link = link;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDisplayNameOrName() {
        return displayName != null ? displayName : name;
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Site) {
            return Objects.equals(name, ((Site) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Site.class).omitNullValues().add("name", name).add("displayName", displayName).add("link", link).add("ids", ObjectUtil.nullIfEmpty(ids)).toString();
    }

    @Override
    public int compareTo(Site o) {
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
