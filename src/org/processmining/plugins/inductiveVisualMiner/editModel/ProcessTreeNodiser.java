package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
@Deprecated
public class ProcessTreeNodiser {

	private int spaces = 0;
	private final int spacesPerTab;
	private final ProcessTreeTokeniser tokenizer;
	private boolean redoToken;

	private String activityName;
	private NodeType lastNodeType;
	private int lastIndentation;
	private int lastLineNumber;

	public enum NodeType {
		activity, tau, xor, sequence, concurrent, loop, interleaving, or;
	}

	public ProcessTreeNodiser(String string, int spacesPerTab) {
		Reader reader = new StringReader(string);
		tokenizer = new ProcessTreeTokeniser(reader);
		tokenizer.eolIsSignificant(true);
		spaces = 0;
		lastLineNumber = 0;
		this.spacesPerTab = spacesPerTab;
	}

	/**
	 * Fetches the next node and returns whether it exists.
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean nextNode() throws IOException {
		if (redoToken) {
			redoToken = false;
			return true;
		}
		
		while (tokenizer.nextToken() != ProcessTreeTokeniser.TT_EOF) {
			if (tokenizer.ttype == '\t') {
				spaces += spacesPerTab;
				continue;
			} else if (tokenizer.ttype == ProcessTreeTokeniser.TT_EOL) {
				spaces = 0;
				lastLineNumber++;
				continue;
			}

			boolean isQuotedString = tokenizer.ttype != StreamTokenizer.TT_WORD;

			activityName = tokenizer.sval;

			// chop off trailing spaces and count them as indentation
			while (!isQuotedString
					&& (activityName.startsWith(" ") || activityName
							.startsWith("\t"))) {
				if (activityName.startsWith(" ")) {
					spaces += 1;
				} else {
					spaces += spacesPerTab;
				}
				activityName = activityName.substring(1);
			}

			// if nothing is left of the string, there's probably a quoted
			// string afterwards; do nothing
			if (!isQuotedString && activityName.equals("")) {
				continue;
			}

			//System.out.println(spaces + ": " + "\"" + activityName + "\"");
			lastIndentation = spaces;
			spaces = 0;

			if (isQuotedString) {
				lastNodeType = NodeType.activity;
				return true;
			}
			switch (activityName) {
			case "tau":
				lastNodeType = NodeType.tau;
				return true;
			case "xor":
				lastNodeType = NodeType.xor;
				return true;
			case "sequence":
				lastNodeType = NodeType.sequence;
				return true;
			case "parallel":
			case "concurrent":
				lastNodeType = NodeType.concurrent;
				return true;
			case "interleaved":
				lastNodeType = NodeType.interleaving;
				return true;
			case "loop":
				lastNodeType = NodeType.loop;
				return true;
			case "or":
				lastNodeType = NodeType.or;
				return true;
			default:
				lastNodeType = NodeType.activity;
				return true;
			}
		}

		return false;
	}

	public String getLastActivity() {
		return activityName;
	}

	public NodeType getLastNodeType() {
		return lastNodeType;
	}

	public int getLastIndentation() {
		return lastIndentation;
	}
	
	public int getLastLineNumber() {
		return lastLineNumber;
	}
	
	public void pushBack() {
		redoToken = true;
	}
}
