package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

import java.awt.geom.Point2D;

import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.visualisation3d.GraphicsPipeline;
import org.processmining.visualisation3d.gldatastructures.JoglShader;
import org.processmining.visualisation3d.maths.JoglVectord2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

public class RendererImplOpenGLEventListenerInstances implements GLEventListener {

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
			double[][] translations = new double[100][2];
			int index = 0;
			float offset = 0.1f;
			for (int y = -10; y < 10; y += 2) {
				for (int x = -10; x < 10; x += 2) {
					translations[index][0] = x / 10.0f + offset;
					translations[index][1] = y / 10.0f + offset;
					index++;
				}
			}
			
//			for(GLuint i = 0; i < 100; i++)
//			{
//			    stringstream ss;
//			    string index;
//			    ss << i; 
//			    index = ss.str(); 
//			    GLint location = glGetUniformLocation(shader.Program, ("offsets[" + index + "]").c_str())
//			    glUniform2f(location, translations[i].x, translations[i].y);
//			}
		}

		//set up the shaders
		{
			shader = new JoglShader("/org/processmining/plugins/inductiveVisualMiner/animation/renderingthread",
					"vaoOtherShaderVSinstances", "vaoOtherShaderFSinstances");
			shader.Create(pipeLine);
			shader.Bind(pipeLine);
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

			float[] quadVertices = {
					// Positions     // Colors
					-0.05f, 0.05f, 1.0f, 0.0f, 0.0f, //
					0.05f, -0.05f, 0.0f, 1.0f, 0.0f, //
					-0.05f, -0.05f, 0.0f, 0.0f, 1.0f, //

					-0.05f, 0.05f, 1.0f, 0.0f, 0.0f, //
					0.05f, -0.05f, 0.0f, 1.0f, 0.0f, //   
					0.05f, 0.05f, 0.0f, 1.0f, 1.0f //	    		
			};

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
