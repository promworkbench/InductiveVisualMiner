package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Timer;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.Transformation;
import org.processmining.plugins.graphviz.visualisation.ZoomPan;
import org.processmining.plugins.graphviz.visualisation.ZoomPanState;
import org.processmining.plugins.graphviz.visualisation.listeners.ZoomPanChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationRenderer;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.RenderingThread;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

/**
 * This class takes care of the node popups and render an animation
 * 
 * @author sleemans
 *
 */
public class InductiveVisualMinerGraphPanel extends DotPanel {

	private static final long serialVersionUID = 5688379065627860575L;

	//popup
	private boolean showPopup = false;
	private List<String> popupText = null;
	public static final int popupWidth = 300;

	//animation
	private GraphVizTokens tokens = null;
	private IvMLogFiltered filteredLog = null;
	private BufferedImage animationImage = null; //this is the lastly rendered animation image
	RenderingThread renderingThread;

	private AnimationRenderer animationRenderer = new AnimationRenderer(
			new InputFunction<Pair<Double, BufferedImage>>() {
				public void call(Pair<Double, BufferedImage> result) throws Exception {
					animationFrameComplete(result.getA(), result.getB());
				}
			});
	private Runnable onAnimationCompleted = null;
	private InputFunction<Double> onAnimationTimeOut = null;

	//animation buffer
	private Action timeStepAction2 = new AbstractAction() {
		private static final long serialVersionUID = -7525967531724409532L;

		public void actionPerformed(ActionEvent arg0) {

			if (isAnimationPlaying()) {
				renderAnimationFrame();
			}
		}
	};
	private Timer animationTimer2 = new Timer(30, timeStepAction2);

	public InductiveVisualMinerGraphPanel() {
		super(getSplashScreen());
		animationTimer2.start();

		renderingThread = new RenderingThread(0, 180, 0, 0);

		//set up listener for zooming and panning
		setZoomPanChangedListener(new ZoomPanChangedListener() {
			public void zoomPanChanged(ZoomPanState zoomPanState) {
				//if we are panning, we need to rerender.
				if (isAnimationEnabled()) {
					animationImage = null;
					animationRenderer.cancelAsynchronousRendering();
					if (isAnimationPlaying()) {
						//if the animation is playing anyway, there's no need to trigger a render.
					} else {
						renderAnimationFrame();
					}
				}
			}
		});
	}

	
	boolean initialised = false;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (!initialised) {
			renderingThread.resizeTo(getWidth(), getHeight());
			renderingThread.start();
			initialised = true;
		}

		if (animationImage != null && isAnimationEnabled()) {
			Rectangle bb = getVisibleImageBoundingBoxInUserCoordinates();
			g.drawImage(animationImage, (int) bb.getX(), (int) bb.getY(), null);
		}

		//draw a pop-up if the mouse is over a node
		if (showPopup && popupText != null) {
			paintPopup((Graphics2D) g);
		}
	};

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

	/**
	 * Render an animation frame.
	 */
	public void renderAnimationFrame() {
		if (tokens != null && filteredLog != null) {
			//paint on it
			Transformation t = ZoomPan.getImage2PanelTransformation(image, panel);
			animationRenderer.paintAsynchronous(tokens, filteredLog, getAnimationCurrentTime(), true,
					isAnimationPlaying(), getVisibleImageBoundingBoxInUserCoordinates(),
					getImageBoundingBoxInUserCoordinates(), t, state.getZoomPanState());
		}
	}

	public void animationFrameComplete(double progress, BufferedImage image) {
		animationImage = image;

		if (progress < 1) {
			if (onAnimationTimeOut != null) {
				try {
					onAnimationTimeOut.call(progress);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			if (onAnimationCompleted != null) {
				onAnimationCompleted.run();
			}
		}

		repaint();
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

	public void setTokens(GraphVizTokens tokens) {
		this.tokens = tokens;
		renderingThread.setTokens(tokens);
	}

	public void setFilteredLog(IvMLogFiltered filteredLog) {
		this.filteredLog = filteredLog;
		renderingThread.setFilteredLog(filteredLog);
	}

	public void setOnAnimationCompleted(Runnable callBack) {
		this.onAnimationCompleted = callBack;
	}

	public void setOnAnimationTimeOut(InputFunction<Double> callBack) {
		this.onAnimationTimeOut = callBack;
	}
}
