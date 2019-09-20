package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class UserStatus {
	TIntObjectMap<String> component2status;

	private final static int maxShown = 2;

	public UserStatus() {
		component2status = new TIntObjectHashMap<>(10, 0.5f, -1);
	}

	public void setStatus(String message, int index) {
		if (message == null) {
			component2status.remove(index);
		} else {
			component2status.put(index, message);
		}
	}

	public String getText() {
		if (component2status.isEmpty()) {
			return " ";
		}

		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		int i = 0;
		for (TIntObjectIterator<String> it = component2status.iterator(); it.hasNext();) {
			it.advance();
			builder.append(it.value());
			i++;

			if (i == maxShown && it.hasNext()) {
				builder.append("(+" + (component2status.size() - maxShown) + ")");
				break;
			}

			if (it.hasNext()) {
				builder.append("<br>");
			}
		}
		builder.append("</html>");

		return builder.toString();
	}
}