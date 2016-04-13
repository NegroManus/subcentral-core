package de.subcentral.fx.settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.correct.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.StringConverter;

public class ConfigurationPropertyHandlers
{
	public static final ConfigurationPropertyHandler<String>							STRING_HANDLER					= new StringConverterHandler<>(FxUtil.IDENTITY_STRING_CONVERTER);
	public static final ConfigurationPropertyHandler<Path>								PATH_HANDLER					= new StringConverterHandler<>(FxUtil.PATH_STRING_CONVERTER);
	public static final ConfigurationPropertyHandler<ObservableList<Path>>				PATH_LIST_HANDLER				= new ListStringConverterHandler<>(FxUtil.PATH_STRING_CONVERTER);
	public static final ConfigurationPropertyHandler<ObservableList<Tag>>				TAG_LIST_HANDLER				= new ListStringConverterHandler<>(SubCentralFxUtil.TAG_STRING_CONVERTER);
	public static final ConfigurationPropertyHandler<ObservableList<StandardRelease>>	STANDARD_RELEASE_LIST_HANDLER	= new StandardReleaseListHandler();
	public static final ConfigurationPropertyHandler<Locale>							LOCALE_HANDLER					= new LocaleHandler();
	public static final ConfigurationPropertyHandler<ObservableList<Locale>>			LOCALE_LIST_HANDLER				= new LocaleListHandler();
	public static final ConfigurationPropertyHandler<LanguageFormat>					LANGUAGE_FORMAT_HANDLER			= new LanguageFormatHandler();
	public static final ConfigurationPropertyHandler<ObservableMap<String, Object>>		NAMING_PARAMETER_MAP_HANDLER	= new NamingParameterMapHandler();

	public static class StringConverterHandler<T> implements ConfigurationPropertyHandler<T>
	{
		private final StringConverter<T> stringConverter;

		public StringConverterHandler(StringConverter<T> stringConverter)
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

	public static class ListStringConverterHandler<E> implements ConfigurationPropertyHandler<ObservableList<E>>
	{
		private final StringConverter<E> stringConverter;

		public ListStringConverterHandler(StringConverter<E> stringConverter)
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

	private static class StandardReleaseListHandler implements ConfigurationPropertyHandler<ObservableList<StandardRelease>>
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

	private static class LocaleHandler implements ConfigurationPropertyHandler<Locale>
	{
		@Override
		public Locale get(ImmutableConfiguration cfg, String key)
		{
			return Locale.forLanguageTag(cfg.getString(key + "[@tag]"));
		}

		@Override
		public void add(Configuration cfg, String key, Locale value)
		{
			cfg.addProperty(key + "[@tag]", value.toLanguageTag());
		}
	}

	private static class LanguageFormatHandler implements ConfigurationPropertyHandler<LanguageFormat>
	{
		@Override
		public LanguageFormat get(ImmutableConfiguration cfg, String key)
		{
			return LanguageFormat.valueOf(cfg.getString(key));
		}

		@Override
		public void add(Configuration cfg, String key, LanguageFormat value)
		{
			cfg.addProperty(key, value.name());
		}
	}

	private static class LocaleListHandler implements ConfigurationPropertyHandler<ObservableList<Locale>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<Locale> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<Locale> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			List<HierarchicalConfiguration<ImmutableNode>> parsingLangsCfgs = cfg.configurationsAt(key + ".language");
			List<Locale> parsingLangs = new ArrayList<>(parsingLangsCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> parsingLangCfg : parsingLangsCfgs)
			{
				parsingLangs.add(Locale.forLanguageTag(parsingLangCfg.getString("[@tag]")));
			}
			return FXCollections.observableList(parsingLangs);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<Locale> list)
		{
			for (int i = 0; i < list.size(); i++)
			{
				Locale lang = list.get(i);
				cfg.addProperty(key + ".language(" + i + ")[@tag]", lang.toLanguageTag());
			}
		}
	}

	private static class NamingParameterMapHandler implements ConfigurationPropertyHandler<ObservableMap<String, Object>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableMap<String, Object> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableMap<String, Object> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			Map<String, Object> params = new LinkedHashMap<>(3);
			// read actual values
			List<HierarchicalConfiguration<ImmutableNode>> paramCfgs = cfg.configurationsAt(key + ".param");
			for (HierarchicalConfiguration<ImmutableNode> paramCfg : paramCfgs)
			{
				String paramKey = paramCfg.getString("[@key]");
				boolean paramValue = paramCfg.getBoolean("[@value]");
				params.put(paramKey, paramValue);
			}
			return FXCollections.observableMap(params);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableMap<String, Object> map)
		{
			int i = 0;
			for (Map.Entry<String, Object> param : map.entrySet())
			{
				cfg.addProperty(key + ".param(" + i + ")[@key]", param.getKey());
				cfg.addProperty(key + ".param(" + i + ")[@value]", param.getValue());
				i++;
			}
		}
	}
}
