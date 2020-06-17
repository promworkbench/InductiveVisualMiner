package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.processmining.earthmoversstochasticconformancechecking.parameters.EMSCParametersLogLogAbstract;
import org.processmining.earthmoversstochasticconformancechecking.parameters.EMSCParametersLogLogDefault;
import org.processmining.earthmoversstochasticconformancechecking.plugins.EarthMoversStochasticConformancePlugin;
import org.processmining.earthmoversstochasticconformancechecking.tracealignments.StochasticTraceAlignmentsLogLog;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog2XLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

/**
 * Log attribute analysis consists of two parts: the log attributes, and
 * (virtual) highlighted log attributes. They are added in two stages.
 * 
 * @author sander
 *
 */
public class LogAttributeAnalysis extends ArrayList<Pair<String, ? extends DisplayType>> {

	private static final long serialVersionUID = 5687504321633958714L;

	public LogAttributeAnalysis(final XLog sortedXLog, final IvMCanceller canceller) {

		//log attributes
		Collection<XAttribute> xAttributes = sortedXLog.getAttributes().values();
		for (XAttribute xAttribute : xAttributes) {
			if (xAttribute instanceof XAttributeDiscrete) {
				add(Pair.of(xAttribute.getKey(),
						DisplayType.numeric(((XAttributeDiscreteImpl) xAttribute).getValue())));
			} else if (xAttribute instanceof XAttributeContinuous) {
				add(Pair.of(xAttribute.getKey(), DisplayType.numeric(((XAttributeContinuous) xAttribute).getValue())));
			} else if (xAttribute instanceof XAttributeLiteral) {
				add(Pair.of(xAttribute.getKey(), DisplayType.literal(((XAttributeLiteral) xAttribute).getValue())));
			} else if (xAttribute instanceof XAttributeBoolean) {
				add(Pair.of(xAttribute.getKey(),
						DisplayType.literal(((XAttributeBoolean) xAttribute).getValue() + "")));
			} else {
				add(Pair.of(xAttribute.getKey(), DisplayType.literal(xAttribute.toString())));
			}
		}

		//virtual attributes
		{
			int numberOfEvents = 0;
			for (XTrace trace : sortedXLog) {
				numberOfEvents += trace.size();
			}
			add(Pair.of("number of events (full log)", DisplayType.numeric(numberOfEvents)));
		}

		add(Pair.of("number of traces (full log)", DisplayType.numeric(sortedXLog.size())));

		//add placeholders for part two
		addVirtualAttributePlaceholders();

		sort();
	}

	private void sort() {
		Collections.sort(this, new Comparator<Pair<String, ? extends DisplayType>>() {
			public int compare(Pair<String, ? extends DisplayType> arg0, Pair<String, ? extends DisplayType> arg1) {
				return arg0.getA().toLowerCase().compareTo(arg1.getA().toLowerCase());
			}
		});
	}

	/*
	 * Part two: highlighted log virtual attributes
	 */

	public static enum Field {
		stochasticSimilarity {
			public String toString() {
				return "stochastic similarity between highlighted and non-highlighted traces";
			}
		},
		tracesHighlighted {
			public String toString() {
				return "number of traces (highlighted)";
			}
		},
		tracesNotHighlighted {
			public String toString() {
				return "number of traces (not highlighted)";
			}
		},
		eventsHighlighted {
			public String toString() {
				return "number of events (highlighted traces)";
			}
		},
		eventsNotHighlighted {
			public String toString() {
				return "number of events (not-highlighted traces)";
			}
		},
		fitnessHighlighted {
			public String toString() {
				return "fitness (highlighted traces)";
			}
		},
		fitnessNotHighlighted {
			public String toString() {
				return "fitness (not-highlighted traces)";
			}
		},
	}

