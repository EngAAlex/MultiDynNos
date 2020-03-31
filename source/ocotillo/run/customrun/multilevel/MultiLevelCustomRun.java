package ocotillo.run.customrun.multilevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

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
import ocotillo.samples.parsers.Commons.Mode;
import ocotillo.samples.parsers.VanDeBunt;

public class MultiLevelCustomRun extends CustomRun {

	protected Graph appearanceGraph;
	protected static String[] args;
	protected static HashMap<String, Integer> preloadedGraphs;
	
	public static void main(String[] argv) {
		args = argv;
		preloadedGraphs = new HashMap<String, Integer>();
		preloadedGraphs.put("vandebunt", 0);
		MultiLevelCustomRun mlcr = new MultiLevelCustomRun(argv);
		mlcr.run();
	}

	@Override
	protected void run() {
		
		DyGraph dyGraph = null;
		
		if(preloadedGraphs.containsKey(args[0]))
			switch(preloadedGraphs.get(args[0])) {
				case 0: dyGraph = VanDeBunt.parseGraph(new File("data/van_De_Bunt/"), Mode.keepAppearedNode); break;
				default: System.err.println("Can't load graph dataset"); System.exit(1); break;
			}	
		else
			dyGraph = createDynamicGraph();
		
        MultiLevelDynNoSlice multiDyn = 
        		new MultiLevelDynNoSlice(dyGraph, Run.defaultTau, Run.defaultDelta)
        			.setCoarsener(new WalshawIndependentSet())
        			.setPlacementStrategy(new IdentityNodePlacement())
        			.setFlattener(new DyGraphFlattener.StaticSumPresenceFlattener())
        			.defaultOptions();
        
        //outputGraphOnTerminal(testGraphCoarsening(multiDyn));        
        //outputGraphOnTerminal(testGraphPlacement(multiDyn, true));
        
        //outputGraphOnTerminal(testGraphPlacement(multiDyn, false));
        
        //outputGraphOnTerminal(multiDyn.runMultiLevelLayout());
        
        outputAsGml(multiDyn.runMultiLevelLayout());
	}
	
	public MultiLevelCustomRun(String[] argv) {
		super(argv, !preloadedGraphs.containsKey(argv[0]));
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
	
	public void outputAsGml(DyGraph graph) {

		System.out.println("Writing output as gml");
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(new File(this.output).getParentFile()+File.pathSeparator+"output.gml"));
		} catch (FileNotFoundException e1) {
			System.err.println("Can't find output file");
			e1.printStackTrace();
		}
		
		short indentLevel = 0;
		pw.println(indentString("graph [", indentLevel));
		indentLevel++;
		pw.println(indentString("directed 0", indentLevel));
		
    	DyNodeAttribute<Coordinates> currentLevelNodeCoordinates = graph.nodeAttribute(StdAttribute.nodePosition);
		
    	for(Node n : graph.nodes()) {
			int id = Integer.parseInt(n.id().split("__")[0]);
			pw.println(indentString("node [", indentLevel));
			indentLevel++;
			double[] coords = new double[] {currentLevelNodeCoordinates.get(n).getLastValue().get(0), currentLevelNodeCoordinates.get(n).getLastValue().get(1)};
			pw.println(indentString("id " + id, indentLevel));
			pw.println(indentString("label \"" + n.id() +"\"", indentLevel));			
			pw.println(indentString("graphics [ ", indentLevel));	
			indentLevel++;
			pw.println(indentString("x " + coords[0], indentLevel));
			pw.println(indentString("y " + coords[1], indentLevel));
			pw.println(indentString("w 5", indentLevel));
			pw.println(indentString("h 5", indentLevel));
			indentLevel--;
			pw.println(indentString("]", indentLevel));
			indentLevel--;
			pw.println(indentString("]", indentLevel));
		}
		
    	//DyEdgeAttribute<Double> currentLevelEdgeWeight = graph.edgeAttribute(StdAttribute.weight);
    	
    	for(Edge e: graph.edges()){
			pw.println(indentString("edge [", indentLevel));
			indentLevel++;
			pw.println(indentString("source " + Integer.parseInt(e.source().id().split("__")[0]), indentLevel));
			pw.println(indentString("target " + Integer.parseInt(e.target().id().split("__")[0]), indentLevel));
			pw.println(indentString("graphics [", indentLevel));
			indentLevel++;
			pw.println(indentString("type \"line\"", indentLevel));
			pw.println(indentString("width 1" /*+ (currentLevelEdgeWeight.get(e).getDefaultValue() == -1 ? 1 : currentEdge.getWeight() + 1)*/, indentLevel));		
			indentLevel--;
			pw.println(indentString("]", indentLevel));
			indentLevel--;
			pw.println(indentString("]", indentLevel));
		}		
    	
    	indentLevel--;
		
    	pw.println(indentString("]", indentLevel));
		pw.close();
		
		System.out.println("All done!");
		
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
	
	protected static String indentString(String in, short indentLevel){
		String result = "";
		for(int i=0; i<indentLevel; i++)
			result += "\t";
		return result + in;
	}

}
