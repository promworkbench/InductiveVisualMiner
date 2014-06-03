package org.processmining.plugins.inductiveVisualMiner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.visualisation.AnimatableSVGPanel;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

@Plugin(name = "Inductive visual Miner animation test", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Log" }, userAccessible = false)
@Visualizer
public class AnimationPlugin {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, XLog log) throws IOException {

		//create svg file
		SVGUniverse universe = new SVGUniverse();

		InputStream stream = new FileInputStream(new File("d://animationTest.svg"));
		universe.clear();
		universe.getLoadedDocumentURIs();
		URI uri = universe.loadSVG(stream, "hoi");

		SVGDiagram diagram = universe.getDiagram(uri);

		AnimatableSVGPanel panel = new AnimatableSVGPanel(universe);
		panel.setImage(diagram, true);
		
		panel.setAnimationMaxTime(AnimatableSVGPanel.getExtremeTimes(diagram.getRoot()).get(1));

		return panel;
	}
}
