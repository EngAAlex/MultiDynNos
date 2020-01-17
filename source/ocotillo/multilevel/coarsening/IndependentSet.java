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
import ocotillo.dygraph.Evolution;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.flattener.DyGraphFlattener.StaticSumPresenceFlattener;

public class IndependentSet extends GraphCoarsener {

	public final static long DEFAULT_THRESHOLD = 5;

	private final long threshold;

	public IndependentSet(DyGraph original, long threshold) {
		super(original);
		this.threshold = threshold;
	}


	@Override
	protected boolean stoppingCondition() {
		return coarserGraph.nodeCount() < threshold;
	}

	@Override
	protected void preprocess() { //Flatten the graph and generate the weights
		StaticSumPresenceFlattener flat = new StaticSumPresenceFlattener(); 
		flat.addWeightAttribute(coarserGraph);
	}

	@Override
	protected DyGraph computeNewVertexSet(DyGraph lastLevel) {
		DyNodeAttribute<Double> lastLevelNodeWeight = lastLevel.nodeAttribute(StdAttribute.weight);
		DyEdgeAttribute<Double> lastLevelEdgeWeight = lastLevel.edgeAttribute(StdAttribute.weight);

		List<Node> nodes = new ArrayList<Node>(lastLevel.nodes());

		Collections.sort(nodes, new Comparator<Node>(){
			public int compare(Node a, Node b) {
				double weightA = lastLevelNodeWeight.get(a).getDefaultValue();
				double weightB = lastLevelNodeWeight.get(b).getDefaultValue();

				for(Edge e : lastLevel.outEdges(a))
					weightA += lastLevelEdgeWeight.get(e).getDefaultValue();

				for(Edge e : lastLevel.outEdges(b))
					weightB += lastLevelEdgeWeight.get(e).getDefaultValue();

				if(weightA > weightB)
					return -1;
				else if(weightA < weightB)
					return 1;				
				return 0;				
			}
		});

		DyGraph newLevel = new DyGraph();

		while(!nodes.isEmpty()) {
			Node topNode = nodes.remove(0);
			Node newLevelNode = new Node(getTranslatedNodeId(topNode.id(), current_level));			

			currentLevelEdgeAssociations.put(topNode.id(), newLevelNode.id());
			HashSet<String> newLevelNodeGroup = new HashSet<String>();
			newLevelNodeGroup.add(topNode.id());
			for(Edge e : getCollectionOfNeighbors(lastLevel.inOutEdges(topNode), lastLevelEdgeWeight)) {
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

	protected Collection<Edge> getCollectionOfNeighbors(Collection<Edge> inOutEdges, DyEdgeAttribute<Double> edgeWeights) {
		return inOutEdges;
	}


	public static class WalshawIndependentSet extends IndependentSet{

		public WalshawIndependentSet(DyGraph original, long threshold) {
			super(original, threshold);
		}

		@Override
		protected Collection<Edge> getCollectionOfNeighbors(Collection<Edge> inOutEdges, DyEdgeAttribute<Double> edgeWeights) {
			List<Edge> edges = new ArrayList<Edge>(inOutEdges);

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

	}

}	


