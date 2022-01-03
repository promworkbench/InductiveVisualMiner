package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class AttributeFilterNameGui extends IvMFilterGui {

	private static final long serialVersionUID = -5662487261061931369L;
	private final JComboBox<String> keySelector;
	private final DefaultComboBoxModel<String> keySelectorModel;
	private String selectedAttributeName;
	private Collection<Attribute> attributes;

	private final JTextArea explanation;
	private final Runnable onUpdate;
	private boolean block = false;

	public AttributeFilterNameGui(String title, Runnable onUpdate, IvMDecoratorI decorator) {
		super(title, decorator);
		usesVerticalSpace = true;
		this.onUpdate = onUpdate;
		setLayout(new BorderLayout());

		//header
		{
			JPanel header = new JPanel();
			header.setOpaque(false);
			header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
			add(header, BorderLayout.PAGE_START);
			//explanation
			{
				explanation = createExplanation("explanation");
				header.add(explanation);
			}

			header.add(Box.createVerticalStrut(10));

			//key selector
			{
				keySelectorModel = new DefaultComboBoxModel<>();
				keySelector = new JComboBox<>();
				decorator.decorate(keySelector);
				keySelector.setModel(keySelectorModel);
				header.add(keySelector);
			}

			header.add(Box.createVerticalStrut(10));
		}

		setController();
	}

	public void setController() {
		// Key selector
		keySelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!block) {
					block = true;
					onUpdate.run();
					selectedAttributeName = (String) keySelectorModel.getSelectedItem();
					block = false;
				}
			}
		});

		block = false;
	}

	public void setAttributes(Collection<Attribute> attributes) {
		this.attributes = attributes;

		//populate the combobox with the trace attributes
		{
			block = true;
			keySelectorModel.removeAllElements();
			for (Attribute attribute : attributes) {
				keySelectorModel.addElement(attribute.getName());
			}
			block = false;
		}

		//keep the selection
		{
			boolean found = false;
			if (selectedAttributeName != null) {

				boolean attributeIsPresent = false;
				for (Attribute attribute : attributes) {
					attributeIsPresent = attributeIsPresent || attribute.getName().equals(selectedAttributeName);
				}
				if (attributeIsPresent) {
					for (int i = 0; i < keySelector.getItemCount(); i++) {
						String key = keySelector.getItemAt(i);
						if (key.equals(selectedAttributeName)) {
							block = true;
							keySelector.setSelectedItem(selectedAttributeName);
							block = false;
							found = true;
						}
					}
				}
			}
			if (!found) {
				block = true;
				keySelector.setSelectedIndex(0);
				selectedAttributeName = (String) keySelector.getSelectedItem();
				block = false;
			}
		}
	}

	public JComboBox<String> getKeySelector() {
		return keySelector;
	}

	public JTextArea getExplanationLabel() {
		return explanation;
	}

	public Attribute getSelectedAttribute() {
		if (attributes == null) {
			return null;
		}
		String attributeName = (String) keySelector.getSelectedItem();
		keySelectorModel.getSelectedItem();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals(attributeName)) {
				return attribute;
			}
		}
		return null;
	}

	public boolean isFiltering() {
		if (getSelectedAttribute() == null) {
			//only happens in an empty log
			return false;
		}
		return true;
	}

	public String getExplanation() {
		String intro = "without having ";
		if (!getSelectedAttribute().isVirtual()) {
			intro += "attribute `" + getSelectedAttribute().getName() + "' ";
		} else {
			intro += getSelectedAttribute() + " ";
		}

		return intro;
	}

}