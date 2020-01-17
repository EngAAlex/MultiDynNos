package ocotillo.multilevel.placement;

import java.util.Set;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.coarsening.GraphCoarsener;

public abstract class MultilevelNodePlacementStrategy {

	public MultilevelNodePlacementStrategy() {

	}

	public void placeVertices(Graph finerLevel, Graph upperLevel, GraphCoarsener nodeGroups) {
		NodeAttribute<Coordinates> upperLevelNodeCoordinates = upperLevel.nodeAttribute(StdAttribute.nodePosition);

		for(Node n : upperLevel.nodes()) {				
			Coordinates upperClusterCoordinates = upperLevelNodeCoordinates.get(n);
			assignCoordinates(upperClusterCoordinates, finerLevel, nodeGroups.getGroupMembers(n.id()));
		}
	}

	protected abstract void assignCoordinates(Coordinates upperClusterCoordinates, Graph finerLevel, Set<String> nodeGroup);

	public static class IdentityNodePlacement extends MultilevelNodePlacementStrategy {

		protected double fuzzyness = 0;
		
		public IdentityNodePlacement() {

		}
			
		public void setFuzzyness(double fuzzyness) {
			this.fuzzyness = fuzzyness;
		}

		@Override
		protected void assignCoordinates(Coordinates upperClusterCoordinates, Graph finerLevel, Set<String> nodeGroup) {
			NodeAttribute<Coordinates> finerLevelNodeCoordinates = finerLevel.nodeAttribute(StdAttribute.nodePosition);		
			
			for(String id : nodeGroup) {				
				finerLevelNodeCoordinates.set(finerLevel.getNode(id), 
						new Coordinates(upperClusterCoordinates.x() + Math.random()*fuzzyness, upperClusterCoordinates.y() + Math.random()*fuzzyness));
			}

		}
	}
}

