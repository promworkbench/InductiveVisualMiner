package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ColouringFiltersView extends SideWindow {

	private static final long serialVersionUID = -5500440632866414477L;
	private final JPanel panel;
	private final Map<ColouringFilter, JComponent> filter2panel;
	private final Map<ColouringFilter, Integer> filter2position;

	public ColouringFiltersView(Component parent) {
		super(parent, "Inductive visual Miner - colouring filters");
		panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.gray);
		add(panel);
		filter2panel = new HashMap<>();
		filter2position = new HashMap<>();
	}

	public void initialise(List<ColouringFilter> colouringFilters) {
		int gridy = 1;
		Collections.sort(colouringFilters, new Comparator<ColouringFilter>() {
			public int compare(ColouringFilter o1, ColouringFilter o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (ColouringFilter colouringFilter : colouringFilters) {
			//add label
			JLabel label = SlickerFactory.instance().createLabel(colouringFilter.getName() + " initialising ...");
			GridBagConstraints cLabel = new GridBagConstraints();
			cLabel.gridx = 1;
			cLabel.gridy = gridy;
			cLabel.fill = GridBagConstraints.VERTICAL;
			cLabel.anchor = GridBagConstraints.EAST;
			panel.add(label, cLabel);

			filter2panel.put(colouringFilter, label);
			filter2position.put(colouringFilter, new Integer(gridy));

			gridy++;
		}
	}

	public void setPanel(final ColouringFilter colouringFilter, final Runnable onUpdate) {
		//remove initialising message
		panel.remove(filter2panel.get(colouringFilter));
		
		//add panel
		{
			GridBagConstraints cPanel = new GridBagConstraints();
			cPanel.gridx = 1;
			cPanel.gridy = filter2position.get(colouringFilter);
			cPanel.fill = GridBagConstraints.HORIZONTAL;
			cPanel.anchor = GridBagConstraints.NORTH;
			cPanel.weightx = 1;
			panel.add(colouringFilter.getPanel(), cPanel);
		}
		
		//add checkbox
		{
			JCheckBox checkBox = new JCheckBox();
			checkBox.setBackground(Color.gray);
			GridBagConstraints cCheckBox = new GridBagConstraints();
			cCheckBox.gridx = 2;
			cCheckBox.gridy = filter2position.get(colouringFilter);
			cCheckBox.anchor = GridBagConstraints.CENTER;
			panel.add(checkBox, cCheckBox);
			checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean x = colouringFilter.swapEnabledFilter();
					if (x) {
						colouringFilter.getPanel().setBackground(Color.white);
					} else {
						colouringFilter.getPanel().setBackground(Color.gray);
					}
					onUpdate.run();
				}
			});
		}
	}
}
