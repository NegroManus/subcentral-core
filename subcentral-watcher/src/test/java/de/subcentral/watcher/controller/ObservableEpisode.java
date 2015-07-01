package de.subcentral.watcher.controller;

import java.time.DateTimeException;
import java.time.LocalDate;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.naming.EpisodeNamer;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.util.TimeUtil;

public class ObservableEpisode extends ObservableNamedBeanWrapper<Episode>
{
    private final Property<String>    seriesName;
    private final Property<String>    seriesType;
    private final Property<Integer>   seasonNumber;
    private final Property<String>    seasonTitle;
    private final Property<Integer>   numberInSeason;
    private final Property<String>    title;
    private final Property<Integer>   numberInSeries;
    private final Property<LocalDate> date;

    public ObservableEpisode()
    {
	this(new Episode(), NamingDefaults.getDefaultNamingService());
    }

    public ObservableEpisode(Episode epi)
    {
	this(epi, NamingDefaults.getDefaultNamingService());
    }

    public ObservableEpisode(Episode epi, NamingService namingService)
    {
	super(epi, namingService);

	// init properties
	Series series = bean.getSeries();
	seriesName = new SimpleStringProperty(this, "seriesName", series != null ? series.getName() : null);
	seriesName.addListener((Observable observable) -> updateSeries());
	seriesType = new SimpleStringProperty(this, "seriesType", series != null ? series.getType() : null);
	seriesType.addListener((Observable observable) -> updateSeries());
	Season season = bean.getSeason();
	seasonNumber = new SimpleObjectProperty<Integer>(this, "seasonNumber", season != null ? season.getNumber() : null);
	seasonNumber.addListener((Observable observable) -> updateSeason());
	seasonTitle = new SimpleStringProperty(this, "seasonTitle", season != null ? season.getTitle() : null);
	seasonTitle.addListener((Observable observable) -> updateSeason());
	numberInSeason = new SimpleObjectProperty<Integer>(this, "numberInSeason", bean.getNumberInSeason());
	numberInSeason.addListener((Observable observable) -> bean.setNumberInSeason(getNumberInSeason()));
	title = new SimpleStringProperty(this, "title", bean.getTitle());
	title.addListener((Observable observable) -> bean.setTitle(getTitle()));
	numberInSeries = new SimpleObjectProperty<Integer>(this, "numberInSeries", bean.getNumberInSeries());
	numberInSeries.addListener((Observable observable) -> bean.setNumberInSeries(getNumberInSeries()));
	LocalDate dateValue = null;
	if (bean.getDate() != null)
	{
	    try
	    {
		dateValue = LocalDate.from(bean.getDate());
	    }
	    catch (DateTimeException e)
	    {
		// don't set date if exception
		e.printStackTrace();
	    }
	}
	date = new SimpleObjectProperty<LocalDate>(this, "date", dateValue);
	date.addListener((Observable observable) -> bean.setDate(getDate()));

	// bind properties
	super.bind(seriesName, seriesType, seasonNumber, seasonTitle, numberInSeason, title, numberInSeries, date);
    }

    private void updateSeries()
    {
	// set series
	if (getSeriesName() != null || getSeriesType() != null)
	{
	    Series series = bean.getSeries();
	    if (series == null)
	    {
		series = new Series();
		bean.setSeries(series);
	    }
	    series.setName(getSeriesName());
	    series.setType(getSeriesType());
	    if (bean.isPartOfSeason())
	    {
		bean.getSeason().setSeries(series);
	    }
	}
	else
	{
	    bean.setSeries(null);
	    if (bean.isPartOfSeason())
	    {
		bean.getSeason().setSeries(null);
	    }
	}
    }

    private void updateSeason()
    {
	if (getSeasonNumber() != null || getSeasonTitle() != null)
	{
	    Season season = bean.getSeason();
	    if (season == null)
	    {
		season = new Season(bean.getSeries());
		bean.setSeason(season);
	    }
	    season.setNumber(getSeasonNumber());
	    season.setTitle(getSeasonTitle());
	}
	else
	{
	    bean.setSeason(null);
	}
    }

