package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboPopup;

import org.apache.commons.lang3.ArrayUtils;
import org.processmining.plugins.InductiveMiner.BoundsPopupMenuListener;

public class MultiComboBox<E> extends JComboBox<E> {

	private static final long serialVersionUID = -6813173798243364734L;
	private BitSet selected; //denotes which elements are currently selected
	private BitSet singular; //denotes which elements do not allow other elements to be selected at the same time.
	private Class<E> clazz;
	private int prevIndex = -1;
	private JCheckBox prevButton;

	/**
	 * We need to override setSelectedItem(Object) to provide developers with
	 * the ability to change the selected object. However, JComboBox calls this
	 * function as well internally. Therefore, we introduce a boolean that
	 * denotes that selection changes should be ignored.
	 */
	private boolean preventSelectionChange = false;

	/**
	 * We need to override setPopupVisible to prevent flickering of the popup
	 * when clicking on a checkbox. This boolean performs the trick.
	 */
	private boolean preventPopupClosing = false;

	/**
	 * We need to do the event handling ourselves; otherwise, listeners are
	 * called before we can do an update.
	 */
	protected CopyOnWriteArrayList<ActionListener> listenerList = new CopyOnWriteArrayList<>();

	public MultiComboBox(Class<E> clazz, E[] aModel) {
		super(aModel);
		this.clazz = clazz;
		selected = new BitSet(aModel.length);
		singular = new BitSet(aModel.length);
		if (aModel.length > 0) {
			selected.set(0);
		}

		addPopupMenuListener(new BoundsPopupMenuListener(true, false));
		setRenderer(new ButtonsRenderer());
		addListeners();
	}

	public void addListeners() {
		JList<E> list = getList();
		if (list != null) {
			CellButtonsMouseListener cbml = new CellButtonsMouseListener();
			list.addMouseListener(cbml);
			list.addMouseMotionListener(cbml);
			ComboBoxMouseListener cbbml = new ComboBoxMouseListener();
			addMouseListener(cbbml);
			addMouseMotionListener(cbbml);
		}
	}

	@Override
	public void addActionListener(ActionListener arg0) {
		listenerList.add(arg0);
	}

	/**
	 * Notify listeners that something changed.
	 */
	protected void notifyActionEvent() {
		for (ActionListener listener : listenerList) {
			listener.actionPerformed(new ActionEvent(MultiComboBox.this, ActionEvent.ACTION_PERFORMED, ""));
		}
	}

	/**
	 * Add an element to the combobox.
	 * 
	 * @param item
	 *            The element to be added.
	 * @param singular
	 *            If true, this element can only be selected on its own, i.e
	 *            there is no possibility to select another element as well.
	 */
	public void addItem(E item, boolean singular) {
		this.singular.set(getList().getModel().getSize(), singular);
		if (getList().getModel().getSize() == 0) {
			selected.set(0);
		}
		super.addItem(item);
	}

	/**
	 * Return an array of selected objects.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public E[] getSelectedObjects() {
		E[] result = (E[]) Array.newInstance(clazz, selected.cardinality());
		int j = 0;
		for (int i = selected.nextSetBit(0); i != -1
				&& i < getList().getModel().getSize(); i = selected.nextSetBit(i + 1)) {
			result[j] = getList().getModel().getElementAt(i);
			j++;
		}
		return result;
	};

	@Override
	public void setSelectedItem(Object anObject) {
		if (!clazz.isInstance(anObject)) {
			throw new RuntimeException("please give me an appropriate object to select");
		}
		if (!preventSelectionChange) {
			selected.clear();
			ListModel<E> model = getList().getModel();
			for (int i = 0; i < model.getSize(); i++) {
				if (model.getElementAt(i).equals(anObject)) {
					selected.set(i);
					return;
				}
			}
		}
	};

	public void setSelectedItems(E[] objects) {
		if (!preventSelectionChange) {
			selected.clear();
			ListModel<E> model = getList().getModel();
			for (int i = 0; i < model.getSize(); i++) {
				if (ArrayUtils.contains(objects, model.getElementAt(i))) {
					selected.set(i);
				}
			}
		}
	};

	@SuppressWarnings("unchecked")
	protected JList<E> getList() {
		Accessible a = getAccessibleContext().getAccessibleChild(0);
		if (a instanceof BasicComboPopup) {
			return (JList<E>) ((BasicComboPopup) a).getList();
		} else {
			return null;
		}
	}

	private JCheckBox getCheckBox(JList<E> list, Point pt, int index) {
		Container c = (Container) list.getCellRenderer().getListCellRendererComponent(list, null, index, false, false);
		Rectangle r = list.getCellBounds(index, index);
		c.setBounds(r);
		pt.translate(-r.x, -r.y);
		Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
		if (b instanceof JCheckBox) {
			return (JCheckBox) b;
		} else {
			return null;
		}
	}

	private void listRepaint(JList<E> list, Rectangle rect) {
		if (rect != null) {
			list.repaint(rect);
		}
	}

	/**
	 * 
	 * @return A human-readable summary that represents the selection.
	 */
	public String getTitle() {
		E[] objects = getSelectedObjects();
		if (objects.length == 0) {
			return "...";
		} else if (objects.length == 1) {
			return objects[0].toString();
		} else {
			return "(" + objects.length + ")";
		}
	}

