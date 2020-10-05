package ocotillo.multilevel.coarsening;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import ocotillo.multilevel.logger.Logger;
import ocotillo.run.DynNoSliceRun;

public abstract class GraphCoarsener {

	public final static long DEFAULT_THRESHOLD = 15;

	private static final float CHANGE_TRESHOLD = 0.95f;

	//protected final Graph rootGraph;
	//protected DyGraph coarserGraph;	
	protected LinkedList<DyGraph> hierarchy;
	protected Map<String, String> currentLevelEdgeAssociations = new HashMap<String, String>();
	protected Map<String, Set<String>> currentLevelNodeGroups = new HashMap<String, Set<String>>();


	protected Map<String, Set<String>> groupingMasterMap = new HashMap<String, Set<String>>();
	protected Map<String, String> edgeAssociationMasterMap = new HashMap<String, String>();

	protected int current_level = 0;

	public GraphCoarsener() {

	}

	public void setGraph(DyGraph original) {

		DyGraph coarserGraph = new DyGraph();

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
		hierarchy = new LinkedList<DyGraph>();
		hierarchy.add(coarserGraph);
		/* COPY END */

		Logger.getInstance().log("Set up graph with " + coarserGraph.nodeCount() + " nodes and " + coarserGraph.edgeCount() + " edges");
	}

	public void computeCoarsening() {
		computeCoarsening(false);
	}

	public void computeCoarsening(boolean createInterLevelEdges) {

		initialize();
		DyGraph coarserGraph = getCoarsestGraph();
		current_level = hierarchy.size();
		boolean breakCondition = false;
		while(!breakCondition) {
			DyGraph subgraph = computeNewLevel(coarserGraph);
			if(subgraph == null) {
				breakCondition = true;
				continue;
			}
			Logger.getInstance().log("Level " + current_level + " has " + subgraph.nodeCount() + " nodes and " + subgraph.edgeCount() + " edges");					
			
			hierarchy.add(subgraph);
			coarserGraph = subgraph;
			current_level = hierarchy.size();
		}
	}

	protected abstract void initialize();

	private DyGraph computeNewLevel(DyGraph lastLevel) {
		DyGraph newLevel = computeNewVertexSet(lastLevel);
		//## SANITY CHECK: IF NODE COUNT DID NOT DECREASE -- PROBABLY A LOOP	
		//## STOPPING CONDITION: If the new level has 95% or more vertices of the last level
		if(newLevel.nodeCount() >= lastLevel.nodeCount()
				|| (newLevel.nodeCount()/(float)lastLevel.nodeCount() > CHANGE_TRESHOLD && hierarchy.size() > 1)) 
			newLevel = null;
		else {
			mergeNodePresenceAndWeight(newLevel, lastLevel, new Evolution.EvolutionORMerge());
			generateEdges(newLevel, lastLevel);
			groupingMasterMap.putAll(currentLevelNodeGroups);
			edgeAssociationMasterMap.putAll(currentLevelEdgeAssociations);
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
			Evolution<Boolean> newNodePresence = new Evolution<Boolean>(lastNodePresence.getDefaultValue());//newLevelPresence.get(newLevelNode);
			newNodePresence.insertAll(duplicatePresenceEvolution(lastNodePresence));
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
				if(current.interval().isContainedIn(lastInterval.interval())) {
					continue;
				}else {
					if(current.interval().leftBound() > lastInterval.interval().rightBound()) {
						merged.add(mergeAdjacentIntervals(currentPot));
						currentPot.clear();
					}
				}
				lastInterval = current;
				currentPot.add(current);
			}
		}
		if(currentPot.size() != 0)
			merged.add(mergeAdjacentIntervals(currentPot));

		newPresence.clear();
		newPresence.insertAll(merged);
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
					newLevelEdgeWeight.set(newLevelEdge, new Evolution<Double>(lastLevelEdgeWeight.get(e).getDefaultValue()));//lastLevelEdgeWeight.get(e));
					Evolution<Boolean> newEdgePresence = new Evolution<Boolean>(false);//newLevelPresence.get(newLevelNode);
					newEdgePresence.insertAll(duplicatePresenceEvolution(lastLevelEdgePresence.get(e)));	
					newLevelEdgePresence.set(newLevelEdge, newEdgePresence);
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
		return hierarchy.size();
	}

	/**
	 * Get the coarsest graph in the hierarchy.
	 * @return
	 */
	public DyGraph getCoarsestGraph() {
		return hierarchy.getLast();
	}

	public DyGraph getFinestGraph() {
		return hierarchy.getFirst();
	}

	public Iterator<DyGraph> getGraphIterator(){
		return hierarchy.descendingIterator();
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
	 * A method to get, from the current coarsening, which is the group leader of the parameter node.
	 * @param id the node whose group leader we are looking for
	 * @return the group leader id
	 */
	public String getGroupLeader(String id) {
		return edgeAssociationMasterMap.get(id);
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
	//protected abstract boolean stoppingCondition();	

	public static String getTranslatedNodeId(String nodeId, int level) {
		return nodeId.split("__")[0]+"__"+level;
	}

	public static String translateNodeId(String nodeId, int level) {
		return nodeId+"__"+level;
	}

	public static String nodeIdInverseTranslation(String nodeId) {
		return nodeId.split("__")[0];
	} 

	/**
	 * Check if two nodes are homologues across two adjacent levels
	 * @param nodeIdA
	 * @param nodeIdB
	 * @return
	 */
	public static boolean checkNodeIdEquivalence(String nodeIdA, String nodeIdB) {
		return nodeIdInverseTranslation(nodeIdA).equals(nodeIdInverseTranslation(nodeIdB));
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

	public static Evolution<Boolean> copyEvolution(Evolution<Boolean> evol){
		Evolution<Boolean> result = new Evolution<Boolean>(evol.getDefaultValue());
		for(Function<Boolean> curr : evol.getAllIntervals())
			result.insert(curr);
		return result;
	}
	
	private static LinkedList<Function<Boolean>> duplicatePresenceEvolution(Evolution<Boolean> toDuplicate){
		LinkedList<Function<Boolean>> newEvo = new LinkedList<Function<Boolean>>();
		for(Function<Boolean> func : toDuplicate.getAllIntervals()) {
			Interval currInterval = func.interval();
			newEvo.add(
					new ocotillo.dygraph.FunctionRect.Boolean(
							Interval.newCustom(currInterval.leftBound(), currInterval.rightBound(), currInterval.isLeftClosed(), currInterval.isRightClosed()),
							func.leftValue(),
							func.rightValue(), Interpolation.Std.constant)
					);
		}
		return newEvo;
	}

	public abstract String getDescription();


}
