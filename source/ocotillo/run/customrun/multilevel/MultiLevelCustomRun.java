package ocotillo.run.customrun.multilevel;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.multilevel.layout.MultiLevelDynNoSlice;
import ocotillo.multilevel.coarsening.IndependentSet.WalshawIndependentSet;
import ocotillo.multilevel.flattener.DyGraphFlattener;
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
        
        //outputGraphOnTerminal(testGraphCoarsening(multiDyn));        
        //outputGraphOnTerminal(testGraphPlacement(multiDyn, true));
        
        outputGraphOnTerminal(testGraphPlacement(multiDyn, false));
        
        //outputGraphOnTerminal(multiDyn.runMultiLevelLayout());
	}
	
	public MultiLevelCustomRun(String[] argv) {
		super(argv);
	}		
	
	
	public DyGraph testGraphPlacement(MultiLevelDynNoSlice multiDyn, boolean randomCoordinates) {
        DyGraph coarsenedGraph = testGraphCoarsening(multiDyn);
        
        if(randomCoordinates) {
        NodeAttribute<Evolution<Coordinates>> coarserCoordinates = coarsenedGraph.nodeAttribute(StdAttribute.nodePosition);
        
        for(Node n: coarsenedGraph.nodes())
        	coarserCoordinates.set(n, new Evolution<Coordinates>(new Coordinates(Math.random(), Math.random())));
        }else
			multiDyn.nodesFirstPlacement();
        
		DyGraph parentGraph = coarsenedGraph.parentGraph();
        do {
        	multiDyn.placeVertices(parentGraph, coarsenedGraph);
        	coarsenedGraph = parentGraph;
        	parentGraph = parentGraph.parentGraph();        	
        }while(parentGraph != null);

        return multiDyn.getCoarsestGraph();
	}
	
	public DyGraph testGraphCoarsening(MultiLevelDynNoSlice multiDyn) {
		return multiDyn.runCoarsening();		
	}
	
	public void outputGraphOnTerminal(DyGraph startGraph) {
		int currentLevel = 0;
		
		DyGraph currentGraph = startGraph;
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
        	
        	System.out.println("\n");
        	
        	DyGraph parentGraph = currentGraph.parentGraph();
        	if(parentGraph != null) {
        		parentGraph.nukeSubgraph(currentGraph);        		
	        	currentLevel++;
        	}
        	currentGraph = parentGraph;        

        }while(currentGraph != null);
	}
	

}
