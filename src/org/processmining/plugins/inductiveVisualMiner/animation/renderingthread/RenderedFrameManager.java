package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

public class RenderedFrameManager {
	private final Runnable onFrameComplete;

	private final ExternalSettingsManager settingsManager;

	private final RenderedFrame[] results;

	private int rendering = -1;
	private int drawing = -1;

	public RenderedFrameManager(Runnable onFrameComplete, ExternalSettingsManager settingsManager) {
		this.onFrameComplete = onFrameComplete;
		this.settingsManager = settingsManager;

		//fill rendered frames buffer
		results = new RenderedFrame[3];
		for (int i = 0; i < results.length; i++) {
			results[i] = new RenderedFrame();
		}
	}

	/**
	 * 
	 * @return a RenderedFrame object to store the rendering. It is already
	 *         locked, and should not be unlocked by the renderer.
	 */
	public RenderedFrame getFrameForRendering() {
		for (int i = (rendering + 1) % results.length; i != rendering; i = ((i + 1) % results.length)) {
			if (results[i].startDrawing()) {
				rendering = i;
				return results[i];
			}
		}
		assert(false);
		return null;
	}

	/**
	 * Submit the rendered frame.
	 */
	public boolean submitRendering() {
		results[rendering].doneDrawing();

		if (settingsManager.isMostRecentSetting(results[rendering].settingsId)) {
			drawing = rendering;
			SwingUtilities.invokeLater(onFrameComplete);
			return true;
		} else {
			drawing = -1;
		}
		return false;
	}
	
	public void abortRendering() {
		results[rendering].doneDrawing();
	}

	/**
	 * 
	 * @return the last rendered frame. Call startDrawing() to lock it; call
	 *         doneDrawing() when done with that object.
	 */
	public RenderedFrame getLastRenderedFrame() {
		if (drawing == -1) {
			return null;
		}
		return results[drawing];
	}
	
	public void invalidateLastRenderedFrame() {
		if (drawing != -1) {
			results[drawing].doneDrawing();
		}
		drawing = -1;
	}

	public class RenderedFrame {
		public int settingsId;
		public BufferedImage image;
		public Graphics2D graphics;
		public double time;

		private final AtomicBoolean isDrawing = new AtomicBoolean(false);

		/**
		 * Attempts to non-blockingly lock this frame, and returns whether that
		 * succeeded. Call doneDrawing() when done. Rendering will choose
		 * another object, drawing can skip this frame and paint the next one.
		 * 
		 * @return
		 */
		public boolean startDrawing() {
			return isDrawing.compareAndSet(false, true);
		}

		public void doneDrawing() {
			isDrawing.set(false);
		}
	}
}
