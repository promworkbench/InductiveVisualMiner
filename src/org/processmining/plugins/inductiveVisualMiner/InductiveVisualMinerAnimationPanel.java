package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.List;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.listeners.ImageTransformationChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensLazyIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.Renderer;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderingThread;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

/**
 * This class takes care of the node popups and render an animation
 * 
 * @author sleemans
 * 
 */
public class InductiveVisualMinerAnimationPanel extends DotPanel {

	private static final long serialVersionUID = 5688379065627860575L;

	//popup
	private boolean showPopup = false;
	private List<String> popupText = null;
	public static final int popupWidth = 300;

	//animation
	protected boolean animationEnabled = false;
	RenderingThread renderingThread;
	private AnimationTimeChangedListener animationTimeChangedListener = null;

	public InductiveVisualMinerAnimationPanel() {
		super(getSplashScreen());

		renderingThread = new RenderingThread(0, 180, new Runnable() {

			//set up callback for animation frame complete
			public void run() {
				if (animationTimeChangedListener != null) {
					RenderedFrame lastRenderedFrame = renderingThread.getRenderedFrameManager().getLastRenderedFrame();
					if (lastRenderedFrame != null) {
						animationTimeChangedListener.timeStepTaken(lastRenderedFrame.time);
					}
				}
				repaint();
			}
		});
		renderingThread.start();
		renderingThread.pause();

		//set up listener for image transformation (zooming, panning, resizing) changes
		setImageTransformationChangedListener(new ImageTransformationChangedListener() {
			public void imageTransformationChanged(AffineTransform image2user, AffineTransform user2image) {
				renderingThread.getExternalSettingsManager().setImageTransformation(image2user);
				renderingThread.getRenderedFrameManager().invalidateLastRenderedFrame();
				renderingThread.renderOneFrame();
			}
		});

		//set up listener for resizing
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				//tell the animation thread
				renderingThread.getExternalSettingsManager().setSize(getWidth(), getHeight());
				renderingThread.renderOneFrame();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		//draw a pop-up if the mouse is over a node
		if (showPopup && popupText != null) {
			paintPopup((Graphics2D) g);
		}
	};

	@Override
	protected void drawAnimation(Graphics2D g) {
		if (!isPaintingForPrint()) {
			//draw for screen display (optimised)
			RenderedFrame frame = renderingThread.getRenderedFrameManager().getLastRenderedFrame();

			if (frame != null) {
				frame.startDrawing();
				if (frame.image != null && isAnimationEnabled() && !isDraggingImage) {
					g.drawImage(frame.image, 0, 0, null);
				} else {

				}
				frame.doneDrawing();
			}
		} else {
			//draw for printing (non-optimised)

			ExternalSettings settings = renderingThread.getExternalSettingsManager().getExternalSettings();
			if (settings.tokens != null) {
				double time = renderingThread.getTimeManager().getLastRenderedTime();
				GraphVizTokensIterator tokens = new GraphVizTokensLazyIterator(settings.tokens);
				Renderer.renderTokens(g, tokens, settings.filteredLog, time);
			}
		}

		super.drawAnimation(g);
	}

	@Override
	public void paintImage(Graphics2D g) {
		super.paintImage(g);
	}

	public void paintPopup(Graphics2D g) {
		Color backupColour = g.getColor();
		Font backupFont = g.getFont();

		int x = getWidth() - (25 + popupWidth);
		int y = getHeight() - (popupText.size() * 20 - 10);

		//background
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRoundRect(x - 15, y - 20, popupWidth, helperControlsShortcuts.size() * 20 + 20, 10, 10);

		//text
		g.setColor(new Color(255, 255, 255, 220));
		g.setFont(helperControlsFont);
		for (int i = 0; i < popupText.size(); i++) {
			g.drawString(popupText.get(i), x, y);
			y += 20;
		}

		//revert colour and font
		g.setColor(backupColour);
		g.setFont(backupFont);
	}

	public void setPopup(List<String> popup) {
		this.popupText = popup;
	}

	public boolean isShowPopup() {
		return showPopup;
	}

	public void setShowPopup(boolean showPopup) {
		this.showPopup = showPopup;
	}

	public static Dot getSplashScreen() {
		Dot dot = new Dot();
		dot.addNode("Inductive visual Miner");
		dot.addNode("Mining model...");
		return dot;
	}

	public AnimationTimeChangedListener getAnimationTimeChangedListener() {
		return animationTimeChangedListener;
	}

	/**
	 * Sets a callback that is called whenever the time is updated.
	 * 
	 * @param timeStepCallback
	 */
	public void setAnimationTimeChangedListener(AnimationTimeChangedListener listener) {
		this.animationTimeChangedListener = listener;
	}

	/**
	 * Sets whether the animation is rendered and controls are displayed.
	 * 
	 * @return
	 */
	public void setAnimationEnabled(boolean enabled) {
		animationEnabled = enabled;
	}

	/**
	 * Sets the tokens to be rendered.
	 * 
	 * @param animationGraphVizTokens
	 */
	public void setTokens(GraphVizTokens animationGraphVizTokens) {
		renderingThread.getExternalSettingsManager().setTokens(animationGraphVizTokens);
		renderingThread.renderOneFrame();
	}

	@Override
	public boolean isAnimationEnabled() {
		return animationEnabled;
	}

	/**
	 * Set the extreme times of the animation, in user times.
	 * 
	 * @param animationMinUserTime
	 * @param animationMaxUserTime
	 */
	public void setAnimationExtremeTimes(double animationMinUserTime, double animationMaxUserTime) {
		renderingThread.getTimeManager().setExtremeTimes(animationMinUserTime, animationMaxUserTime);
	}

	public void setFilteredLog(IvMLogFiltered ivMLogFiltered) {
		renderingThread.getExternalSettingsManager().setFilteredLog(ivMLogFiltered);
		renderingThread.renderOneFrame();
	}

	@Override
	public void pause() {
		renderingThread.pause();
	}

	@Override
	public void resume() {
		renderingThread.resume();
	}

	@Override
	public void seek(double time) {
		renderingThread.seek(time);
	}

	@Override
	public void pauseResume() {
		renderingThread.pauseResume();
	}

	@Override
	public boolean isAnimationPlaying() {
		return renderingThread.isPlaying();
	}

	@Override
	public double getAnimationTime() {
		RenderedFrame frame = renderingThread.getRenderedFrameManager().getLastRenderedFrame();
		if (frame != null) {
			return frame.time;
		}
		return -1;
	}

	@Override
	public double getAnimationMinimumTime() {
		return renderingThread.getTimeManager().getMinTime();
	}

	@Override
	public double getAnimationMaximumTime() {
		return renderingThread.getTimeManager().getMaxTime();
	}

	@Override
	public void renderOneFrame() {
		renderingThread.renderOneFrame();
	}
}
