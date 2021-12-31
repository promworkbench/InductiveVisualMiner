package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.OnOffPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public class IvMFilterTreeView<X> extends SideWindow {

	private static final long serialVersionUID = 2689698523420921017L;

	public static final int tabSize = 1;

	private final OnOffPanel<JPanel> onOffPanel;
	private final JTree treeView;
	private final JTextArea explanation;
	private final JPanel nodeViews;
	private final CardLayout nodeViewsLayout;

	public IvMFilterTreeView(Component parent, String title, final IvMDecoratorI decorator) {
		super(parent, title + " - visual Miner");
		setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		onOffPanel = new OnOffPanel<JPanel>(decorator, panel);
		onOffPanel.off();
		onOffPanel.setOffMessage("Waiting for attributes..");
		panel.setOpaque(false);
		add(onOffPanel, BorderLayout.CENTER);

		panel.setLayout(new BorderLayout());

		getContentPane().setBackground(decorator.backGroundColour2());

		//explanation
		{
			explanation = new JTextArea("explanation");
			decorator.decorate(explanation);
			explanation.setTabSize(tabSize);
			explanation.setOpaque(false);
			explanation.setEditable(false);
			explanation.setMargin(new Insets(5, 5, 15, 5));
			explanation.setWrapStyleWord(true);
			explanation.setLineWrap(true);
			panel.add(explanation, BorderLayout.PAGE_START);
		}

		JPanel body = new JPanel();
		body.setOpaque(false);
		panel.add(body, BorderLayout.CENTER);
		body.setLayout(new GridBagLayout());

		//node views
		{
			nodeViews = new JPanel();
			nodeViewsLayout = new CardLayout();
			nodeViews.setLayout(nodeViewsLayout);
			nodeViews.setOpaque(false);

			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 1;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			body.add(nodeViews, c);
		}

		//tree view
		JScrollPane treeScrollPanel;
		{
			treeView = new JTree() {
				private static final long serialVersionUID = -465884907448273587L;

				@Override
				protected void setExpandedState(TreePath path, boolean state) {
					if (state) {
						super.setExpandedState(path, state);
					}
				}
			};
			//disable little handles as we do not allow collapsing of nodes
			treeView.setUI(new IvMTreeUI(decorator));
			treeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			treeView.setCellRenderer(new IvMFilterTreeCellRenderer(decorator));
			treeView.setOpaque(false);
			treeView.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					//new selection
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeView.getLastSelectedPathComponent();
					if (node == null) {
						return;
					}

					Object nodeInfo = node.getUserObject();
					if (nodeInfo != null) {
						assert nodeInfo instanceof IvMFilterTreeNodeView<?>;

						if (((IvMFilterTreeNodeView<?>) nodeInfo).isOnValidPath()) {
							String id = ((IvMFilterTreeNodeView<?>) nodeInfo).getId();
							nodeViewsLayout.show(nodeViews, id);
						} else {
							//The selected node cannot be selected as one of its parent doesn't allow children. Select the parent instead.
							treeView.getSelectionModel().setSelectionPath(
									new TreePath(((DefaultMutableTreeNode) node.getParent()).getPath()));
						}
					}
				}
			});
			treeScrollPanel = new JScrollPane(treeView);
			treeScrollPanel.setOpaque(false);
			treeScrollPanel.getViewport().setOpaque(false);
			treeScrollPanel.setPreferredSize(new Dimension(200, 1));
			treeScrollPanel.setMinimumSize(new Dimension(200, 1));
			treeScrollPanel.setMaximumSize(null);

			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0.5;
			c.weighty = 1;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			body.add(treeScrollPanel, c);
		}

	}

	public void setRootView(IvMFilterTreeNodeView<X> rootView) {
		nodeViews.add(rootView, rootView.getId());
		((DefaultTreeModel) treeView.getModel()).setRoot(rootView.getTreeNode());
		treeView.getSelectionModel().setSelectionPath(new TreePath(rootView.getTreeNode().getPath()));
	}

	public JTree getTreeView() {
		return treeView;
	}

	public JPanel getNodeViews() {
		return nodeViews;
	}

	public JTextArea getExplanation() {
		return explanation;
	}

	@SuppressWarnings("unchecked")
	public IvMFilterTreeNodeView<X> getRootNodeView() {
		return (IvMFilterTreeNodeView<X>) ((DefaultMutableTreeNode) treeView.getModel().getRoot()).getUserObject();
	}

	public OnOffPanel<JPanel> getOnOffPanel() {
		return onOffPanel;
	}

	public static class IvMTreeUI extends BasicTreeUI {

		private final IvMDecoratorI decorator;

		public IvMTreeUI(IvMDecoratorI decorator) {
			this.decorator = decorator;
		}

		@Override
		protected boolean shouldPaintExpandControl(final TreePath path, final int row, final boolean isExpanded,
				final boolean hasBeenExpanded, final boolean isLeaf) {
			return false;
		}

		@Override
		protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
			Color backup = g.getColor();
			g.setColor(decorator.textColour());
			drawDashedHorizontalLine(g, y, left, right);
			g.setColor(backup);
		}

		@Override
		protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
			Color bColour = g.getColor();
			g.setColor(decorator.textColour());
			drawDashedVerticalLine(g, x, top, bottom);
			g.setColor(bColour);
		}
	}
}