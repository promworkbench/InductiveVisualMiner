package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.awt.BorderLayout;
import java.awt.Component;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeEditor;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

public class EditModelView extends SideWindow {

	private static final long serialVersionUID = 2710387572788600877L;

	private final EfficientTreeEditor editor;

	public EditModelView(Component parent) {
		super(parent, "edit model - Inductive visual Miner");
		setLayout(new BorderLayout());
		editor = new EfficientTreeEditor(null, "Mining tree...");
		add(editor, BorderLayout.CENTER);
	}
	
	public EfficientTreeEditor getEditor() {
		return editor;
	}
	
	

}
