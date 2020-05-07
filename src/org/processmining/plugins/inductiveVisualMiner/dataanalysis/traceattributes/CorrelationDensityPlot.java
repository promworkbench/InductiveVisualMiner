package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.math.plot.utils.Array;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMapViridis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class CorrelationDensityPlot {
	private static final int sizeX1DPlot = 10;
	private static final int marginX = 13;
	private static final int sizeX2DPlot = 100;

	private static final int sizeY1DPlot = 10;
	private static final int marginY = 13;
	private static final int sizeY2DPlot = 100;

	private static final int alias = 5;
	private static final ColourMap colourMap = new ColourMapViridis();

	public static int getWidth() {
		return sizeX1DPlot + marginX + sizeX2DPlot;
	}

	public static int getHeight() {
		return sizeY1DPlot + marginY + sizeY2DPlot;
	}

	public static BufferedImage create(String nameX, double[] valuesX, double minX, double maxX, String nameY,
			double[] valuesY, double minY, double maxY) {

		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

		fillImage2DPlot(image, valuesX, minX, maxX, valuesY, minY, maxY, sizeX1DPlot + marginX, 0, sizeX2DPlot,
				sizeY2DPlot);

		fillImage1DPlotHorizontal(image, valuesX, minX, maxX, sizeX1DPlot + marginX, sizeY2DPlot + marginY, sizeX2DPlot,
				sizeY1DPlot);
		drawTextHorizontal(image, nameX, sizeX1DPlot + marginX, sizeY2DPlot, sizeX2DPlot, marginY);

		fillImage1DPlotVertical(image, valuesY, minY, maxY, 0, 0, sizeX1DPlot, sizeY2DPlot);
		drawTextVertical(image, nameY, sizeX1DPlot, 0, marginX, sizeY2DPlot);
		return image;
	}

	public static BufferedImage create(String nameX, long[] valuesX, long minX, long maxX, String nameY,
			double[] valuesY, double minY, double maxY) {
		/*
		 * here we do not need the precision of long anymore, so transform to
		 * double and keep things maintainable around here..
		 */

		double[] valuesXd = new double[valuesX.length];
		for (int i = 0; i < valuesXd.length; i++) {
			valuesXd[i] = valuesX[i];
		}

		return create(nameX, valuesXd, minX, maxX, nameY, valuesY, minY, maxY);
	}

	public static void drawTextHorizontal(BufferedImage image, String name, int offsetX, int offsetY, int sizeX,
			int sizeY) {
		Graphics2D g = (Graphics2D) image.getGraphics();

		Font font = IvMDecorator.font;
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics(font);

		int width = metrics.stringWidth(name);
		int height = ((metrics.getAscent() + metrics.getDescent()) / 2);

		int offsetXp = offsetX + (sizeX - width) / 2;
		offsetXp = Math.max(offsetX, offsetXp);
		offsetY += (sizeY / 2) + (height / 2);
		offsetY += 2; //manual adjustment

		g.setColor(IvMDecorator.textColour);

		g.drawString(name, offsetXp, offsetY);
	}

	public static void drawTextVertical(BufferedImage image, String name, int offsetX, int offsetY, int sizeX,
			int sizeY) {
		Graphics2D g = (Graphics2D) image.getGraphics();

		Font font = IvMDecorator.font;
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics(font);

		int width = metrics.stringWidth(name);
		int height = ((metrics.getAscent() + metrics.getDescent()) / 2);

		offsetY -= (sizeY - width) / 2;
		offsetX += (sizeX / 2) + (height / 2);
		offsetX += 1; //manual adjustment

		g.setColor(IvMDecorator.textColour);

		g.rotate(-Math.PI / 2, offsetX, sizeY + offsetY);
		g.drawString(name, offsetX, sizeY + offsetY);
	}

	public static void fillImage1DPlotHorizontal(BufferedImage image, double[] valuesX, double minX, double maxX,
			int offsetX, int offsetY, int sizeX, int sizeY) {
		double[] counts = new double[sizeX];
		double max = 1;
		if (valuesX.length > 0) {

			//fill array
			{
				for (int i = 0; i < valuesX.length; i++) {
					double x = valuesX[i];

					if (x > -Double.MAX_VALUE) {
						int indexX = (int) Math.round((sizeX - 1) * ((x - minX) / (maxX - minX)));
						counts[indexX]++;
					} else {
						//non-present value
					}
				}
			}

			//get extrema
			max = 0;
			{
				for (int x = 0; x < sizeX; x++) {
					max = Math.max(max, Array.max(counts[x]));
				}
			}
		}

		//set pixels
		for (int x = 0; x < sizeX; x++) {
			Color colour = colourMap.colour(counts[x], 0, max);
			for (int y = 0; y < sizeY; y++) {
				image.setRGB(x + offsetX, y + offsetY, colour.getRGB());
			}
		}
	}

	public static void fillImage1DPlotVertical(BufferedImage image, double[] valuesY, double minY, double maxY,
			int offsetX, int offsetY, int sizeX, int sizeY) {
		double[] counts = new double[sizeY];
		double max = 1;
		if (valuesY.length > 0) {

			//fill array
			{
				for (int i = 0; i < valuesY.length; i++) {
					double y = valuesY[i];
					if (y > -Double.MAX_VALUE) {
						int indexY = (int) Math.round((sizeY - 1) * ((y - minY) / (maxY - minY)));

						counts[indexY]++;
					} else {
						//non-present value
					}
				}
			}

			//get extrema
			max = 0;
			{
				for (int y = 0; y < sizeY; y++) {
					max = Math.max(max, Array.max(counts[y]));
				}
			}
		}

		//set pixels (invert Y)
		for (int y = 0; y < sizeY; y++) {
			Color colour = colourMap.colour(counts[y], 0, max);
			for (int x = 0; x < sizeX; x++) {
				image.setRGB(x + offsetX, (sizeY - y) + offsetY, colour.getRGB());
			}
		}
	}

	public static void fillImage2DPlot(BufferedImage image, double[] valuesX, double minX, double maxX,
			double[] valuesY, double minY, double maxY, int offsetX, int offsetY, int sizeX, int sizeY) {
		double[][] counts = new double[sizeX][sizeY];
		double max = 1;

		if (valuesX.length > 0 && valuesY.length > 0) {

			//fill array
			{
				for (int i = 0; i < valuesX.length; i++) {
					double x = valuesX[i];
					int indexX = (int) Math.round((sizeX - 1) * ((x - minX) / (maxX - minX)));

					double y = valuesY[i];
					int indexY = (int) Math.round((sizeY - 1) * ((y - minY) / (maxY - minY)));

					//counts[indexX][indexY]++;
					//System.out.println("index " + indexX + " " + indexY);
					for (int aX = indexX - alias; aX <= indexX + alias; aX++) {
						for (int aY = indexY - alias; aY <= indexY + alias; aY++) {
							if (0 <= aX && aX < sizeX && 0 <= aY && aY < sizeY) {
								double diagonal = Math.sqrt(Math.pow(aX - indexX, 2) + Math.pow(aY - indexY, 2));
								double value = Math.max(0, 1 - (diagonal / alias));
								//System.out.println(" increase " + aX + " " + aY + " by " + value);
								counts[aX][aY] += value;
							}
						}
					}
				}
			}

			//get extrema
			max = 0;
			{
				for (int x = 0; x < sizeX; x++) {
					max = Math.max(max, Array.max(counts[x]));
				}
			}
		}

		//set pixels (invert Y)
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				Color colour = colourMap.colour(counts[x][y], 0, max);
				image.setRGB(x + offsetX, (sizeY - y) + offsetY, colour.getRGB());
			}
		}
	}
}