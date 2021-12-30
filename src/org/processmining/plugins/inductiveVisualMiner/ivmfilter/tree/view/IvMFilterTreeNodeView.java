package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeComposite;

/**
 * This class shows a single node, and the user can choose and configure the
 * filter.
 * 
 * @param IvMDecoratorI
 */
public class IvMFilterTreeNodeView<X> extends JPanel {

	private static final long serialVersionUID = -5974552986466618725L;

	private final CardLayout layout;
	private final JComboBox<IvMFilterBuilder<X, ?, ?>> filterBuilderChooser;
	private final List<IvMFilterGui> guis;
	private final String id;
	private final List<IvMFilterBuilder<X, ?, ?>> filterBuilders;
	private DefaultMutableTreeNode treeNode;
	private FilterHandler onUpdate;

	public IvMFilterTreeNodeView(List<IvMFilterBuilder<X, ?, ?>> filterBuilders, IvMDecoratorI decorator) {
		this.filterBuilders = filterBuilders;
		setLayout(new BorderLayout());
		id = UUID.randomUUID().toString();
		setOpaque(false);

		guis = new ArrayList<>();
		final JPanel settingsBody = new JPanel();
		{
			layout = new CardLayout();
			settingsBody.setLayout(layout);
			settingsBody.setOpaque(false);

			Runnable onUpdateChild = new Runnable() {
				public void run() {
					onUpdate.filterChanged(IvMFilterTreeNodeView.this);
				}
			};

			for (IvMFilterBuilder<X, ?, ?> filterBuilder : filterBuilders) {
				JPanel childPanel = new JPanel();
				childPanel.setOpaque(false);
				childPanel.setLayout(new BorderLayout());

				IvMFilterGui filterGui = filterBuilder.createGui(onUpdateChild, decorator);
				guis.add(filterGui);
				childPanel.add(filterGui, BorderLayout.CENTER);

				if (filterBuilder.allowsChildren()) {
					JButton addChildLabel = new JButton("add sub-filter");
					decorator.decorate(addChildLabel);
					addChildLabel.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onUpdate.addChild(IvMFilterTreeNodeView.this);
						}
					});
					childPanel.add(addChildLabel, BorderLayout.PAGE_END);
				}

				settingsBody.add(childPanel, filterBuilder.toString());
			}
		}
		add(settingsBody, BorderLayout.CENTER);

		JPanel header = new JPanel();
		{
			header.setOpaque(false);
			header.setLayout(new FlowLayout(FlowLayout.LEADING));

			JLabel label = new JLabel("Filter: ");
			label.setAlignmentX(0);
			decorator.decorate(label);
			header.add(label);

			@SuppressWarnings("unchecked")
			IvMFilterBuilder<X, ?, ?>[] arr = (IvMFilterBuilder<X, ?, ?>[]) new IvMFilterBuilder<?, ?, ?>[filterBuilders
					.size()];
			filterBuilderChooser = new JComboBox<>(filterBuilders.toArray(arr));
			filterBuilderChooser.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					layout.show(settingsBody, filterBuilderChooser.getSelectedItem().toString());
					onUpdate.filterChanged(IvMFilterTreeNodeView.this);
				}

			});
			decorator.decorate(filterBuilderChooser);
			header.add(filterBuilderChooser);
		}
		add(header, BorderLayout.PAGE_START);
	}

	public void setAttributesInfo(IvMAttributesInfo attributesInfo) {
		for (int i = 0; i < guis.size(); i++) {
			setAttributesInfo(attributesInfo, filterBuilders.get(i), guis.get(i));
		}
	}

	@SuppressWarnings("unchecked")
	private <G extends IvMFilterGui, H extends IvMFilterGui> void setAttributesInfo(IvMAttributesInfo attributesInfo,
			IvMFilterBuilder<X, ?, G> filterBuilder, H gui) {
		filterBuilder.setAttributesInfo(attributesInfo, (G) gui);
	}

	@SuppressWarnings("unchecked")
	public IvMFilterTreeNode<X> buildFilter() {
		IvMFilterBuilder<X, ?, ?> filterBuilder = getSelectedFilterBuilder();
		IvMFilterTreeNode<X> result = buildFilter(filterBuilder);
		if (filterBuilder.allowsChildren()) {
			buildFilterChildren((IvMFilterTreeNodeComposite<X, ?>) result);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <G extends IvMFilterGui> IvMFilterTreeNode<X> buildFilter(IvMFilterBuilder<X, ?, G> filterBuilder) {
		return filterBuilder.buildFilter((G) guis.get(filterBuilderChooser.getSelectedIndex()));
	}

	private <Y> void buildFilterChildren(IvMFilterTreeNodeComposite<X, Y> result) {
		for (int index = 0; index < treeNode.getChildCount(); index++) {
			@SuppressWarnings("unchecked")
			IvMFilterTreeNode<Y> childFilterTreeNode = ((IvMFilterTreeNodeView<Y>) ((DefaultMutableTreeNode) treeNode
					.getChildAt(index)).getUserObject()).buildFilter();
			result.add(childFilterTreeNode);
		}
	}

	public String getId() {
		return id;
	}

	public String getCurrentName() {
		return getSelectedFilterBuilder().toString();
	}

	@SuppressWarnings("unchecked")
	public IvMFilterBuilder<X, ?, ?> getSelectedFilterBuilder() {
		return (IvMFilterBuilder<X, ?, ?>) filterBuilderChooser.getSelectedItem();
	}

	public DefaultMutableTreeNode getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(DefaultMutableTreeNode treeNode) {
		this.treeNode = treeNode;
	}

	public FilterHandler getOnUpdate() {
		return onUpdate;
	}

	public void setOnUpdate(FilterHandler onUpdate) {
		this.onUpdate = onUpdate;
	}

	/**
	 * 
	 * @return whether all parents actually accept children
	 */
	public boolean isOnValidPath() {
		TreeNode[] path = getTreeNode().getPath();
		for (int i = 0; i < path.length - 1; i++) {
			IvMFilterTreeNodeView<?> parentView = ((IvMFilterTreeNodeView<?>) ((DefaultMutableTreeNode) path[i])
					.getUserObject());
			IvMFilterTreeNodeView<?> childView = ((IvMFilterTreeNodeView<?>) ((DefaultMutableTreeNode) path[i + 1])
					.getUserObject());

			if (parentView.getSelectedFilterBuilder().getChildrenTargetClass() != childView.getSelectedFilterBuilder()
					.getTargetClass()) {
				return false;
			}

			if (!parentView.getSelectedFilterBuilder().allowsChildren()) {
				return false;
			}
		}
		return true;
	}
}