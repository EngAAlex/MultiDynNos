package ocotillo.multilevel.coarsening;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Evolution.EvolutionMergeValue;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;

public abstract class GraphCoarsener {

	public final static long DEFAULT_THRESHOLD = 15;
	
	//protected final Graph rootGraph;
	protected DyGraph coarserGraph;	
	protected Map<String, String> currentLevelEdgeAssociations = new HashMap<String, String>();
	protected Map<String, Set<String>> currentLevelNodeGroups = new HashMap<String, Set<String>>();


	protected Map<String, Set<String>> groupingMasterMap = new HashMap<String, Set<String>>();

	protected int current_level = 0;

	public GraphCoarsener() {
					
	}
	
	public void setGraph(DyGraph original) {
		//Copy the original graph into the state
				//coarserGraph = original;
				coarserGraph = new DyGraph();

				/*COPY AND SETUP OF THE ORIGINAL GRAPH -- NECESSARY FOR NODE ID TRANSLATION*/

				for(Node n : original.nodes()) {
					Node newNode = coarserGraph.newNode(GraphCoarsener.translateNodeId(n.id(), 0));
					for(String s : original.nodeAttributes().keySet()) {
						DyNodeAttribute<Object> newAttribute;
						try{
							newAttribute = coarserGraph.nodeAttribute(s);
						}catch(IllegalArgumentException ill) {
							/*The attribute is not among the default ones*/
							newAttribute = coarserGraph.newNodeAttribute(s, original.nodeAttribute(s).getDefault());
						}
						DyNodeAttribute<Object> originalAttr = original.nodeAttribute(s);
						newAttribute.set(newNode, originalAttr.get(n));
					}
				}
				for(Edge e : original.edges()) {
					Edge newEdge = coarserGraph.newEdge(coarserGraph.getNode(translateNodeId(e.source().id(), 0)), 
							coarserGraph.getNode(translateNodeId(e.target().id(), 0)));
					for(String s : original.edgeAttributes().keySet()) {
						DyEdgeAttribute<Object> newAttribute;
						try{
							/*The attribute is not among the default ones*/
							newAttribute = coarserGraph.edgeAttribute(s);
						}catch(IllegalArgumentException ill) {
							newAttribute = coarserGraph.newEdgeAttribute(s, original.edgeAttribute(s).getDefault());
						}
						DyEdgeAttribute<Object> originalAttr = original.edgeAttribute(s);
						newAttribute.set(newEdge, originalAttr.get(e));
					}			
					coarserGraph.edgeAttributes().put(newEdge.id(), original.edgeAttributes().get(e.id()));
				}

				coarserGraph.graphAttributes().putAll(original.graphAttributes());
								
				/* COPY END */
				
				System.out.println("Set up graph with " + coarserGraph.nodeCount() + " nodes and " + coarserGraph.edgeCount() + " edges");
	}

	public void computeCoarsening() {
		computeCoarsening(false);
	}

	public void computeCoarsening(boolean createInterLevelEdges) {

		preprocess();

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

			enclosedSubgraph.newLocalNodeAttribute(StdAttribute.weight, 0.0).merge(subgraph.nodeAttribute(StdAttribute.weight));
			enclosedSubgraph.newLocalNodeAttribute(StdAttribute.dyPresence, false).merge(subgraph.nodeAttribute(StdAttribute.dyPresence));
			enclosedSubgraph.newLocalNodeAttribute(StdAttribute.nodePosition, new Evolution<>(new Coordinates(0, 0)));
			
			enclosedSubgraph.newLocalEdgeAttribute(StdAttribute.weight, 0.0).merge(subgraph.edgeAttribute(StdAttribute.weight));
			enclosedSubgraph.newLocalEdgeAttribute(StdAttribute.dyPresence, false).merge(subgraph.edgeAttribute(StdAttribute.dyPresence));			
			
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
		if(newLevel.nodeCount() >= lastLevel.nodeCount()) 
			newLevel = null;
		else {
			mergeNodePresenceAndWeight(newLevel, lastLevel, new Evolution.EvolutionORMerge());
			generateEdges(newLevel, lastLevel);

			groupingMasterMap.putAll(currentLevelNodeGroups);
		}
		currentLevelEdgeAssociations.clear();
		currentLevelNodeGroups.clear();		
		return newLevel;
	}

