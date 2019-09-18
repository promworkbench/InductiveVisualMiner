package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis.AttributeData;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis.AttributeData.Field;
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

	private final JPanel label;
	private final GroupLayout labelLayout;
	private final JLabel image;

	public DataAnalysisAttributeView(Attribute attribute) {
		this.attribute = attribute;

		setOpaque(false);
		setLayout(new BorderLayout());

		MatteBorder border1 = new MatteBorder(2, 1, 2, 1, IvMDecorator.backGroundColour2);
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border1, attribute.getName(), TitledBorder.LEADING,
				TitledBorder.DEFAULT_POSITION);
		setBorder(titledBorder);

		label = new JPanel();
		labelLayout = new GroupLayout(label);
		labelLayout.setAutoCreateGaps(true);
		labelLayout.setAutoCreateContainerGaps(true);
		label.setLayout(labelLayout);
		label.setOpaque(false);
		add(label, BorderLayout.CENTER);

		image = new JLabel();
		add(image, BorderLayout.LINE_END);
	}

	public void set(DataAnalysis dataAnalysis) {
		AttributeData data = dataAnalysis.getAttributeData(attribute);

		//set image
		BufferedImage im = data.getCorrelationDensityPlot();
		if (im != null) {
			ImageIcon icon = new ImageIcon(im);
			image.setIcon(icon);
		} else {
			image.setIcon(null);
		}

		//set text
		{
			label.removeAll();
			List<JLabel> left = new ArrayList<>();
			List<JLabel> right = new ArrayList<>();
			if (dataAnalysis.isSomethingFiltered()) {
				//minimum and maximum
				{
					left.add(createLabel("minimum (full log)"));
					right.add(createLabel(DataAnalysis.getStringMin(attribute)));
					left.add(createLabel("maximum (full log)"));
					right.add(createLabel(DataAnalysis.getStringMax(attribute)));
				}

				//other fields: filtered log
				for (Field field : Field.values()) {
					left.add(createLabel(field + " (highlighted traces)"));
					if (data.get(field) > -Double.MAX_VALUE) {
						if (field.forcedNumeric()) {
							right.add(createLabel(DataAnalysis.numberFormat.format(data.get(field))));
						} else {
							right.add(createLabel(DataAnalysis.getString(attribute, data.get(field))));
						}
					} else {
						right.add(createLabel("n/a"));
					}
				}

				//other fields: negative log
				AttributeData dataNegative = dataAnalysis.getAttributeDataNegative(attribute);
				if (dataNegative != null) {
					for (Field field : Field.values()) {
						left.add(createLabel(field + " (non-highlighted traces)"));
						if (data.get(field) > -Double.MAX_VALUE) {
							if (field.forcedNumeric()) {
								right.add(createLabel(DataAnalysis.numberFormat.format(dataNegative.get(field))));
							} else {
								right.add(createLabel(DataAnalysis.getString(attribute, dataNegative.get(field))));
							}
						} else {
							right.add(createLabel("n/a"));
						}
					}
				}
			} else {
				//other fields
				for (Field field : Field.values()) {
					left.add(createLabel(field));
					if (data.get(field) > -Double.MAX_VALUE) {
						if (field.forcedNumeric()) {
							right.add(createLabel(DataAnalysis.numberFormat.format(data.get(field))));
						} else {
							right.add(createLabel(DataAnalysis.getString(attribute, data.get(field))));
						}
					} else {
						right.add(createLabel("n/a"));
					}
				}
			}

			//put into the panel
			{
				//vertical groups
				{
					Iterator<JLabel> itLeft = left.iterator();
					Iterator<JLabel> itRight = right.iterator();
					SequentialGroup sequentialGroup = labelLayout.createSequentialGroup();
					while (itLeft.hasNext()) {
						JLabel labelLeft = itLeft.next();
						JLabel labelRight = itRight.next();

						sequentialGroup = sequentialGroup.addGroup(
								labelLayout.createParallelGroup().addComponent(labelLeft).addComponent(labelRight));
					}
					labelLayout.setVerticalGroup(sequentialGroup);
				}

				//horizontal group
				{
					ParallelGroup parallelGroup1 = labelLayout.createParallelGroup();
					for (JLabel label : left) {
						parallelGroup1.addComponent(label);
					}
					ParallelGroup parallelGroup2 = labelLayout.createParallelGroup();
					for (JLabel label : right) {
						parallelGroup2.addComponent(label);
					}
					labelLayout.setHorizontalGroup(
							labelLayout.createSequentialGroup().addGroup(parallelGroup1).addGroup(parallelGroup2));
				}
			}
		}

		//		if (false) {
		//
		//			StringBuilder text = new StringBuilder();
		//			if (dataAnalysis.isSomethingFiltered()) {
		//				//filtered log
		//				double correlation = dataAnalysis.getCorrelation(attribute);
		//				if (correlation != -Double.MAX_VALUE) {
		//					text.append("Correlation with fitness " + correlation + " (highlighted traces)");
		//				} else {
		//					text.append("Correlation with fitness n/a (highlighted traces)\n");
		//				}
		//
		//				//negative (non-highlighted traces)
		//				double correlationNegative = dataAnalysis.getCorrelationNegative(attribute);
		//				if (correlationNegative != -Double.MAX_VALUE) {
		//					text.append("Correlation with fitness " + correlationNegative + " (non-highlighted traces)");
		//				} else {
		//					text.append("Correlation with fitness n/a (non-highlighted traces)");
		//				}
		//			} else {
		//				//normal log
		//			}
		//
		//			//label.setText(text.toString());
		//		}
	}

	private static JLabel createLabel(final Object string) {
		JLabel label = new JLabel(string.toString()) {
			public String toString() {
				return string.toString();
			}
		};
		IvMDecorator.decorate(label);
		label.setFont(IvMDecorator.fontLarger);
		return label;
	}
}