package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.deckfour.xes.model.XAttribute;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class AttributeFilterGui extends ColouringFilterGui {

	private static final long serialVersionUID = -5662487261061931369L;
	private final JComboBox<String> keySelector;
	private final JComboBox<XAttribute> attributeSelector;
	private final JLabel explanation;

	@SuppressWarnings("unchecked")
	public AttributeFilterGui(Map<String, Set<XAttribute>> attributes, String title) {
		super(title);

		setLayout(new GridBagLayout());
		{
			explanation = new JLabel("Only highlight traces of which the ");
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTH;
			add(explanation, c);
		}

		{
			keySelector = SlickerFactory.instance().createComboBox(
					attributes.keySet().toArray(new String[attributes.keySet().size()]));
			keySelector.setSelectedIndex(0);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.weightx = 0.5;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(keySelector, c);
		}

		{
			attributeSelector = SlickerFactory.instance().createComboBox(
					attributes.get(getSelectedKey()).toArray(new XAttribute[attributes.get(getSelectedKey()).size()]));
			attributeSelector.setSelectedIndex(0);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 2;
			c.weightx = 0.5;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(attributeSelector, c);
		}
	}

	public String getSelectedKey() {
		return (String) keySelector.getSelectedItem();
	}

	public XAttribute getSelectedAttribute() {
		return (XAttribute) attributeSelector.getSelectedItem();
	}

	public JComboBox<String> getKeySelector() {
		return keySelector;
	}

	public JComboBox<XAttribute> getAttributeSelector() {
		return attributeSelector;
	}

	public JLabel getExplanation() {
		return explanation;
	}

}
