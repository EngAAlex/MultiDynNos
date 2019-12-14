package ocotillo.graph.coarsening;

import java.util.Map;

import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

public abstract class GraphCoarsener {

	//protected final Graph rootGraph;
	protected Graph coarserGraph;	

	protected int current_level = 0;

	public GraphCoarsener(Graph original, Map<String, ?> opts) {
		//Copy the original graph into the state
		coarserGraph = new Graph();
		for(Node n : original.nodes())
			coarserGraph.add(n);
		for(Edge e : original.edges())
			coarserGraph.add(e);

		NodeAttribute<Double> originalNodeWeight = original.nodeAttribute(StdAttribute.weight);
		EdgeAttribute<Double> originalEdgeWeight = original.edgeAttribute(StdAttribute.weight);

		NodeAttribute<Double> coarserGraphNodeWeight = coarserGraph.nodeAttribute(StdAttribute.weight);
		EdgeAttribute<Double> coarserGraphEdgeWeight = coarserGraph.edgeAttribute(StdAttribute.weight);

		coarserGraphNodeWeight.copy(originalNodeWeight);
		coarserGraphEdgeWeight.copy(originalEdgeWeight);

		saveOptions(opts);
	}
	
	public void computeCoarsening() {
		computeCoarsening(false);
	}

	public void computeCoarsening(boolean createInterLevelEdges) {
		//Graph coarserGraph = rootGraph.rootGraph();
		current_level = 1;
		boolean breakCondition = false;
		while(!breakCondition && !stoppingCondition()) {
			Graph subgraph = computeNewLevel(coarserGraph);
			if(subgraph == null) {
				breakCondition = true;
				continue;
			}
			System.out.println("New level has " + subgraph.nodeCount() + " nodes and " + subgraph.edgeCount() + "edges");					
			
			Graph enclosedSubgraph = coarserGraph.newSubGraph(subgraph.nodes(), subgraph.edges());
			
			enclosedSubgraph.nodeAttribute(StdAttribute.weight).merge(subgraph.nodeAttribute(StdAttribute.weight));
			enclosedSubgraph.edgeAttribute(StdAttribute.weight).merge(subgraph.edgeAttribute(StdAttribute.weight));

			if(createInterLevelEdges)
				for(Node source : subgraph.nodes()) {
					Node target = coarserGraph.getNode(getTranslatedNodeId(source.id(), current_level - 1));
					coarserGraph.add(new Edge(source.id() + " - "+target.id(), source, target));
				}				 
			current_level++;
			coarserGraph = enclosedSubgraph;
		}
	}

	public int getHierarchyDepth() {
		return current_level;
	}

	public Graph getCoarserGraph() {
		return coarserGraph;
	}

	protected abstract void saveOptions(Map<String, ?> opts);

	protected abstract boolean stoppingCondition();	

	protected abstract Graph computeNewLevel(Graph lastLevel);

	public static String getTranslatedNodeId(String nodeId, int level) {
		return nodeId.split("__")[0]+"__"+level;
	}

	public static String createTranslatedNodeId(String nodeId, int level) {
		return nodeId+"__"+level;
	}
}
