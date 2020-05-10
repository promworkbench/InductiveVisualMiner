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
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
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
			add(Pair.of("number of events", DisplayType.numeric(numberOfEvents)));
		}

		add(Pair.of("number of traces", DisplayType.numeric(sortedXLog.size())));

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
		tracesHighlighted {
			public String toString() {
				return "number of highlighted traces";
			}
		},
		eventsHighlighted {
			public String toString() {
				return "number of highlighted events";
			}
		}
	}

	public static List<Pair<String, DisplayType>> computeVirtualAttributes(IvMLogFiltered input,
			IvMCanceller canceller) {
		ArrayList<Pair<String, DisplayType>> result = new ArrayList<>();

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

		}

		DisplayType x = DisplayType.numeric(numberOfTraces);
		result.add(Pair.of(Field.tracesHighlighted.toString(), x));

		DisplayType y = DisplayType.numeric(numberOfEvents);
		result.add(Pair.of(Field.eventsHighlighted.toString(), y));

		if (input.isSomethingFiltered()) {

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
					set(i, Pair.of(p.toString(), DisplayType.literal("[computing..]")));
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