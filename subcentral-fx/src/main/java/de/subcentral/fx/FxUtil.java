package de.subcentral.fx;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import de.subcentral.core.util.ExceptionUtil;
import de.subcentral.core.util.ObjectUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class FxUtil {
	public static final StringConverter<String>					IDENTITY_STRING_CONVERTER			= initIdentityStringConverter();
	public static final StringConverter<String>					REJECT_BLANK_STRING_CONVERTER		= initRejectBlankStringConverter();
	public static final StringConverter<Path>					PATH_STRING_CONVERTER				= initPathStringConverter();
	public static final StringConverter<URL>					URL_STRING_CONVERTER				= initUrlStringConverter();
	public static final StringConverter<Locale>					LOCALE_DISPLAY_NAME_CONVERTER		= initLocaleDisplayNameConverter();
	public static final StringConverter<ObservableList<Locale>>	LOCALE_LIST_DISPLAY_NAME_CONVERTER	= initLocaleListDisplayNameConverter();
	public static final Comparator<Locale>						LOCALE_DISPLAY_NAME_COMPARATOR		= initLocaleDisplayNameComparator();

	private FxUtil() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	private static StringConverter<String> initIdentityStringConverter() {
		return new StringConverter<String>() {
			@Override
			public String toString(String s) {
				return s;
			}

			@Override
			public String fromString(String s) {
				return s;
			}
		};
	}

	private static StringConverter<String> initRejectBlankStringConverter() {
		return new StringConverter<String>() {
			@Override
			public String toString(String s) {
				if (s == null) {
					return "";
				}
				return s;
			}

			@Override
			public String fromString(String s) {
				return Validate.notBlank(s);
			}
		};
	}

	private static StringConverter<Path> initPathStringConverter() {
		return new StringConverter<Path>() {
			@Override
			public String toString(Path path) {
				if (path == null) {
					return "";
				}
				return path.toString();
			}

			@Override
			public Path fromString(String string) {
				if (StringUtils.isBlank(string)) {
					return null;
				}
				return Paths.get(string);
			}
		};
	}

	private static StringConverter<URL> initUrlStringConverter() {
		return new StringConverter<URL>() {
			@Override
			public String toString(URL url) {
				if (url == null) {
					return "";
				}
				return url.toString();
			}

			@Override
			public URL fromString(String string) {
				if (StringUtils.isBlank(string)) {
					return null;
				}
				try {
					return new URL(string);
				}
				catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}
		};
	}

	private static StringConverter<Locale> initLocaleDisplayNameConverter() {
		return new StringConverter<Locale>() {
			@Override
			public String toString(Locale locale) {
				if (locale == null) {
					return "";
				}
				return locale.getDisplayName();
			}

			@Override
			public Locale fromString(String string) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static StringConverter<ObservableList<Locale>> initLocaleListDisplayNameConverter() {
		return new StringConverter<ObservableList<Locale>>() {
			@Override
			public String toString(ObservableList<Locale> locales) {
				if (locales == null) {
					return "";
				}
				StringJoiner joiner = new StringJoiner(", ");
				for (Locale l : locales) {
					if (l == null) {
						continue;
					}
					joiner.add(l.getDisplayName());
				}
				return joiner.toString();
			}

			@Override
			public ObservableList<Locale> fromString(String string) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static Comparator<Locale> initLocaleDisplayNameComparator() {
		return new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				// nulls first
				if (o1 == null) {
					return o2 == null ? 0 : -1;
				}
				if (o2 == null) {
					return 1;
				}
				return ObjectUtil.getDefaultStringOrdering().compare(o1.getDisplayName(), o2.getDisplayName());
			}
		};
	}

	public static void requireFxApplicationThread() throws IllegalStateException {
		if (!Platform.isFxApplicationThread()) {
			throw new IllegalStateException("Not on JavaFX Application Thread. Current thread: " + Thread.currentThread());
		}
	}

	public static boolean isFxLauncherThread() {
		return Thread.currentThread().getName().equals("JavaFX-Launcher");
	}

	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread and waits for completion.
	 *
	 * @param action
	 *            the {@link Runnable} to run
	 * @throws NullPointerException
	 *             if {@code action} is {@code null}
	 */
	public static void runAndWait(Runnable action) {
		if (action == null) {
			throw new NullPointerException("action");
		}

		// run synchronously on JavaFX thread
		if (Platform.isFxApplicationThread()) {
			action.run();
			return;
		}

		// queue on JavaFX thread and wait for completion
		final CountDownLatch doneLatch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				action.run();
			}
			finally {
				doneLatch.countDown();
			}
		});

		try {
			doneLatch.await();
		}
		catch (InterruptedException e) {
			// ignore exception
		}
	}

	public static Alert createExceptionAlert(Window owner, String title, String headerText, Throwable exception) {
		Alert alert = new Alert(AlertType.ERROR);
		FxNodes.fixAlertHeight(alert);
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(exception.toString());

		// Create expandable Exception.
		String exceptionText = ExceptionUtil.stackTraceToString(exception);

		Label label = new Label("Exception stacktrace:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		return alert;
	}

	public static ObservableList<Locale> createListOfAvailableLocales(boolean includeEmptyLocale, boolean includeVariants, Comparator<Locale> initialSortOrder) {
		Locale[] allLocales = Locale.getAvailableLocales(); // ca. 160 (without variants 45)
		int estimatedSize = includeVariants ? allLocales.length : (int) (allLocales.length / 3.5f);
		ArrayList<Locale> filteredLocales = new ArrayList<>(estimatedSize);
		for (Locale l : allLocales) {
			if ((includeEmptyLocale || !l.equals(Locale.ROOT)) && (includeVariants || l.getLanguage().equals(l.toString()))) {
				filteredLocales.add(l);
			}
		}
		filteredLocales.trimToSize();
		if (initialSortOrder != null) {
			filteredLocales.sort(initialSortOrder);
		}
		return FXCollections.observableList(filteredLocales);
	}
}
