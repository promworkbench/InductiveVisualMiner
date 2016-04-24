package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

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

public abstract class IvMFiltersView extends SideWindow {
	private static final long serialVersionUID = -5500440632866414477L;
	private final JPanel panel;
	private final Map<IvMFilter, JComponent> filter2label;
	private final Map<IvMFilter, Integer> filter2position;
	private int highFilters;
	
	public IvMFiltersView(Component parent, String title, String header) {
		super(parent, title);
		
		panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.gray);
		add(panel);
		filter2label = new HashMap<>();
		filter2position = new HashMap<>();
		highFilters = 0;
		
		JLabel explanation = new JLabel("<html>" + header + "</html>");
		GridBagConstraints cLabel = new GridBagConstraints();
		cLabel.gridx = 1;
		cLabel.gridy = 1;
		cLabel.fill = GridBagConstraints.HORIZONTAL;
		cLabel.anchor = GridBagConstraints.WEST;
		panel.add(explanation, cLabel);
	}
	
	public void initialise(List<? extends IvMFilter> colouringFilters) {
		int gridy = 2;
		Collections.sort(colouringFilters, new Comparator<IvMFilter>() {
			public int compare(IvMFilter o1, IvMFilter o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (IvMFilter colouringFilter : colouringFilters) {
			//add label
			JLabel label = SlickerFactory.instance().createLabel(colouringFilter.getName() + " initialising ...");
			GridBagConstraints cLabel = new GridBagConstraints();
			cLabel.gridx = 1;
			cLabel.gridy = gridy;
			cLabel.fill = GridBagConstraints.VERTICAL;
			cLabel.anchor = GridBagConstraints.EAST;
			panel.add(label, cLabel);

			filter2label.put(colouringFilter, label);
			filter2position.put(colouringFilter, new Integer(gridy));

			gridy++;
		}
	}
	
	public void setPanel(final IvMFilter filter, final Runnable onUpdate) {

		//if the colouringfilter did not initialise, print error message
		if (filter.getPanel() == null) {
			((JLabel) filter2label.get(filter)).setText(filter.getName()
					+ " did not initialise properly. It could be that the log contains inconsistent attribute types.");
			return;
		}

		//remove initialising message
		panel.remove(filter2label.get(filter));

		//add panel
		{
			GridBagConstraints cPanel = new GridBagConstraints();
			cPanel.gridx = 1;
			cPanel.gridy = filter2position.get(filter);
			cPanel.fill = GridBagConstraints.BOTH;
			cPanel.anchor = GridBagConstraints.NORTH;
			cPanel.weightx = 1;
			if (filter.getPanel().isUsesVerticalSpace()) {
				highFilters++;
				cPanel.weighty = 1 / (highFilters * 1.0);
			}
			panel.add(filter.getPanel(), cPanel);
		}

		//add checkbox
		{
			JCheckBox checkBox = new JCheckBox();
			checkBox.setBackground(Color.gray);
			GridBagConstraints cCheckBox = new GridBagConstraints();
			cCheckBox.gridx = 2;
			cCheckBox.gridy = filter2position.get(filter);
			cCheckBox.anchor = GridBagConstraints.CENTER;
			panel.add(checkBox, cCheckBox);
			checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean x = filter.swapEnabledFilter();
					if (x) {
						filter.getPanel().setBackground(Color.white);
					} else {
						filter.getPanel().setBackground(Color.gray);
					}
					onUpdate.run();
				}
			});
		}
	}
}
