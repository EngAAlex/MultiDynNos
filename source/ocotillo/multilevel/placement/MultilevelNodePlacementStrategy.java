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
	
	public void placeVertices(DyGraph finerLevel, Graph upperLevel, GraphCoarsener nodeGroups) {
		NodeAttribute<Coordinates> upperLevelNodeCoordinates = upperLevel.nodeAttribute(StdAttribute.nodePosition);

		for(Node n : upperLevel.nodes()) {				
			Coordinates upperClusterCoordinates = upperLevelNodeCoordinates.get(n);
			assignCoordinates(upperClusterCoordinates, finerLevel, nodeGroups.getGroupMembers(n.id()));
		}
	}

	public void placeVertices(DyGraph finerLevel, DyGraph upperLevel, GraphCoarsener nodeGroups) {
		DyNodeAttribute<Coordinates> upperLevelNodeCoordinates = upperLevel.nodeAttribute(StdAttribute.nodePosition);

		for(Node n : upperLevel.nodes()) {				
			Coordinates upperClusterCoordinates = upperLevelNodeCoordinates.get(n).getLastValue();
			assignCoordinates(upperClusterCoordinates, finerLevel, nodeGroups.getGroupMembers(n.id()));
		}
	}

	protected abstract void assignCoordinates(Coordinates upperClusterCoordinates, DyGraph finerLevel, Set<String> nodeGroup);

	public static class IdentityNodePlacement extends MultilevelNodePlacementStrategy {

		protected double fuzzyness = 0.05d;
		
		public IdentityNodePlacement() {

		}
			
		public void setFuzzyness(double fuzzyness) {
			this.fuzzyness = fuzzyness;
		}

		@Override
		protected void assignCoordinates(Coordinates upperClusterCoordinates, DyGraph finerLevel, Set<String> nodeGroup) {
			DyNodeAttribute<Coordinates> finerLevelNodeCoordinates = finerLevel.nodeAttribute(StdAttribute.nodePosition);		
			
			for(String id : nodeGroup) {				
				finerLevelNodeCoordinates.set(finerLevel.getNode(id), new Evolution<Coordinates>(
						new Coordinates(upperClusterCoordinates.x() + Math.random()*fuzzyness, upperClusterCoordinates.y() + Math.random()*fuzzyness)));
			}

		}
	}
}

