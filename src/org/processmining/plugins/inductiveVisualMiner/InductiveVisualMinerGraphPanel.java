package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class InductiveVisualMinerGraphPanel extends DotPanel { 
	
	private static final long serialVersionUID = 5688379065627860575L;

	private boolean showPopup = false;
	private List<String> popup = null;
	private static int popupWidth = 300;
	
	
	public InductiveVisualMinerGraphPanel() {
		super(getSplashScreen());
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
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
}
