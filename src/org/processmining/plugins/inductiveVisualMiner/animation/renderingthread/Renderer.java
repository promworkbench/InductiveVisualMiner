package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapFixed;

public class Renderer {

	//rendering constants
	public static final int tokenRadius = 4;
	public static final Color defaultTokenFillColour = Color.yellow;
	public static final Color tokenStrokeColour = Color.black;
	public static final Color backgroundColor = new Color(255, 255, 255, 0);
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final Stroke colouredTokenStroke = new BasicStroke(0.5f);
	public static final int maxAnimationDuration = 10; //after spending xx ms in drawing circles, just quit.
	public static final int maxAnimationPausedDuration = 1000; //after spending xx ms in drawing circles, just quit.

	private static final Shape circle = new Ellipse2D.Float(-tokenRadius, -tokenRadius, tokenRadius * 2,
			tokenRadius * 2);
	private static final Shape outline = tokenStroke.createStrokedShape(circle);
	private static final Shape colouredOutline = colouredTokenStroke.createStrokedShape(circle);

	public static boolean render(ExternalSettings settings, RenderedFrame result, double time) {
		if (settings.filteredLog != null && settings.tokens != null && settings.transform != null) {

			//resize the image if necessary
			if (result.image == null || result.image.getWidth() != settings.width
					|| result.image.getHeight() != settings.height) {
				if (result.graphics != null) {
					result.graphics.dispose();
				}
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice gs = ge.getDefaultScreenDevice();
				GraphicsConfiguration gc = gs.getDefaultConfiguration();
				result.image = gc.createCompatibleImage(settings.width, settings.height, Transparency.TRANSLUCENT);
				result.graphics = result.image.createGraphics();
				result.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				result.graphics.setBackground(backgroundColor);
			}

			//clear the background
			result.graphics.clearRect(0, 0, result.image.getWidth(), result.image.getHeight());

			//transform
			result.graphics.setTransform(settings.transform);

			//render the tokens		
			renderTokens(result.graphics, settings.tokens, settings.filteredLog, settings.trace2colour, time,
					result.image.getWidth(), result.image.getHeight());

			//transform back
			result.graphics.setTransform(new AffineTransform());

			//set the result's time
			result.time = time;

			return true;
		}
		return false;
	}

	public static void renderTokens(Graphics2D graphics, GraphVizTokensIterator tokens, IvMLogFiltered filteredLog,
			TraceColourMap trace2colour, double time, int imgWidth, int imgHeight) {
		tokens.itInit(time);

		//initialise points to keep track of  
		Point2D.Double minTokenCoordinates = new Point2D.Double(tokenRadius, tokenRadius);
		Point2D.Double minImageCoordinates = new Point2D.Double();
		Point2D.Double maxTokenCoordinates = new Point2D.Double(-tokenRadius, -tokenRadius);
		Point2D.Double maxImageCoordinates = new Point2D.Double();

		Color tokenFillColour;

		while (tokens.itHasNext()) {
			tokens.itNext();

			//only paint tokens that are not filtered out
			if (filteredLog == null || !filteredLog.isFilteredOut(tokens.itGetTraceIndex())) {
				tokens.itEval();

				//transform the canvas
				graphics.transform(tokens.itGetTransform());
				graphics.translate(tokens.itGetX(), tokens.itGetY());

				//only attempt to draw if the token is in the visible image
				graphics.getTransform().transform(minTokenCoordinates, minImageCoordinates);
				graphics.getTransform().transform(maxTokenCoordinates, maxImageCoordinates);
				if (minImageCoordinates.y >= 0 && minImageCoordinates.x >= 0 && maxImageCoordinates.x <= imgWidth
						&& maxImageCoordinates.y <= imgHeight) {

					//draw the fill
					if (trace2colour != null) {
						if (tokens.itGetOpacity() == 1) {
							graphics.setPaint(trace2colour.getColour(tokens.itGetTraceIndex()));
						} else {
							tokenFillColour = trace2colour.getColour(tokens.itGetTraceIndex());
							graphics.setPaint(new Color(tokenFillColour.getRed(), tokenFillColour.getGreen(),
									tokenFillColour.getBlue(), (int) Math.round(tokens.itGetOpacity() * 255)));
						}
					} else {
						graphics.setPaint(defaultTokenFillColour);
					}
					graphics.fill(circle);

					//draw the outline/stroke
					{
						if (tokens.itGetOpacity() == 1) {
							graphics.setColor(tokenStrokeColour);
						} else {
							graphics.setColor(new Color(tokenStrokeColour.getRed(), tokenStrokeColour.getGreen(),
									tokenStrokeColour.getBlue(), (int) Math.round(tokens.itGetOpacity() * 255)));
						}

						/*
						 * If the tokens have different colours, probably the
						 * stroke is not what the programmer wants the user to
						 * see. Therefore, draw a much smaller stroke.
						 * 
						 */
						if (trace2colour instanceof TraceColourMapFixed) {
							graphics.fill(outline);
						} else {
							graphics.fill(colouredOutline);
						}
					}
				}

				//transform back
				graphics.translate(-tokens.itGetX(), -tokens.itGetY());
				graphics.transform(tokens.itGetTransformInverse());
			}
		}
	}
}
