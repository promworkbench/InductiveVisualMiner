package org.processmining.plugins.inductiveVisualMiner.traceview;

import java.awt.Color;
import java.util.Iterator;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.processtree.Task.Automatic;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

class TraceBuilderIvMLog implements TraceBuilder<Object> {
	
	private final Selection selection;
	
	public TraceBuilderIvMLog(Selection selection) {
		this.selection = selection;
	}

	public Trace<? extends Event> build(final Object trace) {
		return new ProMTraceView.Trace<IvMMove>() {

			public Iterator<IvMMove> iterator() {
				return FluentIterable.from((IvMTrace) trace).filter(new Predicate<IvMMove>() {
					public boolean apply(final IvMMove input) {
						if (input.isTauStart()) {
							return false;
						}
						if (input.isSyncMove() && input.getUnode().getNode() instanceof Automatic) {
							return selection.isSelected(input);
						}
						return true;
					}
				}).iterator();
			}

			public String getName() {
				String s = ((IvMTrace) trace).getName();
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