package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.SwitchPanel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeKey;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;

import gnu.trove.map.hash.THashMap;

public class TraceColourMapView extends SideWindow {

	private static final long serialVersionUID = -4833037956665918455L;
	private final JComboBox<AttributeKey> keySelector;
	private final DefaultComboBoxModel<AttributeKey> keySelectorModel;
	private final JCheckBox enabled;
	private final JTextArea status;
	private final JTextArea explanation;
	private final JLabel title;
	private final TraceColourMapExample example;
	JPanel filterPanel;

	public static final int maxColours = 7;
	public static final String prefix = "       ";

	private Function<TraceColourMapSettings, Object> onUpdate;

	public TraceColourMapView(InductiveVisualMinerPanel parent) {
		super(parent, "trace colouring - " + InductiveVisualMinerPanel.title);
		setSize(300, 300);
		setMinimumSize(new Dimension(300, 300));
		JPanel content = new IvMPanel();
		add(content);

		BorderLayout layout = new BorderLayout();
		content.setLayout(layout);

		//explanation
		{
			explanation = new JTextArea(
					"Trace colouring annotates the traces with a colour in the animation and the trace view, "
							+ "based on a trace attribute or property.");
			IvMDecorator.decorate(explanation);
			explanation.setWrapStyleWord(true);
			explanation.setLineWrap(true);
			explanation.setEnabled(false);
			explanation.setMargin(new Insets(5, 5, 5, 5));
			content.add(explanation, BorderLayout.PAGE_START);
		}

		//checkbox
		{
			enabled = new JCheckBox("", false);
			IvMDecorator.decorate(enabled);
			content.add(enabled, BorderLayout.LINE_START);
		}

		//filter panel
		{
			SpringLayout filterPanelLayout = new SpringLayout();
			filterPanel = new SwitchPanel();
			filterPanel.setLayout(filterPanelLayout);
			filterPanel.setEnabled(false);
			content.add(filterPanel, BorderLayout.CENTER);

			//title
			{
				title = new JLabel("Trace attribute");
				IvMDecorator.decorate(title);

				filterPanel.add(title);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, title, 10, SpringLayout.NORTH, filterPanel);
				filterPanelLayout.putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, filterPanel);
			}

			//key selector
			{
				keySelectorModel = new DefaultComboBoxModel<>();
				keySelectorModel.addElement(AttributeKey.message("(initialising)"));
				keySelector = new JComboBox<AttributeKey>(new AttributeKey[0]);
				IvMDecorator.decorate(keySelector);
				keySelector.setModel(keySelectorModel);
				keySelector.setSelectedIndex(0);
				keySelector.setEnabled(false);

				filterPanel.add(keySelector);
				filterPanelLayout.putConstraint(SpringLayout.VERTICAL_CENTER, keySelector, 0,
						SpringLayout.VERTICAL_CENTER, title);
				filterPanelLayout.putConstraint(SpringLayout.WEST, keySelector, 5, SpringLayout.EAST, title);

				filterPanelLayout.putConstraint(SpringLayout.EAST, keySelector, -5, SpringLayout.EAST, filterPanel);
			}

