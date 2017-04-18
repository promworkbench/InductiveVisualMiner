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
import java.awt.image.BufferedImage;

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;

public class RendererImplSprites {

	public static boolean render(ExternalSettings settings, RenderedFrame result, double time) {
		if (settings.filteredLog != null && settings.tokens != null && settings.transform != null) {

			//resize the image if necessary
			RendererFactory.recreateImageIfNecessary(settings, result);

			//clear the background
			result.graphics.clearRect(0, 0, result.image.getWidth(), result.image.getHeight());

			//render the tokens		
			renderTokens(result.graphics, settings.tokens, settings.filteredLog, settings.trace2colour, time,
					result.image.getWidth(), result.image.getHeight(), settings.transform);

			//set the result's time
			result.time = time;

			return true;
		}
		return false;
	}

	private static void renderTokens(Graphics2D graphics, GraphVizTokensIterator tokens, IvMLogFiltered filteredLog,
			TraceColourMap trace2colour, double time, int imgWidth, int imgHeight, AffineTransform userTransform) {
		tokens.itInit(time);

		Color tokenFillColour;

		//make a bitmap
		BufferedImage circleImage;
		double radius = 10 * userTransform.getScaleX();
		double strokeWidth = 3 * userTransform.getScaleX();
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			circleImage = gc.createCompatibleImage(Math.max(1, (int) (radius * 2)), Math.max(1, (int) (radius * 2)),
					Transparency.BITMASK);
			Graphics2D circleGraphics = circleImage.createGraphics();
			circleGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			circleGraphics.setPaint(RendererFactory.defaultTokenFillColour);
			Shape circle = new Ellipse2D.Float((float) (0.5 * strokeWidth), (float) (0.5 * strokeWidth),
					(float) (radius * 2 - strokeWidth), (float) (radius * 2 - strokeWidth));
			circleGraphics.fill(circle);

			//stroke
			circleGraphics.setPaint(RendererFactory.tokenStrokeColour);

			Stroke tokenStroke = new BasicStroke((float) strokeWidth);
			circleGraphics.fill(tokenStroke.createStrokedShape(circle));
			circleGraphics.dispose();
		}
		Point2D.Double point = new Point2D.Double(0, 0);

		while (tokens.itHasNext()) {
			tokens.itNext();

			//only paint tokens that are not filtered out
			if (filteredLog == null || !filteredLog.isFilteredOut(tokens.itGetTraceIndex())) {
				tokens.itEval();

				//only attempt to draw if the token is in the visible image
				AffineTransform bezierTransform = tokens.itGetTransform();
				point.x = tokens.itGetX();
				point.y = tokens.itGetY();
				bezierTransform.transform(point, point);
				userTransform.transform(point, point);
				if (point.getX() + radius >= 0 && point.getX() - radius < imgWidth && point.getY() + radius >= 0
						&& point.getY() - radius < imgHeight) {
					graphics.drawImage(circleImage, (int) (point.getX() - radius), (int) (point.getY() - radius), null);
				}
			}
		}
	}
}
