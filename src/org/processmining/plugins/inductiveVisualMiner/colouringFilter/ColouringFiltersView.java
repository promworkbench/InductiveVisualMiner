package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ColouringFiltersView extends SideWindow {

	private static final long serialVersionUID = -5500440632866414477L;
	private final JPanel panel;
	private final Map<ColouringFilter, JComponent> filter2panel;
	
	public ColouringFiltersView(Component parent) {
		super(parent, "Inductive visual Miner - colouring filters");
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(panel);
		filter2panel = new HashMap<>();
	}
	
	public void initialise(List<ColouringFilter> colouringFilters) {
		for (ColouringFilter colouringFilter : colouringFilters) {
			JLabel label = SlickerFactory.instance().createLabel(colouringFilter.getName() + " ... initialising ...");
			panel.add(label);
			filter2panel.put(colouringFilter, label);
		}
	}

	public void setPanel(ColouringFilter colouringFilter) {
		panel.add(colouringFilter.getPanel());
		panel.remove(filter2panel.get(colouringFilter));
	}
}
