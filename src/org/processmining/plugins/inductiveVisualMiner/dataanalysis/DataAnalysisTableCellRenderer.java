package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class DataAnalysisTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = -7148998664457522071L;

	public final DecimalFormat numberFormat = new DecimalFormat("0.0000");

	private Color selectedBackgroundColour = null;
	private Color selectedForegroundColour = null;

	public DataAnalysisTableCellRenderer() {
		IvMDecorator.decorate(this);
		setHorizontalTextPosition(SwingConstants.LEADING);
		setVerticalAlignment(JLabel.TOP);
	}

	public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected, boolean hasFocus,
			int row, int column) {

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
			setText(object.toString());
			setIcon(null);
		}

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
			setForeground(IvMDecorator.textColour);
			setOpaque(false);
		}

		return this;
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