			//status
			{
				status = new JTextArea("Currently not colouring.");
				IvMDecorator.decorate(status);
				status.setWrapStyleWord(true);
				status.setLineWrap(true);
				status.setEnabled(false);

				filterPanel.add(status);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, status, 5, SpringLayout.SOUTH, keySelector);
				filterPanelLayout.putConstraint(SpringLayout.WEST, status, 5, SpringLayout.WEST, filterPanel);
				filterPanelLayout.putConstraint(SpringLayout.EAST, status, -5, SpringLayout.EAST, filterPanel);
			}

			//example colours
			{
				example = new TraceColourMapExample(10, 10);
				IvMDecorator.decorate(example);
				example.setWrapStyleWord(true);
				example.setLineWrap(true);
				example.setEnabled(false);

				filterPanel.add(example);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, example, 5, SpringLayout.SOUTH, status);
				filterPanelLayout.putConstraint(SpringLayout.WEST, example, 5, SpringLayout.WEST, filterPanel);
				filterPanelLayout.putConstraint(SpringLayout.EAST, example, -5, SpringLayout.EAST, filterPanel);
			}
		}
	}

	public void initialise(AttributesInfo attributesInfo,
			final Function<TraceColourMapSettings, Object> onUpdateTraceColourMap) {
		onUpdate = onUpdateTraceColourMap;

		//populate the combobox with the trace attributes
		keySelectorModel.removeAllElements();
		for (Attribute attribute : attributesInfo.getTraceAttributes()) {
			keySelectorModel.addElement(AttributeKey.attribute(attribute));
		}
		keySelector.setSelectedIndex(0);
		keySelector.setEnabled(true);

		keySelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});

		enabled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				filterPanel.setEnabled(enabled.isSelected());
				filterPanel.repaint();
				update();
			}
		});
	}

	public void update() {
		try {
			if (onUpdate != null) {
				if (enabled.isSelected()) {
					AttributeKey key = (AttributeKey) keySelector.getSelectedItem();
					updateAttribute(key.getAttribute());
				} else {
					updateDisable();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateAttribute(Attribute attribute) throws Exception {
		if (attribute.isLiteral()) {
			if (attribute.getStringValues().size() <= maxColours) {

				int numberOfColours = attribute.getStringValues().size();

				//there are few enough colours to just use them as such 
				Color[] colours = TraceColourMapSettings.getColours(numberOfColours);

				//create colours and map to values
				Map<String, Color> value2colour = new THashMap<String, Color>(numberOfColours);
				{
					StringBuilder s = new StringBuilder();
					int i = 0;
					for (String value : attribute.getStringValues()) {
						s.append(prefix + value + "\n");
						value2colour.put(value, colours[i]);
						i++;
					}
					example.setText(s.toString());
				}

				//colour the values in the example
				colourExample(colours);

				status.setText("Currently colouring traces using " + numberOfColours + " colours:");
				onUpdate.call(TraceColourMapSettings.string(attribute, value2colour));
			} else {
				//too many colours
				status.setText("The current attribute would yield " + attribute.getStringValues().size()
						+ " colours. Inductive visual Miner supports up to " + maxColours + " colours.");
				example.setText("");
				onUpdate.call(TraceColourMapSettings.empty());
			}
		} else if (attribute.isNumeric()) {
			//this is a numeric attribute
			ColourMap colourMap = TraceColourMapSettings.getColourMap();
			updateProperty(colourMap, attribute.getNumericMin(), attribute.getNumericMax(), false, false);
			onUpdate.call(TraceColourMapSettings.number(attribute, colourMap, attribute.getNumericMin(),
					attribute.getNumericMax()));
		} else if (attribute.isTime()) {
			//this is a time attribute; divide it in 7 parts
			ColourMap colourMap = TraceColourMapSettings.getColourMap();
			updateProperty(colourMap, attribute.getTimeMin(), attribute.getTimeMax(), false, true);
			onUpdate.call(
					TraceColourMapSettings.time(attribute, colourMap, attribute.getTimeMin(), attribute.getTimeMax()));
		} else if (attribute.isTraceDuration()) {
			//special virtual attribute: trace duration
			long min = attribute.getTimeMin();
			long max = attribute.getTimeMax();
			ColourMap colourMap = TraceColourMapSettings.getColourMap();
			updateProperty(colourMap, min, max, true, false);
			onUpdate.call(TraceColourMapSettings.duration(colourMap, min, max));
		} else if (attribute.isTraceNumberofEvents()) {
			//special virtual attribute: number of events
			long min = attribute.getTimeMin();
			long max = attribute.getTimeMax();
			ColourMap colourMap = TraceColourMapSettings.getColourMap();
			updateProperty(colourMap, min, max, false, false);
			onUpdate.call(TraceColourMapSettings.numberOfEvents(colourMap, min, max));
		}
	}

	private void updateProperty(ColourMap colourMap, double min, double max, boolean isDuration, boolean isTime) {
		//build the example
		setExampleText(maxColours, min, max, isDuration, isTime, true);

		//colour the values in the example
		colourExample(colourMap, maxColours);

		status.setText("Currently colouring traces as follows:");
	}

	private void setExampleText(int numberOfColours, double min, double max, boolean isDuration, boolean isTime,
			boolean isMap) {
		StringBuilder s = new StringBuilder();
		for (double i = 0; i < numberOfColours; i++) {
			s.append(prefix);
			if (!isDuration && !isTime) {
				s.append(min + i * (max - min) / numberOfColours);
			} else if (isDuration) {
				s.append(ResourceTimeUtils.getDuration(min + i * (max - min) / numberOfColours));
			} else {
				s.append(ResourceTimeUtils.timeToString((long) (min + i * (max - min) / numberOfColours)));
			}
			s.append("\n");
		}
		if (!isMap) {
			s.append(prefix.substring(0, prefix.length() - 1) + "(");
		} else {
			s.append(prefix);
		}
		if (!isDuration && !isTime) {
			s.append(max);
		} else if (isDuration) {
			s.append(ResourceTimeUtils.getDuration(max));
		} else {
			s.append(ResourceTimeUtils.timeToString((long) max));
		}
		if (!isMap) {
			s.append(")");
		}
		example.setText(s.toString());
	}

	private void updateDisable() throws Exception {
		status.setText("Currently not colouring.");
		example.setText("");
		example.setColourMap(null);
		onUpdate.call(TraceColourMapSettings.empty());
	}

	private void colourExample(Color[] colours) throws BadLocationException {
		example.setColourMap(null);
		for (int i = 0; i < colours.length; i++) {
			int startIndex = example.getLineStartOffset(i);
			DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(colours[i]);
			example.getHighlighter().addHighlight(startIndex, startIndex + prefix.length() - 2, painter);
		}
	}

	private void colourExample(ColourMap colourMap, int numberOfRows) {
		example.setColourMap(colourMap);
		example.setNumberOfRows(numberOfRows);
	}

	private static class TraceColourMapExample extends JTextArea {
		private static final long serialVersionUID = 2130711974156441942L;
		private ColourMap colourMap;
		private int numberOfRows;

		public TraceColourMapExample(int rows, int columns) {
			super(rows, columns);
		}

		public void setColourMap(ColourMap colourMap) {
			this.colourMap = colourMap;
		}

		public void setNumberOfRows(int numberOfRows) {
			this.numberOfRows = numberOfRows;
		}

		@Override
		protected void paintComponent(java.awt.Graphics g) {
			if (colourMap != null) {

				int rowHeight = 15;

				int x0 = 0;
				int x1 = 16;
				int y0 = (int) (rowHeight * 0.5);
				int y1 = (int) (rowHeight * (numberOfRows + 0.5));
				for (int y = y0; y < y1; y++) {
					double v = (y - y0) / (y1 - y0);
					Color c = colourMap.colour((y - y0) / (y1 - y0 * 1.0));
					g.setColor(c);
					g.drawLine(x0, y, x1, y);
				}
			}

			super.paintComponent(g);
		};
	}
}