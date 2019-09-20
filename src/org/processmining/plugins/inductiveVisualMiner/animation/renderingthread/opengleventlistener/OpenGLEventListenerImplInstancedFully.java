package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.opengleventlistener;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererFactory;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.visualisation3d.GraphicsPipeline;
import org.processmining.visualisation3d.gldatastructures.JoglShader;
import org.processmining.visualisation3d.maths.JoglVectord2;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.kitfox.svg.animation.Bezier;

/**
 * This class computes the location of all tokens on the gpu, but sends all
 * bezier curves again for every token. Supports highlighting, but retransmits
 * everything to the gpu when changing the filtered log.
 * 
 * @author sander
 *
 */

public class OpenGLEventListenerImplInstancedFully implements OpenGLEventListener {

	private GraphicsPipeline pipeLine = new GraphicsPipeline();
	private JoglShader shader = null;

	private ExternalSettings settings;
	private GraphVizTokens tokens;
	private int countTokens;
	private double time;

	private IntBuffer vertexArrayObject;
	private IntBuffer vertexBufferObject;
	private IntBuffer instanceVertexBufferObject;
	private TraceColourMap trace2colour;
	private IvMLogFiltered filteredLog;

	public void init(GLAutoDrawable drawable) {
		drawable.getContext().makeCurrent();
		GL2 gl = drawable.getGL().getGL2();

		pipeLine.gl = gl;

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_CULL_FACE);

		//set up the shaders

		String vertexShader;
		String fragmentShader;
		if (RendererFactory.christmas) {
			vertexShader = "vaoOtherShaderVSinstancedFully";
			fragmentShader = "vaoOtherShaderFSinstancedFullyChristmas";
		} else {
			vertexShader = "vaoOtherShaderVSinstancedFully";
			fragmentShader = "vaoOtherShaderFSinstancedFully";
		}

		shader = new JoglShader(
				"/org/processmining/plugins/inductiveVisualMiner/animation/renderingthread/opengleventlistener",
				vertexShader, fragmentShader, false) {
			@Override
			public void linkShader(GL2 gl, ShaderProgram sp0) {
				getShaderState().attachShaderProgram(gl, sp0, false);
				getShaderState().bindAttribLocation(gl, 0, "position");
				getShaderState().bindAttribLocation(gl, 1, "timeBounds");
				getShaderState().bindAttribLocation(gl, 2, "opacityBounds");
				getShaderState().bindAttribLocation(gl, 3, "colour");
				getShaderState().bindAttribLocation(gl, 4, "bezier0");
				getShaderState().bindAttribLocation(gl, 5, "bezier1");
				getShaderState().bindAttribLocation(gl, 6, "bezier2");
				getShaderState().bindAttribLocation(gl, 7, "bezier3");
				getShaderState().shaderProgram().link(gl, System.err);
			}
		};

		shader.Create(pipeLine);
		shader.Bind(pipeLine);

		//set up quad to draw the circles on later
		{

			//define the basic quad
			float[] quadVertices = { //
					//position
					-1f, -1f, // Left  
					1f, -1f, // Right 
					-1f, 1f, // Top   
					1f, 1f, // Top
			};
			FloatBuffer quadVerticesBuffer = FloatBuffer.wrap(quadVertices);

			//define the instance array
			FloatBuffer instanceArray = FloatBuffer.allocate(15);
			instanceArray.put(0); //start time
			instanceArray.put(0); //end time
			instanceArray.put(0); //start opacity
			instanceArray.put(0); //end opacity
			instanceArray.put(0); //red
			instanceArray.put(0); //green
			instanceArray.put(0); //blue
			instanceArray.put(0); //bezier point 0 x
			instanceArray.put(0); //bezier point 0 y
			instanceArray.put(0); //...
			instanceArray.put(0);
			instanceArray.put(0);
			instanceArray.put(0);
			instanceArray.rewind();

			//set up a vertex array object
			vertexArrayObject = IntBuffer.allocate(1);
			gl.glGenVertexArrays(1, vertexArrayObject);

			//create a vertex buffer
			vertexBufferObject = IntBuffer.allocate(1);
			gl.glGenBuffers(1, vertexBufferObject);

			//create an instanced array
			instanceVertexBufferObject = IntBuffer.allocate(1);
			gl.glGenBuffers(1, instanceVertexBufferObject);

			//bind the vertex array
			gl.glBindVertexArray(vertexArrayObject.get(0));
			{
				//bind and send the vertex buffer in the vertex array
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBufferObject.get(0));
				gl.glBufferData(GL2.GL_ARRAY_BUFFER, quadVertices.length * 4, quadVerticesBuffer, GL2.GL_STATIC_DRAW);

				//tell the structure of the float buffer
				gl.glEnableVertexAttribArray(0);
				gl.glVertexAttribPointer(0, 2, GL2.GL_FLOAT, false, 0, 0); //position

				//bind and send the instanced array
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, instanceVertexBufferObject.get(0));
				gl.glBufferData(GL.GL_ARRAY_BUFFER, 10 * 4, instanceArray, GL.GL_STATIC_DRAW);

