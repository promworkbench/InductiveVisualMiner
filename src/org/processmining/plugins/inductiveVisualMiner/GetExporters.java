package org.processmining.plugins.inductiveVisualMiner;

import java.util.List;

import org.processmining.plugins.graphviz.visualisation.export.Exporter;

public interface GetExporters {
	public List<Exporter> getExporters(List<Exporter> exporters);
}
