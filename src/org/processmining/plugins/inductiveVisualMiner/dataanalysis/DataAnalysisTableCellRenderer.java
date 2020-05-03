package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class DataAnalysisTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -7148998664457522071L;

	public final DecimalFormat numberFormat = new DecimalFormat("0.0000");

	public DataAnalysisTableCellRenderer() {
		IvMDecorator.decorate(this);
		setHorizontalTextPosition(SwingConstants.LEADING);
		setVerticalAlignment(JLabel.TOP);
		setBackground(IvMDecorator.backGroundColour2); //background is for selection only
	}

	public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus,
			int row, int column) {
		boolean forceNumerical = false;

		if (object == null) {
			setText("");
			setIcon(null);
		} else if (object instanceof ImageIcon) {
			setText("");
			setIcon((ImageIcon) object);
		} else if (object instanceof Pair<?, ?>) {
			@SuppressWarnings("unchecked")
			Pair<Integer, ImageIcon> p = (Pair<Integer, ImageIcon>) object;
			setText(p.getA() + " ");
			setIcon(p.getB());
			forceNumerical = true;
		} else {
			setText(object.toString());
			setIcon(null);
		}

		if (forceNumerical) {
			setHorizontalAlignment(JLabel.RIGHT);
			setFont(IvMDecorator.fontMonoSpace);
		} else if (object instanceof DisplayType) {
			setHorizontalAlignment(((DisplayType) object).getHorizontalAlignment());
			setFont(IvMDecorator.fontMonoSpace);
		} else {
			setHorizontalAlignment(JLabel.LEFT);
			setFont(IvMDecorator.fontLarger);
		}

		if (isSelected) {
			setOpaque(true);
		} else {
			setOpaque(false);
		}

		return this;
	}
}