package org.processmining.plugins.inductiveVisualMiner.tracecolouring;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

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
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import gnu.trove.map.hash.THashMap;

public class TraceColourMapView extends SideWindow {

	private static final long serialVersionUID = -4833037956665918455L;
	private final JComboBox<String> keySelector;
	private final JCheckBox enabled;
	private AttributesInfo attributesInfo;
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
							+ "based on a trace attribute.\n");
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
			keySelector = SlickerFactory.instance().createComboBox(new String[0]);
			keySelector.addItem("(initialising)");
			keySelector.setSelectedIndex(0);
			keySelector.setEnabled(false);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 1;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			add(keySelector, c);

			keySelector.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					update();
				}
			});
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

		this.attributesInfo = attributesInfo;

		//populate the combobox with the trace attributes
		keySelector.removeAllItems();
		String[] attributes = attributesInfo.getTraceAttributes();
		if (attributes.length == 0) {
			keySelector.addItem("(no attributes present)");
		} else {
			for (String attribute : attributes) {
				keySelector.addItem(attribute);
			}
			keySelector.setEnabled(true);
		}
	}

	public void update() {
		try {
			if (onUpdate != null) {
				if (enabled.isSelected()) {
					String attribute = (String) keySelector.getSelectedItem();
					int numberOfColours = attributesInfo.getTraceAttributesMap().get(attribute).size();
					if (numberOfColours <= maxColours) {
						//there are little enough colours to just use them as such 
						Color[] colours = TraceColourMapSettings.getColours(numberOfColours);

						//create colours and map to values
						Map<String, Color> colourMap = new THashMap<String, Color>(numberOfColours);
						{
							StringBuilder s = new StringBuilder();
							int i = 0;
							for (String value : attributesInfo.getTraceAttributesMap().get(attribute)) {
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
					} else if (attributesInfo.isTraceAttributeNumeric(attribute)) {
						//this is a numeric attribute; divide it in 7 parts

						numberOfColours = maxColours;
						Color[] colours = TraceColourMapSettings.getColours(numberOfColours);
						double min = attributesInfo.getTraceAttributeMinimum(attribute);
						double max = attributesInfo.getTraceAttributeMaximum(attribute);

						//build the example
						{
							StringBuilder s = new StringBuilder();
							for (double i = 0; i < numberOfColours; i++) {
								s.append(prefix + (min + i * (max - min) / numberOfColours) + "\n");
								//a = (min + i * (max - min) / numberOfColours) + " - "
								//		+ (min + (i + 1) * (max - min) / numberOfColours) + "\n";
							}
							s.append(prefix.substring(0, prefix.length() - 1) + "(" + max + ")");
							example.setText(s.toString());
						}

						//colour the values in the example
						colourExample(colours);

						status.setText("Currently colouring traces using " + numberOfColours + " colours:\n");
						onUpdate.call(TraceColourMapSettings.number(attribute, colours, min, max));
					} else {
						//too many colours
						status.setText("The current attribute would yield " + numberOfColours
								+ " colours. Inductive visual Miner supports up till " + maxColours + " colours.");
						example.setText("");
						onUpdate.call(TraceColourMapSettings.empty());
					}
				} else {
					status.setText("Currently not colouring.");
					example.setText("");
					onUpdate.call(TraceColourMapSettings.empty());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void colourExample(Color[] colours) throws BadLocationException {
		for (int i = 0; i < colours.length; i++) {
			int startIndex = example.getLineStartOffset(i);
			DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(colours[i]);
			example.getHighlighter().addHighlight(startIndex, startIndex + prefix.length() - 2, painter);
		}
	}
}
