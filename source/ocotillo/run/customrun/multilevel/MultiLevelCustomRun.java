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
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.coarsening.GraphCoarsener;
import ocotillo.multilevel.coarsening.IndependentSet;
import ocotillo.multilevel.coarsening.IndependentSet.WalshawIndependentSet;
import ocotillo.multilevel.placement.MultilevelNodePlacementStrategy.IdentityNodePlacement;
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
        generateAppearanceGraph(dyGraph);		
	}
	
	public MultiLevelCustomRun(String[] argv) {
		super(argv);
	}		

	public void generateAppearanceGraph(DyGraph dygraph) {
		Graph graph = new Graph();
        //NodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
		//DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Double> nodeWeight = graph.nodeAttribute(StdAttribute.weight);
        EdgeAttribute<Double> edgeWeight = graph.edgeAttribute(StdAttribute.weight);	
        
        DyNodeAttribute<Boolean> nodePresence = dygraph.nodeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Boolean> edgePresence = dygraph.edgeAttribute(StdAttribute.dyPresence);
        
        for(Node dyN : dygraph.nodes()) {
        	Node n;
        	if(!graph.hasNode(dyN.id()))
        		n = graph.newNode(dyN.id()+"__0");
        	else
        		n = graph.getNode(dyN.id()+"__0");
        	double presence = 0.0;
        	Iterator<Function<Boolean>> it = nodePresence.get(dyN).iterator(); 
        	while(it.hasNext()) {
        		Function<Boolean> f = it.next();
        		Interval currInterval = f.interval();
        		presence += currInterval.rightBound() - currInterval.leftBound(); 
        	}
        	nodeWeight.set(n, presence);
        	System.out.println("Reconstructed Node " + n.id() + " with presence " + nodeWeight.get(n));
        }            
        
        for(Edge dyE : dygraph.edges()) {               
        	Node src = dyE.source();
        	Node tgt = dyE.target();
        	
        	Edge e = graph.newEdge(
        				graph.getNode(GraphCoarsener.createTranslatedNodeId(src.id(), 0)),
        				graph.getNode(GraphCoarsener.createTranslatedNodeId(tgt.id(), 0)));
        	
        	
        	double presence = 0.0;
        	Iterator<Function<Boolean>> it = edgePresence.get(dyE).iterator(); 
        	while(it.hasNext()) {
        		Function<Boolean> f = it.next();
        		Interval currInterval = f.interval();
        		presence += currInterval.rightBound() - currInterval.leftBound(); 
        	}
        	edgeWeight.set(e, presence);
        	System.out.println("Reconstructed Edge From " + src.id() + " to " + tgt.id() + " with presence " + edgeWeight.get(e));
        }
               
        this.appearanceGraph = graph;
        
		System.out.println("Reconstructed graph has " + this.appearanceGraph.nodeCount() + " nodes and " + this.appearanceGraph.edgeCount() + "edges");
        
        Map<String, Long> opts = new HashMap<String, Long>();
        
        opts.put("threshold", new Long(2));
        
        GraphCoarsener gc = new IndependentSet.WalshawIndependentSet(this.appearanceGraph, WalshawIndependentSet.DEFAULT_THRESHOLD);
        gc.computeCoarsening();

        testGraphPlacement(gc);
        testGraphDisplay(gc);
        	             
	}
	
	public void testGraphPlacement(GraphCoarsener gc) {
        Graph currentGraph = gc.getCoarserGraph();
        int currentLevel = gc.getHierarchyDepth();
        
        NodeAttribute<Coordinates> coarserCoordinates = currentGraph.nodeAttribute(StdAttribute.nodePosition);
        
        for(Node n: currentGraph.nodes())
        	coarserCoordinates.set(n, new Coordinates(Math.random(), Math.random()));
        
        IdentityNodePlacement in = new IdentityNodePlacement();
        
        while(currentLevel >= 1)
        	in.placeVertices(currentGraph.parentGraph(), currentGraph, gc);
	}
	
	public void testGraphDisplay(GraphCoarsener gc) {
        Graph currentGraph = gc.getCoarserGraph();
        int currentLevel = gc.getHierarchyDepth();
        Graph rootGraph = currentGraph.rootGraph();
        do {
        	System.out.println("Displaying level " + currentLevel);
        	System.out.println("Nodes:");
        	
        	NodeAttribute<Double> currentLevelNodeWeight = currentGraph.nodeAttribute(StdAttribute.weight);
        	NodeAttribute<Double> currentLevelNodeCoordinates = currentGraph.nodeAttribute(StdAttribute.nodePosition);
    		EdgeAttribute<Double> currentLevelEdgeWeight = currentGraph.edgeAttribute(StdAttribute.weight);
        	
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
