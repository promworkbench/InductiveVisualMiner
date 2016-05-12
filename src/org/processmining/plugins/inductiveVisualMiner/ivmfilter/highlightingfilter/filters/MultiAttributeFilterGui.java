package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class MultiAttributeFilterGui extends IvMFilterGui {

	private static final long serialVersionUID = -5662487261061931369L;
	private final JComboBox<String> keySelector;
	private final JList<String> attributeSelector;
	private final DefaultListModel<String> attributeSelectorListModel;
	private final JLabel explanation;
	private final boolean empty;

	@SuppressWarnings("unchecked")
	public MultiAttributeFilterGui(Map<String, ? extends Set<String>> attributes, String title) {
		super(title);
		usesVerticalSpace = true;
		empty = attributes.isEmpty();

		setLayout(new GridBagLayout());
		//explanation
		{
			explanation = new JLabel("Only highlight traces of which the ");
			explanation.setPreferredSize(new Dimension(100, 50));
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTH;
			add(explanation, c);
		}

		//key selector
		{
			keySelector = SlickerFactory.instance().createComboBox(
					attributes.keySet().toArray(new String[attributes.keySet().size()]));
			if (empty) {
				keySelector.addItem("(no attributes present)");
				keySelector.setEnabled(false);
			}
			keySelector.setSelectedIndex(0);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.weightx = 0.5;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTH;
			add(keySelector, c);
		}

		{
			attributeSelectorListModel = new DefaultListModel<String>();
			if (!empty) {
				for (String a : attributes.get(getSelectedKey())) {
					attributeSelectorListModel.addElement(a);
				}
			}

			attributeSelector = new JList<String>(attributeSelectorListModel);
			attributeSelector.setCellRenderer(new ListCellRenderer<String>() {
				protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

				public Component getListCellRendererComponent(JList<? extends String> list, String value,
						int index, boolean isSelected, boolean cellHasFocus) {

					JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
							isSelected, cellHasFocus);
					if (!isSelected) {
						renderer.setOpaque(false);
					} else {
						renderer.setOpaque(true);
					}
					return renderer;
				}
			});
			attributeSelector.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			JScrollPane scrollPane = new JScrollPane(attributeSelector);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = 2;
			c.weightx = 0.5;
			c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			add(scrollPane, c);

			scrollPane.getViewport().setOpaque(false);
			attributeSelector.setOpaque(false);
			scrollPane.setOpaque(false);
		}
	}

	public String getSelectedKey() {
		return (String) keySelector.getSelectedItem();
	}

	public List<String> getSelectedAttributes() {
		return attributeSelector.getSelectedValuesList();
	}

	public JComboBox<String> getKeySelector() {
		return keySelector;
	}

	public JList<String> getAttributeSelector() {
		return attributeSelector;
	}

	public void replaceAttributes(Iterable<String> attributes) {
		attributeSelectorListModel.clear();
		for (String attribute : attributes) {
			attributeSelectorListModel.addElement(attribute);
		}
	}

	public JLabel getExplanation() {
		return explanation;
	}

}
