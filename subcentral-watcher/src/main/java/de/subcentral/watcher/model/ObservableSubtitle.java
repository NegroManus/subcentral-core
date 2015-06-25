package de.subcentral.watcher.model;

import java.util.HashSet;
import java.util.Set;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingService;

public class ObservableSubtitle extends ObservableNamableBeanWrapper<SubtitleAdjustment>
{
    private final SetProperty<ObservableRelease> matchingReleases;
    private final StringProperty		 language;
    private final Property<Group>		 group;
    private final StringProperty		 source;

    public ObservableSubtitle(SubtitleAdjustment bean, NamingService namingService)
    {
	super(bean, namingService);

	// init props
	matchingReleases = new SimpleSetProperty<>(this, "matchingReleases", convertToWrappedReleaseSet(bean.getMatchingReleases(), this.namingService));
	getDependencies().addAll(matchingReleases);
	matchingReleases.addListener(new SetChangeListener<ObservableRelease>()
	{
	    @Override
	    public void onChanged(SetChangeListener.Change<? extends ObservableRelease> change)
	    {
		if (change.wasAdded())
		{
		    getDependencies().add(change.getElementAdded());
		}
		else if (change.wasRemoved())
		{
		    getDependencies().remove(change.getElementRemoved());
		}
		bean.setMatchingReleases(convertToReleaseSet(change.getSet()));
	    }
	});

	Subtitle firstSub = bean.getFirstSubtitle();

	language = new SimpleStringProperty(this, "language", firstSub.getLanguage());
	language.addListener((Observable o) -> {
	    for (Subtitle sub : bean.getSubtitles())
	    {
		sub.setLanguage(getLanguage());
	    }
	});

	group = new SimpleObjectProperty<Group>(this, "group", firstSub.getGroup());
	group.addListener((Observable o) -> {
	    for (Subtitle sub : bean.getSubtitles())
	    {
		sub.setGroup(getGroup());
	    }
	});

	source = new SimpleStringProperty(this, "source", firstSub.getSource());
	source.addListener((Observable o) -> {
	    for (Subtitle sub : bean.getSubtitles())
	    {
		sub.setSource(getSource());
	    }
	});

	// bind props
	super.bind(matchingReleases, language, group, source);
    }

    private static ObservableSet<ObservableRelease> convertToWrappedReleaseSet(Set<Release> releases, NamingService namingService)
    {
	Set<ObservableRelease> rlsSet = new HashSet<>(releases.size());
	for (Release r : releases)
	{
	    rlsSet.add(new ObservableRelease(r, namingService));
	}
	return FXCollections.observableSet(rlsSet);
    }

    private static Set<Release> convertToReleaseSet(ObservableSet<? extends ObservableRelease> wrappedReleases)
    {
	Set<Release> rlsSet = new HashSet<>(wrappedReleases.size());
	for (ObservableRelease m : wrappedReleases)
	{
	    rlsSet.add(m.getBean());
	}
	return rlsSet;
    }

    public final SetProperty<ObservableRelease> matchingReleasesProperty()
    {
	return this.matchingReleases;
    }

    public final ObservableSet<de.subcentral.watcher.model.ObservableRelease> getMatchingReleases()
    {
	return this.matchingReleasesProperty().get();
    }

    public final StringProperty languageProperty()
    {
	return this.language;
    }

    public final java.lang.String getLanguage()
    {
	return this.languageProperty().get();
    }

    public final void setLanguage(final java.lang.String language)
    {
	this.languageProperty().set(language);
    }

    public final Group getGroup()
    {
	return this.groupProperty().getValue();
    }

    public final void setGroup(final Group group)
    {
	this.groupProperty().setValue(group);
    }

    public final StringProperty sourceProperty()
    {
	return this.source;
    }

    public final java.lang.String getSource()
    {
	return this.sourceProperty().get();
    }

    public final void setSource(final java.lang.String source)
    {
	this.sourceProperty().set(source);
    }

    public final Property<Group> groupProperty()
    {
	return this.group;
    }

}
