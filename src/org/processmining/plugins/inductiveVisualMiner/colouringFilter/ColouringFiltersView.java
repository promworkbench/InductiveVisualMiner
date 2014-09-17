package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ColouringFiltersView extends SideWindow {

	private static final long serialVersionUID = -5500440632866414477L;

	public ColouringFiltersView(Component parent) {
		super(parent, "Inductive visual Miner - colouring filters");
	}
	
	public void initialise(List<ColouringFilter> colouringFilters) {
		for (ColouringFilter colouringFilter : colouringFilters) {
			JLabel label = SlickerFactory.instance().createLabel(colouringFilter.getName() + " ... initialising ...");
			add(label);
		}
	}

	public void setPanel(ColouringFilter colouringFilter) {
		add(colouringFilter.getPanel());
	}
}
