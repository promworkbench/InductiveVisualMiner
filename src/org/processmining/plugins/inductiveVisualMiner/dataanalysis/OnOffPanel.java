package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMPanel;

public class OnOffPanel<X extends JComponent> extends IvMPanel {

	private static final long serialVersionUID = -2730521457101145974L;

	private JPanel offPanel = new JPanel();
	private JLabel offMessage;
	private final JCheckBox offCheckBox;
	private X onPanel;
	private boolean on = true;

	public OnOffPanel(IvMDecoratorI decorator, X onPanel) {
		this(decorator, onPanel, false);
	}

	public OnOffPanel(IvMDecoratorI decorator, X onPanel, boolean switchable) {
		super(decorator);
		setLayout(new CardLayout());
		this.onPanel = onPanel;

		offPanel.setLayout(new BorderLayout());
		offPanel.setOpaque(false);
		if (switchable) {
			offMessage = null;
			offCheckBox = new JCheckBox("off");
			offCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
			decorator.decorate(offCheckBox);
			offCheckBox.setOpaque(false);
			offPanel.add(offCheckBox, BorderLayout.CENTER);
		} else {
			offCheckBox = null;
			try {
				InputStream in = getClass().getResourceAsStream("loading.gif");
				ImageIcon loading = new ImageIcon(
						Toolkit.getDefaultToolkit().createImage(org.apache.commons.io.IOUtils.toByteArray(in)));
				offMessage = new JLabel("off", loading, SwingConstants.CENTER);
			} catch (IOException e) {
				offMessage = new JLabel("off", SwingConstants.CENTER);
			}
			decorator.decorate(offMessage);
			offMessage.setOpaque(false);
			offPanel.add(offMessage, BorderLayout.CENTER);
		}
		add(offPanel, "off");

		add(onPanel, "on");

		on();
	}

	public void on() {
		((CardLayout) getLayout()).show(this, "on");
		on = true;
	}

	public void off() {
		((CardLayout) getLayout()).show(this, "off");
		on = false;
	}

	public void set(boolean on) {
		if (on) {
			on();
		} else {
			off();
		}
	}

	public boolean isOn() {
		return on;
	}

	public boolean isSwitchable() {
		return offCheckBox != null;
	}

	public JCheckBox getSwitch() {
		return offCheckBox;
	}

	public String getOffMessage() {
		return offMessage.getText();
	}

	public void setOffMessage(String message) {
		if (isSwitchable()) {
			offCheckBox.setText(message);
		} else {
			offMessage.setText(message);
		}
	}

	public X getOnPanel() {
		return onPanel;
	}

	public JPanel getOffPanel() {
		return offPanel;
	}
}