package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.view;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

public class IvMFilterTreeViews extends SideWindow {

	private static final long serialVersionUID = 2422473098128821284L;

	private final IvMFilterTreeView<?>[] views;

	public IvMFilterTreeViews(Component parent, String title, IvMFilterTreeView<?>... views) {
		super(parent, title + " - visual Miner");
		this.views = views;

		setSize(500, 600);
		setLayout(new BorderLayout());

		if (views.length == 1) {
			add(views[0], BorderLayout.CENTER);
		} else {
			JTabbedPane tabs = new JTabbedPane();
			add(tabs, BorderLayout.CENTER);
			for (IvMFilterTreeView<?> view : views) {
				tabs.add(view, view.getTitle());
			}
		}
	}

	public IvMFilterTreeView<?> getView(int i) {
		return views[i];
	}

}