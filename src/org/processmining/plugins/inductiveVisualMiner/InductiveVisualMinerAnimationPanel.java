package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.List;
import java.util.ListIterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.graphviz.visualisation.listeners.ImageTransformationChangedListener;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationEnabledChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.AnimationTimeChangedListener;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokensLazyIterator;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.ExternalSettingsManager.ExternalSettings;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderedFrameManager.RenderedFrame;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RendererImplBasic;
import org.processmining.plugins.inductiveVisualMiner.animation.renderingthread.RenderingThread;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AboutMessage;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.popup.HistogramData;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;

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
	private int popupWidth = 10;
	private List<String> popupText = null;
	private int popupHistogramNode = -1;
	private long popupHistogramEdge = -1;
	private boolean popupHistogramLog = false;
	public static final int popupRowHeight = 20;
	public static final int popupEmptyRowHeight = 8;
	public static final int popupPadding = 10;
	public static final int popupRightMargin = 40;
	public static final int popupHistogramHeight = 120;
	public static final int popupHistogramYPadding = 10;

	//animation
	protected boolean animationEnabled = false;
	protected RenderingThread renderingThread;
	private AnimationTimeChangedListener animationTimeChangedListener = null;
	private AnimationEnabledChangedListener animationEnabledChangedListener = null;
	public static final String animationGlobalEnabledTrue = "disable animation";
	public static final String animationGlobalEnabledFalse = "enable animation";

	//histogram
	private HistogramData histogramData = null;

	//exporters
	private GetExporters getExporters = null;

	public InductiveVisualMinerAnimationPanel(ProMCanceller canceller, boolean animationGlobalEnabled) {
		super(getSplashScreen());

		setOpaque(true);
		setBackground(Color.white);

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
		}, canceller);

		//control the starting of the animation initially
		renderingThread.start();

		//listen to ctrl+e for a change in enabledness of the animation
		Action animationEnabledChanged = new AbstractAction() {
			private static final long serialVersionUID = -8480930137301467220L;

			public void actionPerformed(ActionEvent e) {
				if (animationEnabledChangedListener != null) {
					if (animationEnabledChangedListener.animationEnabledChanged()) {
						for (ListIterator<String> it = helperControlsExplanations.listIterator(); it.hasNext();) {
							if (it.next().equals(animationGlobalEnabledFalse)) {
								it.remove();
								it.add(animationGlobalEnabledTrue);
							}
						}
					} else {
						for (ListIterator<String> it = helperControlsExplanations.listIterator(); it.hasNext();) {
							if (it.next().equals(animationGlobalEnabledTrue)) {
								it.remove();
								it.add(animationGlobalEnabledFalse);
							}
						}
					}
				}
			}
		};
		helperControlsShortcuts.add("ctrl e");
		helperControlsExplanations
				.add(animationGlobalEnabled ? animationGlobalEnabledTrue : animationGlobalEnabledFalse);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK),
				"changeInitialAnimation");
		getActionMap().put("changeInitialAnimation", animationEnabledChanged);

		//set up listener for about message (ctrl a)
		Action aboutChanged = new AbstractAction() {
			private static final long serialVersionUID = -8518809641877095503L;

			public void actionPerformed(ActionEvent e) {
				AboutMessage.show(InductiveVisualMinerAnimationPanel.this);
			}
		};
		helperControlsShortcuts.add("ctrl a");
		helperControlsExplanations.add("about");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK),
				"showAbout");
		getActionMap().put("showAbout", aboutChanged);

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
				if (getWidth() > 0 && getHeight() > 0) {
					renderingThread.getExternalSettingsManager().setSize(getWidth(), getHeight());
					renderingThread.renderOneFrame();
				} else {
					renderingThread.pause();
				}
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		//draw a pop-up if the mouse is over a node
		if (showPopup && popupText != null && !isPaintingForPrint()) {
			paintPopup((Graphics2D) g);
		}

		//draw the histogram
		if (isAnimationControlsShowing() && histogramData != null) {
			paintGlobalHistogram((Graphics2D) g);
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
			if (settings.tokens != null && animationEnabled) {
				double time = renderingThread.getTimeManager().getLastRenderedTime();
				GraphVizTokensIterator tokens = new GraphVizTokensLazyIterator(settings.tokens);
				RendererImplBasic.renderTokens(g, tokens, settings.filteredLog, settings.trace2colour, time,
						Integer.MAX_VALUE, Integer.MAX_VALUE, new AffineTransform());

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

		int currentPopupHistogramHeight = histogramData == null
				|| (popupHistogramNode == -1 && popupHistogramEdge == -1 && !popupHistogramLog) ? 0
						: popupHistogramHeight;

		//count the height of the popup
		int popupHeight = currentPopupHistogramHeight;
		for (String line : popupText) {
			if (line != null) {
				popupHeight += popupRowHeight;
			} else {
				popupHeight += popupEmptyRowHeight;
			}
		}

		int x = getWidth() - (popupRightMargin + popupWidth + popupPadding);
		int y = getHeight() - popupHeight - popupPadding;

		//background
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRoundRect(x - popupPadding, y - popupPadding, popupWidth + 2 * popupPadding,
				popupHeight + 2 * popupPadding + popupPadding, popupPadding, popupPadding);

		//local (= unode) histogram
		if (histogramData != null && (popupHistogramNode != -1 || popupHistogramEdge != -1 || popupHistogramLog)) {
			paintPopupHistogram(g, x, y);
		}

		y += currentPopupHistogramHeight;

		//text
		g.setColor(new Color(255, 255, 255, 220));
		g.setFont(helperControlsFont);
		for (String line : popupText) {
			if (line != null) {
				y += popupRowHeight;
				g.drawString(line, x, y);
			} else {
				y += popupEmptyRowHeight;
			}
		}

		//revert colour and font
		g.setColor(backupColour);
		g.setFont(backupFont);
	}

	public void paintPopupHistogram(Graphics2D g, int offsetX, int offsetY) {
		Color backupColour = g.getColor();
		Font backupFont = g.getFont();

		//border
		g.setColor(new Color(255, 255, 255, 50));
		g.drawRect(offsetX, offsetY + popupHistogramYPadding, popupWidth,
				popupHistogramHeight - 2 * popupHistogramYPadding);

		//text
		g.setColor(new Color(255, 255, 255, 150));
		g.setFont(helperControlsFont);
		double casesPerMs = histogramData.getLocalMaximum() / histogramData.getLogTimeInMsPerLocalBucket();
		g.drawString(ResourceTimeUtils.getTimePerUnitString(casesPerMs, "executions"), offsetX + 1,
				offsetY + popupHistogramYPadding - 1);

		//text bottom
		g.drawString("0" + ResourceTimeUtils.getTimeUnitWithoutMeasure(casesPerMs, "executions"), offsetX + 1,
				offsetY + popupHistogramHeight);

		//histogram itself
		{
			//create a path
			GeneralPath path = new GeneralPath();
			path.moveTo(offsetX, offsetY + (popupHistogramHeight - popupHistogramYPadding));
			int buckets = popupHistogramLog ? histogramData.getNrOfLogBuckets() : histogramData.getNrOfLocalBuckets();
			for (int pixel = 0; pixel < buckets; pixel++) {
				path.lineTo(offsetX + pixel, offsetY + (popupHistogramHeight - popupHistogramYPadding)
						- (getLocalBucketFraction(pixel) * (popupHistogramHeight - 2 * popupHistogramYPadding)));
			}
			path.lineTo(offsetX + popupWidth, offsetY + (popupHistogramHeight - popupHistogramYPadding));
			path.closePath();

			//draw path
			g.setColor(new Color(255, 255, 255, 150));
			g.draw(path);
			g.setColor(new Color(255, 255, 255, 50));
			g.fill(path);
		}

		g.setColor(backupColour);
		g.setFont(backupFont);
	}

	private double getLocalBucketFraction(int pixel) {
		if (popupHistogramNode >= 0) {
			return histogramData.getLocalNodeBucketFraction(popupHistogramNode, pixel);
		} else if (popupHistogramEdge >= 0) {
			return histogramData.getLocalEdgeBucketFraction(popupHistogramEdge, pixel);
		} else {
			return histogramData.getLogBucketFraction(pixel);
		}
	}

	public void paintGlobalHistogram(Graphics2D g) {
		Color backupColour = g.getColor();

		double height = getControlsProgressLine().getHeight();
		double offsetX = getControlsProgressLine().getX();
		double offsetY = getControlsProgressLine().getMaxY();
		double width = getControlsProgressLine().getWidth();

		GeneralPath path = new GeneralPath();

		path.moveTo(offsetX, offsetY);
		for (int pixel = 0; pixel < histogramData.getNrOfGlobalBuckets(); pixel++) {
			path.lineTo(offsetX + pixel, offsetY - histogramData.getGlobalBucketFraction(pixel) * height);
		}
		path.lineTo(offsetX + width, offsetY);
		path.closePath();

		g.setColor(new Color(255, 255, 255, 150));
		g.draw(path);
		g.setColor(new Color(255, 255, 255, 50));
		g.fill(path);

		g.setColor(backupColour);
	}

	public void setPopupActivity(List<String> popup, int popupHistogramUnode) {
		this.popupText = popup;
		this.popupHistogramNode = popupHistogramUnode;
		this.popupHistogramEdge = -1;
		this.popupHistogramLog = false;
	}

	public void setPopupLogMove(List<String> popup, LogMovePosition position) {
		this.popupText = popup;
		this.popupHistogramNode = -1;
		this.popupHistogramEdge = HistogramData.getEdgeIndex(position);
		this.popupHistogramLog = false;
	}

	public void setPopupLog(List<String> popup) {
		this.popupText = popup;
		this.popupHistogramNode = -1;
		this.popupHistogramEdge = -1;
		this.popupHistogramLog = true;
	}

	public boolean isShowPopup() {
		return showPopup;
	}

	public void setShowPopup(boolean showPopup, int width) {
		this.showPopup = showPopup;
		this.popupWidth = width;
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
	 * Sets a callback that is called whenever the user changes the animation
	 * enabled-ness.
	 * 
	 * @param animationEnabledChangedListener
	 */
	public void setAnimationEnabledChangedListener(AnimationEnabledChangedListener animationEnabledChangedListener) {
		this.animationEnabledChangedListener = animationEnabledChangedListener;
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

	public void setFilteredLog(IvMLogFilteredImpl ivMLogFiltered) {
		renderingThread.getExternalSettingsManager().setFilteredLog(ivMLogFiltered);
		renderingThread.renderOneFrame();
	}

	public void setTraceColourMap(TraceColourMap trace2colour) {
		renderingThread.getExternalSettingsManager().setTrace2Colour(trace2colour);
		renderingThread.renderOneFrame();
	}

	public void setHistogramData(HistogramData histogramData) {
		this.histogramData = histogramData;
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
		//		RenderedFrame frame = renderingThread.getRenderedFrameManager().getLastRenderedFrame();
		//		if (frame != null) {
		//			return frame.time;
		//		}
		return renderingThread.getTimeManager().getLastRenderedTime();
		//return -1;
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

	@Override
	public void setTimeScale(double timeScale) {
		renderingThread.setTimeScale(timeScale);
	}

	@Override
	public double getTimeScale() {
		return renderingThread.getTimeScale();
	}

	@Override
	public List<Exporter> getExporters() {
		List<Exporter> exporters = super.getExporters();
		if (getExporters != null) {
			exporters = getExporters.getExporters(exporters);
		}
		return exporters;
	}

	public void addAnimationEnabledChangedListener(AnimationEnabledChangedListener animationEnabledChangedListener) {
		this.animationEnabledChangedListener = animationEnabledChangedListener;
	}

	public GetExporters getGetExporters() {
		return getExporters;
	}

	public void setGetExporters(GetExporters getExporters) {
		this.getExporters = getExporters;
	}
}
