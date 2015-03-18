package org.processmining.plugins.inductiveVisualMiner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Stroke;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.WedgeBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace2;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLog;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedLog;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedMove;
import org.processmining.plugins.inductiveVisualMiner.animation.TimedTrace;
import org.processmining.plugins.inductiveVisualMiner.animation.TimestampsAdder;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.processtree.Task.Automatic;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class TraceView extends SideWindow {

	public static class TraceViewColourMap implements WedgeBuilder {
		private final Map<UnfoldedNode, Color> mapFill = new HashMap<>();
		private final Map<UnfoldedNode, Color> mapFont = new HashMap<>();
		private Set<UnfoldedNode> selectedNodes = new HashSet<>();
		private Set<LogMovePosition> selectedLogMoves = new HashSet<>();

		public void set(UnfoldedNode unode, Color colourFill, Color colourFont) {
			mapFill.put(unode, colourFill);
			mapFont.put(unode, colourFont);
		}

		public void setSelectedNodes(Set<UnfoldedNode> selectedNodes, Set<LogMovePosition> selectedLogMoves) {
			this.selectedNodes = selectedNodes;
			this.selectedLogMoves = selectedLogMoves;
		}

		public Color buildWedgeColor(Trace<? extends Event> trace, Event event) {
			if (event instanceof Move && !((Move) event).isModelMove()) {
				return mapFill.get(((Move) event).getUnode());
			}
			return null;
		}

		public Integer assignWedgeGap(Trace<? extends Event> trace, Event event) {
			return 2;
		}

		static final float dash1[] = { 10.0f };
		static final Stroke selectedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
				dash1, 0.0f);

		public Stroke buildBorderStroke(Trace<? extends Event> trace, Event event) {
			if (event instanceof Move && selectedNodes.contains(((Move) event).getUnode())) {
				//selected node
				return selectedStroke;
			} else if (event instanceof Move && ((Move) event).isLogMove()
					&& selectedLogMoves.contains(LogMovePosition.of((Move) event))) {
				//selected log move
				return selectedStroke;
			}
			return null;
		}

		public Color buildBorderColor(Trace<? extends Event> trace, Event event) {
			if (event instanceof Move && selectedNodes.contains(((Move) event).getUnode())) {
				//selected node
				return Color.white;
			} else if (event instanceof Move && ((Move) event).isLogMove()
					&& selectedLogMoves.contains(LogMovePosition.of((Move) event))) {
				//selected log move
				return Color.white;
			}
			return null;

		}

		public Color buildLabelColor(Trace<? extends Event> trace, Event event) {
			if (event instanceof Move) {
				if (((Move) event).isSyncMove()) {
					return mapFont.get(((Move) event).getUnode());
				} else {
					return Color.black;
				}
			}
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

		public Color buildNameColor(Trace<? extends Event> trace) {
			return null;
		}

		public Color buildInfoColor(Trace<? extends Event> trace) {
			return null;
		}
	}

	private static class IMLog2TraceBuilder implements TraceBuilder<Object> {

		private final IMLog log;

		public IMLog2TraceBuilder(IMLog log) {
			this.log = log;
		}

		public Trace<? extends Event> build(final Object trace) {
			return new ProMTraceView.Trace<Event>() {

				public Iterator<Event> iterator() {
					return FluentIterable.from((IMTrace2) trace).transform(new Function<XEvent, Event>() {
						public Event apply(final XEvent input) {
							return new ProMTraceView.AbstractEvent() {
								public String getLabel() {
									return log.classify(input).toString();
								}
								
								public String getTopLabel() {
									Long timestamp = TimestampsAdder.getTimestamp(input);
									return TimestampsAdder.toString(timestamp);
								}
								
								public String getBottomLabel() {
									if (log.isStart(input)) {
										return "start";
									} else {
										return "complete";
									}
								}
							};
						}
					}).iterator();
				}

				public String getName() {
					String s;
					if (((IMTrace2) trace).getAttributes().containsKey("concept:name")) {
						s = ((IMTrace2) trace).getAttributes().get("concept:name").toString();
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

	private static class AlignedLogTraceBuilder implements TraceBuilder<Object> {

		private final AlignedLog alignedLog;

		public AlignedLogTraceBuilder(AlignedLog alignedLog) {
			this.alignedLog = alignedLog;
		}

		public Trace<? extends Event> build(final Object trace) {
			return new ProMTraceView.Trace<Move>() {

				public Iterator<Move> iterator() {
					return FluentIterable.from((AlignedTrace) trace).filter(new Predicate<Move>() {
						public boolean apply(final Move input) {
							return !((input.isSyncMove() && input.getUnode().getNode() instanceof Automatic) || input.isTauStart());
						}
					}).iterator();
				}

				public String getName() {
					return alignedLog.getCardinalityOf(trace) + "x";
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

	private static class TimedLogTraceBuilder implements TraceBuilder<Object> {

		public Trace<? extends Event> build(final Object trace) {
			return new ProMTraceView.Trace<TimedMove>() {

				public Iterator<TimedMove> iterator() {
					return FluentIterable.from((TimedTrace) trace).filter(new Predicate<TimedMove>() {
						public boolean apply(final TimedMove input) {
							return !((input.isSyncMove() && input.getUnode().getNode() instanceof Automatic) || input.isTauStart());
						}
					}).iterator();
				}

				public String getName() {
					String s = ((TimedTrace) trace).getId();
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

	private static final long serialVersionUID = 8386546677949149002L;
	private final ProMTraceList<Object> traceView;

	private Object showing = null;

	public TraceView(Component parent) {
		super(parent, "Inductive visual Miner - trace view");

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

		traceView.setBackground(new Color(30, 30, 30));
		traceView.setOpaque(true);
	}

	/**
	 * update the trace view with an IM log
	 * 
	 * @param log
	 */
	public void set(IMLog log) {
		if (!log.equals(showing)) {
			traceView.clear();
			traceView.setTraceBuilder(new IMLog2TraceBuilder(log));
			for (IMTrace t : log) {
				traceView.add(t);
			}
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
			traceView.clear();
			traceView.setTraceBuilder(new AlignedLogTraceBuilder(alog));
			traceView.addAll((Collection) alog.toSet());
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
			traceView.setTraceBuilder(new TimedLogTraceBuilder());
			traceView.addAll((Collection) tlog);
		}
		showing = tlog;
	}

	public void setColourMap(TraceViewColourMap colourMap) {
		traceView.setWedgeBuilder(colourMap);
	}
}
