package org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class IvMPanel extends JPanel {
	/**
	 * 
	 */
	private final IvMDecoratorI ivMDecorator;
	private static final long serialVersionUID = 4780045831156679205L;

	public IvMPanel(IvMDecoratorI ivMDecorator) {
		this.ivMDecorator = ivMDecorator;
		setOpaque(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		GradientPaint gp = getGradient();
		if (gp != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setPaint(gp);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}

		super.paintComponent(g);
	}

	protected GradientPaint getGradient() {
		return ivMDecorator.getGradient(getHeight());
	}
}