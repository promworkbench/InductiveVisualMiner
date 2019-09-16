package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters;

import java.awt.Component;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersView;

public class PreMiningFiltersView extends IvMFiltersView {

	private static final long serialVersionUID = -4325994113653500535L;

	public PreMiningFiltersView(Component parent) {
		super(parent, "pre-mining filters - " + InductiveVisualMinerPanel.title,
				"These filters alter the traces and events on which a model is discovered. "
						+ "Deviations, animation and performance are computed on the full (unfiltered) log.");
	}

}
