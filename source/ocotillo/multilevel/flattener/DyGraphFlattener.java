package ocotillo.multilevel.flattener;

import java.util.Iterator;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
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
	
	public abstract DyGraph addWeightAttribute(DyGraph flattener);
	
	protected abstract double computeAggregatedValue(double currValue, Interval interval);
	
	protected double yieldNodeAggregatedPresenceValue(DyNodeAttribute<Boolean> presenceAttribute, Node n) {
		double presence = 0.0;
		Iterator<Function<Boolean>> it = presenceAttribute.get(n).iterator(); 
		while(it.hasNext()) {
			Function<Boolean> f = it.next();
			Interval currInterval = f.interval();
			presence = computeAggregatedValue(presence, currInterval);
		}
		return presence;
	}
	
	protected double yieldEdgeAggregatedPresenceValue(DyEdgeAttribute<Boolean> presenceAttribute, Edge e) {
		double presence = 0.0;
		Iterator<Function<Boolean>> it = presenceAttribute.get(e).iterator(); 
		while(it.hasNext()) {
			Function<Boolean> f = it.next();
			Interval currInterval = f.interval();
			presence = computeAggregatedValue(presence, currInterval);						
		}
		return presence;
	}

	/**
	 * @author Alessio
	 * 
	 * In this flattener, the aggregated value is encoded as the default value of the evolution of the attribute.
	 *
	 */
	public static class StaticSumPresenceFlattener extends DyGraphFlattener{

		@Override
		public Graph flattenDyGraph(DyGraph toFlatten) {
			Graph flattenedGraph = new Graph();
			//NodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
			//DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
			NodeAttribute<Double> nodeWeight = flattenedGraph.nodeAttribute(StdAttribute.weight);
			EdgeAttribute<Double> edgeWeight = flattenedGraph.edgeAttribute(StdAttribute.weight);	

			DyNodeAttribute<Boolean> nodePresence = toFlatten.nodeAttribute(StdAttribute.dyPresence);
			DyEdgeAttribute<Boolean> edgePresence = toFlatten.edgeAttribute(StdAttribute.dyPresence);

			for(Node dyN : toFlatten.nodes()) {
				Node n;
				if(!flattenedGraph.hasNode(dyN.id()))
					n = flattenedGraph.newNode(dyN.id());
				else
					n = flattenedGraph.getNode(dyN.id());				
				nodeWeight.set(n, yieldNodeAggregatedPresenceValue(nodePresence, dyN));
				System.out.println("Reconstructed Node " + n.id() + " with presence " + nodeWeight.get(n));
			}            

			for(Edge dyE : toFlatten.edges()) {               
				Node src = dyE.source();
				Node tgt = dyE.target();

				Edge e = flattenedGraph.newEdge(
						flattenedGraph.getNode(src.id()),
						flattenedGraph.getNode(tgt.id()));
				
				edgeWeight.set(e, yieldEdgeAggregatedPresenceValue(edgePresence, dyE));
				System.out.println("Reconstructed Edge From " + src.id() + " to " + tgt.id() + " with presence " + edgeWeight.get(e));
			}
			return flattenedGraph;
		}

		@Override
		public DyGraph addWeightAttribute(DyGraph toFlatten) {
			DyNodeAttribute<Double> nodeWeight = toFlatten.nodeAttribute(StdAttribute.weight);
			DyEdgeAttribute<Double> edgeWeight = toFlatten.edgeAttribute(StdAttribute.weight);	

			DyNodeAttribute<Boolean> nodePresence = toFlatten.nodeAttribute(StdAttribute.dyPresence);
			DyEdgeAttribute<Boolean> edgePresence = toFlatten.edgeAttribute(StdAttribute.dyPresence);
			
			for(Node dyN : toFlatten.nodes()) {				
				nodeWeight.set(dyN, new Evolution<Double>(yieldNodeAggregatedPresenceValue(nodePresence, dyN)));
				System.out.println("Reconstructed Node " + dyN.id() + " with aggregated presence (weight) " + nodeWeight.get(dyN).getDefaultValue());
			}            

			for(Edge dyE : toFlatten.edges()) {               
				Node src = dyE.source();
				Node tgt = dyE.target();
				
				edgeWeight.set(dyE, new Evolution<Double>(yieldEdgeAggregatedPresenceValue(edgePresence, dyE)));
				System.out.println("Reconstructed Edge From " + src.id() + " to " + tgt.id() + " with aggregated presence (weight) " + edgeWeight.get(dyE).getDefaultValue());
			}
			
			return toFlatten;
		}

		@Override
		protected double computeAggregatedValue(double currValue, Interval interval) {
			return currValue + (interval.rightBound() - interval.leftBound());
		}
	}
}
