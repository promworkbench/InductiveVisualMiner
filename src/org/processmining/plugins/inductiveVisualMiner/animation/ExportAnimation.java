package org.processmining.plugins.inductiveVisualMiner.animation;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.HeightKey;
import static org.monte.media.VideoFormatKeys.WidthKey;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.graphviz.visualisation.AnimatableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.AnimatedSVGExporter;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.Function;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

public class ExportAnimation {
	public static void export(SVGTokens tokens, OutputStream streamOut, Dot dot, SVGDiagram svg)
			throws IOException {
		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);
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

	public static void exportMovie(final SVGTokens tokens, ImageOutputStream streamOutMovie,
			final Dot dot, final SVGDiagram svg) throws IOException {

		//create svg and copy the stream
		PipedInputStream stream = copy(new Function<PipedOutputStream, Integer>() {
			public Integer call(PipedOutputStream out) throws Exception {
				export(tokens, out, dot, svg);
				return null;
			}
		});

		//load svg
		SVGUniverse universe = new SVGUniverse();
		URI uri = universe.loadSVG(stream, "hoi");
		SVGDiagram diagram = universe.getDiagram(uri);
		
		//set constants for this animation
		double timeMargin = 0.5;
		int framerate = 30;
		int width = 2000;
		
		List<Double> ex = AnimatableSVGPanel.getExtremeTimes(diagram.getRoot());
		double minDuration = ex.get(0) - timeMargin;
		double maxDuration = ex.get(1) + timeMargin;
		int height = (int) (width * (diagram.getHeight() / (diagram.getWidth() * 1.0)));
		
		//compute the number of frames
		long frames = Math.round((maxDuration - minDuration) * (framerate * 1.0));
		
		//export movie
		AnimatedSVGExporter svgExporter = new AnimatedSVGExporter(universe, diagram, width, height);
		
		//create format
		Format format = new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24);
		format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
                FrameRateKey, new Rational(framerate, 1),//
				WidthKey, width, //
                HeightKey, height);
		
		// Create a buffered image for this format
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //render the movie
        AVIWriter out = null;
        try {
            // Create the writer
            out = new AVIWriter(streamOutMovie);

            // Add a track to the writer
            out.addTrack(format);
            out.setPalette(0, img.getColorModel());

            // initialize the animation
            g.setBackground(Color.WHITE);
            g.clearRect(0, 0, img.getWidth(), img.getHeight());
            
            System.out.println("frames " + frames);
            
            for (long frame = 0; frame < frames; frame++) {
            	
            	double time = minDuration + ((frame / (1.0 * frames)) * (maxDuration - minDuration));
                
            	// Create an animation frame
            	svgExporter.paintFrame(g, time);
            	
            	System.out.println("frame " + frame + " done @" + time);
                
                // write it to the writer
                out.write(0, img, 1);
                
                //write it to file
                File file = new File("d:\\animationTest\\" + frame + ".png");
                ImageIO.write(img, "png", file);
            }

        } finally {
            // Close the writer
            if (out != null) {
                out.close();
            }
            
            // Dispose the graphics object
            g.dispose();
        }
        
//		Main.testWriting(streamOutMovie, new Format(EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24));
	}

	public static PipedInputStream copy(final Function<PipedOutputStream, Integer> f) throws IOException {
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
