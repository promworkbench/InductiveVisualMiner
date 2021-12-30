package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.RangeSlider;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class AttributeFilterGui extends IvMFilterGui {

	private static final long serialVersionUID = -5662487261061931369L;
	private final JComboBox<String> keySelector;
	private final DefaultComboBoxModel<String> keySelectorModel;
	private String selectedAttributeName;
	private Collection<Attribute> attributes;

	private final JScrollPane valueLiteralScrollPane;
	private final JList<String> valueLiteralSelector;
	private final DefaultListModel<String> valueLiteralSelectorListModel;

	private final RangeSlider valueNumericSelector;
	public final int valueNumericRange = 1000;

	private final JTextArea explanation;
	private final Runnable onUpdate;
	private boolean block = false;

	public AttributeFilterGui(String title, Runnable onUpdate, IvMDecoratorI decorator) {
		super(title, decorator);
		usesVerticalSpace = true;
		this.onUpdate = onUpdate;

		//explanation
		{
			explanation = new JTextArea("explanation");
			decorator.decorate(explanation);
			explanation.setEditable(false);
			explanation.setLineWrap(true);
			explanation.setWrapStyleWord(true);
			explanation.setOpaque(false);
			explanation.setHighlighter(null);
			add(explanation);
		}

		add(Box.createVerticalStrut(10));

		//key selector
		{
			keySelectorModel = new DefaultComboBoxModel<>();
			keySelector = new JComboBox<>();
			decorator.decorate(keySelector);
			keySelector.setModel(keySelectorModel);
			add(keySelector);
		}

		add(Box.createVerticalStrut(10));

		//literal values panel
		{
			valueLiteralSelectorListModel = new DefaultListModel<String>();
			valueLiteralSelector = new JList<String>(valueLiteralSelectorListModel);
			valueLiteralSelector.setCellRenderer(new ListCellRenderer<String>() {
				protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

				public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
						boolean isSelected, boolean cellHasFocus) {

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
			valueLiteralSelector.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			valueLiteralScrollPane = new JScrollPane(valueLiteralSelector);

			valueLiteralScrollPane.getViewport().setOpaque(false);
			valueLiteralSelector.setOpaque(false);
			valueLiteralScrollPane.setOpaque(false);
			//valueLiteralScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			valueLiteralScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			//valueLiteralScrollPane.setPreferredSize(new Dimension(0, 50));
			add(valueLiteralScrollPane);
		}

		//numeric & times values panel
		{
			valueNumericSelector = new RangeSlider(0, valueNumericRange);
			valueNumericSelector.setValue(0);
			valueNumericSelector.setUpperValue(valueNumericRange);
			add(valueNumericSelector);
		}

		setController();
	}

	public void setController() {
		// Key selector
		keySelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!block) {
					block = true;
					updateValues();
					onUpdate.run();
					selectedAttributeName = (String) keySelectorModel.getSelectedItem();
					block = false;
				}
			}
		});

		// literal value selector
		valueLiteralSelector.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (!block) {
					onUpdate.run();
				}
			}
		});

		// numeric value selector
		valueNumericSelector.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!block) {
					onUpdate.run();
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
				keySelector.setSelectedIndex(0);
				selectedAttributeName = (String) keySelector.getSelectedItem();
			}
		}

		updateValues();
	}

	private void updateValues() {
		Attribute attribute = getSelectedAttribute();
		if (attribute.isLiteral()) {
			valueLiteralSelectorListModel.clear();
			for (String a : getSelectedAttribute().getStringValues()) {
				valueLiteralSelectorListModel.addElement(a);
			}
			valueLiteralScrollPane.setVisible(true);
			valueNumericSelector.setVisible(false);
		} else if (attribute.isNumeric()) {
			valueLiteralScrollPane.setVisible(false);
			valueNumericSelector.setVisible(true);
		} else if (attribute.isTime()) {
			valueLiteralScrollPane.setVisible(false);
			valueNumericSelector.setVisible(true);
		} else if (attribute.isDuration()) {
			valueLiteralScrollPane.setVisible(false);
			valueNumericSelector.setVisible(true);
		}
	}

	public JComboBox<String> getKeySelector() {
		return keySelector;
	}

	public JList<String> getValueSelector() {
		return valueLiteralSelector;
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

	public List<String> getSelectedLiterals() {
		return valueLiteralSelector.getSelectedValuesList();
	}

	public double getSelectedNumericMin() {
		return getSelectedAttribute().getNumericMin()
				+ (getSelectedAttribute().getNumericMax() - getSelectedAttribute().getNumericMin())
						* (valueNumericSelector.getValue() / (valueNumericRange * 1.0));
	}

	public double getSelectedNumericMax() {
		return getSelectedAttribute().getNumericMin()
				+ (getSelectedAttribute().getNumericMax() - getSelectedAttribute().getNumericMin())
						* (valueNumericSelector.getUpperValue() / (valueNumericRange * 1.0));
	}

	public long getSelectedTimeMin() {
		return (long) (getSelectedAttribute().getTimeMin()
				+ (getSelectedAttribute().getTimeMax() - getSelectedAttribute().getTimeMin())
						* (valueNumericSelector.getValue() / (valueNumericRange * 1.0)));
	}

	public long getSelectedTimeMax() {
		return (long) (getSelectedAttribute().getTimeMin()
				+ (getSelectedAttribute().getTimeMax() - getSelectedAttribute().getTimeMin())
						* (valueNumericSelector.getUpperValue() / (valueNumericRange * 1.0)));
	}

	public long getSelectedDurationMin() {
		return (long) (getSelectedAttribute().getDurationMin()
				+ (getSelectedAttribute().getDurationMax() - getSelectedAttribute().getDurationMin())
						* (valueNumericSelector.getValue() / (valueNumericRange * 1.0)));
	}

	public long getSelectedDurationMax() {
		return (long) (getSelectedAttribute().getDurationMin()
				+ (getSelectedAttribute().getDurationMax() - getSelectedAttribute().getDurationMin())
						* (valueNumericSelector.getUpperValue() / (valueNumericRange * 1.0)));
	}

	public boolean isFiltering() {
		if (getSelectedAttribute() == null) {
			//only happens in an empty log
			return false;
		}
		if (getSelectedAttribute().isLiteral()) {
			//literal
			return !valueLiteralSelector.isSelectionEmpty();
		} else {
			//time
			return valueNumericSelector.getValue() != valueNumericSelector.getMinimum()
					|| valueNumericSelector.getUpperValue() != valueNumericSelector.getMaximum();
		}
	}

	private static DecimalFormat numberFormat = new DecimalFormat("#.##");

	public String getExplanation() {
		String intro = "having ";
		if (!getSelectedAttribute().isVirtual()) {
			intro += "attribute `" + getSelectedAttribute().getName() + "' ";
		} else {
			intro += getSelectedAttribute() + " ";
		}

		if (getSelectedAttribute().isLiteral()) {
			StringBuilder s = new StringBuilder();
			s.append(intro);
			s.append("being ");
			List<String> attributes = getSelectedLiterals();
			if (attributes.size() > 1) {
				s.append("either ");
			}
			for (int i = 0; i < attributes.size(); i++) {
				s.append("`");
				s.append(attributes.get(i));
				s.append("'");
				if (i == attributes.size() - 2) {
					s.append(" or ");
				} else if (i < attributes.size() - 1) {
					s.append(", ");
				}
			}
			return s.toString();
		} else if (getSelectedAttribute().isNumeric()) {
			return intro + "between " + numberFormat.format(getSelectedNumericMin()) + " and "
					+ numberFormat.format(getSelectedNumericMax());
		} else if (getSelectedAttribute().isTime()) {
			return intro + "between " + ResourceTimeUtils.timeToString(getSelectedTimeMin()) + " and "
					+ ResourceTimeUtils.timeToString(getSelectedTimeMax());
		} else if (getSelectedAttribute().isDuration()) {
			return intro + "between " + ResourceTimeUtils.getDuration(getSelectedDurationMin()) + " and "
					+ ResourceTimeUtils.getDuration(getSelectedDurationMax());
		} else {
			return "blaaaa";
		}
	}

	@Override
	public void setForegroundRecursively(Color colour) {
		if (explanation != null && keySelector != null && valueLiteralSelector != null) {
			explanation.setForeground(colour);
			keySelector.setForeground(colour);
			valueLiteralSelector.setForeground(colour);
			valueNumericSelector.setForeground(colour);
		}
	}

}
