package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.math.plot.utils.Array;

/**
 * Draw a picture of a histogram
 * 
 * @author sander
 *
 */
public class Histogram {

	public static final int histogramWidth = 100;
	public static final int histogramHeight = 100;
	public static final int offsetX = 1;
	public static final int offsetY = 1;

	public static BufferedImage create(long[] values) {
		return create(values, null);
	}

	public static BufferedImage create(long[] values, AbstractRealDistribution distribution) {

		//get extreme values
		long minValue = Long.MAX_VALUE;
		long maxValue = Long.MIN_VALUE;
		for (long value : values) {
			minValue = Math.min(minValue, value);
			maxValue = Math.max(maxValue, value);
		}

		minValue = 0; //let the histogram start at 0 for clarity

		//fill array
		int[] counts = new int[histogramWidth];
		double maxCount = 1;
		if (values.length > 0) {

			//fill array
			for (long value : values) {
				int indexX = (int) Math
						.round((histogramWidth - 1) * ((value - minValue) / (maxValue - minValue * 1.0)));
				counts[indexX]++;
			}

			//get count extrema
			maxCount = 0;
			{
				for (int x = 0; x < histogramWidth; x++) {
					maxCount = Math.max(maxCount, Array.max(counts[x]));
				}
			}
		}

		//draw
		//create image
		BufferedImage image = new BufferedImage(histogramWidth + 2 * offsetX, histogramHeight + 2 * offsetY,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D) image.getGraphics();

		//background
		g.setBackground(Color.blue);
		//g.clearRect(0, 0, image.getWidth(), image.getHeight());

		//draw
		{
			GeneralPath pathOutline = new GeneralPath();
			GeneralPath pathFill = new GeneralPath();
			pathFill.moveTo(offsetX, offsetY + histogramHeight);
			for (int pixel = 0; pixel < histogramWidth; pixel++) {
				int x = offsetX + pixel;
				int y = (int) Math
						.round((offsetY + histogramHeight) - ((counts[pixel] / (maxCount * 1.0) * histogramHeight)));
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
			g.setColor(Color.blue);
			g.drawLine(0, image.getHeight() - 1, image.getWidth(), image.getHeight() - 1);
			g.drawLine(0, image.getHeight() - 1, 0, 0);
			//g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
		}

		//distribution
		if (distribution != null) {
			GeneralPath pathOutline = new GeneralPath();
			for (int pixel = 0; pixel < histogramWidth; pixel++) {
				int x = offsetX + pixel;

				double valueXMin = minValue + (pixel - 0.5) * (maxValue - minValue) / histogramWidth;
				double valueXMax = minValue + (pixel + 0.5) * (maxValue - minValue) / histogramWidth;
				double valueY = distribution.probability(valueXMin, valueXMax) * maxCount;

				int y = (int) Math.round((offsetY + histogramHeight) - ((valueY * histogramHeight)));
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
}