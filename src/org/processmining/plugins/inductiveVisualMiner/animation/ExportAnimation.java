package org.processmining.plugins.inductiveVisualMiner.animation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;

public class ExportAnimation {
	public static void export(SVGTokens tokens, OutputStream streamOut, InductiveVisualMinerPanel panel)
			throws IOException {
		InputStream stream = Dot2Image.dot2imageInputStream(panel.getGraph().getDot(), Type.svg);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(streamOut, "utf-8"));

		String line = null;
		String lastLine = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		while ((line = in.readLine()) != null) {
			if (lastLine != null) {
				if (lastLine.equals("</g>") && line.equals("</svg>")) {
					writer.write(tokens.toString());
					writer.write("\n");
				}
				writer.write(lastLine);
				writer.write("\n");
			}
			lastLine = line;
		}
		if (lastLine != null) {
			writer.write(lastLine);
			writer.write("\n");
		}
		writer.close();
	}
}
