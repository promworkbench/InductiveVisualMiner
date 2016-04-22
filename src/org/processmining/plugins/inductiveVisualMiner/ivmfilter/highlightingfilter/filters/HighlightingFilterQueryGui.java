package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public class HighlightingFilterQueryGui extends IvMFilterGui {
	
	private static final long serialVersionUID = 879788371461268561L;
	private final JComponent queryBox;

	public HighlightingFilterQueryGui(String title) {
		super(title);
		
		usesVerticalSpace = false;
		
		setLayout( new BorderLayout() );
		//explanation
		{
			queryBox = new JTextArea();
			queryBox.setBackground(Color.lightGray);
			//queryBox.setOpaque(false);
			add(queryBox, BorderLayout.CENTER);
		}
	}

}
