package ocotillo.graph.coarsening;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

public class IndependentSet extends GraphCoarsener {

	private final long DEFAULT_THRESHOLD = 5;
	private long threshold;

	public IndependentSet(Graph original, Map<String, ?> opts) {
		super(original, opts);
	}

	@Override
	protected void saveOptions(Map<String, ?> opts) {
		if(opts != null && opts.get("threshold") != null)
			threshold = (long) opts.get("threshold");
		else
			threshold = DEFAULT_THRESHOLD;
	}

	@Override
	protected boolean stoppingCondition() {
		return coarserGraph.nodeCount() < threshold;
	}

	@Override
	protected Graph computeNewLevel(Graph lastLevel) {
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
		EdgeAttribute<Double> newLevelEdgeWeight = newLevel.edgeAttribute(StdAttribute.weight);

		Map<String, String> edgeAssociations = new HashMap<String, String>();
		while(!nodes.isEmpty()) {
			Node topNode = nodes.remove(0);
			Node newLevelNode = new Node(getTranslatedNodeId(topNode.id(), current_level));			

			edgeAssociations.put(topNode.id(), newLevelNode.id());

			double totalWeight = lastLevelNodeWeight.get(topNode);
			for(Edge e : lastLevel.inOutEdges(topNode)) {
				Node neighbor = e.otherEnd(topNode);
				if(nodes.contains(neighbor)) {
					nodes.remove(neighbor);
					totalWeight += lastLevelEdgeWeight.get(e);
					totalWeight += lastLevelNodeWeight.get(neighbor);
					edgeAssociations.put(neighbor.id(), newLevelNode.id());
				}
			}
			newLevel.add(newLevelNode);
			newLevelNodeWeight.set(newLevelNode, totalWeight);
		}		
		
		//## SANITY CHECK: IF NODE COUNT DID NOT DECREASE -- PROBABLY A LOOP
		if(newLevel.nodeCount() == lastLevel.nodeCount()) 
			return null;
			
		for(Node sourceUpperLevelNode : newLevel.nodes())  {
			Node homologue = lastLevel.getNode(getTranslatedNodeId(sourceUpperLevelNode.id(), current_level - 1));
			for(Edge e : lastLevel.inOutEdges(homologue)) {
				Node neighbor = e.otherEnd(homologue);	
				for(Edge innerEdge : lastLevel.outEdges(neighbor)) {
					Node innerNeighbor = innerEdge.otherEnd(neighbor);
					if(innerNeighbor.equals(homologue))
						continue;
					Node targetUpperLevelNode = newLevel.getNode(edgeAssociations.get(innerNeighbor.id()));
					
					Edge newLevelEdge = newLevel.betweenEdge(sourceUpperLevelNode, targetUpperLevelNode);
					if(newLevelEdge == null) {
						newLevelEdge = newLevel.newEdge(sourceUpperLevelNode.id()+"-"+targetUpperLevelNode.id(), sourceUpperLevelNode, targetUpperLevelNode);
						newLevelEdgeWeight.set(newLevelEdge, lastLevelEdgeWeight.get(innerEdge));
					}else
						newLevelEdgeWeight.set(newLevelEdge, newLevelEdgeWeight.get(newLevelEdge) + lastLevelEdgeWeight.get(innerEdge));					
				}
			}
		}	

		return newLevel;

	}

}
