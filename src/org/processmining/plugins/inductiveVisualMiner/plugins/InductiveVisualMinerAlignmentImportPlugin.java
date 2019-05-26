package org.processmining.plugins.inductiveVisualMiner.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.log.XContextMonitoredInputStream;
import org.xeslite.parser.XesLiteXmlParser;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;

@Plugin(name = "Inductive visual Miner alignment", parameterLabels = { "Filename" }, returnLabels = {
		"Inductive visual Miner alignment" }, returnTypes = { InductiveVisualMinerAlignment.class })
@UIImportPlugin(description = "Inductive visual Miner alignment files", extensions = { "ivma" })
public class InductiveVisualMinerAlignmentImportPlugin extends AbstractImportPlugin {
	public InductiveVisualMinerAlignment importFromStream(PluginContext context, InputStream input, String filename,
			long fileSizeInBytes) throws Exception {

		XLog log = (XLog) new ZipXESImporter().importFromFile(context, getFile());

		return new InductiveVisualMinerAlignment(log);
	}

	/**
	 * XES Lite always checks the file extension. We can't use that, so had to
	 * copy Felix's code here and adapt to remove this check.
	 * 
	 * @author sander
	 *
	 */

	private class ZipXESImporter {
		protected Object importFromFile(PluginContext context, File file) throws Exception {
			try {
				try (InputStream is = getInputStream(file)) {
					return importFromStreamWithFactory(context, is, file.getName(), file.length(),
							new org.xeslite.lite.factory.XFactoryLiteImpl());
				}
			} catch (Exception e) {
				throw new Exception(Throwables.getRootCause(e).getMessage() + "\n\nDebug message:\n"
						+ Throwables.getStackTraceAsString(e));
			}
		}

		protected InputStream getInputStream(File file) throws Exception {
			return new GZIPInputStream(new FileInputStream(file), 65536);
		}

		protected XLog importFromStreamWithFactory(PluginContext context, InputStream input, String fileName,
				long fileSizeInBytes, XFactory factory) throws Exception {
			Collection<XLog> logs = parseFile(fileName,
					new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()), factory);

			if (logs.size() == 0) {
				throw new Exception("No processes contained in log! Parser could did not find any log.");
			}

			XLog log = Iterators.getOnlyElement(logs.iterator());
			if (XConceptExtension.instance().extractName(log) == null) {
				XConceptExtension.instance().assignName(log, "Anonymous log imported from " + fileName);
			}

			if (context != null) {
				context.getFutureResult(0).setLabel(XConceptExtension.instance().extractName(log));
			}

			return log;
		}

		private final Collection<XLog> parseFile(String fileName, XContextMonitoredInputStream input, XFactory factory)
				throws Exception {
			XParser parser = new XesLiteXmlParser(factory, true);
			return parser.parse(input);
		}
	}
}
