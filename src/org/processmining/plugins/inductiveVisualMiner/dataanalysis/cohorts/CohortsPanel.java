package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BoxLayout;
import javax.swing.Scrollable;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMPanel;

public class CohortsPanel extends IvMPanel implements Scrollable {

	private static final long serialVersionUID = -7438236924200705368L;

	public CohortsPanel(IvMDecoratorI decorator) {
		super(decorator);
		setOpaque(true);
		setBackground(decorator.backGroundColour1());
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 10;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 10;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
