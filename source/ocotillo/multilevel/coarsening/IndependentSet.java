package ocotillo.multilevel.coarsening;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.options.MultiLevelOption;

public class IndependentSet extends GraphCoarsener {
		
	public final static long DEFAULT_THRESHOLD = 5;

	private final long threshold;

	public IndependentSet(Graph original, long threshold) {
		super(original);
		this.threshold = threshold;
	}
	

	@Override
	protected boolean stoppingCondition() {
		return coarserGraph.nodeCount() < threshold;
	}

	@Override
	protected Graph computeNewVertexSet(Graph lastLevel) {
		NodeAttribute<Double> lastLevelNodeWeight = lastLevel.nodeAttribute(StdAttribute.weight);
		EdgeAttribute<Double> lastLevelEdgeWeight = lastLevel.edgeAttribute(StdAttribute.weight);

		List<Node> nodes = new ArrayList<Node>(lastLevel.nodes());

		Collections.sort(nodes, new Comparator<Node>(){
			public int compare(Node a, Node b) {
				double weightA = lastLevelNodeWeight.get(a);
				double weightB = lastLevelNodeWeight.get(b);

				for(Edge e : lastLevel.outEdges(a))
					weightA += lastLevelEdgeWeight.get(e);

				for(Edge e : lastLevel.outEdges(b))
					weightB += lastLevelEdgeWeight.get(e);

				if(weightA > weightB)
					return -1;
				else if(weightA < weightB)
					return 1;				
				return 0;				
			}
		});

		Graph newLevel = new Graph();

		NodeAttribute<Double> newLevelNodeWeight = newLevel.nodeAttribute(StdAttribute.weight);

		while(!nodes.isEmpty()) {
			Node topNode = nodes.remove(0);
			Node newLevelNode = new Node(getTranslatedNodeId(topNode.id(), current_level));			

			currentLevelEdgeAssociations.put(topNode.id(), newLevelNode.id());
			HashSet<String> newLevelNodeGroup = new HashSet<String>();
			newLevelNodeGroup.add(topNode.id());
			double totalWeight = lastLevelNodeWeight.get(topNode);
			for(Edge e : getCollectionOfNeighbors(lastLevel.inOutEdges(topNode), lastLevelEdgeWeight)) {
				Node neighbor = e.otherEnd(topNode);
				if(nodes.contains(neighbor)) {
					nodes.remove(neighbor);
					totalWeight += lastLevelEdgeWeight.get(e);
					totalWeight += lastLevelNodeWeight.get(neighbor);
					currentLevelEdgeAssociations.put(neighbor.id(), newLevelNode.id());
					newLevelNodeGroup.add(e.id());
				}
			}
			newLevel.add(newLevelNode);
			newLevelNodeWeight.set(newLevelNode, totalWeight);
			currentLevelNodeGroups.put(newLevelNode.id(), newLevelNodeGroup);
		}		

		return newLevel;

	}

	protected Collection<Edge> getCollectionOfNeighbors(Collection<Edge> inOutEdges, EdgeAttribute<Double> edgeWeights) {
		return inOutEdges;
	}


	public static class WalshawIndependentSet extends IndependentSet{

		public WalshawIndependentSet(Graph original, long threshold) {
			super(original, threshold);
		}

		@Override
		protected Collection<Edge> getCollectionOfNeighbors(Collection<Edge> inOutEdges, EdgeAttribute<Double> edgeWeights) {
			List<Edge> edges = new ArrayList<Edge>(inOutEdges);

			Collections.sort(edges, new Comparator<Edge>(){
				public int compare(Edge a, Edge b) {
					double weightA = edgeWeights.get(a);
					double weightB = edgeWeights.get(b);

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


