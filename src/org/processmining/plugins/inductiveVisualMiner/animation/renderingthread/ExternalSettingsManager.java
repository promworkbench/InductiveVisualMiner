package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.geom.AffineTransform;
import java.util.Random;

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

/**
 * Keeps track of the external settings of the animation. Thread-safe.
 * 
 * @author sleemans
 *
 */
public class ExternalSettingsManager {
	ExternalSettings settings = null;
	private Random random = new Random();

	public class ExternalSettings {
		//resizing variables
		int width;
		int height;

		//rendering variables
		IvMLogFilter filteredLog;
		GraphVizTokens tokens;
		AffineTransform transform;

		//traceability
		int id;
	}

	public ExternalSettingsManager() {
		ExternalSettings newExternalSettings = new ExternalSettings();
		newExternalSettings.width = 0;
		newExternalSettings.height = 0;
		newExternalSettings.tokens = null;
		newExternalSettings.filteredLog = null;
		newExternalSettings.transform = null;
		newExternalSettings.id = random.nextInt();

		settings = newExternalSettings;
	}

	public ExternalSettings getExternalSettings() {
		return settings;
	}

	public boolean isMostRecentSetting(int settingsId) {
		return settingsId == settings.id;
	}

	public synchronized int setImageTransformation(AffineTransform image2user) {
		ExternalSettings newExternalSettings = new ExternalSettings();
		newExternalSettings.width = settings.width;
		newExternalSettings.height = settings.height;
		newExternalSettings.tokens = settings.tokens;
		newExternalSettings.filteredLog = settings.filteredLog;
		newExternalSettings.transform = image2user;
		newExternalSettings.id = random.nextInt();

		settings = newExternalSettings;
		return newExternalSettings.id;
	}

	public synchronized int setSize(int width, int height) {
		ExternalSettings newExternalSettings = new ExternalSettings();
		newExternalSettings.width = width;
		newExternalSettings.height = height;
		newExternalSettings.tokens = settings.tokens;
		newExternalSettings.filteredLog = settings.filteredLog;
		newExternalSettings.transform = settings.transform;
		newExternalSettings.id = random.nextInt();

		settings = newExternalSettings;
		return newExternalSettings.id;
	}

	public synchronized int setTokens(GraphVizTokens animationGraphVizTokens) {
		ExternalSettings newExternalSettings = new ExternalSettings();
		newExternalSettings.width = settings.width;
		newExternalSettings.height = settings.height;
		newExternalSettings.tokens = animationGraphVizTokens;
		newExternalSettings.filteredLog = settings.filteredLog;
		newExternalSettings.transform = settings.transform;
		newExternalSettings.id = random.nextInt();

		settings = newExternalSettings;
		return newExternalSettings.id;
	}

	public synchronized int setFilteredLog(IvMLogFiltered ivMLogFiltered) {
		ExternalSettings newExternalSettings = new ExternalSettings();
		newExternalSettings.width = settings.width;
		newExternalSettings.height = settings.height;
		newExternalSettings.tokens = settings.tokens;
		newExternalSettings.filteredLog = ivMLogFiltered;
		newExternalSettings.transform = settings.transform;
		newExternalSettings.id = random.nextInt();

		settings = newExternalSettings;
		return newExternalSettings.id;
	}

}
