package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

import com.fluxicon.slickerbox.factory.SlickerDecorator;

public class TraceAttributeFilter extends ColouringFilter {

	JPanel panel = null;
	private String selectedKey;
	private XAttribute selectedAttribute;
	private JComboBox<XAttribute> attributeSelector;
	private JCheckBox enabled;
	private Runnable update;
	
	public String getName() {
		return "Trace attribute filter";
	}

	public JPanel createGui(XLog log) {
		panel = new JPanel();
		final Map<String, Set<XAttribute>> traceAttributes = getTraceAttributeMap(log);

		// Key selector
		final JComboBox<String> keySelector = new JComboBox<String>(traceAttributes.keySet().toArray(
				new String[traceAttributes.keySet().size()]));
		SlickerDecorator.instance().decorate(keySelector);

		keySelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				selectedKey = (String) keySelector.getSelectedItem();
				if (attributeSelector != null) {
					attributeSelector.removeAllItems();
					for (XAttribute a : traceAttributes.get(selectedKey)) {
						attributeSelector.addItem(a);
					}
					attributeSelector.setSelectedIndex(0);
				}
				update();
			}
		});
		keySelector.setSelectedIndex(0); // based on that concept:name always as trace attribute
		panel.add(keySelector);

		// Attribute value selector
		attributeSelector = new JComboBox<XAttribute>(traceAttributes.get(selectedKey).toArray(
				new XAttribute[traceAttributes.get(selectedKey).size()]));
		SlickerDecorator.instance().decorate(attributeSelector);
		attributeSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedAttribute = (XAttribute) attributeSelector.getSelectedItem();
				update();
			}
		});
		panel.add(attributeSelector);
		attributeSelector.setSelectedIndex(0);

		enabled = new JCheckBox();
		enabled.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				update();
			}
		});
		panel.add(enabled);

		return panel;
	}

	public boolean countInColouring(XTrace xTrace, AlignedTrace aTrace) {
		XAttribute attribute = xTrace.getAttributes().get(selectedKey);
		if (!xTrace.getAttributes().containsKey(selectedKey)) {
			return false;
		}
		// TODO: test equals function
		return attribute.equals(selectedAttribute);
	}

	public boolean isEnabled() {
		return selectedKey != null && selectedAttribute != null && enabled.isSelected();
	}
	
	private static Map<String, Set<XAttribute>> getTraceAttributeMap(XLog log) {
		Map<String, Set<XAttribute>> traceAttributes = new HashMap<String, Set<XAttribute>>();

		for (XTrace trace : log) {
			for (XAttribute traceAttribute : trace.getAttributes().values()) {
				if (!traceAttributes.containsKey(traceAttribute.getKey())) {
					traceAttributes.put(traceAttribute.getKey(), new TreeSet<XAttribute>());
				}
				traceAttributes.get(traceAttribute.getKey()).add(traceAttribute);
			}
		}
		return traceAttributes;
	}

}
