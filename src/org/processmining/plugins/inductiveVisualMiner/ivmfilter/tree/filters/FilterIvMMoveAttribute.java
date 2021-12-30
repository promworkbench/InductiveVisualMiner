package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeaf;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class FilterIvMMoveAttribute implements IvMFilterBuilder<IvMMove, Object, AttributeFilterGui> {

	public String toString() {
		return "event attribute filter";
	}

	public IvMFilterTreeNode<IvMMove> buildFilter(final AttributeFilterGui panel) {
		return new IvMFilterTreeNodeLeaf<IvMMove>() {

			public boolean staysInLog(IvMMove element) {
				Attribute attribute = panel.getSelectedAttribute();
				if (attribute.isLiteral()) {
					String value = attribute.getLiteral(element);
					if (value != null && panel.getSelectedLiterals().contains(value)) {
						return true;
					}
				} else if (attribute.isNumeric()) {
					double value = attribute.getNumeric(element);
					if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
							&& value <= panel.getSelectedNumericMax()) {
						return true;
					}
				} else if (attribute.isTime()) {
					long value = attribute.getTime(element);
					if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
							&& value <= panel.getSelectedTimeMax()) {
						return true;
					}
				} else if (attribute.isDuration()) {
					long value = attribute.getDuration(element);
					if (value != Long.MIN_VALUE && value >= panel.getSelectedDurationMin()
							&& value <= panel.getSelectedDurationMax()) {
						return true;
					}
				}
				return false;
			}

			public void getExplanation(StringBuilder result, int indent) {
				result.append(StringUtils.repeat("\t", indent));
				result.append(panel.getExplanation());
			}

			public boolean couldSomethingBeFiltered() {
				return true;
			}
		};
	}

	public AttributeFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		AttributeFilterGui panel = new AttributeFilterGui("REMOVE", onUpdate, decorator);
		panel.getExplanationLabel().setText("Include only events having an attribute as selected.");
		return panel;
	}

	public boolean allowsChildren() {
		return false;
	}

	@Override
	public Class<IvMMove> getTargetClass() {
		return IvMMove.class;
	}

	@Override
	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	public void setAttributesInfo(IvMAttributesInfo attributesInfo, AttributeFilterGui panel) {
		panel.setAttributes(attributesInfo.getEventAttributes());
	}
}