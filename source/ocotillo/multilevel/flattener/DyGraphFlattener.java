package ocotillo.multilevel.flattener;

import java.util.Iterator;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Function;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.coarsening.GraphCoarsener;

public abstract class DyGraphFlattener {

	public DyGraphFlattener() {
		// TODO Auto-generated constructor stub
	}

	public abstract Graph flattenDyGraph(DyGraph flattener); 

	public static class SumFlattener extends DyGraphFlattener{

		@Override
		public Graph flattenDyGraph(DyGraph flattener) {
			Graph graph = new Graph();
			//NodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
			//DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
			NodeAttribute<Double> nodeWeight = graph.nodeAttribute(StdAttribute.weight);
			EdgeAttribute<Double> edgeWeight = graph.edgeAttribute(StdAttribute.weight);	

			DyNodeAttribute<Boolean> nodePresence = flattener.nodeAttribute(StdAttribute.dyPresence);
			DyEdgeAttribute<Boolean> edgePresence = flattener.edgeAttribute(StdAttribute.dyPresence);

			for(Node dyN : flattener.nodes()) {
				Node n;
				if(!graph.hasNode(dyN.id()))
					n = graph.newNode(dyN.id()+"__0");
				else
					n = graph.getNode(dyN.id()+"__0");
				double presence = 0.0;
				Iterator<Function<Boolean>> it = nodePresence.get(dyN).iterator(); 
				while(it.hasNext()) {
					Function<Boolean> f = it.next();
					Interval currInterval = f.interval();
					presence += currInterval.rightBound() - currInterval.leftBound(); 
				}
				nodeWeight.set(n, presence);
				System.out.println("Reconstructed Node " + n.id() + " with presence " + nodeWeight.get(n));
			}            

			for(Edge dyE : flattener.edges()) {               
				Node src = dyE.source();
				Node tgt = dyE.target();

				Edge e = graph.newEdge(
						graph.getNode(GraphCoarsener.createTranslatedNodeId(src.id(), 0)),
						graph.getNode(GraphCoarsener.createTranslatedNodeId(tgt.id(), 0)));


				double presence = 0.0;
				Iterator<Function<Boolean>> it = edgePresence.get(dyE).iterator(); 
				while(it.hasNext()) {
					Function<Boolean> f = it.next();
					Interval currInterval = f.interval();
					presence += currInterval.rightBound() - currInterval.leftBound(); 
				}
				edgeWeight.set(e, presence);
				System.out.println("Reconstructed Edge From " + src.id() + " to " + tgt.id() + " with presence " + edgeWeight.get(e));
			}
			return graph;
		}
	}
}
