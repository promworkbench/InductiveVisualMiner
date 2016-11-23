package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.Component;

import javax.swing.JFrame;

public abstract class SideWindow extends JFrame {

	private static final long serialVersionUID = -6870740281868257007L;

	public SideWindow(Component parent, String title) {
		super(title);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setSize(400, 600);
		setLocationRelativeTo(parent);
	}
	
	public boolean isVisible() {
		return super.isVisible();
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible == true) {
			toFront();
			requestFocus();
			repaint();
		}
	}
	
	public void swapVisibility() {
		setVisible(!isVisible());
	}
	
	public void enableAndShow() {
		setVisible(true);
	}
	
	public @Override void toFront() {
	    int sta = super.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;

	    super.setExtendedState(sta);
	    super.setAlwaysOnTop(true);
	    super.toFront();
	    super.requestFocus();
	    super.setAlwaysOnTop(false);
	}
}
