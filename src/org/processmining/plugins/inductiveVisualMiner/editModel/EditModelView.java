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
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel.Source;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public class EditModelView extends SideWindow {

	private static final long serialVersionUID = 2710387572788600877L;

	private final JPanel panel;
	private final EfficientTreeEditor treeEditor;
	private final DfgEditor dfgEditor;

	private final static String TREEPANEL = "tree panel";
	private final static String DFGPANEL = "dfg panel";

	public EditModelView(Component parent, IvMDecoratorI decorator) {
		super(parent, "edit model - " + InductiveVisualMinerPanel.title);

		getContentPane().setBackground(decorator.backGroundColour1());

		panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new CardLayout());

		treeEditor = new IvMEfficientTreeEditor(null, "Minnig tree...", decorator);
		panel.add(treeEditor, TREEPANEL);

		dfgEditor = new IvMDfgEditor(null, "Mining directly follows graph...", decorator);
		panel.add(dfgEditor, DFGPANEL);
	}

	public void setModel(IvMModel model) {
		if (model.getSource() != Source.user) {
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
	}

	public static class IvMEfficientTreeEditor extends EfficientTreeEditor {
		private static final long serialVersionUID = -3553121183719500176L;

		public IvMEfficientTreeEditor(EfficientTree tree, String message, IvMDecoratorI decorator) {
			super(tree, message);
			decorator.decorate(text);
			decorator.decorate(errorMessage);
			text.setBackground(decorator.buttonColour());
			text.setAntiAliasingEnabled(true);

			text.setBorder(new EmptyBorder(0, 2, 2, 2));

			text.setForeground(decorator.textColour());
			text.setFont(text.getFont().deriveFont(20));

			text.setSelectionColor(decorator.backGroundColour2());
			text.setSelectedTextColor(decorator.buttonColour());
			text.setUseSelectedTextColor(true);

			text.setCurrentLineHighlightColor(decorator.backGroundColour1());
			text.setFadeCurrentLineHighlight(true);

			text.revalidate();
		}
	}

	public static class IvMDfgEditor extends DfgEditor {
		private static final long serialVersionUID = -3553121183719500176L;

		public IvMDfgEditor(DirectlyFollowsModel dfg, String message, IvMDecoratorI decorator) {
			super(dfg, message);

			decorator.decorate(labelStartActivities);
			decorator.decorate(labelEdges);
			decorator.decorate(labelEndActivities);

			decorator.decorate(textStartActivities);
			textStartActivities.setBackground(decorator.buttonColour());
			textStartActivities.setAntiAliasingEnabled(true);
			textStartActivities.setBorder(new EmptyBorder(0, 2, 2, 2));
			textStartActivities.setForeground(decorator.textColour());
			textStartActivities.setFont(textStartActivities.getFont().deriveFont(20));
			textStartActivities.setSelectionColor(decorator.backGroundColour2());
			textStartActivities.setSelectedTextColor(decorator.buttonColour());
			textStartActivities.setUseSelectedTextColor(true);
			textStartActivities.setCurrentLineHighlightColor(decorator.backGroundColour1());
			textStartActivities.setFadeCurrentLineHighlight(true);
			textStartActivities.revalidate();

			decorator.decorate(textEdges);
			textEdges.setBackground(decorator.buttonColour());
			textEdges.setAntiAliasingEnabled(true);
			textEdges.setBorder(new EmptyBorder(0, 2, 2, 2));
			textEdges.setForeground(decorator.textColour());
			textEdges.setFont(textEdges.getFont().deriveFont(20));
			textEdges.setSelectionColor(decorator.backGroundColour2());
			textEdges.setSelectedTextColor(decorator.buttonColour());
			textEdges.setUseSelectedTextColor(true);
			textEdges.setCurrentLineHighlightColor(decorator.backGroundColour1());
			textEdges.setFadeCurrentLineHighlight(true);
			textEdges.revalidate();

			decorator.decorate(textEndActivities);
			textEndActivities.setBackground(decorator.buttonColour());
			textEndActivities.setAntiAliasingEnabled(true);
			textEndActivities.setBorder(new EmptyBorder(0, 2, 2, 2));
			textEndActivities.setForeground(decorator.textColour());
			textEndActivities.setFont(textEndActivities.getFont().deriveFont(20));
			textEndActivities.setSelectionColor(decorator.backGroundColour2());
			textEndActivities.setSelectedTextColor(decorator.buttonColour());
			textEndActivities.setUseSelectedTextColor(true);
			textEndActivities.setCurrentLineHighlightColor(decorator.backGroundColour1());
			textEndActivities.setFadeCurrentLineHighlight(true);
			textEndActivities.revalidate();

			decorator.decorate(emptyTraces);

			decorator.decorate(errorMessage);

			setBackground(decorator.backGroundColour1());
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
					IvMModel model = new IvMModel(new IvMEfficientTree((EfficientTree) e.getSource()));
					model.setSource(Source.user);
					e.setSource(model);
				}
				actionListener.actionPerformed(e);
			}
		});
		dfgEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof Dfg) {
					IvMModel model = new IvMModel((DirectlyFollowsModel) e.getSource());
					model.setSource(Source.user);
					e.setSource(model);
				}
				actionListener.actionPerformed(e);
			}
		});
	}
}
