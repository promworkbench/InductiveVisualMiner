package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

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

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilter;

public class Renderer {

	//rendering constants
	public static final int tokenRadius = 4;
	public static final Color tokenFillColour = Color.yellow;
	public static final Color tokenStrokeColour = Color.black;
	public static final Color backgroundColor = new Color(255, 255, 255, 0);
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final int maxAnimationDuration = 10; //after spending xx ms in drawing circles, just quit.
	public static final int maxAnimationPausedDuration = 1000; //after spending xx ms in drawing circles, just quit.

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
			renderTokens(result.graphics, settings.tokens, settings.filteredLog, time);

			//transform back
			result.graphics.setTransform(new AffineTransform());

			//set the result's time
			result.time = time;

			return true;
		}
		return false;
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
}
