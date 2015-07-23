package org.processmining.plugins.inductiveVisualMiner.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import nl.tue.astar.AStarThread.Canceller;

import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Nonuple;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.visualisation.Transformation;
import org.processmining.plugins.graphviz.visualisation.ZoomPanState;
import org.processmining.plugins.inductiveVisualMiner.chain.ChainLink.ResettableCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ThreadedComputer;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class AnimationRenderer {
	public static final int tokenRadius = 4;
	public static final Color tokenFillColour = Color.yellow;
	public static final Color tokenStrokeColour = Color.black;
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final int maxAnimationDuration = 10; //after spending xx ms in drawing circles, just quit.
	public static final int maxAnimationPausedDuration = 1000; //after spending xx ms in drawing circles, just quit.

	public ThreadedComputer<Nonuple<GraphVizTokens, IvMLogFiltered, Double, Boolean, Boolean, Rectangle, Rectangle, Transformation, ZoomPanState>, Pair<Double, BufferedImage>> pool;

	public AnimationRenderer(InputFunction<Pair<Double, BufferedImage>> onComplete) {
		pool = new ThreadedComputer<Nonuple<GraphVizTokens, IvMLogFiltered, Double, Boolean, Boolean, Rectangle, Rectangle, Transformation, ZoomPanState>, Pair<Double, BufferedImage>>(
				new Function<Pair<ResettableCanceller, Nonuple<GraphVizTokens, IvMLogFiltered, Double, Boolean, Boolean, Rectangle, Rectangle, Transformation, ZoomPanState>>, Pair<Double, BufferedImage>>() {

					//this function is called when the computation is called
					public Pair<Double, BufferedImage> call(
							Pair<ResettableCanceller, Nonuple<GraphVizTokens, IvMLogFiltered, Double, Boolean, Boolean, Rectangle, Rectangle, Transformation, ZoomPanState>> input)
							throws Exception {
						Canceller canceller = input.getA();
						GraphVizTokens tokens = input.getB().getA();
						IvMLogFiltered filteredLog = input.getB().getB();
						double time = input.getB().getC();
						boolean timeOutPossible = input.getB().getD();
						boolean animationPlaying = input.getB().getE();
						Rectangle visibleImageInUserCoordinates = input.getB().getF();
						Rectangle imageInUserCoordinates = input.getB().getG();
						Transformation t = input.getB().getH();
						ZoomPanState zoomPanState = input.getB().getI();

						GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
						GraphicsDevice gs = ge.getDefaultScreenDevice();
						GraphicsConfiguration gc = gs.getDefaultConfiguration();
						final BufferedImage image = gc.createCompatibleImage(
								(int) visibleImageInUserCoordinates.getWidth(),
								(int) visibleImageInUserCoordinates.getHeight(), Transparency.TRANSLUCENT);
						Graphics2D g = image.createGraphics();
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						//transform the image to the correct position
						if (imageInUserCoordinates.getX() < 0) {
							g.translate(imageInUserCoordinates.getX(), 0);
						}
						if (imageInUserCoordinates.getY() < 0) {
							g.translate(0, imageInUserCoordinates.getY());
						}
						g.scale(t.dscale(zoomPanState), t.dscale(zoomPanState));

						return Pair
								.of(paintTokens(g, tokens, filteredLog, time, timeOutPossible, animationPlaying,
										canceller), image);
					}
				}, onComplete);
	}

	/**
	 * Paint the tokens asynchronously and call callBack when done.
	 * 
	 * @param image
	 * @param g
	 * @param tokens
	 * @param filteredLog
	 * @param time
	 * @param timeOutPossible
	 * @param animationPlaying
	 * @param visibleImageInUserCoordinates
	 * @param imageInUserCoordinates
	 * @param t 
	 * @param zoomPanState 
	 */
	public void paintAsynchronous(GraphVizTokens tokens, IvMLogFiltered filteredLog, double time,
			boolean timeOutPossible, boolean animationPlaying, Rectangle visibleImageInUserCoordinates,
			Rectangle imageInUserCoordinates, Transformation t, ZoomPanState zoomPanState) {

		pool.compute(Nonuple.of(tokens, filteredLog, time, timeOutPossible, animationPlaying,
				visibleImageInUserCoordinates, imageInUserCoordinates, t, zoomPanState));
	}

	public void cancelAsynchronousRendering() {
		pool.cancelCurrentComputation();
	}
	
	/**
	 * Paints tokens
	 * 
	 * @param g
	 * @param tokens
	 * @param time
	 * @param timeOutPossible
	 * @param animationPlaying
	 * @return the last token that was painted
	 */
	public static double paintTokens(Graphics2D g, GraphVizTokens tokens, IvMLogFiltered filteredLog, double time,
			boolean timeOutPossible, boolean animationPlaying, Canceller canceller) {

		long startTime = System.currentTimeMillis();
		long nowTime;
		int countTotal = 0;
		int countPainted = 0;
		boolean stillPainting = true;

		Color backupColour = g.getColor();
		Stroke backupStroke = g.getStroke();

		g.setStroke(tokenStroke);

		IteratorWithPosition<Integer> it = tokens.getTokensAtTime(time);
		while (it.hasNext()) {
			int tokenIndex = it.next();

			//only paint tokens that are not filtered out
			if (filteredLog == null || !filteredLog.isFilteredOut(tokens.getTraceIndex(tokenIndex))) {
				countTotal++;

				if (stillPainting) {
					paintToken(g, tokens, time, tokenIndex);
					countPainted++;

					//see if it's already time to stop
					nowTime = System.currentTimeMillis() - startTime;
					stillPainting = !timeOutPossible
							|| !((animationPlaying && nowTime > maxAnimationDuration) || nowTime > maxAnimationPausedDuration);
				} else {
					return 0;
				}
			}

			if (canceller.isCancelled()) {
				return -1;
			}
		}

		g.setColor(backupColour);
		g.setStroke(backupStroke);

		return countPainted / (countTotal * 1.0);
	}

	public static void paintToken(Graphics2D g, GraphVizTokens tokens, double time, int tokenIndex) {
		//ask for the point
		Triple<Double, Double, Double> point = tokens.eval(tokenIndex, time);

		g.transform(tokens.getTransform(tokenIndex));

		g.translate(point.getA(), point.getB());

		//draw the oval
		if (point.getC() == 1) {
			g.setPaint(tokenFillColour);
		} else {
			g.setPaint(new Color(tokenFillColour.getRed(), tokenFillColour.getGreen(), tokenFillColour.getBlue(),
					(int) Math.round(point.getC() * 255)));
		}
		g.fillOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

		//draw the fill
		if (point.getC() == 1) {
			g.setColor(tokenStrokeColour);
		} else {
			g.setColor(new Color(tokenStrokeColour.getRed(), tokenStrokeColour.getGreen(), tokenStrokeColour.getBlue(),
					(int) Math.round(point.getC() * 255)));
		}
		g.drawOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

		g.translate(-point.getA(), -point.getB());

		g.transform(tokens.getTransformInverse(tokenIndex));
	}
}