    public final Property<String> seriesNameProperty()
    {
	return this.seriesName;
    }

    public final String getSeriesName()
    {
	return this.seriesNameProperty().getValue();
    }

    public final void setSeriesName(final String seriesName)
    {
	this.seriesNameProperty().setValue(seriesName);
    }

    public final Property<String> seriesTypeProperty()
    {
	return this.seriesType;
    }

    public final String getSeriesType()
    {
	return this.seriesTypeProperty().getValue();
    }

    public final void setSeriesType(final String seriesType)
    {
	this.seriesTypeProperty().setValue(seriesType);
    }

    public final Property<Integer> seasonNumberProperty()
    {
	return this.seasonNumber;
    }

    public final Integer getSeasonNumber()
    {
	return this.seasonNumberProperty().getValue();
    }

    public final void setSeasonNumber(final Integer seasonNumber)
    {
	this.seasonNumberProperty().setValue(seasonNumber);
    }

    public final Property<String> seasonTitleProperty()
    {
	return this.seasonTitle;
    }

    public final String getSeasonTitle()
    {
	return this.seasonTitleProperty().getValue();
    }

    public final void setSeasonTitle(final String seasonTitle)
    {
	this.seasonTitleProperty().setValue(seasonTitle);
    }

    public final Property<Integer> numberInSeasonProperty()
    {
	return this.numberInSeason;
    }

    public final Integer getNumberInSeason()
    {
	return this.numberInSeasonProperty().getValue();
    }

    public final void setNumberInSeason(final Integer numberInSeason)
    {
	this.numberInSeasonProperty().setValue(numberInSeason);
    }

    public final Property<String> titleProperty()
    {
	return this.title;
    }

    public final String getTitle()
    {
	return this.titleProperty().getValue();
    }

    public final void setTitle(final String title)
    {
	this.titleProperty().setValue(title);
    }

    public final Property<Integer> numberInSeriesProperty()
    {
	return this.numberInSeries;
    }

    public final Integer getNumberInSeries()
    {
	return this.numberInSeriesProperty().getValue();
    }

    public final void setNumberInSeries(final Integer numberInSeries)
    {
	this.numberInSeriesProperty().setValue(numberInSeries);
    }

    public final Property<LocalDate> dateProperty()
    {
	return this.date;
    }

    public final LocalDate getDate()
    {
	return this.dateProperty().getValue();
    }

    public final void setDate(final LocalDate date)
    {
	this.dateProperty().setValue(date);
    }

    public static void main(String[] args)
    {
	ObservableEpisode epi = new ObservableEpisode(Episode.createDatedEpisode("Psych", LocalDate.of(2014, 1, 8)));
	long totalStart = System.nanoTime();
	long start = System.nanoTime();
	System.out.println(epi.getName());
	TimeUtil.printDurationMillis("compute name", start);

	start = System.nanoTime();
	epi.setSeriesName("Psych");
	epi.setSeasonNumber(8);
	epi.setNumberInSeason(1);
	TimeUtil.printDurationMillis("setting of 3 attributes", start);

	start = System.nanoTime();
	System.out.println(epi.getName());
	TimeUtil.printDurationMillis("compute name", start);

	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("get name", start);

	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("get name", start);

	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("get name", start);

	epi.setSeriesType(Series.TYPE_SEASONED);
	epi.setTitle("Lock, Stock");
	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("compute name", start);

	epi.getNamingParameters().put(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE);
	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("compute name", start);

	epi.setTitle(null);
	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("compute name", start);

	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("get name", start);

	epi.setSeriesName("How I Met Your Mother");
	epi.setSeasonNumber(5);
	epi.setNumberInSeason(24);
	epi.setTitle("Finale");

	start = System.nanoTime();
	System.out.println(epi.getComputedName());
	TimeUtil.printDurationMillis("compute name", start);

	TimeUtil.printDurationMillis("total", totalStart);
    }
}
