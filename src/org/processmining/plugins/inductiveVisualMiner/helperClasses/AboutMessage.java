package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class AboutMessage {

	public static void show(JComponent parent) {

		String aboutMessage = "The visual Miner.<br>"//
				+ "<a href=\"http://visualminer.org/\">http://visualminer.org</a>"//
				+ "<p>"//
				+ "Developed by Sander Leemans, Queensland University of Technology" //
				+ "<p>"//
				+ "With contributions of:<br>" //
				+ "Wil van der Aalst, Robert Andrews, Dirk Fahland, Kanika Goel,<br>"//
				+ "Felix Mannhardt, Erik Poppe, Eric Verbeek, Moe Wynn,<br>"//
				+ "and several industry partners of QUT and TU/e"//
				+ "<p>"//
				+ "Using ProM packages:<br>" //
				+ "Evolutionary Tree Miner (Joos Buijs, Boudewijn van Dongen)<br>"//
				+ "PNetReplayer (Arya Adriansyah, Boudewijn van Dongen)<br>"//
				+ "Visualisation3D (Erik Poppe)";

		Font font = IvMDecorator.fontLarger;

		// create some css from the label's font
		StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;");

		// html content
		JEditorPane ep = new JEditorPane("text/html",
				"<html><body style=\"" + style + "\">" + aboutMessage + "</body></html>");

		// handle link events
		ep.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						} catch (IOException | URISyntaxException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		ep.setEditable(false);
		ep.setBackground(IvMDecorator.backGroundColour1);
		//ep.setBackground(label.getBackground());

		// show
		JOptionPane pane = new JOptionPane(ep, JOptionPane.PLAIN_MESSAGE);
		pane.setOpaque(true);
		setBackgroundRecursively(pane);
		pane.setBackground(IvMDecorator.backGroundColour1);
		JDialog dialog = pane.createDialog(parent, "visual Miner - about");
		dialog.setVisible(true);

		//JOptionPane.showMessageDialog(parent, ep, "visual Miner - about", JOptionPane.PLAIN_MESSAGE);
	}

	private static void setBackgroundRecursively(Container c) {
		Component[] m = c.getComponents();
		for (int i = 0; i < m.length; i++) {

			if (m[i] instanceof JPanel) {
				m[i].setBackground(IvMDecorator.backGroundColour1);
			}

			if (m[i] instanceof Container) {
				setBackgroundRecursively((Container) m[i]);
			}
		}
	}
}
