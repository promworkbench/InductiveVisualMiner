package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ColouringFiltersView extends SideWindow {

	private static final long serialVersionUID = -5500440632866414477L;

	private HashMap<ColouringFilter, JPanel> filter2panel;

	public ColouringFiltersView(Component parent) {
		super(parent, "Inductive visual Miner - colouring filters");
		filter2panel = new HashMap<>();
	}
	
	public void initialise(List<ColouringFilter> colouringFilters) {
		for (ColouringFilter colouringFilter : colouringFilters) {
			JLabel label = SlickerFactory.instance().createLabel(colouringFilter.getName() + " ... initialising ...");
			add(label);
		}
	}

	public void setPanel(ColouringFilter colouringFilter, JPanel filterPanel) {
		filter2panel.put(colouringFilter, filterPanel);
		add(filterPanel);
	}
}
