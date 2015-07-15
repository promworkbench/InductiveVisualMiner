package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken.GraphVizTokens;

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
	private List<String> popup = null;
	private static int popupWidth = 300;

	//animation
	private GraphVizTokens tokens = null;

	public InductiveVisualMinerGraphPanel() {
		super(getSplashScreen());
	}

	int debugUserTime = 0;

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		paintTokens((Graphics2D) g, debugUserTime);
		debugUserTime = (debugUserTime + 1 % 100);

		//draw a pop-up if the mouse is over a node
		if (showPopup && popup != null) {
			Color backupColour = g.getColor();
			Font backupFont = g.getFont();

			int x = getWidth() - (25 + popupWidth);
			int y = getHeight() - (popup.size() * 20 - 10);

			//background
			g.setColor(new Color(0, 0, 0, 180));
			g.fillRoundRect(x - 15, y - 20, popupWidth, helperControlsShortcuts.size() * 20 + 20, 10, 10);

			//text
			g.setColor(new Color(255, 255, 255, 220));
			g.setFont(helperControlsFont);
			for (int i = 0; i < popup.size(); i++) {
				g.drawString(popup.get(i), x, y);
				y += 20;
			}

			//revert colour and font
			g.setColor(backupColour);
			g.setFont(backupFont);
		}
	}

	public void paintTokens(Graphics2D g, double time) {
		if (tokens != null) {
			Color backupColour = g.getColor();

			g.setColor(Color.green);
			for (int tokenIndex : tokens.getTokensAtTime(time)) {
				paintToken(g, time, tokenIndex);
			}

			g.setColor(backupColour);
		}
	}

	public void paintToken(Graphics2D g, double time, int tokenIndex) {
		//normalise how far we are on the path to [0..1]
		double t = (time - tokens.getStart(tokenIndex)) / (tokens.getEnd(tokenIndex) - tokens.getStart(tokenIndex));

		//ask for the point
		Point2D.Double point = tokens.eval(tokenIndex, t);
		int x = (int) Math.round(point.getX());
		int y = (int) Math.round(point.getY());
		
		g.fillRect(x, y, x + 10, y + 10);
	}

	public void setPopup(List<String> popup) {
		this.popup = popup;
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
	}
}