	/**
	 * If a user clicks on the checkbox, we do not want the popup menu to close.
	 */
	@Override
	public void setPopupVisible(boolean v) {
		if (!preventPopupClosing) {
			super.setPopupVisible(v);
		}
	};

	private void processMouseMove(Point pt) {
		JList<E> list = getList();
		int index = list.locationToIndex(pt);
		@SuppressWarnings("unchecked")
		ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
		if (index < 0) {
			renderer.rolloverCheckBoxIndex = index;
		}
		if (!list.getCellBounds(index, index).contains(pt)) {
			if (prevIndex >= 0) {
				Rectangle r = list.getCellBounds(prevIndex, prevIndex);
				listRepaint(list, r);
			}
			index = -1;
			prevButton = null;
			return;
		}
		if (index >= 0) {
			JCheckBox button = getCheckBox(list, pt, index);
			if (button != null) {
				renderer.rolloverCheckBoxIndex = index;
				if (!button.equals(prevButton)) {
					Rectangle r = list.getCellBounds(prevIndex, index);
					listRepaint(list, r);
				}
			} else {
				renderer.rolloverCheckBoxIndex = -1;
				Rectangle r = null;
				if (prevIndex == index) {
					if (prevIndex >= 0 && prevButton != null) {
						r = list.getCellBounds(prevIndex, prevIndex);
					}
				} else {
					r = list.getCellBounds(index, index);
				}
				listRepaint(list, r);
				prevIndex = -1;
			}
			prevButton = button;
		}
		prevIndex = index;
	}

	private void processMouseRelease(Point pt) {
		JList<E> list = getList();
		int index = list.locationToIndex(pt);
		if (index >= 0 && list.getCellBounds(index, index).contains(pt)) {
			preventSelectionChange = false;
			preventPopupClosing = false;
			JCheckBox checkBox = getCheckBox(list, pt, index);
			if (checkBox != null && !singular.get(index)) {
				//click on checkbox
				checkBox.doClick();
				Rectangle r = list.getCellBounds(index, index);
				listRepaint(list, r);
				repaint();
			} else {
				//click on object
				selected.clear();
				selected.set(index);
				//as we're preventing the pop-up from being hidden, we need to hide it ourselves now
				setPopupVisible(false);
			}
			//notify listeners that something changed
			notifyActionEvent();
		}
	}

	private void processMousePressed(Point pt) {
		JList<E> list = getList();
		int index = list.locationToIndex(pt);
		if (index >= 0) {
			JCheckBox button = getCheckBox(list, pt, index);
			if (button != null) {
				listRepaint(list, list.getCellBounds(index, index));
			}
			preventSelectionChange = true;
			preventPopupClosing = true;
		}
	}

	private class CellButtonsMouseListener extends MouseAdapter {

		@Override
		public void mouseDragged(MouseEvent e) {
			processMouseMove(e.getPoint());
		};

		@Override
		public void mouseMoved(MouseEvent e) {
			processMouseMove(e.getPoint());
		}

		@Override
		public void mousePressed(MouseEvent e) {
			processMousePressed(e.getPoint());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			processMouseRelease(e.getPoint());
		}

