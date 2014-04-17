package org.processmining.plugins.inductiveVisualMiner.alignment;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;
import org.processmining.processtree.impl.AbstractTask.Automatic;

public class Move {

	public enum Type {
		model, log, synchronous
	}

	public final Type type;
	public final UnfoldedNode unode;
	public final XEventClass eventClass;

	public Move(Type type, UnfoldedNode unode) {
		this.type = type;
		this.unode = unode;
		this.eventClass = null;
	}
	
	public Move(Type type, UnfoldedNode unode, XEventClass eventClass) {
		this.type = type;
		this.unode = unode;
		this.eventClass = eventClass;
	}

//	public Move(StepTypes moveType, Object event, Map<Transition, UnfoldedNode> mapTransition2Path) {
//		if ((moveType == StepTypes.MINVI || moveType == StepTypes.LMGOOD) && event instanceof Transition) {
//			//synchronous move
//			type = Type.synchronous;
//			this.unode = mapTransition2Path.get(event);
//			this.eventClass = null;
//		} else if (moveType == StepTypes.L && event instanceof XEventClass) {
//			//log move
//			type = Type.log;
//			this.unode = null;
//			this.eventClass = (XEventClass) event;
//		} else if (moveType == StepTypes.MREAL && event instanceof Transition) {
//			//model move
//			type = Type.model;
//			this.unode = mapTransition2Path.get(event);
//			this.eventClass = null;
//		} else {
//			System.out.println("unknown move " + moveType + " " + event);
//			this.type = null;
//			this.unode = null;
//			this.eventClass = null;
//		}
//	}

	public Move(XEventClass eventClass, UnfoldedNode unode) {
		this.unode = unode;
		this.eventClass = eventClass;
		if ((unode != null && eventClass != null) || (unode != null && unode.getNode() instanceof Automatic)) {
			this.type = Type.synchronous;
		} else if (unode != null) {
			this.type = Type.model;
		} else {
			this.type = Type.log;
		}
	}

	public String toString() {
		if (unode != null) {
			return type + " " + unode.toString();
		} else {
			return type + " " + eventClass.toString();
		}
	}

	@Override
	public int hashCode() {
		if (unode != null) {
			return type.hashCode() ^ unode.hashCode();
		} else {
			return type.hashCode() ^ eventClass.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Move)) {
			return false;
		}
		Move arg = (Move) obj;
		if (!type.equals(arg.type)) {
			return false;
		}
		if (unode != null) {
			return unode.equals(arg.unode);
		} else {
			return eventClass.equals(arg.eventClass);
		}
	}
}
