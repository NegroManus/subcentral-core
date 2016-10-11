package de.subcentral.core.name;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.util.Context;

public class SeasonNamer extends AbstractPropertySequenceNamer<Season> {
    /**
     * The name of the parameter "includeSeries" of type {@link Boolean}. If set to {@code true}, the season's series is included in the name, otherwise it is excluded. The default value is
     * {@code true}.
     */
    public static final String PARAM_INCLUDE_SERIES       = SeasonNamer.class.getName() + ".includeSeries";

    /**
     * The name of the parameter "alwaysIncludeTitle" of type {@link Boolean}. If set to {@code true}, the title of the season is always included in the name, otherwise only if the season is not
     * numbered. The default value is {@code false}.
     */
    public static final String PARAM_ALWAYS_INCLUDE_TITLE = SeasonNamer.class.getName() + ".alwaysIncludeTitle";

    private final SeriesNamer  seriesNamer;

    public SeasonNamer(PropSequenceNameBuilder.Config config) {
        this(config, null);
    }

    public SeasonNamer(PropSequenceNameBuilder.Config config, SeriesNamer seriesNamer) {
        super(config);
        this.seriesNamer = seriesNamer != null ? seriesNamer : new SeriesNamer(config);
    }

    public SeriesNamer getSeriesNamer() {
        return seriesNamer;
    }

    @Override
    protected void appendName(PropSequenceNameBuilder b, Season season, Context ctx) {
        // read naming parameters
        boolean includeSeries = ctx.getBoolean(PARAM_INCLUDE_SERIES, Boolean.TRUE);

        // add series
        if (includeSeries && season.getSeries() != null) {
            seriesNamer.appendName(b, season.getSeries(), ctx);
        }

        appendOwnName(b, season, ctx);
    }

    protected void appendOwnName(PropSequenceNameBuilder b, Season season, Context ctx) {
        // add season
        if (season.isNumbered()) {
            b.append(Season.PROP_NUMBER, season.getNumber());
            boolean alwaysIncludeTitle = ctx.getBoolean(PARAM_ALWAYS_INCLUDE_TITLE, Boolean.FALSE);
            b.appendIf(Season.PROP_TITLE, season.getTitle(), alwaysIncludeTitle && season.isTitled());
        }
        else {
            b.appendIfNotNull(Season.PROP_TITLE, season.getTitle());
        }
    }
}
