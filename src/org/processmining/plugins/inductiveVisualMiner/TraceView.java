package org.processmining.plugins.inductiveVisualMiner;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.ColorBuilder;
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
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class TraceView extends JFrame {

	public static class TraceViewColourMap implements ColorBuilder {
		private Map<UnfoldedNode, Color> map = new HashMap<>();

		public void set(UnfoldedNode unode, Color colour) {
			map.put(unode, colour);
		}

		public void clear() {
			map.clear();
		}

		public Color buildWedgeColor(Trace<? extends Event> trace, Event event) {
			if (event instanceof Move) {
				return map.get(((Move) event).getUnode());
			}
			return null;
		}

		public Color buildLabelColor(Trace<? extends Event> trace, Event event) {
			return null;
		}

		public Color buildTopLabelColor(Trace<? extends Event> trace, Event event) {
			return null;
		}

		public Color buildBottomLabelColor(Trace<? extends Event> trace, Event event) {
			return null;
		}

		public Color buildBottom2LabelColor(Trace<? extends Event> trace, Event event) {
			return null;
		}
	}

	private static final long serialVersionUID = 8386546677949149002L;
	private final TraceBuilder<Object> traceBuilder;
	private final ProMTraceList<Object> traceView;

	private Object showing = null;

	public TraceView(Component parent) {
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
		traceView.setBackground(new Color(30, 30, 30));
		traceView.setOpaque(true);

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setSize(400, 600);
		setLocationRelativeTo(parent);
	}

	/**
	 * update the trace view with an IM log
	 * 
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
	 * 
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
	 * 
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

	public void setColourMap(TraceViewColourMap colourMap) {
		traceView.setColorBuilder(colourMap);
	}
}
