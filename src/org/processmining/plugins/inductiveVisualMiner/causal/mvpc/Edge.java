package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.EnumSet;
import java.util.Objects;

public class Edge {

	//	class Property(Enum):
	//        dd = 1
	//        nl = 2
	//        pd = 3
	//        pl = 4
	public static enum Property {
		dd, nl, pd, pl
	}

	public EnumSet<Property> properties;
	private Node node1;
	private Node node2;
	private Endpoint endpoint1;
	private Endpoint endpoint2;
	private int numerical_endpoint_1;
	private int numerical_endpoint_2;

	//	def __init__(self, node1: Node, node2: Node, end1: Endpoint, end2: Endpoint):
	//        self.properties = []
	//
	//        if node1 is None or node2 is None:
	//            raise TypeError('Nodes must not be of NoneType. node1 = ' + str(node1) + ' node2 = ' + str(node2))
	//
	//        if end1 is None or end2 is None:
	//            raise TypeError(
	//                'Endpoints must not be of NoneType. endpoint1 = ' + str(end1) + ' endpoint2 = ' + str(end2))
	//
	//        # assign nodes and endpoints; if the edge points left, flip it
	//        if self.pointing_left(end1, end2):
	//            self.node1 = node2
	//            self.node2 = node1
	//            self.endpoint1 = end2
	//            self.endpoint2 = end1
	//            self.numerical_endpoint_1 = end2.value
	//            self.numerical_endpoint_2 = end1.value
	//        else:
	//            self.node1 = node1
	//            self.node2 = node2
	//            self.endpoint1 = end1
	//            self.endpoint2 = end2
	//            self.numerical_endpoint_1 = end1.value
	//            self.numerical_endpoint_2 = end2.value
	public Edge(Node node1, Node node2, Endpoint end1, Endpoint end2) {
		properties = EnumSet.noneOf(Property.class);

		if (node1 == null || node2 == null) {
			throw new RuntimeException("Nodes must not be of NoneType. node1 = " + node1 + " node2 = " + node2);
		}

		if (end1 == null || end2 == null) {
			throw new RuntimeException(
					"Endpoints must not be of NoneType. endpoint1 = " + end1 + " endpoint2 = " + end2);
		}

		//# assign nodes and endpoints; if the edge points left, flip it
		if (pointing_left(end1, end2)) {
			this.node1 = node2;
			this.node2 = node1;
			this.endpoint1 = end2;
			this.endpoint2 = end1;
			this.numerical_endpoint_1 = end2.value();
			this.numerical_endpoint_2 = end1.value();
		} else {
			this.node1 = node1;
			this.node2 = node2;
			this.endpoint1 = end1;
			this.endpoint2 = end2;
			this.numerical_endpoint_1 = end1.value();
			this.numerical_endpoint_2 = end2.value();
		}
	}

	public Node get_node1() {
		return node1;
	}

	public Node get_node2() {
		return node2;
	}

	//    # returns True if the edge is pointing "left"
	//    def pointing_left(self, endpoint1: Endpoint, endpoint2: Endpoint):
	//        return endpoint1 == Endpoint.ARROW and (endpoint2 == Endpoint.TAIL or endpoint2 == Endpoint.CIRCLE)
	public boolean pointing_left(Endpoint endpoint1, Endpoint endpoint2) {
		return endpoint1 == Endpoint.ARROW && (endpoint2 == Endpoint.TAIL || endpoint2 == Endpoint.CIRCLE);
	}

	public int hashCode() {
		return Objects.hash(endpoint1, endpoint2, node1, node2);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		return endpoint1 == other.endpoint1 && endpoint2 == other.endpoint2 && Objects.equals(node1, other.node1)
				&& Objects.equals(node2, other.node2);
	}

	public int getNumerical_endpoint1() {
		return numerical_endpoint_1;
	}

	public int getNumerical_endpoint2() {
		return numerical_endpoint_2;
	}

	public Endpoint get_endpoint1() {
		return endpoint1;
	}

	public Endpoint get_endpoint2() {
		return endpoint2;
	}
}