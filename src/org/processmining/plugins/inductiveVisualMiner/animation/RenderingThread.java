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
import java.awt.image.BufferedImage;

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
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final int maxAnimationDuration = 10; //after spending xx ms in drawing circles, just quit.
	public static final int maxAnimationPausedDuration = 1000; //after spending xx ms in drawing circles, just quit.

	//state
	private static class Result {
		//result variables
		BufferedImage image;
		Graphics2D graphics;
	}

	private static class Settings {
		//resizing variables
		int width;
		int height;

		//time variables
		double minTime; //in seconds
		double maxTime; //in seconds

		//rendering variables
		IvMLogFiltered filteredLog;
		GraphVizTokens tokens;

		public Settings clone() {
			Settings result = new Settings();
			result.width = width;
			result.height = height;
			result.minTime = minTime;
			result.maxTime = maxTime;
			result.filteredLog = filteredLog;
			result.tokens = tokens;
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

	public RenderingThread(int minTime, int maxTime, int width, int height) {
		settings.minTime = minTime;
		settings.maxTime = maxTime;
		settings.width = width;
		settings.height = height;

		now = System.currentTimeMillis();
		lastUpdated = System.currentTimeMillis();
		time = minTime;
	}

	//settings handling

	/**
	 * Resize the produced image.
	 * 
	 * @param width
	 * @param height
	 */
	public void resizeTo(int width, int height) {
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

	public void setFilteredLog(IvMLogFiltered filteredLog) {
		Settings s = getNewSettings();
		s.filteredLog = filteredLog;
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
		if (runThread == null || !runThread.isAlive())
			runThread = new Thread(this);
		else if (runThread.isAlive())
			throw new IllegalStateException("Thread already started.");
		runThread.start();
	}

	public void stop() {
		if (runThread == null)
			throw new IllegalStateException("Thread not started.");
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
		if (runThread == null)
			throw new IllegalStateException("Thread not started.");
		synchronized (runThread) {
			paused = true;
		}
	}

	public void resume() {
		if (runThread == null)
			throw new IllegalStateException("Thread not started.");
		synchronized (runThread) {
			paused = false;
			runThread.notify();
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

	private final static Color backgroundColor = new Color(255, 255, 255, 0);

	public void render() {
		if (settings.filteredLog != null && settings.tokens != null) {
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
			}

			//compute the next timestep
			takeTimeStep();

			//render the tokens		
			renderTokens();
		}

		//rendering done, swap the images
		synchronized (this) {
			swapResult = externalResult;
			externalResult = internalResult;
			internalResult = swapResult;
			swapResult = null;

			if (newSettings != null) {
				settings = newSettings;
				newSettings = null;
			}
		}
	}

	private void takeTimeStep() {
		now = System.currentTimeMillis();
		time = time + ((now - lastUpdated) / 1000.0);
		while (time > settings.maxTime) {
			time -= (settings.maxTime - settings.minTime);
		}
		lastUpdated = now;
	}

	private void renderTokens() {
		//clear the background
		internalResult.graphics.setBackground(backgroundColor);
		internalResult.graphics.clearRect(0, 0, settings.width, settings.height);

		internalResult.graphics.setStroke(tokenStroke);

		settings.tokens.itInit(time);
		while (settings.tokens.itHasNext()) {
			settings.tokens.itNext();
			settings.tokens.itEval();

			//only paint tokens that are not filtered out
			if (settings.filteredLog == null || !settings.filteredLog.isFilteredOut(settings.tokens.itGetTraceIndex())) {

				//transform
				internalResult.graphics.transform(settings.tokens.itGetTransform());
				internalResult.graphics.translate(settings.tokens.itGetX(), settings.tokens.itGetY());

				//draw the oval
				if (settings.tokens.itGetOpacity() == 1) {
					internalResult.graphics.setPaint(tokenFillColour);
				} else {
					internalResult.graphics.setPaint(new Color(tokenFillColour.getRed(), tokenFillColour.getGreen(),
							tokenFillColour.getBlue(), (int) Math.round(settings.tokens.itGetOpacity() * 255)));
				}
				internalResult.graphics.fillOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

				//draw the fill
				if (settings.tokens.itGetOpacity() == 1) {
					internalResult.graphics.setColor(tokenStrokeColour);
				} else {
					internalResult.graphics.setColor(new Color(tokenStrokeColour.getRed(),
							tokenStrokeColour.getGreen(), tokenStrokeColour.getBlue(), (int) Math.round(settings.tokens
									.itGetOpacity() * 255)));
				}
				internalResult.graphics.drawOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

				//transform back
				internalResult.graphics.translate(-settings.tokens.itGetX(), -settings.tokens.itGetY());
				internalResult.graphics.transform(settings.tokens.itGetTransformInverse());
			}
		}
	}

	public BufferedImage getLastRenderedImage() {
		return externalResult.image;
	}
}
