package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeEditor;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class EditModelView extends SideWindow {

	private static final long serialVersionUID = 2710387572788600877L;

	private final JPanel panel;
	private final EfficientTreeEditor treeEditor;
	private final JComponent dfgEditor;

	private final static String TREEPANEL = "tree panel";
	private final static String DFGPANEL = "dfg panel";

	public EditModelView(Component parent) {
		super(parent, "edit model - Inductive visual Miner");

		getContentPane().setBackground(IvMDecorator.backGroundColour1);

		panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new CardLayout());

		treeEditor = new IvMEfficientTreeEditor(null, "Minnig tree...");
		panel.add(treeEditor, TREEPANEL);

		dfgEditor = new JLabel("Dfg editor coming soon...");
		panel.add(dfgEditor, DFGPANEL);
	}

	public void setModel(IvMModel model) {
		if (model.isTree()) {
			treeEditor.setVisible(false);
			treeEditor.setTree(model.getTree());

			//show the tree editor
			CardLayout cl = (CardLayout) panel.getLayout();
			cl.show(panel, TREEPANEL);
		} else {
			//TODO: update dfg editor

			//show the dfg editor
			CardLayout cl = (CardLayout) panel.getLayout();
			cl.show(panel, TREEPANEL);
		}
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

	public void setMessage(String message) {
		treeEditor.setMessage(message);
		//TODO dfgEditor.setMessage(message);
	}

	/**
	 * Redirect the actionlistener to the appropriate type.
	 * 
	 * @param actionListener
	 */
	public void addActionListener(final ActionListener actionListener) {
		treeEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof EfficientTree) {
					e.setSource(new IvMModel(new IvMEfficientTree((EfficientTree) e.getSource())));
				} else if (e.getSource() instanceof Dfg) {
					e.setSource(new IvMModel((EfficientTree) e.getSource()));
				}
				actionListener.actionPerformed(e);
			}
		});
	}
}
