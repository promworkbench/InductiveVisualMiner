package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;

public class DataAnalysisView extends SideWindow {

	private static final long serialVersionUID = -1113805892324898124L;

	private static final String busyMessageString = "Data analysis will become available after the computations of alignments and highlighting filters have been completed.";
	private static final String messageString = "For each numeric trace attribute, the correlation of that attribute with the trace's fitness (that is, its conformance to the model) is shown.";

	private final JTextArea busyMessage;
	private final DataAnalysisAttributesPanel attributesPanel;
	private final JScrollPane scrollPane;

	public DataAnalysisView(Component parent) {
		super(parent, "Data analysis - " + InductiveVisualMinerPanel.title);
		setLayout(new BorderLayout());
		IvMPanel topPanel = new IvMPanel();
		topPanel.setLayout(new BorderLayout());
		add(topPanel, BorderLayout.CENTER);

		attributesPanel = new DataAnalysisAttributesPanel();
		scrollPane = new JScrollPane(attributesPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getViewport().setBackground(IvMDecorator.backGroundColour1);
		topPanel.add(scrollPane, BorderLayout.CENTER);

		busyMessage = new JTextArea(busyMessageString);
		IvMDecorator.decorate(busyMessage);
		busyMessage.setWrapStyleWord(true);
		busyMessage.setLineWrap(true);
		busyMessage.setEnabled(false);
		busyMessage.setMargin(new Insets(5, 5, 5, 5));
		topPanel.add(busyMessage, BorderLayout.PAGE_START);

		invalidateContent();
	}

	public void invalidateContent() {
		busyMessage.setText(busyMessageString);
		scrollPane.setVisible(false);
		revalidate();
		repaint();
	}

	public void initialiseAttributes(AttributesInfo attributesInfo) {
		//clear the existing list
		attributesPanel.removeAll();

		for (Attribute attribute : attributesInfo.getTraceAttributes()) {
			if (DataAnalysis.isSupported(attribute)) {
				DataAnalysisAttributeView attributePanel = new DataAnalysisAttributeView(attribute);
				attributesPanel.add(attributePanel);
			}
		}

		revalidate();
		repaint();
	}

	public void setDataAnalysis(DataAnalysis dataAnalysis) {
		busyMessage.setText(messageString);
		scrollPane.setVisible(true);

		attributesPanel.setDataAnalysis(dataAnalysis);

		revalidate();
		repaint();
	}
}