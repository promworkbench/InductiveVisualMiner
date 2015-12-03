package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.util.concurrent.atomic.AtomicBoolean;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;

public class RenderingThread implements Runnable {

	//thread variables
	private Thread runThread;
	private final AtomicBoolean stopRequested = new AtomicBoolean(false);
	private final AtomicBoolean pauseRequested = new AtomicBoolean(false);
	private final AtomicBoolean singleFrameRequested = new AtomicBoolean(false);
	private static final int minRenderDuration = 30;
	private final ProMCanceller canceller;

	//time
	private final TimeManager timeManager;

	//external settings (tokens, log, ...)
	private final ExternalSettingsManager settingsManager;

	//result
	private final RenderedFrameManager renderedFrameManager;

	/**
	 * Initialise the rendering thread.
	 * 
	 * @param minTime
	 * @param maxTime
	 * @param onFrameComplete
	 * @param width
	 * @param height
	 */
	public RenderingThread(int minTime, int maxTime, Runnable onFrameComplete, ProMCanceller canceller) {
		timeManager = new TimeManager(minTime, maxTime);
		settingsManager = new ExternalSettingsManager();
		renderedFrameManager = new RenderedFrameManager(onFrameComplete, settingsManager);
		this.canceller = canceller;
	}

	public void seek(double time) {
		timeManager.seek(time);
	}

	public void renderOneFrame() {
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
		timeManager.resume();
		pauseRequested.set(false);
	}

	public void pauseResume() {
		boolean v;
		do {
			v = pauseRequested.get();
		} while (!pauseRequested.compareAndSet(v, !v));
		if (v) {
			timeManager.resume();
		}
	}

	public void run() {
		long sleep = 0;
		long before = 0;
		long lastDuration = 0;

		//debug frame rate
		long frameRateStart = 0;
		long frameRateCount = 0;
		int avg = 10;
		while (!stopRequested.get() && !canceller.isCancelled()) {
			//get the time before we do our game logic
			before = System.currentTimeMillis();

			//do the work
			if (singleFrameRequested.compareAndSet(true, false)) {
				while (!performRender() && !canceller.isCancelled()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			} else if (!pauseRequested.get()) {
				performRender();
			}

			try {
				// sleep for xx - how long it took us to do the rendering
				lastDuration = System.currentTimeMillis() - before;
				sleep = minRenderDuration - lastDuration;
				if (sleep > 0) {
					Thread.sleep(sleep);
				}

				//debug framerate
				if (frameRateCount % avg == 0) {
					System.out.println((1 / ((System.currentTimeMillis() - frameRateStart) / (1000.0 * avg))));
					frameRateStart = before;
				}
				frameRateCount++;
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
