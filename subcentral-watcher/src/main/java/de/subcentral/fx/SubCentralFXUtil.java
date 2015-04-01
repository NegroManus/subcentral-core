package de.subcentral.fx;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.settings.CompatibilitySettingEntry;
import de.subcentral.watcher.settings.ReleaseTagsStandardizerSettingEntry;
import de.subcentral.watcher.settings.SeriesNameStandardizerSettingEntry;
import de.subcentral.watcher.settings.StandardizerSettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;

public class SubCentralFXUtil
{
	public static final String									DEFAULT_TAGS_PROMPT_TEXT			= "tags (separated by comma)";
	public static final String									DEFAULT_GROUP_PROMPT_TEXT			= "group";

	public static final StringConverter<List<Tag>>				TAGS_STRING_CONVERTER				= initTagsStringConverter();
	public static final StringConverter<ObservableList<Tag>>	OBSERVABLE_TAGS_STRING_CONVERTER	= initObservableTagsStringConverter();
	public static final StringConverter<Group>					GROUP_STRING_CONVERTER				= initGroupStringConverter();
	public static final StringConverter<DeletionMode>			DELETION_MODE_STRING_CONVERTER		= initDeletionModeStringConverter();
	public static final StringConverter<LanguageFormat>			LANGUAGE_FORMAT_STRING_CONVERTER	= initLanguageFormatStringConverter();

	private static StringConverter<List<Tag>> initTagsStringConverter()
	{
		return new StringConverter<List<Tag>>()
		{
			@Override
			public String toString(List<Tag> tags)
			{
				return Tag.listToString(tags);
			}

			@Override
			public List<Tag> fromString(String tagList)
			{
				return Tag.parseList(tagList);
			}
		};
	}

	private static StringConverter<ObservableList<Tag>> initObservableTagsStringConverter()
	{
		return new StringConverter<ObservableList<Tag>>()
		{
			@Override
			public String toString(ObservableList<Tag> tags)
			{
				return Tag.listToString(tags);
			}

			@Override
			public ObservableList<Tag> fromString(String tagList)
			{
				return FXCollections.observableList(Tag.parseList(tagList));
			}
		};
	}

	private static StringConverter<Group> initGroupStringConverter()
	{
		return new StringConverter<Group>()
		{
			@Override
			public String toString(Group group)
			{
				return Group.toSafeString(group);
			}

			@Override
			public Group fromString(String group)
			{
				return Group.parse(group);
			}
		};
	}

