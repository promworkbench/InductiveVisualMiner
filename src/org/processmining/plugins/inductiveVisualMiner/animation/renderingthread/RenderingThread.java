package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.util.concurrent.atomic.AtomicBoolean;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;

public class RenderingThread implements Runnable {

	//thread variables
	private Thread runThread;
	private final AtomicBoolean stopRequested = new AtomicBoolean(false);
	private final AtomicBoolean pauseRequested = new AtomicBoolean(false);
	private final AtomicBoolean singleFrameRequested = new AtomicBoolean(false);
	private static final int minRenderDuration = 30;

	//time
	public final TimeManager timeManager;

	//external settings (tokens, log, ...)
	public final ExternalSettingsManager settingsManager;

	//result
	public final RenderedFrameManager renderedFrameManager;

	/**
	 * Initialise the rendering thread.
	 * 
	 * @param minTime
	 * @param maxTime
	 * @param onFrameComplete
	 * @param width
	 * @param height
	 */
	public RenderingThread(int minTime, int maxTime, Runnable onFrameComplete) {
		timeManager = new TimeManager(minTime, maxTime);
		settingsManager = new ExternalSettingsManager();
		renderedFrameManager = new RenderedFrameManager(onFrameComplete, settingsManager);
	}

	public void seek(double time) {
		timeManager.seek(time);
	}
	
	public void renderOneFrame() {
		System.out.println("render one frame");
		singleFrameRequested.set(true);
	}

	//thread handling
	public void start() {
		pauseRequested.set(false);
		stopRequested.set(false);
		if (runThread == null || !runThread.isAlive()) {
			runThread = new Thread(this);
		} else if (runThread.isAlive()) {
			return;
		}
		runThread.start();
	}

	public void stop() throws InterruptedException {
		if (runThread == null) {
			return;
		}
		stopRequested.set(true);
		runThread.join();
		runThread = null;
	}

	public void pause() {
		pauseRequested.set(true);
	}

	public void resume() {
		pauseRequested.set(false);
	}

	public void pauseResume() {
		boolean v;
		do {
			v = pauseRequested.get();
		} while (!pauseRequested.compareAndSet(v, !v));
	}

	public void run() {
		long sleep = 0, before;
		while (!stopRequested.get()) {
			//get the time before we do our game logic
			before = System.currentTimeMillis();

			//do the work
			if (singleFrameRequested.compareAndSet(true, false)) {
				while (!performRender()) {
				}
			} else if (!pauseRequested.get()) {
				performRender();
			}

			try {
				// sleep for xx - how long it took us to do the rendering
				sleep = minRenderDuration - (System.currentTimeMillis() - before);
				Thread.sleep(sleep > 0 ? sleep : 0);
			} catch (InterruptedException ex) {
			}
		}
	}

	public boolean performRender() {
		ExternalSettings settings = settingsManager.getExternalSettings();
		RenderedFrame result = renderedFrameManager.getFrameForRendering();
		double time = timeManager.getTimeToBeRendered(!pauseRequested.get() && !stopRequested.get());

		if (!Renderer.render(settings, result, time)) {
			renderedFrameManager.abortRendering();
			return false;
		}

		result.settingsId = settings.id;
		return renderedFrameManager.submitRendering();
	}

	/**
	 * 
	 * @return whether the animation is running.
	 */
	public boolean isPlaying() {
		return !pauseRequested.get() && !stopRequested.get();
	}

	public ExternalSettingsManager getExternalSettingsManager() {
		return settingsManager;
	}

	public TimeManager getTimeManager() {
		return timeManager;
	}

	public RenderedFrameManager getRenderedFrameManager() {
		return renderedFrameManager;
	}
}
