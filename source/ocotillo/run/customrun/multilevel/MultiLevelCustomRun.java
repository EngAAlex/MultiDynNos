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
	}
	
	public MultiLevelCustomRun(String[] argv) {
		super(argv);
	}		
	
	
	public void testGraphPlacement(MultiLevelDynNoSlice multiDyn) {
        DyGraph currentGraph = multiDyn.runCoarsening();
        
        NodeAttribute<Evolution<Coordinates>> coarserCoordinates = currentGraph.nodeAttribute(StdAttribute.nodePosition);
        
        for(Node n: currentGraph.nodes())
        	coarserCoordinates.set(n, new Evolution<Coordinates>(new Coordinates(Math.random(), Math.random())));
        
        multiDyn.placeVertices(currentGraph.parentGraph(), currentGraph);
        
        do {
        	multiDyn.placeVertices(currentGraph.parentGraph(), currentGraph);
        }while(currentGraph.parentGraph() != null);
        
	}
	
	public void testGraphCoarsening(MultiLevelDynNoSlice multiDyn) {
		multiDyn.runCoarsening();		
	}
	
	public void outputGraphOnTerminal(MultiLevelDynNoSlice multiDyn) {
        Graph currentGraph = gc.getCoarserGraph();
        int currentLevel = gc.getHierarchyDepth();
        Graph rootGraph = currentGraph.rootGraph();
        do {
        	System.out.println("Displaying level " + currentLevel);
        	System.out.println("Nodes:");
        	
        	NodeAttribute<Evolution<Double>> currentLevelNodeWeight = dynG.nodeAttribute(StdAttribute.weight);
    		EdgeAttribute<Evolution<Double>> currentLevelEdgeWeight = dynG.edgeAttribute(StdAttribute.weight);
        	
        	NodeAttribute<Evolution<Double>> currentLevelNodeCoordinates = dynG.nodeAttribute(StdAttribute.nodePosition);
    		
    		NodeAttribute<Evolution<Double>> currentLevelNodePresence = dynG.nodeAttribute(StdAttribute.dyPresence);
    		EdgeAttribute<Evolution<Double>> currentLevelEdgePresence = dynG.edgeAttribute(StdAttribute.dyPresence);
    		
        	for(Node n : currentGraph.nodes()) 
        		System.out.println(n.id() + " weight " + currentLevelNodeWeight.get(n) + " position " + currentLevelNodeCoordinates.get(n));
        	
        	System.out.println("Edges:");

        	for(Edge e: currentGraph.edges())
            	System.out.println(e.id() + " weight " + currentLevelEdgeWeight.get(e));
        	
        	System.out.println("Done\n\n");
        	
        	Graph parentGraph = currentGraph.parentGraph();
        	if(parentGraph != null) {
        		rootGraph.nukeSubgraph(currentGraph);        	
	        	currentLevel--;
        	}
        	currentGraph = parentGraph;        

        }while(currentGraph != null);
	}
	

}
