package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Vector;

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.opengltries.RendererImplOpenGLEventListenerInstancedArrays;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.visualisation3d.GraphicsPipeline;
import org.processmining.visualisation3d.gldatastructures.JoglShader;
import org.processmining.visualisation3d.gldatastructures.JoglVertexArrayObject;
import org.processmining.visualisation3d.graphicsdatastructures.JoglMeshDataBufferVectord2;
import org.processmining.visualisation3d.graphicsdatastructures.JoglMeshIndexBuffer;
import org.processmining.visualisation3d.maths.JoglVectord2;
import org.processmining.visualisation3d.maths.JoglVectord3;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class RendererImplOpenGL {

	private GLOffscreenAutoDrawable drawable;
	private GLDrawableFactory factory;
	private GLCapabilities capabilities;
	private JoglShader shader = null;
	private GraphicsPipeline pipeLine;

	private RendererImplOpenGLEventListenerInstancedArrays eventListener;

	public final static double radius = 6;

	public RendererImplOpenGL() {
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
		eventListener = new RendererImplOpenGLEventListenerInstancedArrays();
		drawable.addGLEventListener(eventListener);
	}

	public boolean render(ExternalSettings settings, RenderedFrame result, double time) {
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
				result.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

				result.graphics.setBackground(RendererFactory.backgroundColor);

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
	
	private void renderTokens2GL(GL2 gl, GraphVizTokensIterator tokens, IvMLogFiltered filteredLog,
			TraceColourMap trace2colour, double time, int imgWidth, int imgHeight, AffineTransform userTransform)
			throws Exception {
		pipeLine.gl = gl;
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// use pixel coordinates 
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glPointSize(4f);

		Point2D.Double point = new Point2D.Double(0, 0);
		double radiusGLx = (radius / imgWidth) * 2 * userTransform.getScaleX();
		double radiusGLy = (radius / imgHeight) * 2 * userTransform.getScaleY();

		JoglMeshDataBufferVectord2 bufferFill = new JoglMeshDataBufferVectord2(3);
		JoglMeshDataBufferVectord2 bufferFillUV = new JoglMeshDataBufferVectord2(3);
		Vector<JoglVectord2> vectorFill = new Vector<>();
		Vector<JoglVectord2> vectorFillUV = new Vector<>();
		vectorFill.add(new JoglVectord2(-radiusGLx, radiusGLy));
		vectorFill.add(new JoglVectord2(radiusGLx, radiusGLy));
		vectorFill.add(new JoglVectord2(-radiusGLx, -radiusGLy));
		vectorFillUV.add(new JoglVectord2(0, 1));
		vectorFillUV.add(new JoglVectord2(1, 1));
		vectorFillUV.add(new JoglVectord2(0, 0));

		vectorFill.add(new JoglVectord2(radiusGLx, radiusGLy));
		vectorFill.add(new JoglVectord2(radiusGLx, -radiusGLy));
		vectorFill.add(new JoglVectord2(-radiusGLx, -radiusGLy));
		vectorFillUV.add(new JoglVectord2(1, 1));
		vectorFillUV.add(new JoglVectord2(1, 0));
		vectorFillUV.add(new JoglVectord2(0, 0));
		bufferFill.add(vectorFill);
		bufferFillUV.add(vectorFillUV);
		JoglMeshIndexBuffer indexBufferFill = JoglMeshIndexBuffer.GenerateIndicesTri(6);

		JoglVertexArrayObject vao = new JoglVertexArrayObject(pipeLine);
		vao.Bind(pipeLine);
		vao.AttachBuffer(bufferFill.getVAOBuffer(pipeLine));
		vao.AttachBuffer(bufferFillUV.getVAOBuffer(pipeLine));
		vao.AttachIndexBuffer(indexBufferFill.getVAOBuffer(pipeLine));
		vao.UpdateBufferAttachment(pipeLine);
		vao.Unbind(pipeLine);

		shader = new JoglShader("/org/processmining/plugins/inductiveVisualMiner/animation/renderingthread",
				"vaoOtherShaderVS", "vaoOtherShaderFS");
		shader.Create(pipeLine);
		shader.Bind(pipeLine);
		shader.SetUniform(pipeLine, "fillColourInner", new JoglVectord3(1, 1, 1));
		shader.SetUniform(pipeLine, "strokeColour", new JoglVectord3(0, 0, 0));
		shader.SetUniform(pipeLine, "opacity", 1);

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		vao.Bind(pipeLine);

		Color previousFillColour = null;
		double previousOpacity = -1;
		tokens.itInit(time);
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

				//transform to OpenGL coordinates
				//point.x = (point.x / imgWidth) * 2 - 1;
				//point.y = (-point.y / imgHeight) * 2 + 1;
				if (point.getX() + radiusGLx >= -1 && point.getX() - radiusGLx <= 1 && point.getY() - radiusGLy <= 1
						&& point.getY() + radiusGLy >= -1) {

					//set fill colour
					Color fillColourNew;
					if (trace2colour != null) {
						fillColourNew = trace2colour.getColour(tokens.itGetTraceIndex());
					} else {
						fillColourNew = RendererFactory.defaultTokenFillColour;
					}
					if (fillColourNew != previousFillColour) {
						previousFillColour = fillColourNew;
						shader.SetUniform(pipeLine, "fillColourOuter",
								new JoglVectord3(previousFillColour.getRed() / 255f,
										previousFillColour.getGreen() / 255f, previousFillColour.getBlue() / 255f));
					}

					//set opacity
					if (tokens.itGetOpacity() != previousOpacity) {
						shader.SetUniform(pipeLine, "opacity", (float) tokens.itGetOpacity());
						previousOpacity = tokens.itGetOpacity();
					}

					//paint the token
					gl.glPushMatrix();
					gl.glTranslated(point.x, point.y, 0);
					gl.glDrawArrays(GL2.GL_TRIANGLES, 0, 6);
					gl.glPopMatrix();
				}
			}
		}
		vao.Unbind(pipeLine);
		shader.Unbind(pipeLine);
		gl.glDisable(GL.GL_BLEND);
	}
}
