package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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

	private final JCheckBox valueBooleanTrue;
	private final JCheckBox valueBooleanFalse;

	private final JTextArea explanation;
	private final Runnable onUpdate;
	private boolean block = false;
	private final JPanel valuesPanel;
	private final CardLayout valuesPanelLayout;

	public AttributeFilterGui(String title, Runnable onUpdate, IvMDecoratorI decorator) {
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

		//values
		{
			valuesPanel = new JPanel();
			valuesPanelLayout = new CardLayout();
			valuesPanel.setLayout(valuesPanelLayout);
			valuesPanel.setOpaque(false);
			add(valuesPanel, BorderLayout.CENTER);

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
				valuesPanel.add(valueLiteralScrollPane, "literal");
			}

			//numeric & times values panel
			{
				valueNumericSelector = new RangeSlider(0, valueNumericRange);
				valueNumericSelector.setValue(0);
				valueNumericSelector.setUpperValue(valueNumericRange);
				JPanel blup = new JPanel();
				blup.setOpaque(false);
				blup.setLayout(new BorderLayout());
				blup.add(valueNumericSelector, BorderLayout.PAGE_START);
				valuesPanel.add(blup, "numeric");
			}

			//boolean values panel
			{
				valueBooleanTrue = new JCheckBox("include true values");
				valueBooleanFalse = new JCheckBox("include false values");
				decorator.decorate(valueBooleanTrue);
				decorator.decorate(valueBooleanFalse);
				JPanel blup = new JPanel();
				blup.setOpaque(false);
				blup.add(valueBooleanTrue);
				blup.add(Box.createHorizontalGlue());
				blup.add(valueBooleanFalse);
				valuesPanel.add(blup, "boolean");
			}
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

		valueBooleanTrue.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!block) {
					onUpdate.run();
				}
			}
		});
		valueBooleanFalse.addChangeListener(new ChangeListener() {
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
				block = true;
				keySelector.setSelectedIndex(0);
				selectedAttributeName = (String) keySelector.getSelectedItem();
				block = false;
			}
		}

		updateValues();
	}

	private void updateValues() {
		Attribute attribute = getSelectedAttribute();
		if (attribute.isLiteral()) {
			DefaultListSelectionModel selectionModel = (DefaultListSelectionModel) valueLiteralSelector
					.getSelectionModel();
			List<String> backupSelection = valueLiteralSelector.getSelectedValuesList();

			valueLiteralSelectorListModel.clear();
			for (String a : getSelectedAttribute().getStringValues()) {
				valueLiteralSelectorListModel.addElement(a);
			}

			//reset selection
			for (int i = 0, c = valueLiteralSelectorListModel.getSize(); i < c; i++) {
				if (backupSelection.contains(valueLiteralSelectorListModel.getElementAt(i))) {
					selectionModel.addSelectionInterval(i, i);
				}
			}

			valuesPanelLayout.show(valuesPanel, "literal");
		} else if (attribute.isNumeric()) {
			valuesPanelLayout.show(valuesPanel, "numeric");
		} else if (attribute.isBoolean()) {
			valuesPanelLayout.show(valuesPanel, "boolean");
		} else if (attribute.isTime()) {
			valuesPanelLayout.show(valuesPanel, "numeric");
		} else if (attribute.isDuration()) {
			valuesPanelLayout.show(valuesPanel, "numeric");
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

	public boolean getSelectedBooleanTrue() {
		return valueBooleanTrue.isSelected();
	}

	public boolean getSelectedBooleanFalse() {
		return valueBooleanFalse.isSelected();
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
			if (attributes.size() == 0) {
				s.append("any value");
			} else {
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
			}
			return s.toString();
		} else if (getSelectedAttribute().isNumeric()) {
			return intro + "between " + numberFormat.format(getSelectedNumericMin()) + " and "
					+ numberFormat.format(getSelectedNumericMax());
		} else if (getSelectedAttribute().isBoolean()) {
			if (getSelectedBooleanTrue() && getSelectedBooleanFalse()) {
				return intro + "being either true or false";
			} else if (getSelectedBooleanTrue()) {
				return intro + "being true";
			} else if (getSelectedBooleanFalse()) {
				return intro + "being false";
			} else {
				return intro + "being any value";
			}
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
}