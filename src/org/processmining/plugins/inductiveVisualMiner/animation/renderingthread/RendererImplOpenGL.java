package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.image.BufferedImage;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.opengleventlistener.OpenGLEventListener;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class RendererImplOpenGL {

	private GLOffscreenAutoDrawable drawable;

	private OpenGLEventListener eventListener;

	public RendererImplOpenGL(OpenGLEventListener eventListener) {
		//System.out.println(GLProfile.glAvailabilityToString());

		GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);
		capabilities.setDepthBits(24);
		capabilities.setAlphaBits(8);
		capabilities.setDoubleBuffered(false);
		capabilities.setHardwareAccelerated(true);
		capabilities.setOnscreen(false);
		GLDrawableFactory factory = GLDrawableFactory.getFactory(profile);

		drawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), capabilities, null, 1, 1);
		this.eventListener = eventListener;
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