	protected void mergeNodePresenceAndWeight(DyGraph newLevel, DyGraph lastLevel, EvolutionMergeValue<Boolean> eval) {
		DyNodeAttribute<Boolean> lastLevelPresence = lastLevel.nodeAttribute(StdAttribute.dyPresence);
		DyNodeAttribute<Boolean> newLevelPresence = newLevel.nodeAttribute(StdAttribute.dyPresence);

		DyNodeAttribute<Double> lastLevelNodeWeight = lastLevel.nodeAttribute(StdAttribute.weight);
		DyNodeAttribute<Double> newLevelNodeWeight = newLevel.nodeAttribute(StdAttribute.weight);
		

		for(String s : currentLevelNodeGroups.keySet()) {
			Node newLevelNode = newLevel.getNode(s);
			Node lastLevelTopNode = lastLevel.getNode(getTranslatedNodeId(s, current_level-1));
			double totalWeight = lastLevelNodeWeight.get(lastLevelTopNode).getDefaultValue();
			Evolution<Boolean> lastNodePresence = lastLevelPresence.get(lastLevelTopNode);								
			Evolution<Boolean> newNodePresence = new Evolution<Boolean>(false);//newLevelPresence.get(newLevelNode);
			newNodePresence.copyFrom(lastNodePresence);			
			for(String n : currentLevelNodeGroups.get(s)) {
				Node lastLevelGroupNode = lastLevel.getNode(n);
				if(nodeIdInverseTranslation(n).equals(nodeIdInverseTranslation(newLevelNode.id())))
					continue;
				totalWeight += lastLevelNodeWeight.get(lastLevelGroupNode).getDefaultValue();				
				lastNodePresence = lastLevelPresence.get(lastLevelGroupNode);
				computeMergedPresence(newNodePresence, lastNodePresence, eval);
			}
			newLevelPresence.set(newLevelNode, newNodePresence);
			newLevelNodeWeight.set(newLevelNode, new Evolution<Double>(totalWeight));
		}
	}
	
	private void computeMergedPresence(Evolution<Boolean> newPresence, Evolution<Boolean> lastPresence, EvolutionMergeValue<Boolean> eval) { 	
		List<Function<Boolean>> all = new ArrayList<Function<Boolean>>();
		all.addAll(newPresence.getAllIntervals()); 
		all.addAll(lastPresence.getAllIntervals());
		all.sort(new BooleanFunctionComparator());
		List<Function<Boolean>> merged = new ArrayList<Function<Boolean>>();
		List<Function<Boolean>> currentPot = new ArrayList<Function<Boolean>>();
		Function<Boolean> lastInterval = null;
		for(Function<Boolean> current : all) {			
			if(currentPot.size() == 0) {
				currentPot.add(current); 
				lastInterval = current;
			} else {
				currentPot.add(current);
				if(current.interval().leftBound() == lastInterval.interval().rightBound()) {
					lastInterval = current;
				}else {
					merged.add(mergeAdjacentIntervals(currentPot));
					currentPot.clear();
				}
			}
		}
		if(currentPot.size() != 0)
			merged.add(mergeAdjacentIntervals(currentPot));
				
		newPresence.clear();
		newPresence.insertAll(merged);
	}
	
	
	

	private List<Function<Boolean>> postProcessIntervals(List<Function<Boolean>> list) {
		List<Function<Boolean>> merged = new ArrayList<Function<Boolean>>();
		List<Function<Boolean>> currentPot = new ArrayList<Function<Boolean>>();
		Function<Boolean> lastInterval = null;
		for(Function<Boolean> current : list) {			
			if(currentPot.size() == 0) {
				currentPot.add(current); 
				lastInterval = current;
			} else {
				currentPot.add(current);
				if(current.interval().leftBound() == lastInterval.interval().rightBound()) {
					lastInterval = current;
				}else {
					merged.add(mergeAdjacentIntervals(currentPot));
					currentPot.clear();
				}
			}
		}
		if(currentPot.size() != 0)
			merged.add(mergeAdjacentIntervals(currentPot));
				
		return merged;
	}
	
	/**
	 * Method that merges together the list of Intervals provided into one interval that spans from the 
	 * left bound of the first in the list to the right bound of the last.
	 * @param temp The list of intervals
	 * @return The merged interval.
	 */
	private Function<Boolean> mergeAdjacentIntervals(List<Function<Boolean>> temp){
		Interval left = temp.get(0).interval();
		Interval right = temp.get(temp.size()-1).interval();
		return new ocotillo.dygraph.FunctionRect.Boolean(
				Interval.newCustom(left.leftBound(), right.rightBound(), left.isLeftClosed(), right.isRightClosed()),
				temp.get(0).leftValue(),
				temp.get(0).rightValue(), Interpolation.Std.constant
				);
	}

