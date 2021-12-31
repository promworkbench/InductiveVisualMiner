package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public class IvMFilterTreeCellRenderer extends JButton implements TreeCellRenderer {

	private static final long serialVersionUID = 4097210394485528564L;
	private final IvMDecoratorI decorator;

	public IvMFilterTreeCellRenderer(IvMDecoratorI decorator) {
		this.decorator = decorator;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		assert value instanceof DefaultMutableTreeNode;

		if (!(((DefaultMutableTreeNode) value).getUserObject() instanceof IvMFilterTreeNodeView<?>)) {
			return this;
		}

		IvMFilterTreeNodeView<?> view = ((IvMFilterTreeNodeView<?>) ((DefaultMutableTreeNode) value).getUserObject());
		decorator.decorate(this);

		setEnabled(view.isOnValidPath());

		setText(view.getCurrentName());

		//colour
		if (sel) {
			setOpaque(true);
			Color swap = getBackground();
			setBackground(getForeground());
			setForeground(swap);
		} else {
			setOpaque(false);
		}

		return this;
	}
}