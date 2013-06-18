package gui.dialog.rbac;
import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class RoleGraphViewer {

	Graph<Integer, String> g;
	int nodeCount, edgeCount;
	Factory<Integer> vertexFactory;
	Factory<String> edgeFactory;

	/** Creates a new instance of SimpleGraphView */
	public RoleGraphViewer() {
		// Graph<V, E> where V is the type of the vertices and E is the type of
		// the edges
		g = new SparseMultigraph<Integer, String>();
		nodeCount = 0;
		edgeCount = 0;
		vertexFactory = new Factory<Integer>() { // My vertex factory
			public Integer create() {
				return nodeCount++;
			}
		};
		edgeFactory = new Factory<String>() { // My edge factory
			public String create() {
				return "E" + edgeCount++;
			}
		};
	}

}
