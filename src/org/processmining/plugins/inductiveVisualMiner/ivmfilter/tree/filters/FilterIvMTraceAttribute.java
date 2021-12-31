package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilder;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeafAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class FilterIvMTraceAttribute implements IvMFilterBuilder<IvMTrace, Object, AttributeFilterGui> {

	public String toString() {
		return "trace attribute";
	}

	@Override
	public String toString(AttributeFilterGui panel) {
		Attribute attribute = panel.getSelectedAttribute();
		return "trace attribute " + (attribute != null ? attribute.toString() : "filter");
	}

	@Override
	public IvMFilterTreeNode<IvMTrace> buildFilter(final AttributeFilterGui panel) {
		return buildFilterA(panel);
	}

	public static <X extends XAttributable> IvMFilterTreeNode<X> buildFilterA(final AttributeFilterGui panel) {
		final Attribute attribute = panel.getSelectedAttribute();

		if (attribute.isLiteral()) {
			final List<String> selectedLiterals = new ArrayList<>(panel.getSelectedLiterals());

			return new IvMFilterTreeNodeLeafAbstract<X>() {
				public boolean staysInLogA(X element) {
					String value = attribute.getLiteral(element);
					return value != null && selectedLiterals.contains(value);
				}

				public void getExplanation(StringBuilder result, int indent) {
					FilterIvMMoveAttribute.getExplanation(panel, result, indent);
				}

				public boolean couldSomethingBeFiltered() {
					return !selectedLiterals.isEmpty();
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
					return numericMin != attribute.getNumericMin() || numericMax != attribute.getNumericMax();
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
					return timeMin != attribute.getTimeMin() || timeMax != attribute.getTimeMax();
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
					return durationMin != attribute.getDurationMin() || durationMax != attribute.getDurationMax();
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
	public Class<IvMTrace> getTargetClass() {
		return IvMTrace.class;
	}

	@Override
	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	@Override
	public void setAttributesInfo(IvMAttributesInfo attributesInfo, AttributeFilterGui panel) {
		panel.setAttributes(attributesInfo.getTraceAttributes());
	}
}