package org.processmining.plugins.inductiveVisualMiner.traceview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JScrollPane;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;

public class TraceView extends SideWindow {

	private static final long serialVersionUID = 8386546677949149002L;
	private final ProMTraceList<Object> traceView;
	private TraceColourMap traceColourMap;

	private Object showing = null;

	public TraceView(Component parent) {
		super(parent, "trace view - " + InductiveVisualMinerPanel.title);

		TraceBuilder<Object> traceBuilder = new TraceBuilder<Object>() {
			public Trace<? extends Event> build(Object element) {
				return null;
			}
		};

		traceView = new ProMTraceList<>(traceBuilder);
		add(traceView);
		
		//replace the scroll pane
		traceView.remove(traceView.getScrollPane());
		JScrollPane scrollPane = new JScrollPane(traceView.getList());
		traceView.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setOpaque(false);
		
		traceView.getList().setOpaque(true);
		traceView.getList().setBackground(IvMDecorator.backGroundColour1);

		traceView.setMaxWedgeWidth(130);
		traceView.setFixedInfoWidth(50);
		traceView.setOpaque(false);
	}
	
	@Override
	public void paintComponents(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setPaint(IvMDecorator.getGradient(getHeight()));
		g2d.fillRect(0, 0, getWidth(), getHeight());
		super.paintComponents(g);
	}

	/**
	 * update the trace view with an IM log
	 * 
	 * @param log
	 */
	@SuppressWarnings({ "unchecked" })
	public void set(IMLog log, TraceColourMap traceColourMap) {
		if (!log.equals(showing)) {
			showing = log;
			traceView.clear();
			traceView.setTraceBuilder(new TraceBuilderIMLog(log, traceColourMap));
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
	public void set(IvMModel model, IvMLog tlog, Selection selection, TraceColourMap traceColourMap) {
		if (!tlog.equals(showing)) {
			showing = tlog;
			traceView.clear();
			traceView.setTraceBuilder(new TraceBuilderIvMLog(model, selection, traceColourMap));
			traceView.addAll((Iterable<Object>) ((Iterable<? extends Object>) tlog));
		}
	}

	public void setEventColourMap(TraceViewEventColourMap colourMap) {
		traceView.setWedgeBuilder(colourMap);
	}

	public void setTraceColourMap(TraceColourMap traceColourMap) {
		((TraceBuilderWrapper) traceView.getTraceBuilder()).setTraceColourMap(traceColourMap);
	}

}
