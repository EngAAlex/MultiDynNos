package ocotillo.multilevel.coarsening;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ocotillo.dygraph.DyGraph;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

public abstract class GraphCoarsener {
	
	//protected final Graph rootGraph;
	protected DyGraph coarserGraph;	
	protected Map<String, String> currentLevelEdgeAssociations = new HashMap<String, String>();
	protected Map<String, Set<String>> currentLevelNodeGroups = new HashMap<String, Set<String>>();

	
	protected Map<String, Set<String>> groupingMasterMap = new HashMap<String, Set<String>>();

	protected int current_level = 0;
	
	public GraphCoarsener(DyGraph original) {
		//Copy the original graph into the state
		coarserGraph = original;
		/*for(Node n : original.nodes())
			coarserGraph.add(n);
		for(Edge e : original.edges())
			coarserGraph.add(e);*/

		/*NodeAttribute<Double> originalNodeWeight = original.nodeAttribute(StdAttribute.weight);
		EdgeAttribute<Double> originalEdgeWeight = original.edgeAttribute(StdAttribute.weight);

		NodeAttribute<Double> coarserGraphNodeWeight = coarserGraph.nodeAttribute(StdAttribute.weight);
		EdgeAttribute<Double> coarserGraphEdgeWeight = coarserGraph.edgeAttribute(StdAttribute.weight);

		coarserGraphNodeWeight.copy(originalNodeWeight);
		coarserGraphEdgeWeight.copy(originalEdgeWeight);*/

	}
			
	public void computeCoarsening() {
		computeCoarsening(false);
	}

	public void computeCoarsening(boolean createInterLevelEdges) {
		
		preprocess();
		
		//Graph coarserGraph = rootGraph.rootGraph();
		current_level = 1;
		boolean breakCondition = false;
		while(!breakCondition && !stoppingCondition()) {
			DyGraph subgraph = computeNewLevel(coarserGraph);
			if(subgraph == null) {
				breakCondition = true;
				continue;
			}
			System.out.println("New level has " + subgraph.nodeCount() + " nodes and " + subgraph.edgeCount() + "edges");					
			
			DyGraph enclosedSubgraph = coarserGraph.newSubGraph(subgraph.nodes(), subgraph.edges());
			
			enclosedSubgraph.nodeAttributes().putAll(subgraph.nodeAttributes());
			enclosedSubgraph.edgeAttributes().putAll(subgraph.edgeAttributes());

			/*enclosedSubgraph.nodeAttribute(StdAttribute.weight).merge(subgraph.nodeAttribute(StdAttribute.weight));
			enclosedSubgraph.edgeAttribute(StdAttribute.weight).merge(subgraph.edgeAttribute(StdAttribute.weight));*/

			if(createInterLevelEdges)
				for(Node source : subgraph.nodes()) {
					Node target = coarserGraph.getNode(getTranslatedNodeId(source.id(), current_level - 1));
					coarserGraph.add(new Edge(source.id() + " - "+target.id(), source, target));
				}				 
			current_level++;
			coarserGraph = enclosedSubgraph;
		}
	}
	
	protected abstract void preprocess();

	private DyGraph computeNewLevel(DyGraph lastLevel) {
		DyGraph newLevel = computeNewVertexSet(lastLevel);
		//## SANITY CHECK: IF NODE COUNT DID NOT DECREASE -- PROBABLY A LOOP		
		if(newLevel.nodeCount() == lastLevel.nodeCount()) 
			newLevel = null;
		else {
			generateEdges(lastLevel, newLevel);
			groupingMasterMap.putAll(currentLevelNodeGroups);
		}
		currentLevelEdgeAssociations.clear();
		currentLevelNodeGroups.clear();		
		return newLevel;
	}

	private void generateEdges(DyGraph lastLevel, DyGraph newLevel) {
		/*EdgeAttribute<Double> lastLevelEdgeWeight = lastLevel.edgeAttribute(StdAttribute.weight);
		
		EdgeAttribute<Double> newLevelEdgeWeight = newLevel.edgeAttribute(StdAttribute.weight);*/
			
		for(Node sourceUpperLevelNode : newLevel.nodes())  {
			Node homologue = lastLevel.getNode(getTranslatedNodeId(sourceUpperLevelNode.id(), current_level - 1));
			for(Edge e : lastLevel.inOutEdges(homologue)) {
				Node neighbor = e.otherEnd(homologue);	
				for(Edge innerEdge : lastLevel.outEdges(neighbor)) {
					Node innerNeighbor = innerEdge.otherEnd(neighbor);
					if(innerNeighbor.equals(homologue))
						continue;
					Node targetUpperLevelNode = newLevel.getNode(currentLevelEdgeAssociations.get(innerNeighbor.id()));
					
					Edge newLevelEdge = newLevel.betweenEdge(sourceUpperLevelNode, targetUpperLevelNode);
					if(newLevelEdge == null) {
						newLevelEdge = newLevel.newEdge(sourceUpperLevelNode.id()+"-"+targetUpperLevelNode.id(), sourceUpperLevelNode, targetUpperLevelNode);
						newLevelEdgeWeight.set(newLevelEdge, lastLevelEdgeWeight.get(innerEdge));
					}else
						newLevelEdgeWeight.set(newLevelEdge, newLevelEdgeWeight.get(newLevelEdge) + lastLevelEdgeWeight.get(innerEdge));					
				}
			}
		}	
	}
	
	public int getHierarchyDepth() {
		return current_level;
	}

	public DyGraph getCoarserGraph() {
		return coarserGraph;
	}
	
	public Set<String> getGroupMembers(String id){
		return groupingMasterMap.get(id);
	}

	protected abstract DyGraph computeNewVertexSet(DyGraph lastLevel);	
		
	protected abstract boolean stoppingCondition();	

	public static String getTranslatedNodeId(String nodeId, int level) {
		return nodeId.split("__")[0]+"__"+level;
	}

	public static String createTranslatedNodeId(String nodeId, int level) {
		return nodeId+"__"+level;
	}
}
