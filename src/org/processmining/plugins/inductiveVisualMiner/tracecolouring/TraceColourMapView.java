package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.MultiComboBox;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeKey;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import gnu.trove.map.hash.THashMap;

public class TraceColourMapView extends SideWindow {

	private static final long serialVersionUID = -4833037956665918455L;
	private final JComboBox<AttributeKey> keySelector;
	private final DefaultComboBoxModel<AttributeKey> keySelectorModel;
	private final JCheckBox enabled;
	private final JTextArea status;
	private final JTextArea explanation;
	private final JLabel title;
	private final JTextArea example;

	public static final int maxColours = 7;
	public static final String prefix = "       ";

	private Function<TraceColourMapSettings, Object> onUpdate;

	public TraceColourMapView(InductiveVisualMinerPanel parent) {
		super(parent, "trace colouring - Inductive visual Miner");

		setLayout(new GridBagLayout());
		getContentPane().setBackground(MultiComboBox.even_colour_bg);

		//explanation
		{
			explanation = new JTextArea(
					"Trace colouring annotates the traces with a colour in the animation and the trace view, "
							+ "based on a trace attribute or property.\n");
			explanation.setWrapStyleWord(true);
			explanation.setLineWrap(true);
			explanation.setOpaque(false);
			explanation.setEnabled(false);
			explanation.setFont(new JLabel("blaa").getFont());
			explanation.setDisabledTextColor(MultiComboBox.colour_fg);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 3;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTH;
			add(explanation, c);
		}

		//checkbox
		{
			enabled = SlickerFactory.instance().createCheckBox("", false);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			add(enabled, c);
		}

		//title
		{
			title = SlickerFactory.instance().createLabel("  Trace attribute");
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			add(title, c);
		}

		//key selector
		{
			keySelectorModel = new DefaultComboBoxModel<>();
			keySelectorModel.addElement(AttributeKey.message("(initialising)"));

			keySelector = SlickerFactory.instance().createComboBox(new AttributeKey[0]);
			keySelector.setModel(keySelectorModel);
			keySelector.setSelectedIndex(0);
			keySelector.setEnabled(false);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			add(keySelector, c);
		}

		//status
		{
			status = new JTextArea("Currently not colouring.");
			status.setWrapStyleWord(true);
			status.setLineWrap(true);
			status.setOpaque(false);
			status.setEnabled(false);
			status.setFont(new JLabel("blaa").getFont());
			status.setDisabledTextColor(MultiComboBox.colour_fg);

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTH;

			add(status, c);
		}

		//example colours
		{
			example = new JTextArea("");
			example.setWrapStyleWord(true);
			example.setLineWrap(true);
			example.setOpaque(false);
			example.setEnabled(false);
			example.setFont(new JLabel("blaa").getFont());
			example.setDisabledTextColor(MultiComboBox.colour_fg);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 3;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTH;
			add(example, c);
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
				boolean x = enabled.isSelected();
				if (x) {
					getContentPane().setBackground(MultiComboBox.selection_colour_bg);
					explanation.setDisabledTextColor(MultiComboBox.selection_colour_fg);
					title.setForeground(MultiComboBox.selection_colour_fg);
					status.setDisabledTextColor(MultiComboBox.selection_colour_fg);
					example.setDisabledTextColor(MultiComboBox.selection_colour_fg);
				} else {
					getContentPane().setBackground(MultiComboBox.even_colour_bg);
					explanation.setDisabledTextColor(MultiComboBox.colour_fg);
					title.setForeground(MultiComboBox.colour_fg);
					status.setDisabledTextColor(MultiComboBox.colour_fg);
					example.setDisabledTextColor(MultiComboBox.colour_fg);
				}
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

				//there are little enough colours to just use them as such 
				Color[] colours = TraceColourMapSettings.getColours(numberOfColours);

				//create colours and map to values
				Map<String, Color> colourMap = new THashMap<String, Color>(numberOfColours);
				{
					StringBuilder s = new StringBuilder();
					int i = 0;
					for (String value : attribute.getStringValues()) {
						s.append(prefix + value + "\n");
						colourMap.put(value, colours[i]);
						i++;
					}
					example.setText(s.toString());
				}

				//colour the values in the example
				colourExample(colours);

				status.setText("Currently colouring traces using " + numberOfColours + " colours:\n");
				onUpdate.call(TraceColourMapSettings.string(attribute, colours, colourMap));
			} else {
				//too many colours
				status.setText("The current attribute would yield " + attribute.getStringValues().size()
						+ " colours. Inductive visual Miner supports up till " + maxColours + " colours.");
				example.setText("");
				onUpdate.call(TraceColourMapSettings.empty());
			}
		} else if (attribute.isNumeric()) {
			//this is a numeric attribute; divide it in 7 parts
			Color[] colours = TraceColourMapSettings.getColours(maxColours);
			updateProperty(colours, attribute.getNumericMin(), attribute.getNumericMax(), false, false);
			onUpdate.call(TraceColourMapSettings.number(attribute, colours, attribute.getNumericMin(),
					attribute.getNumericMax()));
		} else if (attribute.isTime()) {
			//this is a time attribute; divide it in 7 parts
			Color[] colours = TraceColourMapSettings.getColours(maxColours);
			updateProperty(colours, attribute.getTimeMin(), attribute.getTimeMax(), false, true);
			onUpdate.call(
					TraceColourMapSettings.time(attribute, colours, attribute.getTimeMin(), attribute.getTimeMax()));
		} else if (attribute.isTraceDuration()) {
			//special virtual attribute: trace duration
			Color[] colours = TraceColourMapSettings.getColours(maxColours);
			long min = attribute.getTimeMin();
			long max = attribute.getTimeMax();
			updateProperty(colours, min, max, true, false);
			onUpdate.call(TraceColourMapSettings.duration(colours, min, max));
		} else if (attribute.isTraceNumberofEvents()) {
			//special virtual attribute: number of events
			Color[] colours = TraceColourMapSettings.getColours(maxColours);
			long min = attribute.getTimeMin();
			long max = attribute.getTimeMax();
			updateProperty(colours, min, max, false, false);
			onUpdate.call(TraceColourMapSettings.numberOfEvents(colours, min, max));
		}
	}

	private void updateProperty(Color[] colours, double min, double max, boolean isDuration, boolean isTime)
			throws BadLocationException {
		int numberOfColours = maxColours;

		//build the example
		{
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
			s.append(prefix.substring(0, prefix.length() - 1) + "(");
			if (!isDuration && !isTime) {
				s.append(max);
			} else if (isDuration){
				s.append(ResourceTimeUtils.getDuration(max));
			} else {
				s.append(ResourceTimeUtils.timeToString((long) max));
			}
			s.append(")");
			example.setText(s.toString());
		}

		//colour the values in the example
		colourExample(colours);

		status.setText("Currently colouring traces using " + numberOfColours + " colours:\n");
	}

	private void updateDisable() throws Exception {
		status.setText("Currently not colouring.");
		example.setText("");
		onUpdate.call(TraceColourMapSettings.empty());
	}

	private void colourExample(Color[] colours) throws BadLocationException {
		for (int i = 0; i < colours.length; i++) {
			int startIndex = example.getLineStartOffset(i);
			DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(colours[i]);
			example.getHighlighter().addHighlight(startIndex, startIndex + prefix.length() - 2, painter);
		}
	}
}
