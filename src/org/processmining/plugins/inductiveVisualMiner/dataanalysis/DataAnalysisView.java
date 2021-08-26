package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMPanel;

public class DataAnalysisView extends JPanel {

	private static final long serialVersionUID = -5719337013697465055L;

	public DataAnalysisView(IvMDecoratorI decorator, JTable table, String explanation) {
		setLayout(new BorderLayout());

		IvMPanel topPanel = new IvMPanel(decorator);
		topPanel.setLayout(new BorderLayout());
		add(topPanel, BorderLayout.CENTER);

		JTextArea explanationT = new JTextArea();
		explanationT.setLineWrap(true);
		explanationT.setWrapStyleWord(true);
		explanationT.setEnabled(false);
		explanationT.setText(explanation);
		explanationT.setMargin(new Insets(5, 5, 5, 5));
		decorator.decorate(explanationT);
		topPanel.add(explanationT, BorderLayout.PAGE_START);

		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		topPanel.add(scrollPane, BorderLayout.CENTER);
	}

}