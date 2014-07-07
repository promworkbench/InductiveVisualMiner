package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.AbstractTrace;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class TraceView extends JFrame {

	private static final long serialVersionUID = 8386546677949149002L;
	private final TraceBuilder<Object> traceBuilder;
	private final ProMTraceList<Object> traceView;
	
	private Object showing = null;

	public TraceView() {
		super("Inductive visual Miner trace view");

		traceBuilder = new TraceBuilder<Object>() {
			public Trace<? extends Event> build(final Object element) {
				if (element instanceof AlignedTrace) {
					//aligned trace
					return (AlignedTrace) element;
				} else {
					//normal trace
					AbstractTrace<Event> trace = new ProMTraceView.AbstractTrace<Event>() {

						protected List<Event> delegate() {
							return Lists.transform((IMTrace) element, new Function<XEventClass, Event>() {
								public Event apply(final XEventClass input) {
									return new ProMTraceView.AbstractEvent() {
										public String getLabel() {
											return input.toString();
										}
									};
								}
							});
						}

					};

					return trace;
				}
			}
		};

		traceView = new ProMTraceList<>(traceBuilder);
		add(traceView);
		traceView.setForeground(Color.white);
		traceView.setForeground(Color.white);
		traceView.setBackground(new Color(30, 30, 30));
		traceView.setOpaque(true);
		

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		pack();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void set(IMLog log) {
		if (!log.equals(showing)) {
			traceView.clear();
			traceView.addAll((Collection) log.toSet());
		}
		showing = log;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void set(AlignedLog alog) {
		if (!alog.equals(showing)) {
			traceView.clear();
			traceView.addAll((Collection) alog.toSet());
		}
		showing = alog;
	}

	public ProMTraceList<Object> getTraceView() {
		return traceView;
	}
}
