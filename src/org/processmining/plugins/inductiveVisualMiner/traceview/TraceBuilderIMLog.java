package org.processmining.plugins.inductiveVisualMiner.traceview;

import java.awt.Color;
import java.util.Iterator;

import org.deckfour.xes.model.XEvent;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class TraceBuilderIMLog implements TraceBuilder<Object> {

	private final IMLog log;

	public TraceBuilderIMLog(IMLog log) {
		this.log = log;
	}

	public Trace<? extends Event> build(final Object trace) {
		return new ProMTraceView.Trace<Event>() {

			public Iterator<Event> iterator() {
				return FluentIterable.from((IMTrace) trace).transform(new Function<XEvent, Event>() {
					public Event apply(final XEvent input) {
						return new ProMTraceView.AbstractEvent() {
							public String getLabel() {
								return log.classify(input).toString();
							}

							public String getTopLabel() {
								Long timestamp = ResourceTimeUtils.getTimestamp(input);
								return ResourceTimeUtils.timeToString(timestamp);
							}

							public String getBottomLabel() {
								return log.getLifeCycle(input).toString();
							}
						};
					}
				}).iterator();
			}

			public String getName() {
				String s;
				if (((IMTrace) trace).getAttributes().containsKey("concept:name")) {
					s = ((IMTrace) trace).getAttributes().get("concept:name").toString();
				} else {
					s = "";
				}
				if (s.length() > 9) {
					return s.substring(0, 7) + "..";
				}
				return s;
			}

			public Color getNameColor() {
				return Color.white;
			}

			public String getInfo() {
				return null;
			}

			public Color getInfoColor() {
				return null;
			}
		};
	}
}