	/**
	 * Method to create the edges of the new level starting from the nodes of the new level
	 * @param newLevel The new level graph -- now it only contains the new vertices.
	 * @param lastLevel The current level graph.
	 */
	private void generateEdges(DyGraph newLevel, DyGraph lastLevel) {
		 DyEdgeAttribute<Double> lastLevelEdgeWeight = lastLevel.edgeAttribute(StdAttribute.weight);
		 DyEdgeAttribute<Double> newLevelEdgeWeight = newLevel.edgeAttribute(StdAttribute.weight);

		 DyEdgeAttribute<Boolean> lastLevelEdgePresence = lastLevel.edgeAttribute(StdAttribute.dyPresence);
		 DyEdgeAttribute<Boolean> newLevelEdgePresence = newLevel.edgeAttribute(StdAttribute.dyPresence);
		 
		for(Node sourceLowerNode : lastLevel.nodes()) {
			Node sourceUpperNode = newLevel.getNode(currentLevelEdgeAssociations.get(sourceLowerNode.id()));
			for(Edge e : lastLevel.outEdges(sourceLowerNode)) {
				Node targetLowerNode = e.otherEnd(sourceLowerNode);
				Node targetUpperNode = newLevel.getNode(currentLevelEdgeAssociations.get(targetLowerNode.id()));
				if(sourceUpperNode.equals(targetUpperNode))
					continue;
				Edge newLevelEdge = newLevel.betweenEdge(sourceUpperNode, targetUpperNode);
				
				if(newLevelEdge == null) {
					newLevelEdge = newLevel.newEdge(sourceUpperNode.id()+"-"+targetUpperNode.id(), sourceUpperNode, targetUpperNode);
					newLevelEdgeWeight.set(newLevelEdge, lastLevelEdgeWeight.get(e));
					newLevelEdgePresence.set(newLevelEdge, lastLevelEdgePresence.get(e));
				}else {
					newLevelEdgeWeight.get(newLevelEdge).setDefaultValue(
							newLevelEdgeWeight.get(newLevelEdge).getDefaultValue() + lastLevelEdgeWeight.get(e).getDefaultValue());
					computeMergedPresence(newLevelEdgePresence.get(newLevelEdge), lastLevelEdgePresence.get(e), new Evolution.EvolutionORMerge());
				}
			}
		}
	}

	
	/**
	 * Get how many levels the hierarchy has. 
	 * @return
	 */
	public int getHierarchyDepth() {
		return current_level;
	}

	
	/**
	 * Get the coarsest graph in the hierarchy.
	 * @return
	 */
	public DyGraph getCoarsestGraph() {
		return coarserGraph;
	}

	/**
	 * When a node on level n+1 is created, it will represent a group of nodes from level n. With this function, the members of that group can be recovered.  
	 * @param id The node of the id to look for.
	 * @return The members of the group belonging to the provided node.
	 */
	public Set<String> getGroupMembers(String id){
		return groupingMasterMap.get(id);
	}

	/**
	 * This function yields the new set of nodes that will be part of the new (n+1) level. 
	 * @param lastLevel The graph at level n
	 * @return The graph containing the nodes of the new nevel
	 */
	protected abstract DyGraph computeNewVertexSet(DyGraph lastLevel);	

	/**
	 * A method to evaluate whether or not to continue the coarsening.
	 * @return Whether the coarsening should stop or a new level should be created
	 */
	protected abstract boolean stoppingCondition();	

	public static String getTranslatedNodeId(String nodeId, int level) {
		return nodeId.split("__")[0]+"__"+level;
	}

	public static String translateNodeId(String nodeId, int level) {
		return nodeId+"__"+level;
	}
	
	public static String nodeIdInverseTranslation(String nodeId) {
		return nodeId.split("__")[0];
	} 
	
	public static class NodeWeightComparator implements Comparator<Node>{

		private final DyGraph lastLevel;
		private final DyNodeAttribute<Double> lastLevelNodeWeight;
		private final DyEdgeAttribute<Double> lastLevelEdgeWeight;
		
		public NodeWeightComparator(DyGraph lastLevel, DyEdgeAttribute<Double> lastLevelEdgeWeight,
				ocotillo.dygraph.DyNodeAttribute<Double> lastLevelNodeWeight) {
			this.lastLevel = lastLevel;
			this.lastLevelEdgeWeight = lastLevelEdgeWeight;
			this.lastLevelNodeWeight = lastLevelNodeWeight;
		}

		@Override
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
	
	}


	public static class BooleanFunctionComparator implements Comparator<Function<Boolean>>{

		@Override
		public int compare(Function<Boolean> o1, Function<Boolean> o2) {
			Interval o1i = o1.interval();
			Interval o2i = o2.interval();

			if((!o1i.isLeftClosed() && o2i.isLeftClosed()) || (o1i.leftBound() < o2i.leftBound()))
				return -1;
			else if((o1i.isLeftClosed() && !o2i.isLeftClosed()) || (o1i.leftBound() > o2i.leftBound()))
				return 1;
			return 0;
		}

	}

}
