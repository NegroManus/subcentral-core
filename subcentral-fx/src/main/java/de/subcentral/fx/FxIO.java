package de.subcentral.fx;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.TimeUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;

public class FxIO {
	private static final Logger	log								= LogManager.getLogger(FxIO.class);

	private static final String	DEFAULT_IMG_PATH				= "img/";
	private static final String	DEFAULT_FXML_PATH				= "fxml/";
	private static final String	DEFAULT_RESOURCE_BUNDLE_PATH	= "i18n/";

	private FxIO() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static <T> T loadView(String fxmlFilename, Object controller) throws IOException {
		return loadView(fxmlFilename, controller, null, null);
	}

	/**
	 * Load from fxml file and connect with ResourceBundle and controller.
	 * 
	 * @param fxmlFilename
	 * @param controller
	 * @param resourceBundleBaseName
	 * @param locale
	 * @return
	 * @throws IOException
	 */
	public static <T> T loadView(String fxmlFilename, Object controller, String resourceBundleBaseName, Locale locale) throws IOException {
		long start = System.nanoTime();
		log.debug("Loading view {} for controller {}", fxmlFilename, controller);
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(FxIO.class.getClassLoader().getResource(DEFAULT_FXML_PATH + fxmlFilename));
		loader.setController(controller);
		loader.setResources(resourceBundleBaseName == null ? null
				: ResourceBundle.getBundle(DEFAULT_RESOURCE_BUNDLE_PATH + resourceBundleBaseName, locale != null ? locale : Locale.getDefault(), FxIO.class.getClassLoader()));
		T view = loader.load();
		log.debug("Loaded view {} for controller {} in {} ms", fxmlFilename, controller, TimeUtil.durationMillis(start));
		return view;
	}

	public static Image loadImg(String img) {
		if (img == null) {
			return null;
		}
		String url = DEFAULT_IMG_PATH + img;
		try {
			return new Image(url);
		}
		catch (IllegalArgumentException e) {
			log.warn("Could not load img from url " + url);
			return null;
		}
	}

	public static java.awt.Image loadAwtImg(String img) throws IOException {
		if (img == null) {
			return null;
		}
		String url = DEFAULT_IMG_PATH + img;
		try {
			return ImageIO.read(FxIO.class.getClassLoader().getResource(url));
		}
		catch (IllegalArgumentException | IOException e) {
			log.warn("Could not load awt img from url " + url);
			return null;
		}
	}
}