				//tell the structure of the instanced array
				gl.glEnableVertexAttribArray(1);
				gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 15 * 4, 0); //time

				gl.glEnableVertexAttribArray(2);
				gl.glVertexAttribPointer(2, 2, GL.GL_FLOAT, false, 15 * 4, 2 * 4); //opacity

				gl.glEnableVertexAttribArray(3);
				gl.glVertexAttribPointer(3, 3, GL.GL_FLOAT, false, 15 * 4, 4 * 4); //colour

				gl.glEnableVertexAttribArray(4);
				gl.glVertexAttribPointer(4, 2, GL.GL_FLOAT, false, 15 * 4, 7 * 4); //bezier point 0

				gl.glEnableVertexAttribArray(5);
				gl.glVertexAttribPointer(5, 2, GL.GL_FLOAT, false, 15 * 4, 9 * 4); //bezier point 1

				gl.glEnableVertexAttribArray(6);
				gl.glVertexAttribPointer(6, 2, GL.GL_FLOAT, false, 15 * 4, 11 * 4); //bezier point 2

				gl.glEnableVertexAttribArray(7);
				gl.glVertexAttribPointer(7, 2, GL.GL_FLOAT, false, 15 * 4, 13 * 4); //bezier point 3

				//unbind the vertex buffer
				gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

				gl.glVertexAttribDivisor(1, 1);
				gl.glVertexAttribDivisor(2, 1);
				gl.glVertexAttribDivisor(3, 1);
				gl.glVertexAttribDivisor(4, 1);
				gl.glVertexAttribDivisor(5, 1);
				gl.glVertexAttribDivisor(6, 1);
				gl.glVertexAttribDivisor(7, 1);
			}

			//unbind the vertex array
			gl.glBindVertexArray(0);
		}
	}

	public void dispose(GLAutoDrawable drawable) {
		drawable.getGL().getGL2().glDeleteVertexArrays(vertexArrayObject.limit(), vertexArrayObject);
		drawable.getGL().glDeleteBuffers(instanceVertexBufferObject.limit(), instanceVertexBufferObject);
		drawable.getGL().glDeleteBuffers(vertexBufferObject.limit(), vertexBufferObject);

		shader.Unbind(pipeLine);
		shader.Dispose(pipeLine);
	}

	public void display(GLAutoDrawable drawable) {
		if (settings != null && settings.filteredLog != null && settings.tokens != null && settings.transform != null) {
			GL2 gl = drawable.getGL().getGL2();

			if (!settings.tokens.getTokens().equals(tokens) || !settings.trace2colour.equals(trace2colour)
					|| !settings.filteredLog.equals(filteredLog)) {
				tokens = settings.tokens.getTokens();
				trace2colour = settings.trace2colour;
				filteredLog = settings.filteredLog;

				//put the highlighted tokens in a buffer
				FloatBuffer tokenBuffer = FloatBuffer.allocate(tokens.size() * 15);
				{
					countTokens = 0;
					for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {

						//only include tokens that have survived the highlighting filters
						if (!settings.filteredLog.isFilteredOut(tokens.getTraceIndex(tokenIndex))) {
							//token-specific
							tokenBuffer.put((float) tokens.getStartTime(tokenIndex));
							tokenBuffer.put((float) tokens.getEndTime(tokenIndex));

							//bezier-specific
							int bezierIndex = tokens.getBezierIndex(tokenIndex);
							int traceIndex = tokens.getTraceIndex(tokenIndex);
							tokenBuffer.put((float) tokens.getBeziers().getStartOpacity(bezierIndex));
							tokenBuffer.put((float) tokens.getBeziers().getEndOpacity(bezierIndex));
							tokenBuffer.put(settings.trace2colour.getColour(traceIndex).getRed() / 256.0f);
							tokenBuffer.put(settings.trace2colour.getColour(traceIndex).getGreen() / 256.0f);
							tokenBuffer.put(settings.trace2colour.getColour(traceIndex).getBlue() / 256.0f);
							Bezier bezier = tokens.getBeziers().getBezier(bezierIndex);
							double[] coords = bezier.getCoord();
							assert (coords.length <= 8);
							for (int i = 0; i < 8; i++) {
								if (i < coords.length) {
									tokenBuffer.put((float) coords[i]);
								} else {
									tokenBuffer.put(-10000f);
								}
							}
							countTokens++;
						}
					}
					tokenBuffer.rewind();
				}

				//send the token buffer to OpenGL
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, instanceVertexBufferObject.get(0));
				gl.glBufferData(GL.GL_ARRAY_BUFFER, countTokens * 15 * 4, tokenBuffer, GL.GL_DYNAMIC_DRAW);
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
			}

			//clear the previous drawing
			gl.glClearColor(0f, 0f, 0f, 0f);
			//gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			gl.glLoadIdentity();

			//pass scale and translation to OpenGL
			shader.SetUniform(pipeLine, "userScale",
					new JoglVectord2(settings.transform.getScaleX(), settings.transform.getScaleY()));
			shader.SetUniform(pipeLine, "userTranslate",
					new JoglVectord2(settings.transform.getTranslateX(), settings.transform.getTranslateY()));
			shader.SetUniform(pipeLine, "time", (float) time);

			gl.glBindVertexArray(vertexArrayObject.get(0));
			gl.glDrawArraysInstanced(GL2.GL_TRIANGLE_STRIP, 0, 4, countTokens);
			gl.glBindVertexArray(0);

		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();

		if (height == 0) {
			height = 1; // prevent divide by zero
		}

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		//tell the graphics card
		shader.SetUniform(pipeLine, "imageSize", new JoglVectord2(width, height));

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
	}

	public void setParameters(ExternalSettings settings, double time) {
		this.settings = settings;
		this.time = time;
	}

}
