package org.processmining.plugins.inductiveVisualMiner.dataanalysis.associations;

public enum AssociationType {
	association,

	associationMeasure {
		public String toString() {
			return "association measure";
		}
	},

	associationPlot {
		public String toString() {
			return "association plot";
		}
	}
}