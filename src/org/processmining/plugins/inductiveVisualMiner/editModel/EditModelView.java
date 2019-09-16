package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeEditor;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;

public class EditModelView extends SideWindow {

	private static final long serialVersionUID = 2710387572788600877L;

	private final JPanel panel;
	private final EfficientTreeEditor treeEditor;
	private final DfgEditor dfgEditor;

	private final static String TREEPANEL = "tree panel";
	private final static String DFGPANEL = "dfg panel";

	public EditModelView(Component parent) {
		super(parent, "edit model - " + InductiveVisualMinerPanel.title);

		getContentPane().setBackground(IvMDecorator.backGroundColour1);

		panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new CardLayout());

		treeEditor = new IvMEfficientTreeEditor(null, "Minnig tree...");
		panel.add(treeEditor, TREEPANEL);

		dfgEditor = new IvMDfgEditor(null, "Mining directly follows graph...");
		panel.add(dfgEditor, DFGPANEL);
	}

	public void setModel(IvMModel model) {
		if (model.isTree()) {
			treeEditor.setTree(model.getTree());

			//show the tree editor
			CardLayout cl = (CardLayout) panel.getLayout();
			cl.show(panel, TREEPANEL);
		} else {
			dfgEditor.setDfg(model.getDfg());

			//show the dfg editor
			CardLayout cl = (CardLayout) panel.getLayout();
			cl.show(panel, DFGPANEL);
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

	public static class IvMDfgEditor extends DfgEditor {
		private static final long serialVersionUID = -3553121183719500176L;

		public IvMDfgEditor(DirectlyFollowsModel dfg, String message) {
			super(dfg, message);

			IvMDecorator.decorate(labelStartActivities);
			IvMDecorator.decorate(labelEdges);
			IvMDecorator.decorate(labelEndActivities);

			IvMDecorator.decorate(textStartActivities);
			textStartActivities.setBackground(IvMDecorator.buttonColour);
			textStartActivities.setAntiAliasingEnabled(true);
			textStartActivities.setBorder(new EmptyBorder(0, 2, 2, 2));
			textStartActivities.setForeground(IvMDecorator.textColour);
			textStartActivities.setFont(textStartActivities.getFont().deriveFont(20));
			textStartActivities.setSelectionColor(IvMDecorator.backGroundColour2);
			textStartActivities.setSelectedTextColor(IvMDecorator.buttonColour);
			textStartActivities.setUseSelectedTextColor(true);
			textStartActivities.setCurrentLineHighlightColor(IvMDecorator.backGroundColour1);
			textStartActivities.setFadeCurrentLineHighlight(true);
			textStartActivities.revalidate();

			IvMDecorator.decorate(textEdges);
			textEdges.setBackground(IvMDecorator.buttonColour);
			textEdges.setAntiAliasingEnabled(true);
			textEdges.setBorder(new EmptyBorder(0, 2, 2, 2));
			textEdges.setForeground(IvMDecorator.textColour);
			textEdges.setFont(textEdges.getFont().deriveFont(20));
			textEdges.setSelectionColor(IvMDecorator.backGroundColour2);
			textEdges.setSelectedTextColor(IvMDecorator.buttonColour);
			textEdges.setUseSelectedTextColor(true);
			textEdges.setCurrentLineHighlightColor(IvMDecorator.backGroundColour1);
			textEdges.setFadeCurrentLineHighlight(true);
			textEdges.revalidate();

			IvMDecorator.decorate(textEndActivities);
			textEndActivities.setBackground(IvMDecorator.buttonColour);
			textEndActivities.setAntiAliasingEnabled(true);
			textEndActivities.setBorder(new EmptyBorder(0, 2, 2, 2));
			textEndActivities.setForeground(IvMDecorator.textColour);
			textEndActivities.setFont(textEndActivities.getFont().deriveFont(20));
			textEndActivities.setSelectionColor(IvMDecorator.backGroundColour2);
			textEndActivities.setSelectedTextColor(IvMDecorator.buttonColour);
			textEndActivities.setUseSelectedTextColor(true);
			textEndActivities.setCurrentLineHighlightColor(IvMDecorator.backGroundColour1);
			textEndActivities.setFadeCurrentLineHighlight(true);
			textEndActivities.revalidate();

			IvMDecorator.decorate(emptyTraces);

			IvMDecorator.decorate(errorMessage);

			setBackground(IvMDecorator.backGroundColour1);
			setOpaque(true);
		}
	}

	public void setMessage(String message) {
		treeEditor.setMessage(message);
		dfgEditor.setMessage(message);
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
				}
				actionListener.actionPerformed(e);
			}
		});
		dfgEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof Dfg) {
					e.setSource(new IvMModel((DirectlyFollowsModel) e.getSource()));
				}
				actionListener.actionPerformed(e);
			}
		});
	}
}
