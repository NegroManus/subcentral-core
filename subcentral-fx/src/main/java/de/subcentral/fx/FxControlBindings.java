package de.subcentral.fx;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.fx.UserPattern.Mode;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class FxControlBindings
{
	private static final Logger log = LogManager.getLogger(FxControlBindings.class);

	private FxControlBindings()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static <T> TextFormatter<T> bindToTextField(TextField txtFld, StringConverter<T> converter)
	{
		TextFormatter<T> formatter = new TextFormatter<T>(converter);
		txtFld.setTextFormatter(formatter);
		return formatter;
	}

	public static <T> TextFormatter<T> bindTextFieldToProperty(TextField txtFld, Property<T> prop, StringConverter<T> converter)
	{
		TextFormatter<T> formatter = new TextFormatter<T>(converter);
		formatter.valueProperty().bindBidirectional(prop);
		txtFld.setTextFormatter(formatter);
		return formatter;
	}

	public static TextFormatter<Path> bindTextFieldToPath(TextField pathTxtFld)
	{
		return bindToTextField(pathTxtFld, FxUtil.PATH_STRING_CONVERTER);
	}

	public static TextFormatter<Path> bindTextFieldToPath(TextField pathTxtFld, Property<Path> path)
	{
		return bindTextFieldToProperty(pathTxtFld, path, FxUtil.PATH_STRING_CONVERTER);
	}

	public static TextFormatter<URL> bindTextFieldToUrl(TextField pathTxtFld)
	{
		return bindToTextField(pathTxtFld, FxUtil.URL_STRING_CONVERTER);
	}

	public static Hyperlink createParentDirectoryHyperlink(Path file, ExecutorService executor)
	{
		return createParentDirectoryHyperlink(file, executor, FxActions.DEFAULT_TASK_FAILED_HANDLER);
	}

	public static Hyperlink createParentDirectoryHyperlink(Path file, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler)
	{
		Hyperlink link = new Hyperlink();
		link.setVisited(true);
		link.setText(file.toString());
		link.setTooltip(new Tooltip("Show " + file));
		String uri = file.getParent().toUri().toString();
		link.setOnAction((ActionEvent evt) -> FxActions.browse(uri, executor, onFailedHandler));
		return link;
	}

	public static Hyperlink createFileHyperlink(Path file, ExecutorService executor)
	{
		return createFileHyperlink(file, executor, FxActions.DEFAULT_TASK_FAILED_HANDLER);
	}

	public static Hyperlink createFileHyperlink(Path file, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler)
	{
		Hyperlink link = new Hyperlink();
		link.setVisited(true);
		link.setText(file.toString());
		link.setTooltip(new Tooltip("Open " + file));
		String uri = file.toUri().toString();
		link.setOnAction((ActionEvent evt) -> FxActions.browse(uri, executor, onFailedHandler));
		return link;
	}

	public static Hyperlink createUrlHyperlink(URL url, ExecutorService executor) throws URISyntaxException
	{
		return createUrlHyperlink(url, executor, FxActions.DEFAULT_TASK_FAILED_HANDLER);
	}

	public static Hyperlink createUrlHyperlink(URL url, ExecutorService executor, EventHandler<WorkerStateEvent> onFailedHandler) throws URISyntaxException
	{
		String uri = url.toURI().toString();
		Hyperlink link = new Hyperlink();
		link.setVisited(true);
		link.setText(url.toString());
		link.setTooltip(new Tooltip("Open " + url));
		link.setOnAction((ActionEvent evt) -> FxActions.browse(uri, executor, onFailedHandler));
		return link;
	}

	public static Binding<UserPattern> createUiPatternTextFieldBinding(ToggleGroup patternModeToggleGrp,
			Toggle literalToggle,
			Toggle simpleToggle,
			Toggle regexToggle,
			TextField patternTxtFld,
			Text patternErrorTxt)
	{
		return new ObjectBinding<UserPattern>()
		{
			{
				super.bind(patternModeToggleGrp.selectedToggleProperty(), patternTxtFld.textProperty());
			}

			@Override
			protected UserPattern computeValue()
			{
				String pattern = patternTxtFld.getText();
				if (StringUtils.isBlank(pattern))
				{
					patternErrorTxt.setText("");
					return null;
				}
				Mode mode;
				if (patternModeToggleGrp.getSelectedToggle() == simpleToggle)
				{
					mode = Mode.SIMPLE;
				}
				else if (patternModeToggleGrp.getSelectedToggle() == regexToggle)
				{
					mode = Mode.REGEX;
				}
				else
				{
					mode = Mode.LITERAL;
				}
				try
				{
					UserPattern p = new UserPattern(pattern, mode);
					// try to convert -> may throw an exception
					p.toPattern();
					patternErrorTxt.setText("");
					return p;
				}
				catch (Exception e)
				{
					log.debug("Could not compile UserPattern due to invalid pattern string. Exception: " + e);
					// Only use the first line
					String errorMsg = e.getMessage().split("(\\r)?\\n", 2)[0];
					patternErrorTxt.setText("Invalid pattern: " + errorMsg);
					return null;
				}
			}
		};
	}

	public static <E extends Enum<E>> ToggleEnumBinding<E> bindToggleGroupToEnumProp(ToggleGroup toggleGroup, Property<E> enumProp, Map<Toggle, E> mapping)
	{
		return new ToggleEnumBinding<>(toggleGroup, enumProp, mapping);
	}

	public static class ToggleEnumBinding<E extends Enum<E>>
	{
		private final ToggleGroup				toggleGroup;
		private final Property<E>				enumProp;
		private final Map<Toggle, E>			mapping;
		private final ChangeListener<Toggle>	toggleListener;
		private final ChangeListener<E>			enumListener;
		private boolean							updating;

		private ToggleEnumBinding(ToggleGroup toggleGroup, Property<E> enumProp, Map<Toggle, E> mapping)
		{
			this.toggleGroup = Objects.requireNonNull(toggleGroup, "toggleGroup");
			this.enumProp = Objects.requireNonNull(enumProp, "enumProp");
			this.mapping = Objects.requireNonNull(mapping, "mapping");

			// set initial value
			selectToggle(this.enumProp.getValue());

			// init listeners and add them
			this.toggleListener = initToggleListener();
			this.enumListener = initEnumListener();
			this.toggleGroup.selectedToggleProperty().addListener(toggleListener);
			this.enumProp.addListener(enumListener);
		}

		public void unbind()
		{
			toggleGroup.selectedToggleProperty().removeListener(toggleListener);
			enumProp.removeListener(enumListener);
		}

		private ChangeListener<Toggle> initToggleListener()
		{
			return (ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) ->
			{
				if (!updating)
				{
					try
					{
						updating = true;
						enumProp.setValue(mapping.get(newValue));
					}
					finally
					{
						updating = false;
					}
				}
			};
		}

		private ChangeListener<E> initEnumListener()
		{
			return (ObservableValue<? extends E> observable, E oldValue, E newValue) ->
			{
				if (!updating)
				{
					try
					{
						updating = true;
						selectToggle(newValue);
					}
					finally
					{
						updating = false;
					}
				}
			};
		}

		private void selectToggle(E enumValue)
		{
			for (Map.Entry<Toggle, E> entry : mapping.entrySet())
			{
				if (enumValue == entry.getValue())
				{
					toggleGroup.selectToggle(entry.getKey());
					return;
				}
			}
			// if non found, select default (which is the first)
			if (!mapping.keySet().isEmpty())
			{
				toggleGroup.selectToggle(mapping.keySet().iterator().next());
			}
		}
	}
}
