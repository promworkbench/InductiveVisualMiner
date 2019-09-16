package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BoxLayout;
import javax.swing.Scrollable;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;

public class DataAnalysisAttributesPanel extends IvMPanel implements Scrollable {
	private static final long serialVersionUID = 8311080909592746520L;

	public DataAnalysisAttributesPanel() {
		setOpaque(true);
		setBackground(IvMDecorator.backGroundColour1);
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

	public void setDataAnalysis(DataAnalysis dataAnalysis) {
		for (Component component : this.getComponents()) {
			if (component instanceof DataAnalysisAttributeView) {
				DataAnalysisAttributeView attributeView = (DataAnalysisAttributeView) component;
				attributeView.set(dataAnalysis);
			}
		}
	}
}