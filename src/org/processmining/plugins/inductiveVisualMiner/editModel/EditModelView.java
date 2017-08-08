package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.border.EmptyBorder;

import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeEditor;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class EditModelView extends SideWindow {

	private static final long serialVersionUID = 2710387572788600877L;

	private final EfficientTreeEditor editor;

	public EditModelView(Component parent) {
		super(parent, "edit model - Inductive visual Miner");
		setLayout(new BorderLayout());
		getContentPane().setBackground(IvMDecorator.backGroundColour1);

		editor = new IvMEfficientTreeEditor(null, "Minnig tree...");
		add(editor, BorderLayout.CENTER);
	}

	public EfficientTreeEditor getEditor() {
		return editor;
	}

	public static class IvMEfficientTreeEditor extends EfficientTreeEditor {
		private static final long serialVersionUID = -3553121183719500176L;

		public IvMEfficientTreeEditor(EfficientTree tree, String message) {
			super(tree, message);
			IvMDecorator.decorate(text);
			IvMDecorator.decorate(errorMessage);
			text.setBackground(IvMDecorator.buttonColour);
			text.setAntiAliasingEnabled(true);

			text.setBorder(new EmptyBorder(0, 2, 2, 2));

			text.setForeground(IvMDecorator.textColour);
			text.setFont(text.getFont().deriveFont(20));

			text.setSelectionColor(IvMDecorator.backGroundColour2);
			text.setSelectedTextColor(IvMDecorator.buttonColour);
			text.setUseSelectedTextColor(true);

			text.setCurrentLineHighlightColor(IvMDecorator.backGroundColour1);
			text.setFadeCurrentLineHighlight(true);

			text.revalidate();
		}
	}
}
