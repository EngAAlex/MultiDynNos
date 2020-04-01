package ocotillo.multilevel.coarsening;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.flattener.DyGraphFlattener.StaticSumPresenceFlattener;

public class SolarMerger extends GraphCoarsener {

	private final long threshold;
	
	public static final String STATUS_NODE_ATTRIBUTE_NAME = "SolarMerger_Status";
	
	public static final byte UNASSIGNED_STATE = 0;
	public static final byte SUN = 1;
	public static final byte PLANET = 2;
	public static final byte MOON = 3;

	public SolarMerger(long threshold) {
		super();
		this.threshold = threshold;
	}
	
	public SolarMerger() {
		super();
		this.threshold = DEFAULT_THRESHOLD;
	}

	@Override
	protected void preprocess() {
		StaticSumPresenceFlattener flat = new StaticSumPresenceFlattener(); 
		flat.addWeightAttribute(coarserGraph);
		
		coarserGraph.newNodeAttribute(STATUS_NODE_ATTRIBUTE_NAME, UNASSIGNED_STATE);
	}

	@Override
	protected DyGraph computeNewVertexSet(DyGraph lastLevel) {
		DyNodeAttribute<Double> lastLevelNodeWeight = lastLevel.nodeAttribute(StdAttribute.weight);
		DyEdgeAttribute<Double> lastLevelEdgeWeight = lastLevel.edgeAttribute(StdAttribute.weight);
		DyNodeAttribute<Byte> lastLevelNodeStatus = lastLevel.nodeAttribute(STATUS_NODE_ATTRIBUTE_NAME);
		List<Node> nodes = new ArrayList<Node>(lastLevel.nodes());

		Collections.sort(nodes, new GraphCoarsener.NodeWeightComparator(lastLevel, lastLevelEdgeWeight, lastLevelNodeWeight));

		DyGraph newLevel = new DyGraph();

		while(!nodes.isEmpty()) {
			Node topNode = nodes.remove(0);
			Node newLevelNode = newLevel.newNode((getTranslatedNodeId(topNode.id(), current_level)));			

			currentLevelEdgeAssociations.put(topNode.id(), newLevelNode.id());
			HashSet<String> newLevelNodeGroup = new HashSet<String>();
			newLevelNodeGroup.add(topNode.id());
			
			lastLevelNodeStatus.get(topNode).setDefaultValue(SUN);
			
			for(Edge e : lastLevel.outEdges(topNode)) {
				Node neighbor = e.otherEnd(topNode);
				if(nodes.contains(neighbor)) {
					nodes.remove(neighbor);
					currentLevelEdgeAssociations.put(neighbor.id(), newLevelNode.id());
					newLevelNodeGroup.add(neighbor.id());
					lastLevelNodeStatus.get(neighbor).setDefaultValue(PLANET);
					
					for(Edge eE : lastLevel.outEdges(neighbor)) {
						Node neighborOfNeighbor = eE.otherEnd(neighbor);
						
						if(nodes.contains(neighborOfNeighbor)) {
							nodes.remove(neighborOfNeighbor);
							currentLevelEdgeAssociations.put(neighborOfNeighbor.id(), newLevelNode.id());
							newLevelNodeGroup.add(neighborOfNeighbor.id());
							lastLevelNodeStatus.get(neighborOfNeighbor).setDefaultValue(MOON);
						}
					}
				}				
			}		
			currentLevelNodeGroups.put(newLevelNode.id(), newLevelNodeGroup);
		}
		
		return newLevel;
	}

	@Override
	protected boolean stoppingCondition() {
		return coarserGraph.nodeCount() <= threshold;
	}

}
