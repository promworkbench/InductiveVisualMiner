package org.processmining.plugins.inductiveVisualMiner.colouringFilter.filters;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.colouringFilter.ColouringFilterGui;

public class ColouringFilterQueryGui extends ColouringFilterGui {
	
	private static final long serialVersionUID = 879788371461268561L;
	private final JComponent queryBox;

	public ColouringFilterQueryGui(String title) {
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
