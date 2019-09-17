package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;

/**
 * Displays the information of one attribute.
 * 
 * @author sander
 *
 */
public class DataAnalysisAttributeView extends JPanel {

	private static final long serialVersionUID = -6066139069263040657L;
	private final Attribute attribute;

	private final JTextArea label;
	private final JLabel image;

	public DataAnalysisAttributeView(Attribute attribute) {
		this.attribute = attribute;

		setOpaque(false);
		setLayout(new BorderLayout());

		MatteBorder border1 = new MatteBorder(2, 1, 2, 1, IvMDecorator.backGroundColour2);
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border1, attribute.getName(), TitledBorder.LEADING,
				TitledBorder.DEFAULT_POSITION);
		setBorder(titledBorder);

		label = new JTextArea();
		label.setOpaque(false);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		add(label, BorderLayout.CENTER);

		image = new JLabel();
		add(image, BorderLayout.LINE_END);
	}

	public void set(DataAnalysis dataAnalysis) {
		StringBuilder text = new StringBuilder();
		if (dataAnalysis.isSomethingFiltered()) {
			//filtered log
			double correlation = dataAnalysis.getCorrelation(attribute);
			if (correlation != Double.MIN_VALUE) {
				text.append("Correlation with fitness: " + correlation + " (highlighted traces)\n");
			} else {
				text.append("Correlation with fitness: n/a (highlighted traces)\n");
			}

			//negative (non-highlighted traces)
			double correlationNegative = dataAnalysis.getCorrelationNegative(attribute);
			if (correlationNegative != Double.MIN_VALUE) {
				text.append("Correlation with fitness: " + correlationNegative + " (non-highlighted traces)");
			} else {
				text.append("Correlation with fitness: n/a (non-highlighted traces)");
			}
		} else {
			//normal log
			double correlation = dataAnalysis.getCorrelation(attribute);
			if (correlation != Double.MIN_VALUE) {
				text.append("Correlation with fitness: " + correlation);
			} else {
				text.append("Correlation with fitness: n/a");
			}
		}

		BufferedImage im = dataAnalysis.getCorrelationDensityPlot(attribute);
		if (im != null) {
			ImageIcon icon = new ImageIcon(dataAnalysis.getCorrelationDensityPlot(attribute));
			image.setIcon(icon);
		} else {
			image.setIcon(null);
		}

		label.setText(text.toString());
	}

}
