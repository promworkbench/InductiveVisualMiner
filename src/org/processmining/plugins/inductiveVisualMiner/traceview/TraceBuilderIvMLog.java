package org.processmining.plugins.inductiveVisualMiner.traceview;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

class TraceBuilderIvMLog extends TraceBuilderWrapper {

	private final Selection selection;
	private final IvMModel model;

	public TraceBuilderIvMLog(IvMModel model, Selection selection, TraceColourMap traceColourMap) {
		super(traceColourMap);
		this.selection = selection;
		this.model = model;
	}

	public TraceBuilderIvMLogTrace build(final Object trace) {
		return new TraceBuilderIvMLogTrace((IvMTrace) trace);
	}

	public class TraceBuilderIvMLogTrace implements ProMTraceView.Trace<IvMMove> {
		private IvMTrace trace;

		public TraceBuilderIvMLogTrace(IvMTrace trace) {
			this.trace = trace;
		}

		public IvMTrace getTrace() {
			return trace;
		}

		public Iterator<IvMMove> iterator() {
			return FluentIterable.from(trace).filter(new Predicate<IvMMove>() {
				@SuppressWarnings("unchecked")
				public boolean apply(final IvMMove input) {
					if (input.isTauStart()) {
						return false;
					}
					if (input.isSyncMove() && model.isTau(input.getTreeNode())) {
						List<? extends Move> trace2 = trace;
						return selection.isSelected(model, (List<Move>) trace2, input);
					}
					return true;
				}
			}).iterator();
		}

		public String getName() {
			String s = trace.getName();
			if (s.length() == 0) {
				return " ";
			}
			return s;
		}

		public Color getNameColor() {
			return IvMDecorator.textColour;
		}

		public String getInfo() {
			if (traceColourMap == null) {
				return "";
			}
			String s = traceColourMap.getValue((trace));
			if (s.length() > 9) {
				return s.substring(0, 7) + "..";
			}
			return s;
		}

		public Color getInfoColor() {
			if (traceColourMap == null) {
				return null;
			}
			return traceColourMap.getColour((trace));
		}
	}
}