package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.awt.Color;

import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeCompositeAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class FilterIvMTraceWithEvent implements IvMFilterBuilder<IvMTrace, IvMMove, IvMFilterGui> {

	@Override
	public String toString() {
		return "trace with event";
	}

	@Override
	public IvMFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		IvMFilterGui result = new IvMFilterGui("REMOVE", decorator) {
			private static final long serialVersionUID = 110211772022409817L;

			protected void setForegroundRecursively(Color colour) {

			}
		};
		JTextArea label = new JTextArea("Include traces that have an event that passes all the sub-filters.");
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		decorator.decorate(label);
		result.add(label);
		return result;
	}

	@Override
	public IvMFilterTreeNode<IvMTrace> buildFilter(IvMFilterGui gui) {
		return new IvMFilterTreeNodeCompositeAbstract<IvMTrace, IvMMove>() {

			private static final long serialVersionUID = 8213030059677606305L;

			public boolean staysInLog(IvMTrace x) {
				for (IvMMove move : x) {
					for (IvMFilterTreeNode<IvMMove> child : this) {
						if (!child.staysInLog(move)) {
							break;
						}
					}
					return true;
				}
				return false;
			}

			@Override
			public String getPrefix() {
				return "having an event that";
			}

			public String getDivider() {
				return "and";
			}

			public boolean couldSomethingBeFiltered() {
				for (IvMFilterTreeNode<IvMMove> child : this) {
					if (child.couldSomethingBeFiltered()) {
						return true;
					}
				}
				return false;
			}
		};
	}

	@Override
	public boolean allowsChildren() {
		return true;
	}

	@Override
	public Class<IvMTrace> getTargetClass() {
		return IvMTrace.class;
	}

	@Override
	public Class<IvMMove> getChildrenTargetClass() {
		return IvMMove.class;
	}

	@Override
	public void setAttributesInfo(IvMAttributesInfo attributesInfo, IvMFilterGui gui) {

	}
}