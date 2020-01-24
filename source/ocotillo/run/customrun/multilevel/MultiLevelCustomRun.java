package ocotillo.run.customrun.multilevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularFdl;
import ocotillo.graph.multilevel.layout.MultiLevelDynNoSlice;
import ocotillo.multilevel.coarsening.GraphCoarsener;
import ocotillo.multilevel.coarsening.IndependentSet;
import ocotillo.multilevel.coarsening.IndependentSet.WalshawIndependentSet;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.placement.MultilevelNodePlacementStrategy;
import ocotillo.multilevel.placement.MultilevelNodePlacementStrategy.IdentityNodePlacement;
import ocotillo.run.Run;
import ocotillo.run.customrun.CustomRun;

public class MultiLevelCustomRun extends CustomRun {

	protected Graph appearanceGraph;
	
	public static void main(String[] argv) {
		MultiLevelCustomRun mlcr = new MultiLevelCustomRun(argv);
		mlcr.run();
	}

	@Override
	protected void run() {
		DyGraph dyGraph = createDynamicGraph();
        MultiLevelDynNoSlice multiDyn = 
        		new MultiLevelDynNoSlice(dyGraph, Run.defaultTau, Run.defaultDelta)
        			.setCoarsener(new WalshawIndependentSet())
        			.setPlacementStrategy(new IdentityNodePlacement())
        			.setFlattener(new DyGraphFlattener.StaticSumPresenceFlattener())
        			.defaultOptions();
        
        outputGraphOnTerminal(testGraphCoarsening(multiDyn));
        
        outputGraphOnTerminal(testGraphPlacement(multiDyn));
        
        outputGraphOnTerminal(multiDyn.runMultiLevelLayout());
	}
	
	public MultiLevelCustomRun(String[] argv) {
		super(argv);
	}		
	
	
	public DyGraph testGraphPlacement(MultiLevelDynNoSlice multiDyn) {
        DyGraph currentGraph = multiDyn.runCoarsening();
        
        NodeAttribute<Evolution<Coordinates>> coarserCoordinates = currentGraph.nodeAttribute(StdAttribute.nodePosition);
        
        for(Node n: currentGraph.nodes())
        	coarserCoordinates.set(n, new Evolution<Coordinates>(new Coordinates(Math.random(), Math.random())));
        
        multiDyn.placeVertices(currentGraph.parentGraph(), currentGraph);
        
        do {
        	multiDyn.placeVertices(currentGraph.parentGraph(), currentGraph);
        }while(currentGraph.parentGraph() != null);

        return currentGraph;
	}
	
	public DyGraph testGraphCoarsening(MultiLevelDynNoSlice multiDyn) {
		return multiDyn.runCoarsening();		
	}
	
	public void outputGraphOnTerminal(DyGraph currentGraph) {
		int currentLevel = 0;
        do {
        	System.out.println("Displaying level " + currentLevel);
        	System.out.println("Nodes:");
        	
        	DyNodeAttribute<Double> currentLevelNodeWeight = currentGraph.nodeAttribute(StdAttribute.weight);
        	DyEdgeAttribute<Double> currentLevelEdgeWeight = currentGraph.edgeAttribute(StdAttribute.weight);
        	
        	DyNodeAttribute<Evolution<Double>> currentLevelNodeCoordinates = currentGraph.nodeAttribute(StdAttribute.nodePosition);
    		
        	DyNodeAttribute<Boolean> currentLevelNodePresence = currentGraph.nodeAttribute(StdAttribute.dyPresence);
        	DyEdgeAttribute<Boolean> currentLevelEdgePresence = currentGraph.edgeAttribute(StdAttribute.dyPresence);
    		
        	for(Node n : currentGraph.nodes()) {
        		System.out.println(n.id() + "\n\tWeight " + currentLevelNodeWeight.get(n).getDefaultValue() + "\n\tPosition " + currentLevelNodeCoordinates.get(n).getLastValue());
        		Evolution<Boolean> nodePresence = currentLevelNodePresence.get(n);
        		for(Function<Boolean> f : nodePresence.getAllIntervals())
        			System.out.println("Exists from " + f.interval().leftBound() + " to " + f.interval().rightBound());        		
        	}
        	System.out.println("Edges:");

        	for(Edge e: currentGraph.edges()) {        		
            	System.out.println("From " + e.source().id() + " to " + e.target().id() + "\n\tWeight " + currentLevelEdgeWeight.get(e).getDefaultValue());
        		Evolution<Boolean> edgePresence = currentLevelEdgePresence.get(e);
        		for(Function<Boolean> f : edgePresence.getAllIntervals())
        			System.out.println("Exists from " + f.interval().leftBound() + " to " + f.interval().rightBound());    
        	}
        	
        	System.out.println("\n\n");
        	
        	DyGraph parentGraph = currentGraph.parentGraph();
        	if(parentGraph != null) {
        		parentGraph.nukeSubgraph(currentGraph);        	
	        	currentLevel++;
        	}
        	currentGraph = parentGraph;        

        }while(currentGraph != null);
	}
	

}