		@Override
		public void mouseExited(MouseEvent e) {
			@SuppressWarnings("unchecked")
			JList<E> list = (JList<E>) e.getComponent();
			@SuppressWarnings("unchecked")
			ButtonsRenderer renderer = (ButtonsRenderer) list.getCellRenderer();
			renderer.rolloverCheckBoxIndex = -1;
			list.repaint();
		}
	}

	public class ButtonsRenderer extends JPanel implements ListCellRenderer<E> {

		private static final long serialVersionUID = -7823075274156956172L;
		public int rolloverCheckBoxIndex = -1;
		private final JLabel label = new DefaultListCellRenderer();

		private final JCheckBox checkBox = new JCheckBox(new AbstractAction("") {
			private static final long serialVersionUID = 6852381387051875126L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (singular.get(selected.nextSetBit(0))) {
					selected.clear();
				}

				selected.flip(rolloverCheckBoxIndex);
				if (selected.isEmpty()) {
					selected.set(rolloverCheckBoxIndex);
				}
			}
		});

		private final JPanel checkBoxPanel = new JPanel(new CardLayout(0, 0));
		private final JPanel showCheckBoxPanel = new JPanel(new BorderLayout(0, 0));
		private final JPanel hideCheckBoxPanel = new JPanel(new BorderLayout(0, 0));
		private final static String showCheckBox = "show";
		private final static String hideCheckBox = "hide";

		public ButtonsRenderer() {
			super(new BorderLayout(0, 0));

			label.setOpaque(false);
			add(label);

			add(checkBoxPanel, BorderLayout.EAST);
			checkBoxPanel.add(showCheckBoxPanel, showCheckBox);
			checkBoxPanel.setBorder(BorderFactory.createEmptyBorder());
			checkBoxPanel.setOpaque(false);

			showCheckBoxPanel.setBorder(BorderFactory.createEmptyBorder());
			showCheckBoxPanel.add(checkBox, BorderLayout.CENTER);
			showCheckBoxPanel.setOpaque(false);
			checkBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
			checkBox.setContentAreaFilled(false);

			hideCheckBoxPanel.setBorder(BorderFactory.createEmptyBorder());
			hideCheckBoxPanel.setOpaque(false);
			checkBoxPanel.add(hideCheckBoxPanel, hideCheckBox);

			((CardLayout) checkBoxPanel.getLayout()).show(checkBoxPanel, showCheckBox);
		}

		public JLabel getLabel() {
			return label;
		}

		public JCheckBox getCheckBox() {
			return checkBox;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected,
				boolean cellHasFocus) {

			label.setText(value != null ? value.toString() : "");
			if (index == -1 || list.getModel().getSize() <= 0) {
				//not in pop-up
				checkBoxPanel.setVisible(false);
				label.setText(getTitle());
				label.setForeground(MultiComboBox.this.getForeground());
			} else {
				//in pop-up
				checkBoxPanel.setVisible(true);

				label.setForeground(null);
				if (isSelected) {
					this.setBackground(list.getSelectionBackground());
					this.setForeground(list.getSelectionForeground());
				} else {
					this.setBackground(list.getBackground());
					this.setForeground(list.getForeground());
				}

				boolean f = index == rolloverCheckBoxIndex;
				((CardLayout) checkBoxPanel.getLayout()).show(checkBoxPanel,
						singular.get(index) ? hideCheckBox : showCheckBox);
				checkBox.getModel().setRollover(f);
				checkBox.setSelected(selected.get(index));
			}
			return this;
		}
	}

	private class ComboBoxMouseListener extends MouseAdapter {

		/**
		 * This class contains the mouse listeners on the combobox. These
		 * require a point conversion.
		 * 
		 * @param e
		 * @return
		 */
		public Point transform(MouseEvent e) {
			return SwingUtilities.convertPoint(MultiComboBox.this, e.getPoint(), getList());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			processMouseMove(transform(e));
		};

		@Override
		public void mouseReleased(MouseEvent e) {
			processMouseRelease(transform(e));
		}

		@Override
		public void mousePressed(MouseEvent e) {
			processMousePressed(transform(e));
		}
	}
}
