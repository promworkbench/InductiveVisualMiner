package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class RenderingThread implements Runnable {

	//thread variables
	private Thread runThread;
	private boolean running = false;
	private boolean paused = false;
	private static final int minRenderDuration = 30;

	//rendering constants
	public static final int tokenRadius = 4;
	public static final Color tokenFillColour = Color.yellow;
	public static final Color tokenStrokeColour = Color.black;
	public static final Color backgroundColor = new Color(255, 255, 255, 0);
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final int maxAnimationDuration = 10; //after spending xx ms in drawing circles, just quit.
	public static final int maxAnimationPausedDuration = 1000; //after spending xx ms in drawing circles, just quit.

	//state
	private static class Result {
		//result variables
		BufferedImage image;
		Graphics2D graphics;
		double time;
		double minTime;
		double maxTime;
	}

	private static class Settings {
		//resizing variables
		int width;
		int height;

		//time variables
		boolean pauseRequested;
		Double time;
		double minTime; //in seconds
		double maxTime; //in seconds

		//rendering variables
		IvMLogFilter filteredLog;
		GraphVizTokens tokens;
		AffineTransform transform;

		public Settings clone() {
			Settings result = new Settings();
			result.width = width;
			result.height = height;
			result.pauseRequested = result.pauseRequested;
			result.time = time;
			result.minTime = minTime;
			result.maxTime = maxTime;
			result.filteredLog = filteredLog;
			result.tokens = tokens;
			result.transform = transform;
			return result;
		}
	}

	private Settings settings = new Settings();
	private Settings newSettings = null; //set to something non-null if required
	private Result internalResult = new Result();
	private Result externalResult = new Result();
	private Result swapResult = null;

	private long now; //in ms
	private long lastUpdated; //in ms
	private double time; //in s

	private final Runnable onFrameComplete;

	/**
	 * Initialise the rendering thread. To function, it will need calls to
	 * setSize(), setTokens(), setFilteredLog() and start().
	 * 
	 * @param minTime
	 * @param maxTime
	 * @param onFrameComplete
	 */
	public RenderingThread(int minTime, int maxTime, Runnable onFrameComplete) {
		settings.minTime = minTime;
		settings.maxTime = maxTime;
		settings.width = 0;
		settings.height = 0;

		now = System.currentTimeMillis();
		lastUpdated = System.currentTimeMillis();
		time = minTime;

		this.onFrameComplete = onFrameComplete;
	}

	//settings handling

	/**
	 * Resize the produced image.
	 * 
	 * @param width
	 * @param height
	 */
	public void setSize(int width, int height) {
		Settings s = getNewSettings();
		s.width = width;
		s.height = height;
		newSettings = s;
	}

	/**
	 * Sets the tokens that are to be rendered.
	 * 
	 * @param tokens
	 */
	public void setTokens(GraphVizTokens tokens) {
		Settings s = getNewSettings();
		s.tokens = tokens;
		newSettings = s;
	}

	/**
	 * Set the log, which denotes which tokens should be drawn.
	 * 
	 * @param filteredLog
	 */
	public void setFilteredLog(IvMLogFiltered filteredLog) {
		Settings s = getNewSettings();
		s.filteredLog = filteredLog;
		newSettings = s;
	}

	/**
	 * Set a new transformation.
	 * 
	 * @param transform
	 */
	public void setImageTransformation(AffineTransform transform) {
		Settings s = getNewSettings();
		s.transform = transform;
		newSettings = s;
	}

	/**
	 * Sets the bounds of the animation.
	 * 
	 * @param minTime
	 * @param maxTime
	 */
	public void setExtremeTimes(double minTime, double maxTime) {
		Settings s = getNewSettings();
		s.minTime = minTime;
		s.maxTime = maxTime;
		newSettings = s;
	}

	/**
	 * Sets the next rendered time of the animation.
	 * 
	 * @param time
	 */
	public void setTime(double time) {
		Settings s = getNewSettings();
		s.time = time;
		newSettings = s;
	}

	/**
	 * Get a new settings object. Must be called synchronously.
	 * 
	 * @return
	 */
	private Settings getNewSettings() {
		if (newSettings != null) {
			return newSettings.clone();
		} else {
			return settings.clone();
		}
	}

	//thread handling
	public void start() {
		running = true;
		paused = false;
		if (runThread == null || !runThread.isAlive()) {
			runThread = new Thread(this);
		} else if (runThread.isAlive()) {
			return;
		}
		runThread.start();
	}

	public void stop() {
		if (runThread == null) {
			return;
		}
		synchronized (runThread) {
			try {
				running = false;
				runThread.notify();
				runThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void pause() {
		if (runThread == null) {
			return;
		}
		synchronized (runThread) {
			paused = true;
		}
	}
	
	public void resume() {
		if (runThread == null) {
			return;
		}
		synchronized (runThread) {
			paused = false;
			runThread.notify();
		}
	}

	public void pauseResume() {
		if (runThread == null) {
			return;
		}
		synchronized (runThread) {
			if (paused) {
				//resume
				Settings s = getNewSettings();
				s.time = getLastRenderedTime();
				newSettings = s;

				paused = false;
				runThread.notify();
			} else {
				paused = true;
			}
		}
	}

	/**
	 * If playing, has no effect. If paused, runs for one frame and pauses
	 * again.
	 */
	public void startOneFrame() {
		synchronized (runThread) {
			if (paused) {
				//set a pause request
				Settings s = getNewSettings();
				s.pauseRequested = true;
				newSettings = s;
				
				//resume
				paused = false;
				runThread.notify();
			}			
		}
	}

	public void run() {
		long sleep = 0, before;
		while (running) {
			// get the time before we do our game logic
			before = System.currentTimeMillis();
			render();
			try {
				// sleep for xx - how long it took us to do our game logic
				sleep = minRenderDuration - (System.currentTimeMillis() - before);
				Thread.sleep(sleep > 0 ? sleep : 0);
			} catch (InterruptedException ex) {
			}
			synchronized (runThread) {
				if (paused) {
					try {
						runThread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		paused = false;
	}

	/**
	 * 
	 * @return whether the animation is running.
	 */
	public boolean isPlaying() {
		return runThread != null && runThread.isAlive() && running && !paused;
	}

	public void render() {
		if (newSettings != null) {
			synchronized (this) {
				settings = newSettings;
				newSettings = null;
			}
		}

		if (settings.filteredLog != null && settings.tokens != null && settings.transform != null) {

			//resize the image if necessary
			if (internalResult.image == null || internalResult.image.getWidth() != settings.width
					|| internalResult.image.getHeight() != settings.height) {
				if (internalResult.graphics != null) {
					internalResult.graphics.dispose();
				}
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice gs = ge.getDefaultScreenDevice();
				GraphicsConfiguration gc = gs.getDefaultConfiguration();
				internalResult.image = gc.createCompatibleImage(settings.width, settings.height,
						Transparency.TRANSLUCENT);
				internalResult.graphics = internalResult.image.createGraphics();
				internalResult.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				internalResult.graphics.setBackground(backgroundColor);
			}

			//clear the background
			internalResult.graphics.clearRect(0, 0, internalResult.image.getWidth(), internalResult.image.getHeight());

			//transform
			internalResult.graphics.setTransform(settings.transform);

			//compute the next timestep
			if (settings.time != null) {
				lastUpdated = System.currentTimeMillis();
				time = settings.time;
				settings.time = null;
			} else {
				takeTimeStep();
			}

			//render the tokens		
			renderTokens(internalResult.graphics, settings.tokens, settings.filteredLog, time);

			//transform back
			internalResult.graphics.setTransform(new AffineTransform());
			internalResult.time = time;
			internalResult.minTime = settings.minTime;
			internalResult.maxTime = settings.maxTime;
		}

		//rendering done, swap the images (if we're not got paused in the meantime)
		if (!paused) {
			swapResult = externalResult;
			externalResult = internalResult;
			internalResult = swapResult;
			swapResult = null;
		}

		if (settings.pauseRequested) {
			pause();
			settings.pauseRequested = false;
		}

		SwingUtilities.invokeLater(onFrameComplete);
	}

	private void takeTimeStep() {
		now = System.currentTimeMillis();
		time = time + ((now - lastUpdated) / 1000.0);
		while (time < settings.minTime) {
			time += (settings.maxTime - settings.minTime);
		}
		while (time > settings.maxTime) {
			time -= (settings.maxTime - settings.minTime);
		}
		lastUpdated = now;
	}

	public static void renderTokens(Graphics2D graphics, GraphVizTokensIterator tokens, IvMLogFilter filteredLog,
			double time) {
		graphics.setStroke(tokenStroke);

		tokens.itInit(time);
		while (tokens.itHasNext()) {
			tokens.itNext();
			tokens.itEval();

			//only paint tokens that are not filtered out
			if (filteredLog == null || !filteredLog.isFilteredOut(tokens.itGetTraceIndex())) {

				//transform
				graphics.transform(tokens.itGetTransform());
				graphics.translate(tokens.itGetX(), tokens.itGetY());

				//draw the oval
				if (tokens.itGetOpacity() == 1) {
					graphics.setPaint(tokenFillColour);
				} else {
					graphics.setPaint(new Color(tokenFillColour.getRed(), tokenFillColour.getGreen(), tokenFillColour
							.getBlue(), (int) Math.round(tokens.itGetOpacity() * 255)));
				}
				graphics.fillOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

				//draw the fill
				if (tokens.itGetOpacity() == 1) {
					graphics.setColor(tokenStrokeColour);
				} else {
					graphics.setColor(new Color(tokenStrokeColour.getRed(), tokenStrokeColour.getGreen(),
							tokenStrokeColour.getBlue(), (int) Math.round(tokens.itGetOpacity() * 255)));
				}
				graphics.drawOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

				//transform back
				graphics.translate(-tokens.itGetX(), -tokens.itGetY());
				graphics.transform(tokens.itGetTransformInverse());
			}
		}
	}

	public BufferedImage getLastRenderedImage() {
		return externalResult.image;
	}

	public double getLastRenderedTime() {
		return externalResult.time;
	}

	public double getLastRenderedMinTime() {
		return externalResult.minTime;
	}

	public double getLastRenderedMaxTime() {
		return externalResult.maxTime;
	}
}
