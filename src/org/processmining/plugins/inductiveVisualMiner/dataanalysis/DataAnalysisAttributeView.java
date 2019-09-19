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
import javax.swing.LayoutStyle;
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
	}

	public void set(DataAnalysis dataAnalysis) {
		AttributeData data = dataAnalysis.getAttributeData(attribute);

		label.removeAll();
		if (!dataAnalysis.isSomethingFiltered()) {
			//full log without filters
			List<JLabel> columnA = new ArrayList<>();
			List<JLabel> columnB = new ArrayList<>();

			columnA.add(createLabel("Full log:"));
			columnB.add(createLabel(""));

			processLog(data, columnA, columnB, "");

			putInPanel(columnA, columnB);
		} else {
			//filtered log

			List<JLabel> columnA = new ArrayList<>();
			List<JLabel> columnB = new ArrayList<>();
			List<JLabel> columnC = new ArrayList<>();
			List<JLabel> columnD = new ArrayList<>();

			//minimum and maximum
			if (false) {
				columnA.add(createLabel("minimum (full log)"));
				columnB.add(createLabel(DataAnalysis.getStringMin(attribute)));
				columnA.add(createLabel("maximum (full log)"));
				columnB.add(createLabel(DataAnalysis.getStringMax(attribute)));
			}

			//other fields: filtered log
			columnA.add(createLabel("Highlighted traces:"));
			columnB.add(createLabel(""));
			processLog(data, columnA, columnB, "");

			//other fields: negative log
			AttributeData dataNegative = dataAnalysis.getAttributeDataNegative(attribute);
			if (dataNegative != null) {
				columnC.add(createLabel("Non-highlighted traces:"));
				columnD.add(createLabel(""));
				processLog(dataNegative, columnC, columnD, "");
				putInPanel(columnA, columnB, columnC, columnD);
			} else {
				putInPanel(columnA, columnB);
			}
		}
	}

	private void putInPanel(List<JLabel> columnA, List<JLabel> columnB, List<JLabel> columnC, List<JLabel> columnD) {
		//vertical groups
		{
			Iterator<JLabel> itA = columnA.iterator();
			Iterator<JLabel> itB = columnB.iterator();
			Iterator<JLabel> itC = columnC.iterator();
			Iterator<JLabel> itD = columnD.iterator();
			SequentialGroup sequentialGroup = labelLayout.createSequentialGroup();
			while (itA.hasNext()) {
				JLabel labelA = itA.next();
				JLabel labelB = itB.next();
				JLabel labelC = itC.next();
				JLabel labelD = itD.next();

				sequentialGroup = sequentialGroup.addGroup(labelLayout.createParallelGroup().addComponent(labelA)
						.addComponent(labelB).addComponent(labelC).addComponent(labelD));
			}
			labelLayout.setVerticalGroup(sequentialGroup);
		}

		//horizontal group
		{
			ParallelGroup parallelGroupA = labelLayout.createParallelGroup();
			for (JLabel label : columnA) {
				parallelGroupA.addComponent(label);
			}
			ParallelGroup parallelGroupB = labelLayout.createParallelGroup();
			for (JLabel label : columnB) {
				parallelGroupB.addComponent(label);
			}
			ParallelGroup parallelGroupC = labelLayout.createParallelGroup();
			for (JLabel label : columnC) {
				parallelGroupC.addComponent(label);
			}
			ParallelGroup parallelGroupD = labelLayout.createParallelGroup();
			for (JLabel label : columnD) {
				parallelGroupD.addComponent(label);
			}
			labelLayout.setHorizontalGroup(labelLayout.createSequentialGroup().addGroup(parallelGroupA)
					.addGroup(parallelGroupB)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(parallelGroupC).addGroup(parallelGroupD));
		}
	}

	public void putInPanel(List<JLabel> left, List<JLabel> right) {
		//vertical groups
		{
			Iterator<JLabel> itLeft = left.iterator();
			Iterator<JLabel> itRight = right.iterator();
			SequentialGroup sequentialGroup = labelLayout.createSequentialGroup();
			while (itLeft.hasNext()) {
				JLabel labelLeft = itLeft.next();
				JLabel labelRight = itRight.next();

				sequentialGroup = sequentialGroup
						.addGroup(labelLayout.createParallelGroup().addComponent(labelLeft).addComponent(labelRight));
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

	public void processLog(AttributeData data, List<JLabel> left, List<JLabel> right, String postfix) {
		for (Field field : Field.values()) {
			left.add(createLabel(field + postfix));
			switch (field.type()) {
				case image :
					BufferedImage im = data.getImage(field);
					if (im != null) {
						ImageIcon icon = new ImageIcon(im);
						right.add(createLabel(icon));
					} else {
						right.add(createLabel("n/a"));
					}
					break;
				case number :
					if (data.getNumber(field) > -Double.MAX_VALUE) {
						if (field.forcedNumeric()) {
							right.add(createLabel(DataAnalysis.numberFormat.format(data.getNumber(field))));
						} else {
							right.add(createLabel(DataAnalysis.getString(attribute, data.getNumber(field))));
						}
					} else {
						right.add(createLabel("n/a"));
					}
					break;
				default :
					break;
			}
		}
	}

	private static JLabel createLabel(final String string) {
		JLabel label = new JLabel(string.toString()) {
			private static final long serialVersionUID = -3542530385464222088L;

			public String toString() {
				return string.toString();
			}
		};
		IvMDecorator.decorate(label);
		label.setFont(IvMDecorator.fontLarger);
		return label;
	}

	private static JLabel createLabel(final ImageIcon icon) {
		JLabel label = new JLabel();
		label.setIcon(icon);
		return label;
	}
}