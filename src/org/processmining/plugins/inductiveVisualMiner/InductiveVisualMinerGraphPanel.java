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
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.InputFunction;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
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
	public static final int tokenRadius = 4;
	public static final Color tokenFillColour = Color.yellow;
	public static final Color tokenStrokeColour = Color.black;
	public static final Stroke tokenStroke = new BasicStroke(1.5f);
	public static final int maxAnimationDuration = 20; //after spending xx ms in drawing circles, just quit.
	public static final int maxAnimationPausedDuration = 200; //after spending xx ms in drawing circles, just quit.

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
			double progress = paintTokens(g, tokens, filteredLog, getAnimationCurrentTime(), true, animationPlaying());

			//report whether we finished on time
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
	 * @param tokens
	 * @param time
	 * @param timeOutPossible
	 * @param animationPlaying
	 * @return the last token that was painted
	 */
	public static double paintTokens(Graphics2D g, GraphVizTokens tokens, IvMLogFiltered filteredLog, double time,
			boolean timeOutPossible, boolean animationPlaying) {

		long startTime = System.currentTimeMillis();
		long nowTime;
		int countTotal = 0;
		int countPainted = 0;
		boolean stillPainting = true;

		Color backupColour = g.getColor();
		Stroke backupStroke = g.getStroke();

		g.setStroke(tokenStroke);

		IteratorWithPosition<Integer> it = tokens.getTokensAtTime(time);
		while (it.hasNext()) {
			int tokenIndex = it.next();

			//only paint tokens that are not filtered out
			if (filteredLog == null || !filteredLog.isFilteredOut(tokens.getTraceIndex(tokenIndex))) {
				countTotal++;
				
				if (stillPainting) {
					paintToken(g, tokens, time, tokenIndex);
					countPainted++;

					//see if it's already time to stop
					nowTime = System.currentTimeMillis() - startTime;
					stillPainting = !timeOutPossible || !((animationPlaying && nowTime > maxAnimationDuration) || nowTime > maxAnimationPausedDuration);
				}
			}
		}

		g.setColor(backupColour);
		g.setStroke(backupStroke);

		return countPainted / (countTotal * 1.0);
	}

	public static void paintToken(Graphics2D g, GraphVizTokens tokens, double time, int tokenIndex) {
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

	public void setFilteredLog(IvMLogFiltered filteredLog) {
		this.filteredLog = filteredLog;
	}

	public void setOnAnimationCompleted(Runnable callBack) {
		this.onAnimationCompleted = callBack;
	}

	public void setOnAnimationTimeOut(InputFunction<Double> callBack) {
		this.onAnimationTimeOut = callBack;
	}
}
