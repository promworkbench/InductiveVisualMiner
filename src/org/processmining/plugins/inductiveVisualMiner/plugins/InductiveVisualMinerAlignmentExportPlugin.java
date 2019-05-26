package org.processmining.plugins.inductiveVisualMiner.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.log.exporting.ExportLogXesGz;

@Plugin(name = "IvM alignment export", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Inductive visual Miner alignment", "File" }, userAccessible = true)
@UIExportPlugin(description = "Inductive visual Miner alignment", extension = "ivma")
public class InductiveVisualMinerAlignmentExportPlugin {
	@PluginVariant(variantLabel = "Dfg export (Directly follows graph)", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, InductiveVisualMinerAlignment ivmAlignment, File file)
			throws IOException {
		export(ivmAlignment, file);
	}

	public static void export(InductiveVisualMinerAlignment ivmAlignment, File file) throws IOException {
		new ExportLogXesGz().export(null, ivmAlignment.getAlignedLog(), file);
	}
}
