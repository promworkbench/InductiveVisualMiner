package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.listeners.ImageTransformationChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.RenderingThread;
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
	private BufferedImage animationImage = null; //this is the lastly rendered animation image
	RenderingThread renderingThread;

	public InductiveVisualMinerGraphPanel() {
		super(getSplashScreen());

		renderingThread = new RenderingThread(0, 180, new Runnable() {

			//set up callbak for animation frame complete
			public void run() {
				repaint();
			}
		});
		renderingThread.start();

		//set up listener for image transformation (zooming, panning, resizing) changes
		setImageTransformationChangedListener(new ImageTransformationChangedListener() {
			public void imageTransformationChanged(AffineTransform image2user, AffineTransform user2image) {
				renderingThread.setImageTransformation(image2user);
			}
		});

		//set up listener for resizing
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				renderingThread.setSize(getWidth(), getHeight());
			}
		});
	}

	boolean initialised = false;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (renderingThread.getLastRenderedImage() != null && isAnimationEnabled()) {
			g.drawImage(renderingThread.getLastRenderedImage(), 0, 0, null);
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
		renderingThread.setTokens(tokens);
	}

	public void setFilteredLog(IvMLogFiltered filteredLog) {
		renderingThread.setFilteredLog(filteredLog);
	}
}