	public static List<Pair<String, DisplayType>> computeVirtualAttributes(IvMModel model, IvMLogFiltered input,
			IvMCanceller canceller) throws CloneNotSupportedException, InterruptedException {
		ArrayList<Pair<String, DisplayType>> result = new ArrayList<>();

		//number of traces and events (highlighted)
		{
			int numberOfTraces = 0;
			int numberOfEvents = 0;
			for (IteratorWithPosition<IvMTrace> it = input.iterator(); it.hasNext();) {
				numberOfTraces++;

				IvMTrace trace = it.next();
				for (IvMMove move : trace) {
					if (move.getAttributes() != null) {
						numberOfEvents++;
					}
				}

				if (canceller.isCancelled()) {
					return null;
				}
			}

			DisplayType x = DisplayType.numeric(numberOfTraces);
			result.add(Pair.of(Field.tracesHighlighted.toString(), x));

			DisplayType y = DisplayType.numeric(numberOfEvents);
			result.add(Pair.of(Field.eventsHighlighted.toString(), y));
		}

		//fitness (highlighted)
		{
			DisplayType x = DisplayType.numeric(Fitness.compute(input));
			result.add(Pair.of(Field.fitnessHighlighted.toString(), x));
		}

		if (canceller.isCancelled()) {
			return null;
		}

		//not-highlighted
		if (input.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = input.clone();
			negativeLog.invert();

			int numberOfTraces = 0;
			int numberOfEvents = 0;
			for (IteratorWithPosition<IvMTrace> it = negativeLog.iterator(); it.hasNext();) {
				numberOfTraces++;

				IvMTrace trace = it.next();
				for (IvMMove move : trace) {
					if (move.getAttributes() != null) {
						numberOfEvents++;
					}
				}

				if (canceller.isCancelled()) {
					return null;
				}
			}

			DisplayType x = DisplayType.numeric(numberOfTraces);
			result.add(Pair.of(Field.tracesNotHighlighted.toString(), x));

			DisplayType y = DisplayType.numeric(numberOfEvents);
			result.add(Pair.of(Field.eventsNotHighlighted.toString(), y));

			DisplayType z = DisplayType.numeric(Fitness.compute(negativeLog));
			result.add(Pair.of(Field.fitnessNotHighlighted.toString(), z));

			if (canceller.isCancelled()) {
				return null;
			}

			//compute stochastic similarity
			{
				//transform to xlog
				XLog logA = IvMLog2XLog.convert(input, model);
				XLog logB = IvMLog2XLog.convert(negativeLog, model);

				EMSCParametersLogLogAbstract parameters = new EMSCParametersLogLogDefault();
				parameters.setComputeStochasticTraceAlignments(false);
				StochasticTraceAlignmentsLogLog alignments = EarthMoversStochasticConformancePlugin.measureLogLog(logA,
						logB, parameters, canceller);

				if (canceller.isCancelled()) {
					return null;
				}

				DisplayType zz = DisplayType.numeric(alignments.getSimilarity());
				result.add(Pair.of(Field.stochasticSimilarity.toString(), zz));
			}

		} else {
			DisplayType x = DisplayType.numeric(0);
			result.add(Pair.of(Field.tracesNotHighlighted.toString(), x));

			DisplayType y = DisplayType.numeric(0);
			result.add(Pair.of(Field.eventsNotHighlighted.toString(), y));

			DisplayType zz = DisplayType.NA();
			result.add(Pair.of(Field.fitnessNotHighlighted.toString(), zz));

			DisplayType z = DisplayType.NA();
			result.add(Pair.of(Field.stochasticSimilarity.toString(), z));
		}

		return result;
	}

	public void addVirtualAttributePlaceholders() {
		for (Field field : Field.values()) {
			add(Pair.of(field.toString(), DisplayType.literal("[computing..]")));
		}
	}

	public void addVirtualAttributes(List<Pair<String, DisplayType>> attributes) {
		removeVirtualAttributes();
		addAll(attributes);
		sort();
	}

	public void setVirtualAttributesToPlaceholders() {
		for (int i = 0; i < size(); i++) {
			for (Field field : Field.values()) {
				Pair<String, ? extends DisplayType> p = get(i);
				if (p.getA().equals(field.toString())) {
					set(i, Pair.of(p.getA(), DisplayType.literal("[computing..]")));
					break;
				}
			}
		}
	}

	private void removeVirtualAttributes() {
		Iterator<Pair<String, ? extends DisplayType>> it = iterator();
		while (it.hasNext()) {
			Pair<String, ? extends DisplayType> p = it.next();
			for (Field field : Field.values()) {
				if (p.getA().equals(field.toString())) {
					it.remove();
					break;
				}
			}
		}
	}
}