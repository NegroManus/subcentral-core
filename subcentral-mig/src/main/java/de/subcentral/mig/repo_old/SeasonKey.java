package de.subcentral.mig.repo_old;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.name.NamingDefaults;

public class SeasonKey {
    private final SeriesKey series;
    private final Integer   number;
    private final String    title;

    public SeasonKey(Season season) {
        this(new SeriesKey(season.getSeries()), season.getNumber(), season.getTitle());
    }

    public SeasonKey(SeriesKey series, Integer number, String title) {
        this.series = series;
        this.number = number;
        this.title = NamingDefaults.getDefaultNormalizingFormatter().apply(title);
    }

    public SeriesKey getSeries() {
        return series;
    }

    public Integer getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SeasonKey) {
            SeasonKey other = (SeasonKey) obj;
            return this.series.equals(other.series) && Objects.equal(number, other.number) && Objects.equal(title, other.title);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(191, 72).append(series).append(number).append(title).toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SeriesKey.class).add("series", series).add("number", number).add("title", title).toString();
    }
}
