package de.subcentral.fx;

import java.util.List;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public class SubCentralFxUtil
{
    public static final String DEFAULT_TAGS_PROMPT_TEXT	 = "tags (separated by comma)";
    public static final String DEFAULT_GROUP_PROMPT_TEXT = "group";

    public static final StringConverter<List<Tag>>	     TAGS_STRING_CONVERTER	       = initTagsStringConverter();
    public static final StringConverter<ObservableList<Tag>> OBSERVABLE_TAGS_STRING_CONVERTER  = initObservableTagsStringConverter();
    public static final StringConverter<Group>		     GROUP_STRING_CONVERTER	       = initGroupStringConverter();
    public static final StringConverter<DeletionMode>	     DELETION_MODE_STRING_CONVERTER    = initDeletionModeStringConverter();
    public static final StringConverter<LanguageFormat>	     LANGUAGE_FORMAT_STRING_CONVERTER  = initLanguageFormatStringConverter();
    public static final StringConverter<StandardRelease>     STANDARD_RELEASE_STRING_CONVERTER = initStandardReleaseStringConverter();

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

    public static StringConverter<StandardRelease> initStandardReleaseStringConverter()
    {
	return new StringConverter<StandardRelease>()
	{

	    @Override
	    public String toString(StandardRelease rls)
	    {
		return NamingDefaults.getDefaultReleaseNamer().name(rls.getRelease()) + " (" + rls.getAssumeExistence() + ")";
	    }

	    @Override
	    public StandardRelease fromString(String string)
	    {
		throw new UnsupportedOperationException();
	    }
	};
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
	TextFormatter<ObservableList<Tag>> tagsFormatter = new TextFormatter<>(SubCentralFxUtil.OBSERVABLE_TAGS_STRING_CONVERTER);
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

    private SubCentralFxUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
