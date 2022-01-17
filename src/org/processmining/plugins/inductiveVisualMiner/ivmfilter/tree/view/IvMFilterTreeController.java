package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.view;

import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.FilterCommunicator;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.FilterCommunicator.toFilterController;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderFactory;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTree;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class IvMFilterTreeController<X> {

	private final IvMFilterTreeView<X> view;
	private Runnable globalOnUpdate;
	private AttributesInfo attributesInfo;
	private IvMFilterTree<X> currentFilter;
	private final CopyOnWriteArrayList<FilterCommunicator<?, ?, ?, ?>> channels;

	public IvMFilterTreeController(final String prefix, Class<X> targetClass, final IvMFilterTreeView<X> view,
			final IvMFilterBuilderFactory factory, final IvMDecoratorI decorator) {
		this.view = view;
		channels = new CopyOnWriteArrayList<FilterCommunicator<?, ?, ?, ?>>();

		final IvMFilterTreeNodeView<X> rootView = createFilterNodeView(targetClass, factory, decorator);
		view.setRootView(rootView);

		rootView.setOnUpdate(new FilterHandler() {
			public void filterChanged(IvMFilterTreeNodeView<?> nodeView) {
				((DefaultTreeModel) view.getTreeView().getModel()).nodeChanged(nodeView.getTreeNode());

				/*
				 * User-assistance: if a filter builder is selected that can
				 * have children but does not have children yet, create a child.
				 */
				if (nodeView.getSelectedFilterBuilder().allowsChildren()
						&& nodeView.getTreeNode().getChildCount() == 0) {
					addChild(nodeView, false);
				}

				currentFilter = new IvMFilterTree<>(rootView.buildFilter(), prefix);
				updateExplanation();

				//notify the IvM controller that the filter changed
				if (globalOnUpdate != null) {
					globalOnUpdate.run();
				}
			}

			public <K> void addChild(IvMFilterTreeNodeView<K> parent, boolean selectNewChild) {
				IvMFilterBuilder<K, ?, ?> filterBuilder = parent.getSelectedFilterBuilder();
				IvMFilterTreeNodeView<?> newChild = IvMFilterTreeController
						.createFilterNodeView(filterBuilder.getChildrenTargetClass(), factory, decorator);
				view.getNodeViews().add(newChild, newChild.getId());
				newChild.setOnUpdate(this);
				if (attributesInfo != null) {
					newChild.setAttributesInfo(attributesInfo);
				}
				for (FilterCommunicator<?, ?, ?, ?> channel : channels) {
					newChild.setCommunicationChannel(channel);
				}

				DefaultTreeModel model = (DefaultTreeModel) view.getTreeView().getModel();
				DefaultMutableTreeNode parentNode = parent.getTreeNode();
				model.insertNodeInto(newChild.getTreeNode(), parentNode, parentNode.getChildCount());
				view.getTreeView().expandPath(new TreePath(parent.getTreeNode().getPath()));

				//select the newly generated node
				if (selectNewChild) {
					view.getTreeView().getSelectionModel()
							.setSelectionPath(new TreePath(newChild.getTreeNode().getPath()));
				}

				filterChanged(newChild);
			}
		});

		currentFilter = new IvMFilterTree<>(rootView.buildFilter(), prefix);
		updateExplanation();
	}

	/**
	 * Add the attributes to the filters
	 * 
	 * @param attributesInfo
	 */
	public void setAttributesInfo(AttributesInfo attributesInfo) {
		this.attributesInfo = attributesInfo;

		if (attributesInfo == null) {
			view.getOnOffPanel().set(false);
			return;
		}

		//set the attributesInfo over all filter builders
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> it = ((DefaultMutableTreeNode) view.getTreeView().getModel().getRoot())
				.breadthFirstEnumeration();
		while (it.hasMoreElements()) {
			DefaultMutableTreeNode treeNode = it.nextElement();
			((IvMFilterTreeNodeView<?>) treeNode.getUserObject()).setAttributesInfo(attributesInfo);
		}

		//enable access to the filters for the user
		view.getOnOffPanel().set(true);
	}

	public static <X> IvMFilterTreeNodeView<X> createFilterNodeView(Class<X> clazz, IvMFilterBuilderFactory factory,
			IvMDecoratorI decorator) {
		List<IvMFilterBuilder<X, ?, ?>> filterBuilders = factory.get(clazz);
		IvMFilterTreeNodeView<X> rootView = new IvMFilterTreeNodeView<>(filterBuilders, decorator);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootView);
		rootView.setTreeNode(root);
		return rootView;
	}

	public void updateExplanation() {
		view.getExplanation().setText(currentFilter.getExplanation());
	}

	public Runnable getOnUpdate() {
		return globalOnUpdate;
	}

	public void setOnUpdate(Runnable onUpdate) {
		this.globalOnUpdate = onUpdate;
	}

	public void addCommunicationChannel(FilterCommunicator<?, ?, ?, ?> channel) {
		channels.add(channel);

		channel.setSetAndSelectRootFilter(new toFilterController() {
			public void setAndSelectRootFilter(String name) {
				//select the root
				IvMFilterTreeNodeView<?> rootView = (IvMFilterTreeNodeView<?>) ((DefaultMutableTreeNode) view
						.getTreeView().getModel().getRoot()).getUserObject();
				view.getTreeView().getSelectionModel().setSelectionPath(new TreePath(rootView.getTreeNode().getPath()));

				//change the selection
				rootView.setSelectedFilterBuilder(name);
			}
		});

		//set the attributesInfo over all filter builders
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> it = ((DefaultMutableTreeNode) view.getTreeView().getModel().getRoot())
				.breadthFirstEnumeration();
		while (it.hasMoreElements()) {
			DefaultMutableTreeNode treeNode = it.nextElement();
			((IvMFilterTreeNodeView<?>) treeNode.getUserObject()).setCommunicationChannel(channel);
		}
	}

	public IvMFilterTree<X> getCurrentFilter() {
		return currentFilter;
	}
}