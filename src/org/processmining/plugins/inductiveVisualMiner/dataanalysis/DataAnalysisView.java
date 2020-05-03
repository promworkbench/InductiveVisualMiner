package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator.IvMPanel;

public class DataAnalysisView extends JPanel {

	private static final long serialVersionUID = -5719337013697465055L;

	public DataAnalysisView(DataAnalysisTable table, String explanation) {
		setLayout(new BorderLayout());

		IvMPanel topPanel = new IvMPanel();
		topPanel.setLayout(new BorderLayout());
		add(topPanel, BorderLayout.CENTER);

		JTextArea explanationT = new JTextArea();
		explanationT.setLineWrap(true);
		explanationT.setWrapStyleWord(true);
		explanationT.setEnabled(false);
		explanationT.setText(explanation);
		explanationT.setMargin(new Insets(5, 5, 5, 5));
		IvMDecorator.decorate(explanationT);
		topPanel.add(explanationT, BorderLayout.PAGE_START);

		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().setBackground(IvMDecorator.backGroundColour1);
		topPanel.add(scrollPane, BorderLayout.CENTER);
	}

}