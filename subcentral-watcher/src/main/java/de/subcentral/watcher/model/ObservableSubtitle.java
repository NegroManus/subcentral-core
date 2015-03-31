package de.subcentral.watcher.model;

import java.util.HashSet;
import java.util.Set;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingService;

public class ObservableSubtitle extends ObservableNamableBeanWrapper<SubtitleAdjustment>
{

	private final SetProperty<ObservableRelease>	matchingReleases;
	private final StringProperty					language;
	private final BooleanProperty					hearingImpaired;
	private final ListProperty<Tag>					tags;
	private final StringProperty					version;
	private final Property<Group>					group;
	private final StringProperty					source;

	public ObservableSubtitle(SubtitleAdjustment bean, NamingService namingService)
	{
		super(bean, namingService);

		// init props
		matchingReleases = new SimpleSetProperty<>(this, "matchingReleases", convertToWrappedReleaseSet(bean.getMatchingReleases(),
				this.namingService));
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

		hearingImpaired = new SimpleBooleanProperty(this, "hearingImpaired", firstSub.isHearingImpaired());
		hearingImpaired.addListener((Observable o) -> {
			for (Subtitle sub : bean.getSubtitles())
			{
				sub.setHearingImpaired(isHearingImpaired());
			}
		});

		tags = new SimpleListProperty<Tag>(this, "tags", FXCollections.observableArrayList(firstSub.getTags()));
		tags.addListener((Observable o) -> {
			for (Subtitle sub : bean.getSubtitles())
			{
				sub.setTags(getTags());
			}
		});

		version = new SimpleStringProperty(this, "version", firstSub.getVersion());
		version.addListener((Observable o) -> {
			for (Subtitle sub : bean.getSubtitles())
			{
				sub.setVersion(getVersion());
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
		super.bind(matchingReleases, language, hearingImpaired, tags, version, group, source);
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

	public final BooleanProperty hearingImpairedProperty()
	{
		return this.hearingImpaired;
	}

	public final boolean isHearingImpaired()
	{
		return this.hearingImpairedProperty().get();
	}

	public final void setHearingImpaired(final boolean hearingImpaired)
	{
		this.hearingImpairedProperty().set(hearingImpaired);
	}

	public final ListProperty<Tag> tagsProperty()
	{
		return this.tags;
	}

	public final ObservableList<de.subcentral.core.metadata.release.Tag> getTags()
	{
		return this.tagsProperty().get();
	}

	public final StringProperty versionProperty()
	{
		return this.version;
	}

	public final java.lang.String getVersion()
	{
		return this.versionProperty().get();
	}

	public final void setVersion(final java.lang.String version)
	{
		this.versionProperty().set(version);
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
