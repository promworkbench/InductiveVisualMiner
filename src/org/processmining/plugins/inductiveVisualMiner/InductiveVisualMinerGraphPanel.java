package org.processmining.plugins.inductiveVisualMiner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.graphviztoken.GraphVizTokens.TokenIterator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;

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
	public static final int tokenRadius = 4;
	public static final Color tokenFillColour = Color.yellow;
	public static final Color tokenStrokeColour = Color.black;
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final int maxAnimationDuration = 20; //after spending xx ms in drawing circles, just quit.
	public static final int maxAnimationPausedDuration = 500; //after spending xx ms in drawing circles, just quit.

	private Runnable onAnimationCompleted = null;
	private InputFunction<Double> onAnimationTimeOut = null;

	public InductiveVisualMinerGraphPanel() {
		super(getSplashScreen());
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
	public void paintImage(Graphics2D g) {
		super.paintImage(g);

		if (isEnableAnimation() && tokens != null) {

			//paint tokens
			int tokensPainted = paintTokens(g, getAnimationCurrentTime());

			//report whether we finished on time
			if (tokensPainted < tokens.size()) {
				double progress = tokensPainted / (tokens.size() * 1.0);
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
		}
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
	 * Paints tokens
	 * 
	 * @param g
	 * @param time
	 * @return the next token that will be painted
	 */
	public int paintTokens(Graphics2D g, double time) {
		int result = tokens.size();

		long startTime = System.currentTimeMillis();
		long nowTime;

		Color backupColour = g.getColor();
		Stroke backupStroke = g.getStroke();

		g.setStroke(tokenStroke);

		TokenIterator it = tokens.getTokensAtTime(time);
		while (it.hasNext()) {
			int tokenIndex = it.next();
			paintToken(g, time, tokenIndex);

			nowTime = System.currentTimeMillis() - startTime;
			if ((animationPlaying() && nowTime > maxAnimationDuration) || nowTime > maxAnimationPausedDuration) {
				result = it.getPosition();
				break;
			}
		}

		g.setColor(backupColour);
		g.setStroke(backupStroke);

		return result;
	}

	public void paintToken(Graphics2D g, double time, int tokenIndex) {
		//ask for the point
		Triple<Double, Double, Double> point = tokens.eval(tokenIndex, time);

		g.transform(tokens.getTransform(tokenIndex));

		g.translate(point.getA(), point.getB());

		//draw the oval
		if (point.getC() == 1) {
			g.setPaint(tokenFillColour);
		} else {
			g.setPaint(new Color(tokenFillColour.getRed(), tokenFillColour.getGreen(), tokenFillColour.getBlue(),
					(int) Math.round(point.getC() * 255)));
		}
		g.fillOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

		//draw the fill
		if (point.getC() == 1) {
			g.setColor(tokenStrokeColour);
		} else {
			g.setColor(new Color(tokenStrokeColour.getRed(), tokenStrokeColour.getGreen(), tokenStrokeColour.getBlue(),
					(int) Math.round(point.getC() * 255)));
		}
		g.drawOval(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);
		//		g.drawRect(-tokenRadius, -tokenRadius, tokenRadius * 2, tokenRadius * 2);

		g.translate(-point.getA(), -point.getB());

		g.transform(tokens.getTransformInverse(tokenIndex));
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
	}

	public void setOnAnimationCompleted(Runnable callBack) {
		this.onAnimationCompleted = callBack;
	}

	public void setOnAnimationTimeOut(InputFunction<Double> callBack) {
		this.onAnimationTimeOut = callBack;
	}
}
