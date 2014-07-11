package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import java.awt.Color;

import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.colourMaps.ColourMapBlue;
import org.processmining.plugins.graphviz.colourMaps.ColourMapFixed;
import org.processmining.plugins.graphviz.colourMaps.ColourMapOpacity;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMap;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps.SizeMapLinear;

public class AlignedLogVisualisationParameters {
	
	//nodes
	private boolean showFrequenciesOnNodes = true;
	private ColourMap colourNodes = new ColourMapBlue();
	
	//model edges
	private ColourMap colourModelEdges = new ColourMapOpacity(new ColourMapFixed(Color.black));
	private SizeMap modelEdgesWidth = new SizeMapLinear(1, 3);
	private boolean showFrequenciesOnModelEdges = false;
	
	//moves
	private boolean showLogMoves = true;
	private boolean showModelMoves = true;
	private ColourMap colourMoves = new ColourMapFixed(Color.red);
	private SizeMap moveEdgesWidth = new SizeMapLinear(3, 4);
	private boolean showFrequenciesOnMoveEdges = true;
	private boolean repairLogMoves = false;
	
	private boolean addOnClick = false;
	
	public boolean isShowLogMoves() {
		return showLogMoves;
	}
	public void setShowLogMoves(boolean showLogMoves) {
		this.showLogMoves = showLogMoves;
	}
	public boolean isShowModelMoves() {
		return showModelMoves;
	}
	public void setShowModelMoves(boolean showModelMoves) {
		this.showModelMoves = showModelMoves;
	}
	public boolean isShowFrequenciesOnNodes() {
		return showFrequenciesOnNodes;
	}
	public void setShowFrequenciesOnNodes(boolean showFrequenciesOnNodes) {
		this.showFrequenciesOnNodes = showFrequenciesOnNodes;
	}
	public ColourMap getColourNodes() {
		return colourNodes;
	}
	public void setColourNodes(ColourMap colourNodes) {
		this.colourNodes = colourNodes;
	}
	public ColourMap getColourModelEdges() {
		return colourModelEdges;
	}
	public void setColourModelEdges(ColourMap colourModelEdges) {
		this.colourModelEdges = colourModelEdges;
	}
	public ColourMap getColourMoves() {
		return colourMoves;
	}
	public void setColourMoves(ColourMap colourModelMoves) {
		this.colourMoves = colourModelMoves;
	}
	public SizeMap getModelEdgesWidth() {
		return modelEdgesWidth;
	}
	public void setModelEdgesWidth(SizeMap modelEdgesWidth) {
		this.modelEdgesWidth = modelEdgesWidth;
	}
	public SizeMap getMoveEdgesWidth() {
		return moveEdgesWidth;
	}
	public void setMoveEdgesWidth(SizeMap moveEdgesWidth) {
		this.moveEdgesWidth = moveEdgesWidth;
	}
	public boolean isShowFrequenciesOnModelEdges() {
		return showFrequenciesOnModelEdges;
	}
	public void setShowFrequenciesOnModelEdges(boolean showFrequenciesOnModelEdges) {
		this.showFrequenciesOnModelEdges = showFrequenciesOnModelEdges;
	}
	public boolean isShowFrequenciesOnMoveEdges() {
		return showFrequenciesOnMoveEdges;
	}
	public void setShowFrequenciesOnMoveEdges(boolean showFrequenciesOnMoveEdges) {
		this.showFrequenciesOnMoveEdges = showFrequenciesOnMoveEdges;
	}
	public boolean isRepairLogMoves() {
		return repairLogMoves;
	}
	public void setRepairLogMoves(boolean repairLogMoves) {
		this.repairLogMoves = repairLogMoves;
	}
	public boolean isAddOnClick() {
		return addOnClick;
	}
	public void setAddOnClick(boolean addOnClick) {
		this.addOnClick = addOnClick;
	}
}
