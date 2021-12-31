package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class FilterIvMMoveAttribute implements IvMFilterBuilder<IvMMove, Object, AttributeFilterGui> {

	public String toString() {
		return "event attribute filter";
	}

	@Override
	public String toString(AttributeFilterGui panel) {
		Attribute attribute = panel.getSelectedAttribute();
		return "event attribute " + (attribute != null ? attribute.toString() : "filter");
	}

	public static void getExplanation(AttributeFilterGui panel, StringBuilder result, int indent) {
		result.append(StringUtils.repeat("\t", indent));
		result.append(panel.getExplanation());
	}

	@Override
	public IvMFilterTreeNode<IvMMove> buildFilter(final AttributeFilterGui panel) {
		return FilterIvMTraceAttribute.buildFilterA(panel);
	}

	public AttributeFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		AttributeFilterGui panel = new AttributeFilterGui(toString(), onUpdate, decorator);
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