package de.subcentral.fx.settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

public class ConfigurationPropertyHandlers
{
	public static ConvertingHandler<String>		STRING_HANDLER					= new ConvertingHandler<>(FxUtil.IDENTITY_STRING_CONVERTER);
	public static ConvertingHandler<Path>		PATH_HANDLER					= new ConvertingHandler<>(FxUtil.PATH_STRING_CONVERTER);
	public static ConvertingListHandler<Path>	PATH_LIST_HANDLER				= new ConvertingListHandler<>(FxUtil.PATH_STRING_CONVERTER);
	public static ConvertingListHandler<Tag>	TAG_LIST_HANDLER				= new ConvertingListHandler<>(SubCentralFxUtil.TAG_STRING_CONVERTER);
	public static StandardReleaseListHandler	STANDARD_RELEASE_LIST_HANDLER	= new StandardReleaseListHandler();

	public static class ConvertingHandler<T> implements ConfigurationPropertyHandler<T>
	{
		private final StringConverter<T> stringConverter;

		public ConvertingHandler(StringConverter<T> stringConverter)
		{
			this.stringConverter = Objects.requireNonNull(stringConverter, "stringConverter");
		}

		@Override
		public T get(ImmutableConfiguration cfg, String key)
		{
			return stringConverter.fromString(cfg.getString(key));
		}

		@Override
		public void add(Configuration cfg, String key, T value)
		{
			cfg.setProperty(key, stringConverter.toString(value));
		}
	}

	public static class ConvertingListHandler<E> implements ConfigurationPropertyHandler<ObservableList<E>>
	{
		private final StringConverter<E> stringConverter;

		public ConvertingListHandler(StringConverter<E> stringConverter)
		{
			this.stringConverter = Objects.requireNonNull(stringConverter, "stringConverter");
		}

		@Override
		public ObservableList<E> get(ImmutableConfiguration cfg, String key)
		{
			String[] strings = cfg.getStringArray(key);
			List<E> items = new ArrayList<>(strings.length);
			for (String s : strings)
			{
				items.add(stringConverter.fromString(s));
			}
			return FXCollections.observableList(items);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<E> value)
		{
			for (E item : value)
			{
				cfg.addProperty(key, stringConverter.toString(item));
			}
		}
	}

	public static class StandardReleaseListHandler implements ConfigurationPropertyHandler<ObservableList<StandardRelease>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<StandardRelease> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<StandardRelease> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			ArrayList<StandardRelease> list = new ArrayList<>();
			List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".standardRelease");
			for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
			{
				List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
				Group group = Group.from(rlsCfg.getString("[@group]"));
				Scope scope = Scope.valueOf(rlsCfg.getString("[@scope]"));
				list.add(new StandardRelease(tags, group, scope));
			}
			list.trimToSize();
			return FXCollections.observableList(list);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<StandardRelease> list)
		{
			for (int i = 0; i < list.size(); i++)
			{
				StandardRelease stdRls = list.get(i);
				cfg.addProperty(key + ".standardRelease(" + i + ")[@tags]", Tag.formatList(stdRls.getRelease().getTags()));
				cfg.addProperty(key + ".standardRelease(" + i + ")[@group]", Group.toStringNullSafe(stdRls.getRelease().getGroup()));
				cfg.addProperty(key + ".standardRelease(" + i + ")[@scope]", stdRls.getScope());
			}
		}
	}

}
