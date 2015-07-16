package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.inductiveVisualMiner.animation.svgToken.SVGTokens;

public class ExportAnimation {
	public static void exportSVG(final SVGTokens tokens, final OutputStream streamOut, final Dot dot)
			throws IOException {
		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(streamOut, "utf-8"));

		//copy all lines except for the last two
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

//	public static void saveSVGtoFile(final IvMLog timedLog, final AlignedLogVisualisationInfo info,
//			final ColouringMode colourMode, final SVGDiagram svg, final Canceller canceller, final Dot dot, final File file)
//			throws IOException {
//		final Pair<SVGTokens, Scaler> p = ComputeAnimation.computeSVGTokens(timedLog, info, colourMode, svg, canceller);
//		OutputStream streamOut = new FileOutputStream(file);
//		ExportAnimation.exportSVG(p.getA(), streamOut, dot);
//	}

//	public static boolean saveAVItoFile(final IvMLog timedLog, final AlignedLogVisualisationInfo info,
//			final ColouringMode colourMode, final SVGDiagram svg, final Dot dot, final File file, final JPanel panel)
//			throws IOException {
//
//		final GuaranteedProgressMonitor progressMonitor = new GuaranteedProgressMonitor(panel, "",
//				"Preparing animation", 0, 100);
//
//		final ImageOutputStream streamOutMovie = new FileImageOutputStream(file);
//
//		//set up progress monitor and new canceller
//		final Canceller canceller = new Canceller() {
//			public boolean isCancelled() {
//				return progressMonitor.isCanceled();
//			}
//		};
//
//		//create svg tokens
//		final Pair<SVGTokens, Scaler> p = ComputeAnimation.computeSVGTokens(timedLog, info, colourMode, svg, canceller);
//
//		//create svg and copy the stream
//		PipedInputStream stream = copy(new Function<PipedOutputStream, Object>() {
//			public Integer call(PipedOutputStream out) throws Exception {
//				exportSVG(p.getA(), out, dot);
//				return null;
//			}
//		});
//
//		//load svg
//		SVGUniverse universe = new SVGUniverse();
//		URI uri = universe.loadSVG(stream, "hoi");
//		SVGDiagram diagram = universe.getDiagram(uri);
//
//		//set constants for this animation
//		double timeMargin = 0.5;
//		int framerate = 30;
//		int width = 1500;
//
//		double[] ex = AnimatableSVGPanel.getExtremeTimes(diagram.getRoot());
//		double minDuration = ex[0] - timeMargin;
//		double maxDuration = ex[1] + timeMargin;
//		int height = (int) (width * (diagram.getHeight() / (diagram.getWidth() * 1.0)));
//
//		progressMonitor.setNote("Rendering animation..");
//
//		//compute the number of frames
//		long frames = Math.round((maxDuration - minDuration) * (framerate * 1.0));
//
//		//export movie
//		AnimatedSVGExporter svgExporter = new AnimatedSVGExporter(universe, diagram, width, height);
//
//		//create format
//		Format format = new Format(EncodingKey, VideoFormatKeys.ENCODING_AVI_MJPG, DepthKey, 24);
//		format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
//				FrameRateKey, new Rational(framerate, 1),//
//				WidthKey, width, //
//				HeightKey, height);
//
//		// Create a buffered image for this format
//		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		Graphics2D g = img.createGraphics();
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//		//render the movie
//		AVIWriter out = null;
//		try {
//			// Create the writer
//			out = new AVIWriter(streamOutMovie);
//
//			// Add a track to the writer
//			out.addTrack(format);
//			out.setPalette(0, img.getColorModel());
//
//			// initialize the animation
//			g.setBackground(Color.WHITE);
//			g.clearRect(0, 0, img.getWidth(), img.getHeight());
//
//			//			System.out.println("frames " + frames);
//
//			for (long frame = 0; frame < frames; frame++) {
//
//				double time = minDuration + ((frame / (1.0 * frames)) * (maxDuration - minDuration));
//
//				// Create an animation frame
//				svgExporter.paintFrame(g, time);
//
//				//				System.out.println("frame " + frame + " done @" + time);
//
//				// write it to the writer
//				out.write(0, img, 1);
//
//				//write it to file
//				//				File file = new File("d:\\animationTest\\" + frame + ".png");
//				//				ImageIO.write(img, "png", file);
//
//				//write progress
//				progressMonitor.setProgress((int) Math.round(frame / (frames / 100.0)));
//				if (canceller.isCancelled()) {
//					return false;
//				}
//			}
//
//		} finally {
//			// Close the writer
//			if (out != null) {
//				out.close();
//			}
//
//			// Dispose the graphics object
//			g.dispose();
//		}
//
//		progressMonitor.close();
//		return !canceller.isCancelled();
//	}

	public static PipedInputStream copy(final Function<PipedOutputStream, Object> f) throws IOException {
		PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);
		new Thread(new Runnable() {
			public void run() {
				try {
					f.call(out);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return in;
	}
}
