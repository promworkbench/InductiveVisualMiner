package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapFixed;

public class RendererImplBasic {

	//precompupte colours and transformations to save object allocations during the animation.
	public static final AffineTransform identityTransform = new AffineTransform();

	public static boolean render(ExternalSettings settings, RenderedFrame result, double time) {
		if (settings.filteredLog != null && settings.tokens != null && settings.transform != null) {

			//resize the image if necessary
			RendererFactory.recreateImageIfNecessary(settings, result);

			//clear the background
			result.graphics.clearRect(0, 0, result.image.getWidth(), result.image.getHeight());

			//transform
			result.graphics.setTransform(settings.transform);

			//render the tokens		
			renderTokens(result.graphics, settings.tokens, settings.filteredLog, settings.trace2colour, time,
					result.image.getWidth(), result.image.getHeight(), settings.transform);

			//transform back
			result.graphics.setTransform(identityTransform);

			//set the result's time
			result.time = time;

			return true;
		}
		return false;
	}

	public static void renderTokens(Graphics2D graphics, GraphVizTokensIterator tokens, IvMLogFiltered filteredLog,
			TraceColourMap trace2colour, double time, int imgWidth, int imgHeight, AffineTransform userTransform) {
		tokens.itInit(time);

		//initialise points to keep track of  
		Point2D.Double minTokenCoordinates = new Point2D.Double(RendererFactory.tokenRadius,
				RendererFactory.tokenRadius);
		Point2D.Double minImageCoordinates = new Point2D.Double();
		Point2D.Double maxTokenCoordinates = new Point2D.Double(-RendererFactory.tokenRadius,
				-RendererFactory.tokenRadius);
		Point2D.Double maxImageCoordinates = new Point2D.Double();

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
					{
						Color fillColour;
						if (trace2colour != null) {
							fillColour = trace2colour.getColour(tokens.itGetTraceIndex());
						} else {
							fillColour = RendererFactory.defaultTokenFillColour;
						}
						graphics.setPaint(new Color(fillColour.getRed(), fillColour.getGreen(), fillColour.getBlue(),
								(int) Math.round(tokens.itGetOpacity() * 255)));
						graphics.fill(RendererFactory.circle);
					}

					//draw the outline/stroke
					{
						if (tokens.itGetOpacity() == 1) {
							graphics.setColor(RendererFactory.tokenStrokeColour);
						} else {
							graphics.setColor(new Color(RendererFactory.tokenStrokeColour.getRed(),
									RendererFactory.tokenStrokeColour.getGreen(),
									RendererFactory.tokenStrokeColour.getBlue(),
									(int) Math.round(tokens.itGetOpacity() * 255)));
						}

						/*
						 * If the tokens have different colours, probably the
						 * stroke is not what the programmer wants the user to
						 * see. Therefore, draw a much smaller stroke.
						 * 
						 */
						if (trace2colour instanceof TraceColourMapFixed) {
							graphics.fill(RendererFactory.outline);
						} else {
							graphics.fill(RendererFactory.colouredOutline);
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
