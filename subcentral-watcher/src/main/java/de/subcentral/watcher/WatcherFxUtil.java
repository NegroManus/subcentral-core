package de.subcentral.watcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CompatibilityRule;
import de.subcentral.core.metadata.release.CrossGroupCompatibilityRule;
import de.subcentral.core.metadata.release.Nuke;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.SameGroupCompatibilityRule;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.Tags;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.util.CollectionUtil;
import de.subcentral.fx.DirectoryWatchService;
import de.subcentral.fx.FxIO;
import de.subcentral.fx.action.FxActions;
import de.subcentral.watcher.controller.WatcherMainController;
import de.subcentral.watcher.controller.settings.SettingsController;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class WatcherFxUtil {
	private static final Logger log = LogManager.getLogger(WatcherFxUtil.class);

	public static void bindWatchDirectories(final DirectoryWatchService service, final ObservableList<Path> directoryList) throws IOException {
		for (Path dir : directoryList) {
			service.registerDirectory(dir, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
		}
		directoryList.addListener(new ListChangeListener<Path>() {
			@Override
			public void onChanged(Change<? extends Path> c) {
				while (c.next()) {
					if (c.wasRemoved()) {
						for (Path removedDir : c.getRemoved()) {
							service.unregisterDirectory(removedDir);
						}
					}
					if (c.wasAdded()) {
						for (Path addedDir : c.getAddedSubList()) {
							try {
								service.registerDirectory(addedDir, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
							}
							catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
	}

	public static String beanTypeToString(Class<?> beanClass) {
		if (SubtitleRelease.class == beanClass) {
			return "Subtitle";
		}
		return beanClass.getSimpleName();
	}

	public static Label createMatchLabel() {
		Label lbl = new Label("", new ImageView(FxIO.loadImg("checked_16.png")));
		lbl.setTooltip(new Tooltip("Matching release"));
		return lbl;
	}

	public static Hyperlink createFurtherInfoHyperlink(Release rls, Executor executor) {
		if (rls.getFurtherInfoLinks().isEmpty()) {
			return null;
		}
		try {
			String host = new URL(rls.getFurtherInfoLinks().get(0)).getHost().replace("www.", "");
			ImageView dbImg = new ImageView(FxIO.loadImg("database_16.png"));
			Hyperlink hl = new Hyperlink(host, dbImg);
			hl.setTooltip(new Tooltip("Show further info"));
			hl.setVisited(true);
			hl.setOnAction((ActionEvent evt) -> FxActions.browse(rls.getFurtherInfoLinks().get(0), executor));
			return hl;
		}
		catch (MalformedURLException e) {
			log.error("Could not create further info hyperlink", e);
			return null;
		}
	}

	public static void addFurtherInfoHyperlink(Pane pane, Release rls, Executor executor) {
		Hyperlink link = createFurtherInfoHyperlink(rls, executor);
		if (link != null) {
			pane.getChildren().add(link);
		}
	}

	public static List<Node> createNukedLabels(Release rls) {
		if (rls.isNuked()) {
			boolean nuked = false;
			boolean unnuked = false;
			StringJoiner nukeToolTipJoiner = new StringJoiner(", ", "Nuked: ", "");
			StringJoiner unnukeTooltipJoiner = new StringJoiner(", ", "Unnuked: ", "");
			for (Nuke nuke : rls.getNukes()) {
				if (nuke.isUnnuke()) {
					unnuked = true;
					unnukeTooltipJoiner.add(nuke.getReason());
				}
				else {
					nuked = true;
					nukeToolTipJoiner.add(nuke.getReason());
				}
			}
			Label nukedLbl = null;
			Label unnukedLbl = null;
			List<Node> nodes = new ArrayList<>(2);
			if (nuked) {
				ImageView nukedImg = new ImageView(FxIO.loadImg("nuked_16.png"));
				nukedLbl = new Label("", nukedImg);
				nukedLbl.setTooltip(new Tooltip(nukeToolTipJoiner.toString()));
				nodes.add(nukedLbl);
			}
			if (unnuked) {
				ImageView unnukedImg = new ImageView(FxIO.loadImg("nuked_16.png"));
				unnukedLbl = new Label("", unnukedImg);
				unnukedLbl.setTooltip(new Tooltip(unnukeTooltipJoiner.toString()));
				nodes.add(unnukedLbl);
			}
			return nodes;
		}
		return ImmutableList.of();
	}

	public static Label createMetaTaggedLabel(Release rls, List<Tag> metaTags) {
		List<Tag> containedMetaTags = CollectionUtil.getCommonElements(rls.getTags(), metaTags);
		if (!containedMetaTags.isEmpty()) {
			String metaTagsTxt = Tags.join(containedMetaTags);
			ImageView tagImg = new ImageView(FxIO.loadImg("tag_16.png"));
			Label metaTagsLbl = new Label(metaTagsTxt, tagImg);
			metaTagsLbl.setTooltip(new Tooltip("Tagged with meta tags: " + metaTagsTxt));
			return metaTagsLbl;
		}
		return null;
	}

	public static void addMetaTaggedLabel(Pane pane, Release rls, List<Tag> metaTags) {
		Label lbl = createMetaTaggedLabel(rls, metaTags);
		if (lbl != null) {
			pane.getChildren().add(lbl);
		}
	}

	public static Hyperlink createSettingsHyperlink(SettingsController settingsCtrl, String section, String text) {
		ImageView img = new ImageView(FxIO.loadImg("settings_16.png"));
		Hyperlink link = new Hyperlink(text, img);
		link.setTooltip(new Tooltip("Show settings"));
		link.setVisited(true);
		link.setOnAction((ActionEvent evt) -> {
			settingsCtrl.getParent().selectTab(WatcherMainController.SETTINGS_TAB_INDEX);
			settingsCtrl.selectSection(section);
		});
		return link;
	}

	public static Label createCompatibleLabel(Compatibility compatibility, Function<Release, String> releaseNamer) {
		ImageView compImg = new ImageView(FxIO.loadImg("couple_16.png"));
		Label compLbl = new Label(null, compImg);

		StringBuilder tooltip = new StringBuilder();
		tooltip.append("Compatible to ");
		tooltip.append(releaseNamer.apply(compatibility.getSource()));
		String rule = compatibilityRuleToString(compatibility.getRule());
		if (!rule.isEmpty()) {
			tooltip.append(" (");
			tooltip.append(rule);
			tooltip.append(')');
		}

		compLbl.setTooltip(new Tooltip(tooltip.toString()));

		return compLbl;
	}

	private static String compatibilityRuleToString(CompatibilityRule rule) {
		if (rule == null) {
			return "";
		}
		if (rule instanceof SameGroupCompatibilityRule) {
			return "Same group";
		}
		if (rule instanceof CrossGroupCompatibilityRule) {
			return ((CrossGroupCompatibilityRule) rule).toShortString();
		}
		return rule.toString();
	}

	public static Label createGuessedLabel(StandardRelease stdRls, Function<Release, String> releaseNamer) {
		StringBuilder sb = new StringBuilder();
		sb.append("Guessed release");
		if (stdRls != null) {
			sb.append(" using standard release: ");
			sb.append(releaseNamer.apply(stdRls.getRelease()));
		}
		ImageView guessedImg = new ImageView(FxIO.loadImg("idea_16.png"));
		Label guessedLbl = new Label("", guessedImg);
		guessedLbl.setTooltip(new Tooltip(sb.toString()));
		return guessedLbl;
	}

	public static Label createManualLabel() {
		ImageView errorImg = new ImageView(FxIO.loadImg("hand_select_16.png"));
		Label excLbl = new Label("", errorImg);
		excLbl.setTooltip(new Tooltip("Added manually"));
		return excLbl;
	}

	public static Label createExceptionLabel(Throwable exception) {
		ImageView errorImg = new ImageView(FxIO.loadImg("error_16.png"));
		Label excLbl = new Label(exception.toString(), errorImg);
		return excLbl;
	}

	private WatcherFxUtil() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}
