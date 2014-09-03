package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.fluxicon.slickerbox.factory.SlickerDecorator;

public class TraceAttributeFilterFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3999138546847147127L;
	private String selectedKey;
	private XAttribute selectedAttribute;

	private JComboBox<XAttribute> attributeSelector;
	private JCheckBox enabled;
	private Runnable update;

	public TraceAttributeFilterFrame(XLog log, Runnable update) {
		super("Trace Attribute Filter");

		this.update = update;
		JPanel panel = getFilterPanel(log);

		getContentPane().add(panel);
		pack();
		setVisible(true);
	}

	private JPanel getFilterPanel(XLog log) {
		JPanel panel = new JPanel();

		final Map<String, Set<XAttribute>> traceAttributes = getTraceAttributeMap(log);

		// Key selector
		final JComboBox<String> keySelector = new JComboBox<String>(traceAttributes.keySet().toArray(
				new String[traceAttributes.keySet().size()]));
		SlickerDecorator.instance().decorate(keySelector);

		keySelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setSelectedKey((String) keySelector.getSelectedItem());
				if (attributeSelector != null) {
					attributeSelector.removeAllItems();
					for (XAttribute a : traceAttributes.get(selectedKey)) {
						attributeSelector.addItem(a);
					}
					attributeSelector.setSelectedIndex(0);
				}
				update.run();
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
				update.run();
			}
		});
		panel.add(attributeSelector);
		attributeSelector.setSelectedIndex(0);

		// filter applier
//		JButton filter = new JButton("Filter");
//		filter.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//				update.run();
//			}
//		});
//		panel.add(filter);
		
		enabled = new JCheckBox();
		enabled.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				update.run();
			}
		});
		panel.add(enabled);

		return panel;
	}

	private Map<String, Set<XAttribute>> getTraceAttributeMap(XLog log) {
		Map<String, Set<XAttribute>> traceAttributes = new HashMap<String, Set<XAttribute>>();

		for (XTrace t : log) {
			for (String traceAttributeKey : t.getAttributes().keySet()) {
				if (!traceAttributes.containsKey(traceAttributeKey)) {
					traceAttributes.put(traceAttributeKey, new HashSet<XAttribute>());
				}
				traceAttributes.get(traceAttributeKey).add(t.getAttributes().get(traceAttributeKey));
			}
		}
		return traceAttributes;
	}

	public String getSelectedKey() {
		return selectedKey;
	}

	public void setSelectedKey(String selectedKey) {
		this.selectedKey = selectedKey;
	}

	public XAttribute getSelectedAttribute() {
		return selectedAttribute;
	}

	public boolean isEnabledChecked() {
		return enabled.isSelected();
	}
	
	

}
