package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.deckfour.xes.model.XAttribute;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class TraceAttributeFilterGui extends ColouringFilterGui {
	private final JComboBox<String> keySelector;
	private final JComboBox<XAttribute> attributeSelector;
	private final JCheckBox enabled;

	public TraceAttributeFilterGui(Map<String, Set<XAttribute>> attributes) {
		keySelector = SlickerFactory.instance().createComboBox(
				attributes.keySet().toArray(new String[attributes.keySet().size()]));
		add(keySelector);
		
		attributeSelector = SlickerFactory.instance().createComboBox(
				attributes.get(getSelectedKey()).toArray(new XAttribute[attributes.get(getSelectedKey()).size()]));
		add(attributeSelector);
		
		enabled = new JCheckBox();
		add(enabled);
	}

	public String getSelectedKey() {
		return (String) keySelector.getSelectedItem();
	}

	public XAttribute getSelectedAttribute() {
		return (XAttribute) attributeSelector.getSelectedItem();
	}
	
	public JCheckBox getEnabled() {
		return enabled;
	}
	
	public JComboBox<String> getKeySelector() {
		return keySelector;
	}
	
	public JComboBox<XAttribute> getAttributeSelector() {
		return attributeSelector;
	}

}
