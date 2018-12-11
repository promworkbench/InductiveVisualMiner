package org.processmining.plugins.inductiveVisualMiner.traceview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.List;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.WedgeBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceBuilderIvMLog.TraceBuilderIvMLogTrace;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class TraceViewEventColourMap implements WedgeBuilder {
	private final TIntObjectMap<Color> mapFill = new TIntObjectHashMap<Color>(10, 0.5f, -1);
	private final TIntObjectMap<Color> mapFont = new TIntObjectHashMap<Color>(10, 0.5f, -1);
	private Selection selection = new Selection();
	private IvMModel model;

	public TraceViewEventColourMap(IvMModel tree) {
		this.model = tree;
	}

	public void set(int unode, Color colourFill, Color colourFont) {
		mapFill.put(unode, colourFill);
		mapFont.put(unode, colourFont);
	}

	public void setSelectedNodes(Selection selection) {
		this.selection = selection;
	}

	public Color buildWedgeColor(Trace<? extends Event> trace, Event event) {
		if (event instanceof Move && !((Move) event).isModelMove()) {
			return mapFill.get(((Move) event).getTreeNode());
		}
		return null;
	}

	public Integer assignWedgeGap(Trace<? extends Event> trace, Event event) {
		return 2;
	}

	static final float dash1[] = { 10.0f };
	static final Stroke selectedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
			dash1, 0.0f);

	@SuppressWarnings("unchecked")
	public Stroke buildBorderStroke(Trace<? extends Event> trace, Event event) {
		if (trace instanceof TraceBuilderIvMLogTrace) {
			IvMTrace trace2 = ((TraceBuilderIvMLogTrace) trace).getTrace();
			List<? extends Move> trace3 = trace2;
			if (event instanceof Move && selection.isSelected(model, (List<Move>) trace3, (Move) event)) {
				return selectedStroke;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Color buildBorderColor(Trace<? extends Event> trace, Event event) {
		if (trace instanceof TraceBuilderIvMLogTrace) {
			IvMTrace trace2 = ((TraceBuilderIvMLogTrace) trace).getTrace();
			List<? extends Move> trace3 = trace2;
			if (event instanceof Move && selection.isSelected(model, (List<Move>) trace3, (Move) event)) {
				return Color.white;
			}
		}
		return null;
	}

	public Color buildLabelColor(Trace<? extends Event> trace, Event event) {
		if (event instanceof Move) {
			if (((Move) event).isSyncMove()) {
				return mapFont.get(((Move) event).getTreeNode());
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