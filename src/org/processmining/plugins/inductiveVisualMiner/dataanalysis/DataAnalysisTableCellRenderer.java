package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class DataAnalysisTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -7148998664457522071L;

	public final DecimalFormat numberFormat = new DecimalFormat("0.0000");

	private Color selectedBackgroundColour = null;
	private Color selectedForegroundColour = null;

	private static Border emptyBorder = BorderFactory.createEmptyBorder();
	private static Border firstRowOfGroupBorder = new MatteBorder(1, 0, 0, 0, Color.white);

	public DataAnalysisTableCellRenderer() {
		IvMDecorator.decorate(this);
		setHorizontalTextPosition(SwingConstants.LEADING);
		setVerticalAlignment(JLabel.TOP);
	}

	public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus,
			int row, int column) {

		//find out if we are in the first row of a group (of at least 2)
		int firstModelColumn = table.convertColumnIndexToView(0);
		boolean firstRowOfGroup = !getPreviousRowValue(table, row, firstModelColumn)
				.equals(getRowValue(table, row, firstModelColumn))
				&& getRowValue(table, row, firstModelColumn).equals(getNextRowValue(table, row, firstModelColumn));

		//find out if we are different than the row before
		boolean sameAsPreviousRow = getPreviousRowValue(table, row, column).equals(getRowValue(table, row, column))
				&& !(object instanceof DisplayType);

		//default properties
		setHorizontalAlignment(JLabel.LEFT);
		setFont(IvMDecorator.fontLarger);

		if (object == null) {
			setText("");
			setIcon(null);
		} else if (object instanceof DisplayType) {
			if (((DisplayType) object).getType() == Type.image) {
				//image
				setText("");
				BufferedImage im = ((DisplayType.Image) object).getImage();
				setIcon(new ImageIcon(im));
			} else {
				//text
				setText(object.toString());
				setIcon(null);
			}
			setHorizontalAlignment(((DisplayType) object).getHorizontalAlignment());
			setFont(IvMDecorator.fontMonoSpace);
		} else if (object instanceof ImageIcon) {
			setText("");
			setIcon((ImageIcon) object);
		} else if (object instanceof Pair<?, ?>) {
			@SuppressWarnings("unchecked")
			Pair<Integer, ImageIcon> p = (Pair<Integer, ImageIcon>) object;
			setText(p.getA() + " ");
			setIcon(p.getB());
		} else {
			if (sameAsPreviousRow) {
				//in the first column, do not repeat values
				setText("   \"");
			} else {
				setText(object.toString());
			}
			setIcon(null);
		}

		//set border
		if (firstRowOfGroup && row > 0) {
			setBorder(firstRowOfGroupBorder);
		} else {
			setBorder(emptyBorder);
		}

		//set colour
		if (isSelected) {
			if (getSelectedBackgroundColour() != null) {
				setBackground(getSelectedBackgroundColour());
			} else {
				setBackground(IvMDecorator.textColour);
			}
			if (getSelectedForegroundColour() != null) {
				setForeground(getSelectedForegroundColour());
			} else {
				setForeground(IvMDecorator.backGroundColour1);
			}
			setOpaque(true);
		} else {
			if (sameAsPreviousRow) {
				//do not repeat values
				setForeground(Color.white);
			} else {
				setForeground(IvMDecorator.textColour);
			}
			setOpaque(false);
		}

		return this;
	}

	private String getRowValue(JTable table, int row, int column) {
		int modelRow = table.convertRowIndexToModel(row);
		int modelColumn = table.convertColumnIndexToModel(column);

		return table.getModel().getValueAt(modelRow, modelColumn).toString();
	}

	private String getPreviousRowValue(JTable table, int row, int column) {
		if (row <= 0) {
			return "";
		}
		int modelRow = table.convertRowIndexToModel(row - 1);
		int modelColumn = table.convertColumnIndexToModel(column);

		return table.getModel().getValueAt(modelRow, modelColumn).toString();
	}

	private String getNextRowValue(JTable table, int row, int column) {
		if (row >= table.getModel().getRowCount() - 1) {
			return "";
		}
		int modelRow = table.convertRowIndexToModel(row + 1);
		int modelColumn = table.convertColumnIndexToModel(column);

		return table.getModel().getValueAt(modelRow, modelColumn).toString();
	}

	public Color getSelectedBackgroundColour() {
		return selectedBackgroundColour;
	}

	public void setSelectedBackgroundColour(Color selectedBackgroundColour) {
		this.selectedBackgroundColour = selectedBackgroundColour;
	}

	public Color getSelectedForegroundColour() {
		return selectedForegroundColour;
	}

	public void setSelectedForegroundColour(Color selectedForegroundColour) {
		this.selectedForegroundColour = selectedForegroundColour;
	}
}