package ocotillo.multilevel.placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.mockito.internal.util.collections.Sets;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.coarsening.GraphCoarsener;

public abstract class MultilevelNodePlacement {

	public MultilevelNodePlacement(Map<String, ?> opts) {
		saveOptions(opts);
	}

	public void placeVertices(Graph finerLevel, Graph upperLevel, GraphCoarsener nodeGroups) {
		NodeAttribute<Coordinates> upperLevelNodeCoordinates = upperLevel.nodeAttribute(StdAttribute.nodePosition);

		for(Node n : upperLevel.nodes()) {				
			Coordinates upperClusterCoordinates = upperLevelNodeCoordinates.get(n);
			assignCoordinates(upperClusterCoordinates, finerLevel, nodeGroups.getGroupMembers(n.id()));
		}
	}

	protected abstract void assignCoordinates(Coordinates upperClusterCoordinates, Graph finerLevel, Set<String> nodeGroup);

	protected abstract void saveOptions(Map<String, ?> opts);

	public static class IdentityNodePlacement extends MultilevelNodePlacement {

		public IdentityNodePlacement(Map<String, ?> opts) {
			super(opts);
		}

		@Override
		protected void assignCoordinates(Coordinates upperClusterCoordinates, Graph finerLevel, Set<String> nodeGroup) {
			NodeAttribute<Coordinates> finerLevelNodeCoordinates = finerLevel.nodeAttribute(StdAttribute.nodePosition);

			for(String id : nodeGroup)
				finerLevelNodeCoordinates.set(finerLevel.getNode(id), upperClusterCoordinates);

		}

		@Override
		protected void saveOptions(Map<String, ?> opts) {
			//NO-OP
		}

	}
}

