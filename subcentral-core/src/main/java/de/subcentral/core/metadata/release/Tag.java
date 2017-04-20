package de.subcentral.core.metadata.release;

import java.io.Serializable;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.ValidationUtil;

/**
 * Value object.
 * 
 * @implSpec #value-object #immutable #thread-safe
 */
public class Tag implements Comparable<Tag>, Serializable {
    private static final long  serialVersionUID  = -6045437773807621255L;

    /**
     * Tags describing the source. Like HDTV, BluRay, BDRip, DVDRip, ...
     */
    public static final String CATEGORY_SOURCE   = "SOURCE";

    /**
     * Tags describing the format. Like x264, XviD, DD5.1, AC3, 720p, 1080p, (subtitle tags), ... Not including the language tags.
     */
    public static final String CATEGORY_FORMAT   = "FORMAT";

    /**
     * Language tags. Like German, GERMAN.CUSTOM.SUBBED, NLSUBBED, MULTi, ...
     */
    public static final String CATEGORY_LANGUAGE = "LANGUAGE";

    /**
     * Tags for meta information about the release itself, not about the content. Like PROPER, REPACK, READ INFO, iNTERNAL, DIRFIX, ...
     */
    public static final String CATEGORY_META     = "META";

    private final String       name;

    /**
     * 
     * @param name
     * @throws IllegalArgumentException
     */
    private Tag(String name) {
        this.name = ValidationUtil.requireNotBlankAndStrip(name, "name cannot be blank");
    }

    public static Tag of(String name) {
        return new Tag(name);
    }

    public static Tag ofOrNull(String name) {
        try {
            return of(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Tag) {
            return ObjectUtil.stringEqualIgnoreCase(name, ((Tag) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ObjectUtil.stringHashCodeIgnoreCase(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Tag o) {
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
