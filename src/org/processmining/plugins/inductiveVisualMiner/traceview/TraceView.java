package org.processmining.plugins.inductiveVisualMiner.traceview;

import java.awt.Color;
import java.awt.Component;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;

public class TraceView extends SideWindow {

	private static final long serialVersionUID = 8386546677949149002L;
	private final ProMTraceList<Object> traceView;

	private Object showing = null;

	public TraceView(Component parent) {
		super(parent, "trace view - Inductive visual Miner");

		TraceBuilder<Object> traceBuilder = new TraceBuilder<Object>() {
			public Trace<? extends Event> build(Object element) {
				return null;
			}
		};

		traceView = new ProMTraceList<>(traceBuilder);
		add(traceView);

		traceView.setForeground(Color.white);
		traceView.getList().setForeground(Color.white);
		traceView.getScrollPane().setForeground(Color.white);
		setForeground(Color.white);
		traceView.getScrollPane().getViewport().setForeground(Color.white);
		traceView.setMaxWedgeWidth(130);

		traceView.setBackground(new Color(30, 30, 30));
		traceView.setOpaque(true);
	}

	/**
	 * update the trace view with an IM log
	 * 
	 * @param log
	 */
	@SuppressWarnings({ "unchecked" })
	public void set(IMLog log) {
		if (!log.equals(showing)) {
			showing = log;
			traceView.clear();
			traceView.setTraceBuilder(new TraceBuilderIMLog(log));
			traceView.addAll((Iterable<Object>) ((Iterable<? extends Object>) log));
		}
	}

	/**
	 * update the trace view with a timed log
	 * 
	 * @param tlog
	 * @param selection 
	 */
	@SuppressWarnings({ "unchecked" })
	public void set(IvMEfficientTree tree, IvMLog tlog, Selection selection) {
		if (!tlog.equals(showing)) {
			showing = tlog;
			traceView.clear();
			traceView.setTraceBuilder(new TraceBuilderIvMLog(tree, selection));
			traceView.addAll((Iterable<Object>) ((Iterable<? extends Object>) tlog));
		}
	}

	public void setColourMap(TraceViewColourMap colourMap) {
		traceView.setWedgeBuilder(colourMap);
	}
	
}
