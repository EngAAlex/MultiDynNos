package ocotillo.run.customrun.multilevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.function.Consumer;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.StcsBuilder;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.multilevel.layout.MultiLevelDynNoSlice;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.gui.quickview.QuickView;
import ocotillo.multilevel.coarsening.SolarMerger;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.options.MultiLevelDrawingOption;
import ocotillo.multilevel.placement.WeightedBarycenterPlacementStrategy;
import ocotillo.run.Run;
import ocotillo.run.customrun.CustomRun;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;
import ocotillo.samples.parsers.CollegeMsg;
import ocotillo.samples.parsers.DialogSequences;
import ocotillo.samples.parsers.InfoVisCitations;
import ocotillo.samples.parsers.NewcombFraternity;
import ocotillo.samples.parsers.RugbyTweets;
import ocotillo.samples.parsers.VanDeBunt;

public class MultiLevelCustomRun extends CustomRun {

	protected Graph appearanceGraph;
	protected static String[] args;
	protected static HashMap<String, Integer> preloadedGraphs;
	private DecimalFormat secondFormat = new DecimalFormat("#.00");
	
	public static void main(String[] argv) {
		args = argv;
		preloadedGraphs = new HashMap<String, Integer>();
		preloadedGraphs.put("vandebunt", 0);
		preloadedGraphs.put("newcomb", 1);
		preloadedGraphs.put("infovis", 2);
		preloadedGraphs.put("rugby", 3);
		preloadedGraphs.put("dialogs", 4);
		preloadedGraphs.put("college", 5);

		MultiLevelCustomRun mlcr = new MultiLevelCustomRun(argv);
		mlcr.run();
	}

	@Override
	protected void run() {

		DyGraph dyGraph = null;
		String filename = "output";
		DyDataSet data = null;
		final double timeFactor;
		final Interval suggestedInterval;
		final double staticTiming;

		if(preloadedGraphs.containsKey(args[0])) {
			switch(preloadedGraphs.get(args[0])) {
				case 0: 
					data = VanDeBunt.parse(Mode.keepAppearedNode); break;
				case 1: 
					data = NewcombFraternity.parse(Mode.keepAppearedNode); break;
				case 2: 
					data = InfoVisCitations.parse(Mode.keepAppearedNode); break;
				case 3: data = RugbyTweets.parse(Mode.keepAppearedNode); break;
				case 4: data = DialogSequences.parse(Mode.keepAppearedNode); break;
				case 5: data = CollegeMsg.parse(Mode.keepAppearedNode); break;
				default: System.err.println("Can't load graph dataset"); System.exit(1); break;
			}	
			dyGraph = data.dygraph; filename = args[0];
			timeFactor = data.suggestedTimeFactor;
			suggestedInterval = data.suggestedInterval;
			staticTiming = data.suggestedInterval.leftBound();
		}else {
			dyGraph = createDynamicGraph();
			timeFactor = Run.defaultTau;
			suggestedInterval = Interval.newClosed(0, 30);
			staticTiming = suggestedInterval.leftBound();
		}

		MultiLevelDynNoSlice multiDyn = 
				new MultiLevelDynNoSlice(dyGraph, timeFactor, Run.defaultDelta)
				.setCoarsener(new SolarMerger()) //WalshawIndependentSet
				.setPlacementStrategy(new WeightedBarycenterPlacementStrategy())
				.setFlattener(new DyGraphFlattener.StaticSumPresenceFlattener())
				.defaultLayoutParameters()
				.addLayerPostProcessingDrawingOption(new MultiLevelDrawingOption.FlexibleTimeTrajectoriesPostProcessing(2))
				.addOption(MultiLevelDynNoSlice.LOG_OPTION, true).build();

		DyGraph result = multiDyn.runMultiLevelLayout();	

		System.out.println("Algorithm elapsed time: " + secondFormat.format((multiDyn.getComputationStatistics().getTotalRunningTime().toMillis())/Math.pow(10, 3)) + "s");
		
		//## OUTPUT OPTIONS (uncomment the ones preferred)
		animateGraphOnWindow(result, staticTiming, suggestedInterval);
		//showGraphOnWindow(result, staticTiming);
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
	
	public void outputFullLevels(DyGraph startGraph, Consumer<DyGraph> visualization) {
		int currentLevel = 0;

		DyGraph currentGraph = startGraph;
		do {
			System.out.println("Displaying level " + currentLevel);		

			visualization.accept(currentGraph);

			DyGraph parentGraph = currentGraph.parentGraph();
			if(parentGraph != null) {
				parentGraph.nukeSubgraph(currentGraph);        		
				currentLevel++;
			}
			currentGraph = parentGraph;        

		}while(currentGraph != null);
	}

	private void showGraphOnWindow(DyGraph graph, double timing) {

//		DyQuickView window = new DyQuickView(graph, timing);
		SpaceTimeCubeSynchroniser stcs = new StcsBuilder(graph, timing).build();
		QuickView window = new QuickView(stcs.mirrorGraph());
		window.showNewWindow();
	}
	
	private void animateGraphOnWindow(DyGraph graph, double timing, Interval interval) {

//		DyQuickView window = new DyQuickView(graph, timing);
		DyQuickView dyWindow = new DyQuickView(graph, timing);
		dyWindow.setAnimation(new Animation(interval, Duration.ofSeconds(30)));
		dyWindow.showNewWindow();
	}
	
	private void showGraphOnTerminal(DyGraph currentGraph) {
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
	}

	public void outputAsGml(DyGraph graph, String filename) {

		System.out.println("Writing output as gml");

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(new File(this.output).getParentFile()+File.pathSeparator+filename+".gml"));
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
	
	protected static String indentString(String in, short indentLevel){
		String result = "";
		for(int i=0; i<indentLevel; i++)
			result += "\t";
		return result + in;
	}

}
