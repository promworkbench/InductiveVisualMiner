package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.cohortanalysis.feature.Feature;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.FilterCommunicator;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.FilterCommunicator.toFilterChannel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderAbstract;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNode;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterTreeNodeLeaf;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class FilterIvMTraceCohort extends IvMFilterBuilderAbstract<IvMTrace, Object, FilterIvMTraceCohortPanel> {

	private FilterCommunicator<Pair<Cohort, Boolean>, Void, Void, Void> channel;

	public String toString() {
		return "cohort";
	}

	public String toString(FilterIvMTraceCohortPanel panel) {
		return "cohort";
	}

	public boolean allowsChildren() {
		return false;
	}

	public Class<IvMTrace> getTargetClass() {
		return IvMTrace.class;
	}

	public Class<Object> getChildrenTargetClass() {
		return null;
	}

	public FilterIvMTraceCohortPanel createGui(Runnable onUpdate, IvMDecoratorI decorator) {
		FilterIvMTraceCohortPanel result = new FilterIvMTraceCohortPanel(toString(), onUpdate, decorator);

		result.getExplanationLabel().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (channel != null) {
					channel.fromFilter(null);
				}
			}
		});

		return result;
	}

	public IvMFilterTreeNode<IvMTrace> buildFilter(final FilterIvMTraceCohortPanel gui) {
		final Cohort selectedCohort = gui.getSelectedCohort();
		final boolean highlightInCohort = gui.isHighlightInCohort();

		return new IvMFilterTreeNodeLeaf<IvMTrace>() {

			public boolean staysInLog(IvMTrace trace) {
				if (selectedCohort == null) {
					return true;
				}
				return !(inCohort(trace) ^ highlightInCohort);
			}

			public boolean inCohort(IvMTrace trace) {
				/*
				 * Create a temporary xtrace. The CohortAnalysis package doesn't
				 * know about IvMTraces or events (and conceptually, is unaware
				 * of alignment results), thus we have to take this step. The
				 * GUI is not waiting on this step, so that's fine.
				 */
				XTrace xTrace = new XTraceImpl(trace.getAttributes());
				for (IvMMove event : trace) {
					if (event.getAttributes() != null) {
						xTrace.add(new XEventImpl(event.getAttributes()));
					}
				}

				//a trace is in a cohort when all of the cohort's features are present in the trace.
				for (Feature feature : selectedCohort.getFeatures()) {
					if (!feature.includes(xTrace)) {
						return false;
					}
				}
				return true;
			}

			public void getExplanation(StringBuilder result, int indent) {
				result.append(StringUtils.repeat("\t", indent));
				gui.getExplanation(result);
			}

			public boolean couldSomethingBeFiltered() {
				return selectedCohort != null;
			}
		};
	}

	public void setAttributesInfo(AttributesInfo attributesInfo, FilterIvMTraceCohortPanel gui) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public <TI, TO, FI, FO> void setCommunicationChannel(final FilterCommunicator<TI, TO, FI, FO> channel,
			final FilterIvMTraceCohortPanel panel) {
		if (channel.getName() == "cohorts") {
			this.channel = (FilterCommunicator<Pair<Cohort, Boolean>, Void, Void, Void>) channel;
			this.channel.setToFilter(new toFilterChannel<Pair<Cohort, Boolean>, Void>() {
				public Void toFilter(Pair<Cohort, Boolean> input) {
					panel.setSelectedCohort(input.getA(), input.getB());
					channel.setAndSelectRootFilter(FilterIvMTraceCohort.this.toString());
					return null;
				}
			});
		}
	}
}