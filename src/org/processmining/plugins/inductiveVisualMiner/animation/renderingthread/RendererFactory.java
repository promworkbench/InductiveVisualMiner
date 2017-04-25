package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.Ellipse2D;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.opengleventlistener.OpenGLEventListener;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.opengleventlistener.OpenGLEventListenerImplBasic;

public class RendererFactory {

	//rendering constants
	public static final int tokenRadius = 4;
	public static final Color defaultTokenFillColour = Color.yellow;
	public static final Color tokenStrokeColour = Color.black;
	public static final Color backgroundColor = new Color(255, 255, 255, 0);
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final Stroke colouredTokenStroke = new BasicStroke(0.5f);

	public static final Shape circle = new Ellipse2D.Float(-tokenRadius, -tokenRadius, tokenRadius * 2,
			tokenRadius * 2);
	public static final Shape outline = tokenStroke.createStrokedShape(circle);
	public static final Shape colouredOutline = colouredTokenStroke.createStrokedShape(circle);

	private final RendererImplOpenGL rendererOpenGL;
	private boolean openGLgotError;
	
	public RendererFactory() {
		//OpenGLEventListener eventListener = new OpenGLEventListenerImplInstanceArrayOfTokens();
		OpenGLEventListener eventListener = new OpenGLEventListenerImplBasic();
		rendererOpenGL = new RendererImplOpenGL(eventListener);
		openGLgotError = false;
	}
	
	/**
	 * Render a single frame.
	 * 
	 * @param settings
	 * @param result
	 * @param time
	 * @return
	 */
	public boolean render(ExternalSettings settings, RenderedFrame result, double time) {
		if (!openGLgotError) {
			try {
				return rendererOpenGL.render(settings, result, time);
			} catch (Exception e) {
				openGLgotError = true;
				e.printStackTrace();
			}
		}
		return RendererImplBasic.render(settings, result, time);
		//return RendererImplSprites.render(settings, result, time);
		//return rendererOpenGL.render(settings, result, time);
	}

	/**
	 * Helper function.
	 * 
	 * @param settings
	 * @param result
	 */
	public static void recreateImageIfNecessary(ExternalSettings settings, RenderedFrame result) {
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

			result.graphics.setBackground(RendererFactory.backgroundColor);
		}
	}
}