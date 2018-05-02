package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTreeTokeniser;

public class DfgEdgeNodiser {
	private int spaces = 0;
	private final int spacesPerTab = 4;
	private final DfgTokeniser tokenizer;
	private boolean redoToken;

	private String activityName;
	private NodeType lastNodeType;
	private int lastIndentation;
	private int lastLineNumber;

	public enum NodeType {
		activity, edgeSymbol;
	}

	public DfgEdgeNodiser(String string) {
		Reader reader = new StringReader(string);
		tokenizer = new DfgTokeniser(reader);
		tokenizer.eolIsSignificant(true);
		spaces = 0;
		lastLineNumber = 0;
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
			while (!isQuotedString && (activityName.startsWith(" ") || activityName.startsWith("\t"))) {
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
				case "->" :
					lastNodeType = NodeType.edgeSymbol;
					return true;
				default :
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
