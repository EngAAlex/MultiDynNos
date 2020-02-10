package ocotillo.multilevel.placement;

import java.util.Set;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.coarsening.GraphCoarsener;

public abstract class MultilevelNodePlacementStrategy {

	public MultilevelNodePlacementStrategy() {

	}
	
	public void placeVertices(DyGraph coarsestLevel, Graph staticGraph, GraphCoarsener nodeGroups) {
		NodeAttribute<Coordinates> upperLevelNodeCoordinates = staticGraph.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Coordinates> coarsestLevelCoordinates = coarsestLevel.nodeAttribute(StdAttribute.nodePosition);
		
		for(Node n : staticGraph.nodes()) {				
			Coordinates upperClusterCoordinates = upperLevelNodeCoordinates.get(n);
			coarsestLevelCoordinates.set(coarsestLevel.getNode(n.id()), 
					new Evolution<Coordinates>(computeNewCoordinates(upperClusterCoordinates)));
			//assignCoordinates(upperClusterCoordinates, coarsestLevel, nodeGroups.getGroupMembers(n.id()));
		}
	}

	public void placeVertices(DyGraph finerLevel, DyGraph upperLevel, GraphCoarsener nodeGroups) {
		DyNodeAttribute<Coordinates> upperLevelNodeCoordinates = upperLevel.nodeAttribute(StdAttribute.nodePosition);

		for(Node n : upperLevel.nodes()) {				
			Coordinates upperClusterCoordinates = upperLevelNodeCoordinates.get(n).getLastValue();
			if(Double.isNaN(upperClusterCoordinates.x()) || Double.isNaN(upperClusterCoordinates.y()))
				upperClusterCoordinates = upperLevelNodeCoordinates.get(n).getDefaultValue();
			assignCoordinates(upperClusterCoordinates, finerLevel, nodeGroups.getGroupMembers(n.id()));
		}
	}

	protected void assignCoordinates(Coordinates upperClusterCoordinates, DyGraph finerLevel, Set<String> nodeGroup) {
		DyNodeAttribute<Coordinates> finerLevelNodeCoordinates = finerLevel.nodeAttribute(StdAttribute.nodePosition);		
		
		for(String id : nodeGroup) {				
			finerLevelNodeCoordinates.set(finerLevel.getNode(id), new Evolution<Coordinates>(
					computeNewCoordinates(upperClusterCoordinates)));
		}
	}
	
	protected abstract Coordinates computeNewCoordinates(Coordinates input);

	public static class IdentityNodePlacement extends MultilevelNodePlacementStrategy {

		protected double fuzzyness = 0.05d;
		
		public IdentityNodePlacement() {

		}
			
		public void setFuzzyness(double fuzzyness) {
			this.fuzzyness = fuzzyness;
		}

		@Override
		protected Coordinates computeNewCoordinates(Coordinates input) {
			return new Coordinates(input.x() + Math.random()*fuzzyness, input.y() + Math.random()*fuzzyness);
		}
	}
}

