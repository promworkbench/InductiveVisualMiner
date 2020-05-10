package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class PieChart extends JComponent {

	private static final long serialVersionUID = 678405723242383671L;
	private Dimension size = new Dimension(50, 50);

	private double value = 0.33;
	private Color colourSlice = IvMDecorator.textColour;

	@Override
	public Dimension getMinimumSize() {
		return size;
	}

	@Override
	public Dimension getMaximumSize() {
		return size;
	}

	@Override
	public Dimension getPreferredSize() {
		return size;
	}

	@Override
	public void paint(Graphics g) {
		drawPie((Graphics2D) g, getBounds(), value, colourSlice);
	}

	public static BufferedImage drawPie(int size, double value, Color colourSlice) {
		Rectangle area = new Rectangle(size, size);
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = image.createGraphics();
		drawPie(g, area, value, colourSlice);
		return image;
	}

	public static void drawPie(Graphics2D g, Rectangle area, double value, Color colourSlice) {
		int width = area.width - 2;
		int height = area.height - 2;
		int x = 1;
		int y = 1;

		int startAngle;
		int arcAngle;
		if (value >= 0) {
			startAngle = 90;
			arcAngle = (int) (-value * 360);
		} else {
			startAngle = (int) (90 + (value * 360));
			arcAngle = -(int) (360 * (1 + value));
		}

		g.setColor(Color.white);
		g.drawArc(x, y, width, height, startAngle + arcAngle, -(360 + arcAngle));

		g.setColor(colourSlice);
		g.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public double getValue() {
		return value;
	}

	/**
	 * Should lie between [-1, 1], where positive values indicate a fraction
	 * (painted clockwise), and negative values indicate 1-that fraction
	 * (painted counterclockwise).
	 * 
	 * @param value
	 */
	public void setValue(double value) {
		this.value = value;
	}

	public Color getColourSlice() {
		return colourSlice;
	}

	public void setColourSlice(Color colourSlice) {
		this.colourSlice = colourSlice;
	}
}
