package ocotillo.multilevel.placement;

import java.util.function.Function;

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

	protected GraphCoarsener coarsener;
	private Graph currentStaticGraph;
	private DyGraph currentUpperLevelGraph;
	protected double fuzzyness;
	
	protected final double FUZZYNESS_DEFAULT = 0.05d;

	public MultilevelNodePlacementStrategy() {
		fuzzyness = FUZZYNESS_DEFAULT;
	}
	

	public MultilevelNodePlacementStrategy(double fuzzyness) {
		this.fuzzyness = fuzzyness;
	}
	
	public void setCoarsener(GraphCoarsener coarsener) {
		this.coarsener = coarsener;
	}

	protected Node getNodeFromUpperLevel(String id) {
		return currentUpperLevelGraph.getNode(id);
	}
	
	protected Coordinates getStaticUpperLevelCoordinatesOfNode(Node n) {
		return getUpperLevelCoordinatesOfNode(n, false, null);
	}
		
	protected Coordinates getUpperLevelCoordinatesOfNode(Node n) {
		Function<Evolution<Coordinates>, Coordinates> fc = (Evolution<Coordinates> evc) -> {
			Coordinates candidates = evc.getLastValue();
			if(Double.isNaN(candidates.x()) || Double.isNaN(candidates.y()))
				candidates = evc.getDefaultValue();
			return candidates;
			};
		return getUpperLevelCoordinatesOfNode(n, true, fc);
	}
	
	protected Coordinates getUpperLevelCoordinatesOfNode(Node n, Function<Evolution<Coordinates>, Coordinates> extractFromEvolution) {
		return getUpperLevelCoordinatesOfNode(n, true, extractFromEvolution);
	}

	protected Coordinates getUpperLevelCoordinatesOfNode(Node n, boolean dynamic, Function<Evolution<Coordinates>, Coordinates> extractFromEvolution) {
		if(dynamic) {
			DyNodeAttribute<Coordinates> upperLevelNodeCoordinates = currentUpperLevelGraph.nodeAttribute(StdAttribute.nodePosition);
			return extractFromEvolution.apply(upperLevelNodeCoordinates.get(n));
		}else{
			NodeAttribute<Coordinates> upperLevelCoords = currentStaticGraph.nodeAttribute(StdAttribute.nodePosition);
			return upperLevelCoords.get(n);
		}	
	}
	
	public void placeVertices(DyGraph coarsestLevel, Graph staticGraph) {
		DyNodeAttribute<Coordinates> coarsestLevelCoordinates = coarsestLevel.nodeAttribute(StdAttribute.nodePosition);
		this.currentStaticGraph = staticGraph;
		for(Node n : staticGraph.nodes()) {				
			coarsestLevelCoordinates.set(coarsestLevel.getNode(n.id()), 
					new Evolution<Coordinates>(computeNewCoordinates(coarsestLevel.getNode(n.id()), 
							n, coarsestLevel, (Node node) -> getStaticUpperLevelCoordinatesOfNode(node))));
		}
	}

	public void placeVertices(DyGraph finerLevel, DyGraph upperLevel) {
		DyNodeAttribute<Coordinates> finerLevelNodeCoordinates = finerLevel.nodeAttribute(StdAttribute.nodePosition);		
		this.currentUpperLevelGraph = upperLevel;
		for(Node n : upperLevel.nodes()) {				
			for(String id : coarsener.getGroupMembers(n.id())) {
				Node lowerLevelNode = finerLevel.getNode(id);
				finerLevelNodeCoordinates.set(lowerLevelNode, new Evolution<Coordinates>(
						computeNewCoordinates(lowerLevelNode, n, finerLevel, (Node node) -> getUpperLevelCoordinatesOfNode(node))));
			}
		}
	}
	
	protected abstract Coordinates computeNewCoordinates(Node lowerLevelNode, Node upperLevelNode, DyGraph finerLevel, Function<Node, Coordinates> getUpperLevelCoords);

	public static class IdentityNodePlacement extends MultilevelNodePlacementStrategy {
		
		public IdentityNodePlacement() {
			super();
		}
		
		public IdentityNodePlacement(double fuzzyness) {
			super(fuzzyness);
		}

		@Override
		protected Coordinates computeNewCoordinates(Node lowerLevelNode, Node upperLevelNode, DyGraph finerLevel, Function<Node, Coordinates> getUpperLevelCoords) {
		
			Coordinates upperClusterCoordinates = getUpperLevelCoords.apply(upperLevelNode); //upperLevelNodeCoordinates.get(upperLevelNode).getLastValue();
			
			return new Coordinates(upperClusterCoordinates.x() + Math.random()*fuzzyness, upperClusterCoordinates.y() + Math.random()*fuzzyness);
		}

		@Override
		public String getDescription() {
			return "Identity";
		}
	}

	public abstract String getDescription();
}

