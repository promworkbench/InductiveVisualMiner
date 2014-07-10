package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.InductiveMiner.mining.IMLog;
import org.processmining.plugins.InductiveMiner.mining.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedLog;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedMove;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedTrace;
import org.processmining.processtree.Task.Automatic;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class TraceView extends JFrame {

	private static final long serialVersionUID = 8386546677949149002L;
	private final TraceBuilder<Object> traceBuilder;
	private final ProMTraceList<Object> traceView;

	private Object showing = null;

	public TraceView() {
		super("Inductive visual Miner trace view");

		traceBuilder = new TraceBuilder<Object>() {
			public Trace<? extends Event> build(final Object element) {
				if (element instanceof TimedTrace) {
					//timed trace
					return new ProMTraceView.Trace<TimedMove>() {

						public Iterator<TimedMove> iterator() {
							return FluentIterable.from((TimedTrace) element).filter(new Predicate<TimedMove>() {
								public boolean apply(final TimedMove input) {
									return !(input.isSyncMove() && input.getUnode().getNode() instanceof Automatic);
								}
							}).iterator();
						}

						public String getName() {
							return null;
						}

						public String getInfo() {
							return null;
						}

					};
				} else if (element instanceof AlignedTrace) {
					//aligned trace
					return new ProMTraceView.Trace<Move>() {

						public Iterator<Move> iterator() {
							return FluentIterable.from((AlignedTrace) element).filter(new Predicate<Move>() {
								public boolean apply(final Move input) {
									return !(input.isSyncMove() && input.getUnode().getNode() instanceof Automatic);
								}
							}).iterator();
						}

						public String getName() {
							return null;
						}

						public String getInfo() {
							return null;
						}

					};

				} else {
					//normal trace
					return new ProMTraceView.Trace<Event>() {

						public Iterator<Event> iterator() {
							return FluentIterable.from((IMTrace) element).transform(new Function<XEventClass, Event>() {
								public Event apply(final XEventClass input) {
									return new ProMTraceView.AbstractEvent() {
										public String getLabel() {
											return input.toString();
										}
									};
								}
							}).iterator();
						}

						public String getName() {
							return null;
						}

						public String getInfo() {
							return null;
						}
					};
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

	/**
	 * update the trace view with an IM log
	 * @param log
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void set(IMLog log) {
		if (!log.equals(showing)) {
//			traceView.clear();
//			traceView.addAll((Collection) log.toSet());
		}
		showing = log;
	}

	/**
	 * update the trace view with an aligned log
	 * @param alog
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void set(AlignedLog alog) {
		if (!alog.equals(showing)) {
//			traceView.clear();
//			traceView.addAll((Collection) alog.toSet());
		}
		showing = alog;
	}
	
	/**
	 * update the trace view with a timed log
	 * @param tlog
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void set(TimedLog tlog) {
		if (!tlog.equals(showing)) {
			traceView.clear();
			traceView.addAll((Collection) tlog);
		}
		showing = tlog;
	}
}
