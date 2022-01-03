package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterNameGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeafAbstract;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterIMTraceWithoutAttribute extends IvMFilterBuilderAbstract<IMTrace, Object, AttributeFilterNameGui> {

	public String toString() {
		return "without attribute";
	}

	@Override
	public String toString(AttributeFilterNameGui panel) {
		Attribute attribute = panel.getSelectedAttribute();
		return "without attribute " + (attribute != null ? attribute.toString() : "filter");
	}

	@Override
	public IvMFilterTreeNode<IMTrace> buildFilter(final AttributeFilterNameGui panel) {
		return buildFilterA(panel);
	}

	public static <X extends XAttributable> IvMFilterTreeNode<X> buildFilterA(final AttributeFilterNameGui panel) {
		final Attribute attribute = panel.getSelectedAttribute();

		return new IvMFilterTreeNodeLeafAbstract<X>() {
			public boolean staysInLogA(X element) {
				if (attribute.isLiteral()) {
					String value = attribute.getLiteral(element);
					return value == null;
				} else if (attribute.isNumeric()) {
					double value = attribute.getNumeric(element);
					return value == -Double.MAX_VALUE;
				} else if (attribute.isBoolean()) {
					Boolean value = attribute.getBoolean(element);
					return value == null;
				} else if (attribute.isTime()) {
					long value = attribute.getTime(element);
					return value == Long.MIN_VALUE;
				} else if (attribute.isDuration()) {
					long value = attribute.getDuration(element);
					return value == Long.MIN_VALUE;
				} else {
					//	attribute not supported
					throw new RuntimeException("attribute not supported");
				}
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

	@Override
	public AttributeFilterNameGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		AttributeFilterNameGui panel = new AttributeFilterNameGui(toString(), onUpdate, decorator);
		panel.getExplanationLabel().setText("Include only traces not having an attribute as selected.");
		return panel;
	}

	@Override
	public boolean allowsChildren() {
		return false;
	}

	@Override
	public Class<IMTrace> getTargetClass() {
		return IMTrace.class;
	}

	@Override
	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo, AttributeFilterNameGui panel) {
		panel.setAttributes(attributesInfo.getTraceAttributes());
	}
}