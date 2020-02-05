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
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;

public abstract class GraphCoarsener {

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
						DyNodeAttribute<Object> newAttribute = coarserGraph.nodeAttribute(s);
						DyNodeAttribute<Object> originalAttr = original.nodeAttribute(s);
						newAttribute.set(newNode, originalAttr.get(n));
					}
				}
				for(Edge e : original.edges()) {
					Edge newEdge = coarserGraph.newEdge(coarserGraph.getNode(translateNodeId(e.source().id(), 0)), 
							coarserGraph.getNode(translateNodeId(e.target().id(), 0)));
					for(String s : original.edgeAttributes().keySet()) {
						DyEdgeAttribute<Object> newAttribute = coarserGraph.edgeAttribute(s);
						DyEdgeAttribute<Object> originalAttr = original.edgeAttribute(s);
						newAttribute.set(newEdge, originalAttr.get(e));
					}			
					coarserGraph.edgeAttributes().put(newEdge.id(), original.edgeAttributes().get(e.id()));
				}

				coarserGraph.graphAttributes().putAll(original.graphAttributes());
								
				/* COPY END */
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

			enclosedSubgraph.nodeAttribute(StdAttribute.weight).merge(subgraph.nodeAttribute(StdAttribute.weight));
			enclosedSubgraph.nodeAttribute(StdAttribute.dyPresence).merge(subgraph.nodeAttribute(StdAttribute.dyPresence));

			enclosedSubgraph.edgeAttribute(StdAttribute.weight).merge(subgraph.edgeAttribute(StdAttribute.weight));
			enclosedSubgraph.edgeAttribute(StdAttribute.dyPresence).merge(subgraph.edgeAttribute(StdAttribute.dyPresence));			

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
	
	private void computeMergedPresence(Evolution<Boolean> newPresence, Evolution<Boolean> lastPresence, EvolutionMergeValue<Boolean> eval){
		//Obtain the last level segments that overlap with any of the segments on the new level
		List<Function<Boolean>> lastLevelOverlapping = new ArrayList<Function<Boolean>>(lastPresence.getOverlappingIntervals(newPresence));

		//if no overlaps exist, just add all the previous intervals into the new presence function
		if(lastLevelOverlapping.isEmpty())
			newPresence.insertAll(lastPresence.getAllIntervals());
		else {
			//Add to the presence function all the elements that do not overlap
			Collection<Function<Boolean>> elementsWithNoConflicts = lastPresence.getAllIntervals();
			elementsWithNoConflicts.removeAll(lastLevelOverlapping);
			newPresence.insertAll(elementsWithNoConflicts);

			lastLevelOverlapping.sort(new BooleanFunctionComparator());					

			/*
			 * For all the remaining:
			 * 1. Obtain the elements in the NEW tree that overlap with the interval provided from the OLD tree
			 * 2. Compute the intersection(s)
			 * 3. For each intersection:
			 * 3a. Crop the existing interval
			 * 3b. Compute the values at the edges
			 * */

			List<Function<Boolean>> newLevelOverlapping = new ArrayList<Function<Boolean>>(newPresence.getOverlappingIntervals(lastPresence));
			newLevelOverlapping.sort(new BooleanFunctionComparator());

			List<Function<Boolean>> mergedPresences = new ArrayList<Function<Boolean>>();

			int newT = 0, oldT = 0;
			Interval newLevelCarryOn = null, oldLevelCarryOn = null;
			while(newT<newLevelOverlapping.size() || oldT < lastLevelOverlapping.size()) {
				Interval currentLastLevelOverlapInterval = oldLevelCarryOn != null ? oldLevelCarryOn : 
																					 (oldT < lastLevelOverlapping.size() ? lastLevelOverlapping.get(oldT).interval() : null);
				Interval currentNewLevelOverlapInterval = newLevelCarryOn != null ? newLevelCarryOn : 
					 																(newT < newLevelOverlapping.size() ? newLevelOverlapping.get(newT).interval() : null); 																				
				//Check if the old interval is null 
				if(currentLastLevelOverlapInterval == null) {
					mergedPresences.add(newLevelOverlapping.get(newT));
					oldLevelCarryOn = null;
					newT++;
					continue;
				}	
				
				//Check if the two are the same interval
				if(currentLastLevelOverlapInterval.equals(currentNewLevelOverlapInterval) || 
						currentLastLevelOverlapInterval.isContainedIn(currentNewLevelOverlapInterval)) {
					oldT++;
					newLevelCarryOn = currentNewLevelOverlapInterval;
					continue;
				}					
				
				Interval intersectionInterval = currentLastLevelOverlapInterval.intersection(currentNewLevelOverlapInterval);

				//COMPUTE LEFT INTERVAL 
				double leftBound = Double.NEGATIVE_INFINITY;
				double rightBound = Double.POSITIVE_INFINITY;
				if(currentLastLevelOverlapInterval.isLeftClosed() && currentNewLevelOverlapInterval.isLeftClosed())
					leftBound = Math.min(currentLastLevelOverlapInterval.leftBound(), currentNewLevelOverlapInterval.leftBound());
				if(intersectionInterval.isRightClosed())
					rightBound = intersectionInterval.leftBound();//Math.min(currentLastLevelOverlapInterval.rightBound(), currentNewLevelOverlapInterval.rightBound());
				boolean leftValue = eval.left(newPresence.valueAt(leftBound), lastPresence.valueAt(leftBound));
				boolean rightValue = eval.right(newPresence.valueAt(rightBound), lastPresence.valueAt(rightBound));

				ocotillo.dygraph.FunctionRect.Boolean lefty = new ocotillo.dygraph.FunctionRect.Boolean(
						Interval.newCustom(leftBound, rightBound, leftBound != Double.NEGATIVE_INFINITY, rightBound != Double.POSITIVE_INFINITY),
						leftValue,
						rightValue, Interpolation.Std.constant
						);

				mergedPresences.add(lefty);

				if(rightBound == Double.POSITIVE_INFINITY)
					break;

				//COMPUTE INTERSECTION INTERVAL
				leftValue = eval.left(newPresence.valueAt(intersectionInterval.leftBound()), lastPresence.valueAt(intersectionInterval.leftBound()));
				rightValue = eval.left(newPresence.valueAt(intersectionInterval.rightBound()), lastPresence.valueAt(intersectionInterval.rightBound()));
				ocotillo.dygraph.FunctionRect.Boolean intersection = new ocotillo.dygraph.FunctionRect.Boolean(
						intersectionInterval,
						leftValue,
						rightValue, Interpolation.Std.constant
						);

				mergedPresences.add(intersection);

				if(intersectionInterval.rightBound() == Double.POSITIVE_INFINITY)
					break;

				//CALCULATE REMAINING SEGMENT FOR NEXT ITERATION	

				leftBound = intersectionInterval.rightBound();

				if(currentLastLevelOverlapInterval.rightBound() == leftBound) {
					oldT++;
				}
				if(currentNewLevelOverlapInterval.rightBound() == leftBound) {
					newT++;
				}

				if(newT == newLevelOverlapping.size()) {
					mergedPresences.add( new ocotillo.dygraph.FunctionRect.Boolean(
							Interval.newCustom(
									leftBound, currentLastLevelOverlapInterval.rightBound(), 
									true, currentLastLevelOverlapInterval.isRightClosed()),
							leftValue,
							rightValue, Interpolation.Std.constant)							
							);	
					oldT++;
					break;
				}else if(oldT == lastLevelOverlapping.size()) {
					mergedPresences.add( new ocotillo.dygraph.FunctionRect.Boolean(
							Interval.newCustom(
									leftBound, currentNewLevelOverlapInterval.rightBound(), 
									true, currentNewLevelOverlapInterval.isRightClosed()),
							leftValue,
							rightValue, Interpolation.Std.constant)
							);	
					newT++;
					break;
				}
				newLevelCarryOn = null;
				oldLevelCarryOn = null;

				if(newT > oldT) {
					newLevelCarryOn = Interval.newCustom(
							leftBound, currentNewLevelOverlapInterval.rightBound(), 
							false, false);
				}else if(oldT < newT) {
					oldLevelCarryOn = Interval.newCustom(
							leftBound, currentLastLevelOverlapInterval.rightBound(), 
							false, false);							
				}

			}

			newPresence.deleteAll(newLevelOverlapping);
			newPresence.insertAll(mergedPresences);
		}

	}

	private void generateEdges(DyGraph newLevel, DyGraph lastLevel) {
		 DyEdgeAttribute<Double> lastLevelEdgeWeight = lastLevel.edgeAttribute(StdAttribute.weight);
		 DyEdgeAttribute<Double> newLevelEdgeWeight = newLevel.edgeAttribute(StdAttribute.weight);

		 DyEdgeAttribute<Boolean> lastLevelEdgePresence = lastLevel.edgeAttribute(StdAttribute.dyPresence);
		 DyEdgeAttribute<Boolean> newLevelEdgePresence = newLevel.edgeAttribute(StdAttribute.dyPresence);
		 
		for(Node sourceUpperLevelNode : newLevel.nodes())  {
			Node homologue = lastLevel.getNode(getTranslatedNodeId(sourceUpperLevelNode.id(), current_level - 1));
			for(Edge e : lastLevel.inOutEdges(homologue)) {
				Node neighbor = e.otherEnd(homologue);	
				for(Edge innerEdge : lastLevel.outEdges(neighbor)) {
					Node innerNeighbor = innerEdge.otherEnd(neighbor);
					if(innerNeighbor.equals(homologue)) //Eventual edges/attributes are ignored here?
						continue;
					Node targetUpperLevelNode = newLevel.getNode(currentLevelEdgeAssociations.get(innerNeighbor.id()));

					Edge newLevelEdge = newLevel.betweenEdge(sourceUpperLevelNode, targetUpperLevelNode);
					if(newLevelEdge == null) {
						newLevelEdge = newLevel.newEdge(sourceUpperLevelNode.id()+"-"+targetUpperLevelNode.id(), sourceUpperLevelNode, targetUpperLevelNode);
						newLevelEdgeWeight.set(newLevelEdge, lastLevelEdgeWeight.get(innerEdge));
						newLevelEdgePresence.set(newLevelEdge, lastLevelEdgePresence.get(innerEdge));
					}else {
						newLevelEdgeWeight.get(newLevelEdge).setDefaultValue(
								newLevelEdgeWeight.get(newLevelEdge).getDefaultValue() + lastLevelEdgeWeight.get(innerEdge).getDefaultValue());
						computeMergedPresence(newLevelEdgePresence.get(newLevelEdge), lastLevelEdgePresence.get(innerEdge), new Evolution.EvolutionORMerge());
					}
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

	protected abstract Iterable<Edge> getCollectionOfNeighbors(Collection<Edge> inOutEdges,
			DyEdgeAttribute<Double> lastLevelEdgeWeight);

	public static String getTranslatedNodeId(String nodeId, int level) {
		return nodeId.split("__")[0]+"__"+level;
	}

	public static String translateNodeId(String nodeId, int level) {
		return nodeId+"__"+level;
	}
	
	public static String nodeIdInverseTranslation(String nodeId) {
		return nodeId.split("__")[0];
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
