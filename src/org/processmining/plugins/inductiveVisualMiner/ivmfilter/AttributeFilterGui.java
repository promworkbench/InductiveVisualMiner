package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
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
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class AttributeFilterGui extends IvMFilterGui {

	private static final long serialVersionUID = -5662487261061931369L;
	private final JComboBox<AttributeKey> keySelector;

	private final JList<String> valueLiteralSelector;
	private final DefaultListModel<String> valueLiteralSelectorListModel;

	private final RangeSlider valueNumericSelector;
	public final int valueNumericRange = 1000;

	private final JTextArea explanation;
	private final boolean empty;
	private final Runnable onUpdate;
	private boolean block = false;

	public AttributeFilterGui(String title, Collection<Attribute> attributes, Runnable onUpdate) {
		super(title);
		usesVerticalSpace = true;
		empty = attributes.isEmpty();
		this.onUpdate = onUpdate;

		//explanation
		{
			explanation = new JTextArea("Only highlight traces of which the ");
			IvMDecorator.decorate(explanation);
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
			keySelector = new JComboBox<>(new AttributeKey[0]);
			IvMDecorator.decorate(keySelector);
			if (empty) {
				keySelector.addItem(AttributeKey.message("(no attributes present)"));
				keySelector.setEnabled(false);
			} else {
				for (Attribute attribute : attributes) {
					keySelector.addItem(AttributeKey.attribute(attribute));
				}
			}
			keySelector.setSelectedIndex(0);
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
			JScrollPane scrollPane = new JScrollPane(valueLiteralSelector);

			scrollPane.getViewport().setOpaque(false);
			valueLiteralSelector.setOpaque(false);
			scrollPane.setOpaque(false);
			add(valueLiteralSelector);
		}

		//numeric & times values panel
		{
			valueNumericSelector = new RangeSlider(0, valueNumericRange);
			valueNumericSelector.setValue(0);
			valueNumericSelector.setUpperValue(valueNumericRange);
			add(valueNumericSelector);
		}

		updateValues();
		setController();
	}

	public void setController() {
		// Key selector
		keySelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				block = true;
				updateValues();
				onUpdate.run();
				block = false;
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

	private void updateValues() {
		if (!empty) {
			if (getSelectedAttribute().isLiteral()) {
				valueLiteralSelectorListModel.clear();
				for (String a : getSelectedAttribute().getStringValues()) {
					valueLiteralSelectorListModel.addElement(a);
				}
				//((CardLayout) valueLayout.getLayout()).show(valueLayout, "literal");
				valueLiteralSelector.setVisible(true);
				valueNumericSelector.setVisible(false);
			} else if (getSelectedAttribute().isNumeric()) {
				//((CardLayout) valueLayout.getLayout()).show(valueLayout, "numeric");
				valueLiteralSelector.setVisible(false);
				valueNumericSelector.setVisible(true);
			} else if (getSelectedAttribute().isTime()) {
				//((CardLayout) valueLayout.getLayout()).show(valueLayout, "numeric");
				valueLiteralSelector.setVisible(false);
				valueNumericSelector.setVisible(true);
			} else if (getSelectedAttribute().isDuration()) {
				valueLiteralSelector.setVisible(false);
				valueNumericSelector.setVisible(true);
				//			} else if (getSelectedAttribute().isTraceDuration()) {
				//				valueLiteralSelector.setVisible(false);
				//				valueNumericSelector.setVisible(true);
				//			} else if (getSelectedAttribute().isTraceNumberofEvents()) {
				//				valueLiteralSelector.setVisible(false);
				//				valueNumericSelector.setVisible(true);
			}
		}
	}

	public JComboBox<AttributeKey> getKeySelector() {
		return keySelector;
	}

	public JList<String> getValueSelector() {
		return valueLiteralSelector;
	}

	public JTextArea getExplanationLabel() {
		return explanation;
	}

	public Attribute getSelectedAttribute() {
		return ((AttributeKey) keySelector.getSelectedItem()).getAttribute();
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
			return intro + "between " + ResourceTimeUtils.getDuration(getSelectedTimeMin()) + " and "
					+ ResourceTimeUtils.getDuration(getSelectedTimeMax());
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