	private static StringConverter<DeletionMode> initDeletionModeStringConverter()
	{
		return new StringConverter<DeletionMode>()
		{
			@Override
			public String toString(DeletionMode mode)
			{
				if (mode == null)
				{
					return "";
				}
				switch (mode)
				{
					case KEEP:
						return "Keep files";
					case RECYCLE:
						return "Move files to Recycle Bin (Windows-only)";
					case DELETE:
						return "Delete files";
					default:
						return mode.toString();
				}
			}

			@Override
			public DeletionMode fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public static StringConverter<LanguageFormat> initLanguageFormatStringConverter()
	{
		return new StringConverter<LanguageFormat>()
		{
			@Override
			public String toString(LanguageFormat format)
			{
				if (format == null)
				{
					return "";
				}
				switch (format)
				{
					case NAME:
						return "Language tag (Java)";
					case LANGUAGE_TAG:
						return "Language tag (IETF)";
					case ISO2:
						return "2-letter language code (ISO 639-1)";
					case ISO3:
						return "3-letter language code (ISO 639-2/T)";
					case DISPLAY_NAME:
						return "Language name with country";
					case DISPLAY_LANGUAGE:
						return "Language name without country";
					default:
						return format.toString();
				}
			}

			@Override
			public LanguageFormat fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public static void bindWatchDirectories(final DirectoryWatchService service, final ObservableList<Path> directoryList) throws IOException
	{
		for (Path dir : WatcherSettings.INSTANCE.getWatchDirectories())
		{
			service.registerDirectory(dir, StandardWatchEventKinds.ENTRY_CREATE);
		}
		directoryList.addListener(new ListChangeListener<Path>()
		{
			@Override
			public void onChanged(Change<? extends Path> c)
			{
				while (c.next())
				{
					if (c.wasRemoved())
					{
						for (Path removedDir : c.getRemoved())
						{
							service.unregisterDirectory(removedDir);
						}
					}
					if (c.wasAdded())
					{
						for (Path addedDir : c.getAddedSubList())
						{
							try
							{
								service.registerDirectory(addedDir, StandardWatchEventKinds.ENTRY_CREATE);
							}
							catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
	}

	public static ListProperty<Tag> tagPropertyForTextField(TextField tagsTxtFld, List<Tag> initialValue)
	{
		ObservableList<Tag> initialTags;
		if (initialValue instanceof ObservableList)
		{
			initialTags = (ObservableList<Tag>) initialValue;
		}
		else
		{
			initialTags = FXCollections.observableArrayList(initialValue);
		}
		ListProperty<Tag> tags = new SimpleListProperty<>(initialTags);
		bindTagsToTextField(tagsTxtFld, tags);
		return tags;
	}

	public static void bindTagsToTextField(TextField tagsTxtFld, ListProperty<Tag> tags)
	{
		TextFormatter<ObservableList<Tag>> tagsFormatter = new TextFormatter<>(SubCentralFXUtil.OBSERVABLE_TAGS_STRING_CONVERTER);
		tagsFormatter.valueProperty().bindBidirectional(tags);
		tagsTxtFld.setTextFormatter(tagsFormatter);
	}

	public static Property<Group> groupPropertyForTextField(TextField groupTxtFld, Group initialValue)
	{
		TextFormatter<Group> groupFormatter = new TextFormatter<>(GROUP_STRING_CONVERTER, initialValue);
		groupTxtFld.setTextFormatter(groupFormatter);
		return groupFormatter.valueProperty();
	}

	public static void bindGroupToTextField(TextField groupTxtFld, Property<Group> group)
	{
		TextFormatter<Group> groupFormatter = new TextFormatter<>(GROUP_STRING_CONVERTER);
		groupFormatter.valueProperty().bindBidirectional(group);
		groupTxtFld.setTextFormatter(groupFormatter);
	}

	public static void bindStandardizers(final TypeStandardizingService service, final ObservableList<StandardizerSettingEntry<?, ?>> standardizerList)
	{
		final ChangeListener<Boolean> enabledListener = new StandardizingCfgEntryEnabledListener(service);

		// initially set the values
		for (StandardizerSettingEntry<?, ?> entry : standardizerList)
		{
			// add listener for enabled property
			entry.enabledProperty().addListener(enabledListener);
			registerStandardizer(service, entry);
		}

		// add listener to get notified about additions/removals
		standardizerList.addListener(new ListChangeListener<StandardizerSettingEntry<?, ?>>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends StandardizerSettingEntry<?, ?>> c)
			{
				while (c.next())
				{
					if (c.wasRemoved())
					{
						for (StandardizerSettingEntry<?, ?> entry : c.getRemoved())
						{
							// remove listener for enabled property
							entry.enabledProperty().removeListener(enabledListener);
							unregisterStandardizer(service, entry);
						}
					}
					if (c.wasAdded())
					{
						for (StandardizerSettingEntry<?, ?> entry : c.getAddedSubList())
						{
							// add listener for enabled property
							entry.enabledProperty().addListener(enabledListener);
							registerStandardizer(service, entry);
						}
					}
				}
			}
		});
	}

	private static class StandardizingCfgEntryEnabledListener implements ChangeListener<Boolean>
	{
		private final TypeStandardizingService	service;

		private StandardizingCfgEntryEnabledListener(TypeStandardizingService service)
		{
			this.service = service;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			BooleanProperty enabledProp = (BooleanProperty) observable;
			StandardizerSettingEntry<?, ?> entry = (StandardizerSettingEntry<?, ?>) enabledProp.getBean();
			if (newValue)
			{
				registerStandardizer(service, entry);
			}
			else
			{
				unregisterStandardizer(service, entry);
			}
		}
	}

	private static <T> void registerStandardizer(TypeStandardizingService service, StandardizerSettingEntry<T, ?> entry)
	{
		if (entry.isEnabled())
		{
			service.registerStandardizer(entry.getBeanType(), entry.getValue());
		}
	}

	private static <T> boolean unregisterStandardizer(TypeStandardizingService service, StandardizerSettingEntry<T, ?> entry)
	{
		return service.unregisterStandardizer(entry.getValue());
	}

	public static void bindCompatibilities(final CompatibilityService service, ObservableList<CompatibilitySettingEntry> compatibilityList)
	{
		final CompatibilityEntryEnabledListener enabledListener = new CompatibilityEntryEnabledListener(service);
		for (CompatibilitySettingEntry entry : compatibilityList)
		{
			// add listener for enabled property
			entry.enabledProperty().addListener(enabledListener);
			addCompatibility(service, entry);
		}
		// add listener to get notified about additions/removals
		compatibilityList.addListener(new ListChangeListener<CompatibilitySettingEntry>()
		{
			@Override
			public void onChanged(ListChangeListener.Change<? extends CompatibilitySettingEntry> c)
			{
				while (c.next())
				{
					if (c.wasRemoved())
					{
						for (CompatibilitySettingEntry entry : c.getRemoved())
						{
							// remove listener for enabled property
							entry.enabledProperty().removeListener(enabledListener);
							removeCompatibility(service, entry);
						}
					}
					if (c.wasAdded())
					{
						for (CompatibilitySettingEntry entry : c.getAddedSubList())
						{
							// add listener for enabled property
							entry.enabledProperty().addListener(enabledListener);
							addCompatibility(service, entry);
						}
					}
				}
			}
		});
	}

	private static class CompatibilityEntryEnabledListener implements ChangeListener<Boolean>
	{
		private final CompatibilityService	service;

		private CompatibilityEntryEnabledListener(CompatibilityService service)
		{
			this.service = service;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
			BooleanProperty enabledProp = (BooleanProperty) observable;
			CompatibilitySettingEntry entry = (CompatibilitySettingEntry) enabledProp.getBean();
			if (newValue)
			{
				addCompatibility(service, entry);
			}
			else
			{
				removeCompatibility(service, entry);
			}
		}
	}

	private static void addCompatibility(CompatibilityService service, CompatibilitySettingEntry entry)
	{
		if (entry.isEnabled())
		{
			service.getCompatibilities().add(entry.getValue());
		}
	}

	private static boolean removeCompatibility(CompatibilityService service, CompatibilitySettingEntry entry)
	{
		return service.getCompatibilities().remove(entry.getValue());
	}

	public static String standardizingRuleTypeToString(Class<? extends StandardizerSettingEntry<?, ?>> type)
	{
		if (type == null)
		{
			return "";
		}
		else if (type == SeriesNameStandardizerSettingEntry.class)
		{
			return SeriesNameStandardizerSettingEntry.getStandardizerTypeString();
		}
		else if (type == ReleaseTagsStandardizerSettingEntry.class)
		{
			return ReleaseTagsStandardizerSettingEntry.getStandardizerTypeString();
		}
		return type.getSimpleName();
	}

	public static String beanTypeToString(Class<?> beanClass)
	{
		if (SubtitleAdjustment.class == beanClass)
		{
			return "Subtitle";
		}
		return beanClass.getSimpleName();
	}

	private SubCentralFXUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
