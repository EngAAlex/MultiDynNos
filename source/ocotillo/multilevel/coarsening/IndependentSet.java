package ocotillo.multilevel.coarsening;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.flattener.DyGraphFlattener.StaticSumPresenceFlattener;

public class IndependentSet extends GraphCoarsener {


	@Override
	protected void initialize() { //Flatten the graph and generate the weights
		StaticSumPresenceFlattener flat = new StaticSumPresenceFlattener(); 
		flat.addWeightAttribute(getCoarsestGraph());
	}

	@Override
	protected DyGraph computeNewVertexSet(DyGraph lastLevel) {
		DyNodeAttribute<Double> lastLevelNodeWeight = lastLevel.nodeAttribute(StdAttribute.weight);
		DyEdgeAttribute<Double> lastLevelEdgeWeight = lastLevel.edgeAttribute(StdAttribute.weight);

		List<Node> nodes = new ArrayList<Node>(lastLevel.nodes());

		Collections.sort(nodes, new GraphCoarsener.NodeWeightComparator(lastLevel, lastLevelEdgeWeight, lastLevelNodeWeight));

		DyGraph newLevel = new DyGraph();

		while(!nodes.isEmpty()) {
			Node topNode = nodes.remove(0);
			Node newLevelNode = newLevel.newNode((getTranslatedNodeId(topNode.id(), current_level)));			

			currentLevelEdgeAssociations.put(topNode.id(), newLevelNode.id());
			HashSet<String> newLevelNodeGroup = new HashSet<String>();
			newLevelNodeGroup.add(topNode.id());
			for(Edge e : getCollectionOfNeighbors(lastLevel.outEdges(topNode), lastLevelEdgeWeight)) {
				Node neighbor = e.otherEnd(topNode);
				if(nodes.contains(neighbor)) {
					nodes.remove(neighbor);
					currentLevelEdgeAssociations.put(neighbor.id(), newLevelNode.id());
					newLevelNodeGroup.add(neighbor.id());
				}				
			}		
			currentLevelNodeGroups.put(newLevelNode.id(), newLevelNodeGroup);
		}
		return newLevel;
	}

	protected Collection<Edge> getCollectionOfNeighbors(Collection<Edge> outEdges, DyEdgeAttribute<Double> edgeWeights) {
		return outEdges;
	}


	public static class WalshawIndependentSet extends IndependentSet{

		public WalshawIndependentSet() {
			super();
		}

		@Override
		protected Collection<Edge> getCollectionOfNeighbors(Collection<Edge> outEdges, DyEdgeAttribute<Double> edgeWeights) {
			List<Edge> edges = new ArrayList<Edge>(outEdges);

			if(edges.isEmpty())
				return edges;
			
			Collections.sort(edges, new Comparator<Edge>(){
				public int compare(Edge a, Edge b) {
					double weightA = edgeWeights.get(a).getDefaultValue();
					double weightB = edgeWeights.get(b).getDefaultValue();

					if(weightA > weightB)
						return 1;
					else if(weightA < weightB)
						return -1;				
					return 0;				
				}
			});

			ArrayList<Edge> list = new ArrayList<Edge>(1);
			list.add(edges.get(0));
			return list;
		}
		
		@Override
		public String getDescription() {
			return "Walshaw - Independent Set";
		}

	}


	@Override
	public String getDescription() {
		return "GRIP - Independent Set";
	}

}	


