package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.OnOffPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.SwitchPanel;

import gnu.trove.map.hash.THashMap;

public abstract class IvMFiltersView extends SideWindow {
	private static final long serialVersionUID = -5500440632866414477L;
	private final JPanel panel;
	private final OnOffPanel<JScrollPane> onOffPanel;
	private final Map<IvMFilter, JPanel> filter2panel;
	private final Map<IvMFilter, JCheckBox> filter2checkbox;
	private Runnable onUpdate;

	public class IvMFiltersViewPanel extends JPanel implements Scrollable {
		private static final long serialVersionUID = 8311080909592746520L;

		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}

	public IvMFiltersView(Component parent, String title, String header, List<? extends IvMFilter> filters,
			IvMDecoratorI decorator) {
		super(parent, title);
		setLayout(new BorderLayout());
		filter2panel = new THashMap<>();
		filter2checkbox = new THashMap<>();

		panel = new IvMFiltersViewPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		//panel.setBackground(decorator.backGroundColour1);
		panel.setOpaque(false);

		JTextArea explanation = new JTextArea(header);
		decorator.decorate(explanation);
		explanation.setWrapStyleWord(true);
		explanation.setLineWrap(true);
		explanation.setEnabled(false);
		panel.add(explanation);

		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		onOffPanel = new OnOffPanel<>(decorator, scrollPane);
		onOffPanel.setOffMessage("Waiting for attributes..");
		onOffPanel.off();

		add(onOffPanel, BorderLayout.CENTER);

		//add the filters
		{
			//sort the filters
			Collections.sort(filters, new Comparator<IvMFilter>() {
				public int compare(IvMFilter o1, IvMFilter o2) {
					return o1.getFilterName().compareTo(o2.getFilterName());
				}
			});

			for (IvMFilter filter2 : filters) {

				final IvMFilter filter = filter2;

				//filter sub-panel
				final SwitchPanel subPanel;
				{
					subPanel = new SwitchPanel(decorator) {
						private static final long serialVersionUID = 132082897536007044L;

						@Override
						protected java.awt.GradientPaint getDisabledGradient() {
							return null;
						};
					};
					subPanel.setEnabled(false);
					subPanel.setBorder(2, 0, 0, 0, decorator.backGroundColour1());
					subPanel.setLayout(new BorderLayout());
					panel.add(subPanel);
				}

				//add filter panel
				filter.createFilterGui(new Runnable() {
					public void run() {
						if (onUpdate != null) {
							onUpdate.run();
						}
					}
				}, decorator);
				subPanel.add(filter.getPanel(), BorderLayout.CENTER);
				filter2panel.put(filter, filter.getPanel());

				//add checkbox
				{
					final JCheckBox checkBox = new JCheckBox();
					decorator.decorate(checkBox);
					subPanel.add(checkBox, BorderLayout.LINE_START);
					checkBox.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							boolean x = checkBox.isSelected();
							filter.setEnabledFilter(x);
							subPanel.setEnabled(x);
							onUpdate.run();
						}
					});
					filter2checkbox.put(filter, checkBox);
				}
			}
		}
	}

	public Runnable getOnUpdate() {
		return onUpdate;
	}

	public void setOnUpdate(Runnable onUpdate) {
		this.onUpdate = onUpdate;
	}

	public OnOffPanel<JScrollPane> getOnOffPanel() {
		return onOffPanel;
	}
}
