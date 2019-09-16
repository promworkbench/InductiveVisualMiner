package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.math.plot.utils.Array;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMapViridis;

public class CorrelationDensityPlot {
	private static final int sizeX = 100;
	private static final int sizeY = 100;

	private static final int alias = 5;
	private static final ColourMap colourMap = new ColourMapViridis();

	public static BufferedImage create(double[] valuesX, double[] valuesY) {
		double[][] counts = new double[sizeX][sizeY];
		double max = 1;

		if (valuesX.length > 0 && valuesY.length > 0) {

			//fill array
			{
				double minX = Array.min(valuesX);
				double minY = Array.min(valuesY);
				double maxX = Array.max(valuesX);
				double maxY = Array.max(valuesY);

				for (int i = 0; i < valuesX.length; i++) {
					double x = valuesX[i];
					int indexX = (int) Math.round((sizeX - 1) * ((x - minX) / (maxX - minX)));

					double y = valuesY[i];
					int indexY = (int) Math.round((sizeX - 1) * ((y - minY) / (maxY - minY)));

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

		//create figure
		BufferedImage image = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				Color colour = colourMap.colour(counts[x][y], 0, max);
				image.setRGB(x, y, colour.getRGB());
			}
		}

		return image;
	}
}