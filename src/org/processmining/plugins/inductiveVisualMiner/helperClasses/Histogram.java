package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.math.plot.utils.Array;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

/**
 * Draw a picture of a histogram
 * 
 * @author sander
 *
 */
public class Histogram {

	public static final int histogramWidth = 100;
	public static final int histogramHeight = 100;
	public static final int offsetX = 10;
	public static final int offsetY = 0;

	public static BufferedImage create(long[] values, IvMDecoratorI decorator) {
		return create(values, null, true, decorator);
	}

	public static BufferedImage create(long[] values, AbstractRealDistribution distribution, IvMDecoratorI decorator) {
		return create(values, distribution, true, decorator);
	}

	public static BufferedImage create(long[] values, boolean startAtZero, IvMDecoratorI decorator) {
		return create(values, null, startAtZero, decorator);
	}

	public static BufferedImage create(double[] values, IvMDecoratorI decorator) {
		return create(values, null, true, decorator);
	}

	public static BufferedImage create(double[] values, AbstractRealDistribution distribution,
			IvMDecoratorI decorator) {
		return create(values, distribution, true, decorator);
	}

	public static BufferedImage create(double[] values, boolean startAtZero, IvMDecoratorI decorator) {
		return create(values, null, startAtZero, decorator);
	}

	public static BufferedImage create(long[] values, AbstractRealDistribution distribution, boolean startAtZero,
			IvMDecoratorI decorator) {

		//get extreme values
		long minValue = Long.MAX_VALUE;
		long maxValue = Long.MIN_VALUE;
		for (long value : values) {
			minValue = Math.min(minValue, value);
			maxValue = Math.max(maxValue, value);
		}

		if (startAtZero) {
			minValue = 0; //let the histogram start at 0 for clarity
		}

		//fill array
		int[] counts = new int[histogramWidth];
		double yscale = 1;
		if (values.length > 0) {

			//fill array
			for (long value : values) {
				int indexX = (int) Math
						.round((histogramWidth - 1) * ((value - minValue) / (maxValue - minValue * 1.0)));
				counts[indexX]++;
			}

			//scale the graph to have the highest peak reach the top of the image
			yscale = 1.0 / (Array.max(counts) / (values.length * 1.0));
		}

		return draw(values.length, distribution, minValue, maxValue, counts, yscale, decorator);
	}

	public static BufferedImage create(double[] values, AbstractRealDistribution distribution, boolean startAtZero,
			IvMDecoratorI decorator) {
		//get extreme values
		double minValue = Double.MAX_VALUE;
		double maxValue = -Double.MAX_VALUE;
		for (double value : values) {
			minValue = Math.min(minValue, value);
			maxValue = Math.max(maxValue, value);
		}

		if (startAtZero) {
			minValue = 0; //let the histogram start at 0 for clarity
		}

		//fill array
		int[] counts = new int[histogramWidth];
		double yscale = 1;
		if (values.length > 0) {

			//fill array
			for (double value : values) {
				int indexX = (int) Math
						.round((histogramWidth - 1) * ((value - minValue) / (maxValue - minValue * 1.0)));
				counts[indexX]++;
			}

			//scale the graph to have the highest peak reach the top of the image
			yscale = 1.0 / (Array.max(counts) / (values.length * 1.0));
		}

		return draw(values.length, distribution, minValue, maxValue, counts, yscale, decorator);
	}

	public static BufferedImage draw(int totalCount, AbstractRealDistribution distribution, double minValue,
			double maxValue, int[] counts, double yscale, IvMDecoratorI decorator) {
		//draw
		//create image
		BufferedImage image = new BufferedImage(histogramWidth + offsetX, histogramHeight + offsetY,
				BufferedImage.TYPE_4BYTE_ABGR);

		//background
		{
			Graphics2D g = (Graphics2D) image.getGraphics();
			g.setColor(decorator.backGroundColour1());
			g.fillRect(offsetX, offsetY, histogramWidth, histogramHeight);
			g.dispose();
		}

		drawText(counts, image);

		Graphics2D g = (Graphics2D) image.getGraphics();

		//draw
		{
			GeneralPath pathOutline = new GeneralPath();
			GeneralPath pathFill = new GeneralPath();
			pathFill.moveTo(offsetX, offsetY + histogramHeight);
			for (int pixel = 0; pixel < histogramWidth; pixel++) {
				double probability = counts[pixel] / (totalCount * 1.0);

				int x = offsetX + pixel;
				int y = (int) Math.round((offsetY + histogramHeight) - probability * yscale * histogramHeight);
				if (pixel == 0) {
					pathOutline.moveTo(x, y);
				} else {
					pathOutline.lineTo(x, y);
				}
				pathFill.lineTo(x, y);
			}
			pathFill.lineTo(offsetX + histogramWidth, offsetY + histogramHeight);
			pathFill.closePath();

			g.setColor(new Color(255, 255, 255, 100));
			g.fill(pathFill);
			g.setColor(new Color(255, 255, 255, 150));
			g.draw(pathOutline);
		}

		//border
		{
			g.setColor(decorator.textColour());
			g.drawLine(offsetX, image.getHeight() - 1, image.getWidth(), image.getHeight() - 1);
			g.drawLine(offsetX, image.getHeight() - 1, offsetX, offsetY);
			//g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
		}

		//distribution
		if (distribution != null) {
			GeneralPath pathOutline = new GeneralPath();
			for (int pixel = 0; pixel < histogramWidth; pixel++) {
				int x = offsetX + pixel;

				double valueXMin = minValue + (pixel - 0.5) * (maxValue - minValue) / histogramWidth;
				double valueXMax = minValue + (pixel + 0.5) * (maxValue - minValue) / histogramWidth;
				double probability = distribution.probability(valueXMin, valueXMax);

				int y = (int) Math.round((offsetY + histogramHeight) - probability * yscale * histogramHeight);
				if (pixel == 0) {
					pathOutline.moveTo(x, y);
				} else {
					pathOutline.lineTo(x, y);
				}
			}
			g.setColor(Color.blue);
			g.draw(pathOutline);
		}

		g.dispose();
		return image;
	}

	public static void drawText(int[] counts, BufferedImage image) {
		Graphics2D g = (Graphics2D) image.getGraphics();

		//labels
		{
			String name = max(counts) + "";
			Font font = IvMDecorator.font;
			g.setFont(font);
			FontMetrics metrics = g.getFontMetrics(font);

			int width = metrics.stringWidth(name);
			int height = ((metrics.getAscent() + metrics.getDescent()) / 2);

			g.setColor(IvMDecorator.textColour);
			//			g.drawRect(-2, -2, 4, 4);

			g.rotate(-Math.PI / 2, 0, 0);
			//			g.drawRect(-2, -2, 4, 4);

			g.translate(-width, height + 1);
			//			g.drawRect(-2, -2, 4, 4);
			g.drawString(name, 0, 0);

			g.dispose();
		}
	}

	private static int max(int[] arr) {
		int result = Integer.MIN_VALUE;
		for (int ele : arr) {
			result = Math.max(result, ele);
		}
		return result;
	}
}