package org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration;

import java.awt.Color;
import java.awt.GradientPaint;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;

/**
 * A JPanel that can swap its gradient background. If no gradient is applied, it
 * is transparent (not opaque) and a border is drawn.
 * 
 * @author sander
 *
 */
public class SwitchPanel extends IvMPanel {

	private static final long serialVersionUID = -7078479959072318040L;
	protected Border border = new MatteBorder(2, 2, 2, 2, Color.green);
	protected Border emptyBorder = new EmptyBorder(2, 2, 2, 2);

	public SwitchPanel() {
		update();
	}

	private void update() {
		GradientPaint gp;
		if (isEnabled()) {
			gp = getEnabledGradient();
		} else {
			gp = getDisabledGradient();
		}

		if (gp == null) {
			setBorder(border);
		} else {
			setBorder(emptyBorder);
		}
	}

	@Override
	protected GradientPaint getGradient() {
		if (isEnabled()) {
			return getEnabledGradient();
		} else {
			return getDisabledGradient();
		}
	}

	protected GradientPaint getEnabledGradient() {
		return new GradientPaint(0, 0, IvMDecorator.backGroundColour2, 0, getHeight(), IvMDecorator.backGroundColour1);
	}

	protected GradientPaint getDisabledGradient() {
		return new GradientPaint(0, 0, IvMDecorator.backGroundColour1, 0, getHeight(), IvMDecorator.backGroundColour2);
	}

	public void setBorder(int top, int left, int bottom, int right, Color colour) {
		border = new MatteBorder(top, left, bottom, right, colour);
		emptyBorder = new EmptyBorder(top, left, bottom, right);
		update();
	}

	@Override
	public void setEnabled(boolean arg0) {
		super.setEnabled(arg0);
		update();
	}

}
