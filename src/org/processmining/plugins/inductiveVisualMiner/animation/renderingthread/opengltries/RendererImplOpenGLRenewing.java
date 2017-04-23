package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.opengltries;

import java.awt.image.BufferedImage;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.visualisation3d.GraphicsPipeline;
import org.processmining.visualisation3d.gldatastructures.JoglShader;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class RendererImplOpenGLRenewing {

	private GLOffscreenAutoDrawable drawable;
	private GLDrawableFactory factory;
	private GLCapabilities capabilities;
	private JoglShader shader = null;
	private GraphicsPipeline pipeLine;

	private RendererImplOpenGLEventListenerInstancedArrays2 eventListener;

	public RendererImplOpenGLRenewing() {
		//System.out.println(GLProfile.glAvailabilityToString());

		GLProfile profile = GLProfile.get(GLProfile.GL2);
		capabilities = new GLCapabilities(profile);
		capabilities.setDepthBits(24);
		capabilities.setAlphaBits(8);
		capabilities.setDoubleBuffered(false);
		capabilities.setHardwareAccelerated(true);
		capabilities.setOnscreen(false);
		factory = GLDrawableFactory.getFactory(profile);

		drawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), capabilities, null, 1, 1);
		eventListener = new RendererImplOpenGLEventListenerInstancedArrays2();
		drawable.addGLEventListener(eventListener);
	}

	public boolean render(ExternalSettings settings, RenderedFrame result, double time) {
		if (settings.filteredLog != null && settings.tokens != null && settings.transform != null) {

			//resize the image if necessary
			if (result.image == null || result.image.getWidth() != settings.width
					|| result.image.getHeight() != settings.height) {
				RendererFactory.recreateImageIfNecessary(settings, result);

				//tell OpenGL that we are resizing
				drawable.setSurfaceSize(result.image.getWidth(), result.image.getHeight());
			}

			//clear the background
			result.graphics.clearRect(0, 0, result.image.getWidth(), result.image.getHeight());

			//tell the event listener the parameters
			eventListener.setParameters(settings, time);

			//trigger OpenGL to render
			drawable.display();

			//copy the rendered things to an image
			AWTGLReadBufferUtil readBufferUtil = new AWTGLReadBufferUtil(drawable.getGLProfile(), true);
			BufferedImage image = readBufferUtil.readPixelsToBufferedImage(drawable.getGL(), false);
			result.graphics.drawImage(image, 0, 0, null);

			//set the result's time
			result.time = time;

			return true;
		}
		return false;
	}
}
