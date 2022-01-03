package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeafAbstract;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterIMTraceAttribute extends IvMFilterBuilderAbstract<IMTrace, Object, AttributeFilterGui> {

	public String toString() {
		return "attribute";
	}

	@Override
	public String toString(AttributeFilterGui panel) {
		Attribute attribute = panel.getSelectedAttribute();
		return "attribute " + (attribute != null ? attribute.toString() : "filter");
	}

	@Override
	public IvMFilterTreeNode<IMTrace> buildFilter(final AttributeFilterGui panel) {
		return buildFilterA(panel);
	}

	public static <X extends XAttributable> IvMFilterTreeNode<X> buildFilterA(final AttributeFilterGui panel) {
		final Attribute attribute = panel.getSelectedAttribute();

		if (attribute.isLiteral()) {
			final List<String> selectedLiterals = new ArrayList<>(panel.getSelectedLiterals());

			return new IvMFilterTreeNodeLeafAbstract<X>() {
				public boolean staysInLogA(X element) {
					String value = attribute.getLiteral(element);
					return value != null && (selectedLiterals.contains(value) || selectedLiterals.isEmpty());
				}

				public void getExplanation(StringBuilder result, int indent) {
					FilterIvMMoveAttribute.getExplanation(panel, result, indent);
				}

				public boolean couldSomethingBeFiltered() {
					return true;
				}
			};
		} else if (attribute.isNumeric()) {
			final double numericMin = panel.getSelectedNumericMin();
			final double numericMax = panel.getSelectedNumericMax();

			return new IvMFilterTreeNodeLeafAbstract<X>() {
				public boolean staysInLogA(X element) {
					double value = attribute.getNumeric(element);
					return value != -Double.MAX_VALUE && value >= numericMin && value <= numericMax;
				}

				public void getExplanation(StringBuilder result, int indent) {
					FilterIvMMoveAttribute.getExplanation(panel, result, indent);
				}

				public boolean couldSomethingBeFiltered() {
					return true;
				}
			};
		} else if (attribute.isBoolean()) {
			final boolean includeTrue = panel.getSelectedBooleanTrue();
			final boolean includeFalse = panel.getSelectedBooleanFalse();

			return new IvMFilterTreeNodeLeafAbstract<X>() {
				public boolean staysInLogA(X element) {
					Boolean value = attribute.getBoolean(element);
					return value != null && (((value && includeTrue) || (!value && includeFalse))
							|| (!includeTrue && !includeFalse));
				}

				public void getExplanation(StringBuilder result, int indent) {
					FilterIvMMoveAttribute.getExplanation(panel, result, indent);
				}

				public boolean couldSomethingBeFiltered() {
					return true;
				}
			};
		} else if (attribute.isTime()) {
			final long timeMin = panel.getSelectedTimeMin();
			final long timeMax = panel.getSelectedTimeMax();

			return new IvMFilterTreeNodeLeafAbstract<X>() {
				public boolean staysInLogA(X element) {
					long value = attribute.getTime(element);
					return value != Long.MIN_VALUE && value >= timeMin && value <= timeMax;
				}

				public void getExplanation(StringBuilder result, int indent) {
					FilterIvMMoveAttribute.getExplanation(panel, result, indent);
				}

				public boolean couldSomethingBeFiltered() {
					return true;
				}
			};
		} else if (attribute.isDuration()) {
			final long durationMin = panel.getSelectedDurationMin();
			final long durationMax = panel.getSelectedDurationMax();

			return new IvMFilterTreeNodeLeafAbstract<X>() {
				public boolean staysInLogA(X element) {
					long value = attribute.getDuration(element);
					return value != Long.MIN_VALUE && value >= durationMin && value <= durationMax;
				}

				public void getExplanation(StringBuilder result, int indent) {
					FilterIvMMoveAttribute.getExplanation(panel, result, indent);
				}

				public boolean couldSomethingBeFiltered() {
					return true;
				}
			};
		}

		//attribute not supported
		throw new RuntimeException("attribute not supported");
	}

	@Override
	public AttributeFilterGui createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		AttributeFilterGui panel = new AttributeFilterGui(toString(), onUpdate, decorator);
		panel.getExplanationLabel().setText("Include only traces having an attribute as selected.");
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
	public void setAttributesInfo(AttributesInfo attributesInfo, AttributeFilterGui panel) {
		panel.setAttributes(attributesInfo.getTraceAttributes());
	}
}