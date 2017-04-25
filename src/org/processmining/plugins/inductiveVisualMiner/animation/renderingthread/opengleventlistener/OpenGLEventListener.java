package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.opengleventlistener;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;

import com.jogamp.opengl.GLEventListener;

public interface OpenGLEventListener extends GLEventListener {

	/**
	 * Set the parameters for rendering the next frame.
	 * 
	 * @param settings
	 * @param time
	 */
	void setParameters(ExternalSettings settings, double time);

}
