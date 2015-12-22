package org.processmining.plugins.inductiveVisualMiner.traceview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.WedgeBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class TraceViewColourMap implements WedgeBuilder {
	private final Map<UnfoldedNode, Color> mapFill = new HashMap<>();
	private final Map<UnfoldedNode, Color> mapFont = new HashMap<>();
	private Selection selection = new Selection();

	public void set(UnfoldedNode unode, Color colourFill, Color colourFont) {
		mapFill.put(unode, colourFill);
		mapFont.put(unode, colourFont);
	}

	public void setSelectedNodes(Selection selection) {
		this.selection = selection;
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
		if (event instanceof Move && selection.isSelected((Move) event)) {
			return selectedStroke;
		}
		return null;
	}

	public Color buildBorderColor(Trace<? extends Event> trace, Event event) {
		if (event instanceof Move && selection.isSelected((Move) event)) {
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