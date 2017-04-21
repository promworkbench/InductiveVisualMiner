package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.visualisation3d.GraphicsPipeline;
import org.processmining.visualisation3d.gldatastructures.JoglShader;
import org.processmining.visualisation3d.gldatastructures.JoglVertexArrayObject;
import org.processmining.visualisation3d.graphicsdatastructures.JoglMeshDataBufferVectord2;
import org.processmining.visualisation3d.graphicsdatastructures.JoglMeshIndexBuffer;
import org.processmining.visualisation3d.maths.JoglVectord2;
import org.processmining.visualisation3d.maths.JoglVectord3;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

public class RendererImplOpenGLEventListener implements GLEventListener {

	private final GLU glu = new GLU();
	private GraphicsPipeline pipeLine = new GraphicsPipeline();
	private JoglShader shader = null;

	private ExternalSettings settings;
	private double time;
	private Point2D.Double point = new Point2D.Double();
	private int width;
	private int height;

	double radius = 6;

	public void init(GLAutoDrawable drawable) {
		System.out.println(" GL listener init");
		drawable.getContext().makeCurrent();
		GL2 gl = drawable.getGL().getGL2();

		pipeLine.gl = gl;

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		//set up quad to draw the circles on later
		{
			JoglMeshDataBufferVectord2 bufferFill = new JoglMeshDataBufferVectord2(3);
			JoglMeshDataBufferVectord2 bufferFillUV = new JoglMeshDataBufferVectord2(3);
			Vector<JoglVectord2> vectorFill = new Vector<>();
			Vector<JoglVectord2> vectorFillUV = new Vector<>();
			vectorFill.add(new JoglVectord2(-1, 1));
			vectorFill.add(new JoglVectord2(1, 1));
			vectorFill.add(new JoglVectord2(-1, -1));
			vectorFillUV.add(new JoglVectord2(0, 1));
			vectorFillUV.add(new JoglVectord2(1, 1));
			vectorFillUV.add(new JoglVectord2(0, 0));

			vectorFill.add(new JoglVectord2(1, 1));
			vectorFill.add(new JoglVectord2(1, -1));
			vectorFill.add(new JoglVectord2(-1, -1));
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
			//vao.Unbind(pipeLine);
		}

		//set up the shaders
		{
			shader = new JoglShader("/org/processmining/plugins/inductiveVisualMiner/animation/renderingthread",
					"vaoOtherShaderVS", "vaoOtherShaderFS");
			shader.Create(pipeLine);
			shader.Bind(pipeLine);
			shader.SetUniform(pipeLine, "fillColourInner", new JoglVectord3(1, 1, 1));
			shader.SetUniform(pipeLine, "strokeColour", new JoglVectord3(0, 0, 0));
			shader.SetUniform(pipeLine, "opacity", 1);
		}
	}

	public void dispose(GLAutoDrawable drawable) {
		shader.Unbind(pipeLine);
		shader.Dispose(pipeLine);
		System.out.println(" GL listener dispose");
	}

	public void display(GLAutoDrawable drawable) {
		if (settings != null && settings.filteredLog != null && settings.tokens != null && settings.transform != null) {
			GL2 gl = drawable.getGL().getGL2();

			//clear the previous drawing
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			gl.glLoadIdentity();

			//pass the scaling on to the shader
			//			shader.SetUniform(pipeLine, "scaling", new JoglVectord2(((height * 1.0) / width), 1));
			//			shader.SetUniform(pipeLine, "translating", new JoglVectord2(0, 0));

			double radiusGLx = (radius / width) * 2 * settings.transform.getScaleX();
			double radiusGLy = (radius / height) * 2 * settings.transform.getScaleY();

			Color previousFillColour = null;
			double previousOpacity = -1;
			settings.tokens.itInit(time);
			while (settings.tokens.itHasNext()) {
				settings.tokens.itNext();

				//only paint tokens that are not filtered out
				if (settings.filteredLog == null
						|| !settings.filteredLog.isFilteredOut(settings.tokens.itGetTraceIndex())) {
					settings.tokens.itEval();

					//only attempt to draw if the token is in the visible image
					AffineTransform bezierTransform = settings.tokens.itGetTransform();
					point.x = settings.tokens.itGetX();
					point.y = settings.tokens.itGetY();
					bezierTransform.transform(point, point);
					settings.transform.transform(point, point);

					//transform to OpenGL coordinates
					//point.x = (point.x / width);
					//point.y = (-point.y / height);
					//					if (true || point.getX() + radiusGLx >= -1 && point.getX() - radiusGLx <= 1
					//							&& point.getY() - radiusGLy <= 1 && point.getY() + radiusGLy >= -1) {
					if (true) {

						//set fill colour
						Color fillColourNew;
						if (settings.trace2colour != null) {
							fillColourNew = settings.trace2colour.getColour(settings.tokens.itGetTraceIndex());
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
						if (settings.tokens.itGetOpacity() != previousOpacity) {
							shader.SetUniform(pipeLine, "opacity", (float) settings.tokens.itGetOpacity());
							previousOpacity = settings.tokens.itGetOpacity();
						}

						//paint the token
						//gl.glPushMatrix();
						//System.out.println(point);
						//gl.glTranslated(point.x, point.y, 0);
						shader.SetUniform(pipeLine, "x", (float) point.x);
						shader.SetUniform(pipeLine, "y", (float) point.y);
						shader.SetUniform(pipeLine, "imageSize", new JoglVectord2(width, height));
						gl.glDisable(GL2.GL_DEPTH_TEST);
						gl.glDrawArrays(GL2.GL_TRIANGLES, 0, 6);
						//gl.glPopMatrix();
					}
				}
			}
		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		System.out.println(" GL listener reshape");

		GL2 gl = drawable.getGL().getGL2();

		if (height == 0) {
			height = 1; // prevent divide by zero
		}
		double aspectRatio = (double) width / height;
		this.width = width;
		this.height = height;

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		//tell the graphics card
		shader.SetUniform(pipeLine, "imageSize", new JoglVectord2(width, height));

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
		//glu.gluPerspective(45, aspectRatio, 0.1, 100); // fovy, aspect, zNear, zFar
	}

	public void setParameters(ExternalSettings settings, double time) {
		this.settings = settings;
		this.time = time;
	}

}
