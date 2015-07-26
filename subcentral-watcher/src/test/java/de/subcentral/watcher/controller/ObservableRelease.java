package de.subcentral.watcher.controller;

import java.util.ArrayList;
import java.util.List;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Nuke;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ObservableRelease extends ObservableNamableBeanWrapper<Release>
{
	private final ListProperty<ObservableBeanWrapper<? extends Media>>	media;
	private final ListProperty<Tag>										tags;
	private final ObjectProperty<Group>									group;
	private final StringProperty										nfo;
	private final StringProperty										nfoLink;
	private final ListProperty<Nuke>									nukes;

	public ObservableRelease()
	{
		this(new Release(), NamingDefaults.getDefaultNamingService());
	}

	public ObservableRelease(Release rls)
	{
		this(rls, NamingDefaults.getDefaultNamingService());
	}

	public ObservableRelease(Release rls, NamingService namingService)
	{
		super(rls, namingService);

		// init properties
		setActualName(bean.getName());
		actualNameProperty().addListener((Observable o) -> bean.setName(getActualName()));
		media = new SimpleListProperty<ObservableBeanWrapper<? extends Media>>(this, "tags", convertToWrappedMediaList(bean.getMedia(), this.namingService));
		getDependencies().addAll(media);
		media.addListener(new ListChangeListener<ObservableBeanWrapper<? extends Media>>()
		{
			@Override
			public void onChanged(ListChangeListener.Change<? extends ObservableBeanWrapper<? extends Media>> c)
			{
				// add/remove dependencies to the added/removed media elements
				while (c.next())
				{
					if (c.wasAdded())
					{
						getDependencies().addAll(c.getAddedSubList());
					}
					else if (c.wasRemoved())
					{
						getDependencies().removeAll(c.getRemoved());
					}
				}
				// reset the media list of the bean
				bean.setMedia(convertToMediaList(c.getList()));
			}
		});
		tags = new SimpleListProperty<Tag>(this, "tags", FXCollections.observableArrayList(bean.getTags()));
		tags.addListener((Observable o) -> bean.setTags(getTags()));
		group = new SimpleObjectProperty<Group>(this, "group", bean.getGroup());
		group.addListener((Observable o) -> bean.setGroup(getGroup()));
		nfo = new SimpleStringProperty(this, "nfo", bean.getNfo());
		nfo.addListener((Observable o) -> bean.setNfo(getNfo()));
		nfoLink = new SimpleStringProperty(this, "nfoLink", bean.getNfoLink());
		nfoLink.addListener((Observable o) -> bean.setNfoLink(getNfoLink()));
		nukes = new SimpleListProperty<Nuke>(this, "tags", FXCollections.observableArrayList(bean.getNukes()));
		nukes.addListener((Observable o) -> bean.setNukes(getNukes()));

		// register properties
		super.bind(media, tags, group, nfo, nfoLink, nukes);
	}

	private static ObservableList<ObservableBeanWrapper<? extends Media>> convertToWrappedMediaList(List<Media> media, NamingService namingService)
	{
		List<ObservableBeanWrapper<? extends Media>> mediaList = new ArrayList<>(media.size());
		for (Media m : media)
		{
			if (m instanceof Episode)
			{
				mediaList.add(new ObservableEpisode((Episode) m, namingService));
			}
			else
			{
				throw new AssertionError("Unsupported media" + media);
			}
		}
		return FXCollections.observableArrayList(mediaList);
	}

	private static List<Media> convertToMediaList(ObservableList<? extends ObservableBeanWrapper<? extends Media>> wrappedMedia)
	{
		List<Media> mediaList = new ArrayList<>(wrappedMedia.size());
		for (ObservableBeanWrapper<? extends Media> m : wrappedMedia)
		{
			mediaList.add(m.getBean());
		}
		return mediaList;
	}

	public ListProperty<ObservableBeanWrapper<? extends Media>> getMedia()
	{
		return media;
	}

	public final ListProperty<Tag> tagsProperty()
	{
		return this.tags;
	}

	public final ObservableList<Tag> getTags()
	{
		return this.tags.get();
	}

	public final ObjectProperty<Group> groupProperty()
	{
		return this.group;
	}

	public final Group getGroup()
	{
		return this.group.get();
	}

	public final void setGroup(final Group group)
	{
		this.group.set(group);
	}

	public final StringProperty nfoProperty()
	{
		return this.nfo;
	}

	public final java.lang.String getNfo()
	{
		return this.nfoProperty().get();
	}

	public final void setNfo(final java.lang.String nfo)
	{
		this.nfoProperty().set(nfo);
	}

	public final StringProperty nfoLinkProperty()
	{
		return this.nfoLink;
	}

	public final java.lang.String getNfoLink()
	{
		return this.nfoLinkProperty().get();
	}

	public final void setNfoLink(final java.lang.String nfoLink)
	{
		this.nfoLinkProperty().set(nfoLink);
	}

	public final ListProperty<Nuke> nukesProperty()
	{
		return this.nukes;
	}

	public final javafx.collections.ObservableList<de.subcentral.core.metadata.release.Nuke> getNukes()
	{
		return this.nukesProperty().get();
	}

	public final void setNukes(final javafx.collections.ObservableList<de.subcentral.core.metadata.release.Nuke> nukes)
	{
		this.nukesProperty().set(nukes);
	}

	public static void main(String[] args)
	{
		Episode epi = Episode.createSeasonedEpisode("Psych", 8, 1);
		ObservableRelease rls = new ObservableRelease(Release.create("Psych.8x01.HDTV.x264-LOL", epi, "LOL", "HDTV", "x264"));

		System.out.println(rls.getName());
		System.out.println(rls.getComputedName());
		System.out.println(rls.getActualName());

		rls.setPreferActualName(true);
		System.out.println(rls.getName());
		rls.setActualName(null);
		System.out.println(rls.getName());
		rls.setPreferActualName(false);
		rls.getMedia().add(new ObservableEpisode(epi.getSeason().newEpisode(2)));
		rls.getMedia().add(new ObservableEpisode(epi.getSeason().newEpisode(3)));
		rls.setGroup(new Group("DIMENSION"));
		rls.getTags().add(0, new Tag("720p"));
		System.out.println(rls.getName());
	}
}
