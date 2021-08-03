package ocotillo.run;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.swing.WindowConstants;

import ocotillo.DefaultRun.CMDLineOption;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.StcsBuilder;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.gui.quickview.QuickView;
import ocotillo.run.customrun.EdgeAppearance;
import ocotillo.run.customrun.NodeAppearance;
import ocotillo.samples.parsers.Commons;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.serialization.ParserTools;

public abstract class Run {

	public static final double defaultDelta = 5.0;
	public static final double defaultTau = 1.0;
	public static final int defaultNumberOfIterations = 100;
	public static final String defaultOutput = null;

	//	protected final List<NodeAppearance> nodeDataSet;
	//	protected final List<EdgeAppearance> edgeDataSet;

	protected final DyGraph dygraph;
	protected DyGraph drawnGraph;

	protected final double delta;
	protected final double tau;
	protected final String output;
	protected double staticTiming;
	protected Interval suggestedInterval;
	protected final String graphName;

	public Run(String[] argv, DyDataSet requestedDataSet) {

		System.out.println(getDescription() + " layout selected");

		double delta = defaultDelta;
		double tau = defaultTau;
		//Interval suggestedInterval;
		String output = defaultOutput;
		boolean autoTau = true;
		boolean cliTau = false;

		if(requestedDataSet == null) {
			System.out.println("Loading user defined graph");
			File nodeDataSetFile = new File(argv[2]);
			if (!nodeDataSetFile.exists()) {
				System.err.println("The node data set file \"" + argv[2] + "\" does not exist. \n");
				showHelp();
				System.exit(1);				
			}
			List<String> nodeDataSetLines = ParserTools.readFileLines(nodeDataSetFile);

			File edgeDataSetFile = new File(argv[3]);
			if (!edgeDataSetFile.exists()) {
				System.err.println("The node edge set file \"" + argv[3] + "\" does not exist. \n");
				showHelp();
				System.exit(1);				
			}
			List<String> edgeDataSetLines = ParserTools.readFileLines(edgeDataSetFile);

			dygraph = createDynamicGraph(NodeAppearance.parseDataSet(nodeDataSetLines), EdgeAppearance.parseDataSet(edgeDataSetLines));

			//suggestedInterval = //Interval.newClosed(0, 30);
			//staticTiming = suggestedInterval.leftBound();
			graphName = "Custom Graph";
			System.out.println("Custom Graph Loading Done");
		}else{
			dygraph = requestedDataSet.dygraph;
//			tau = requestedDataSet.getSuggestedTimeFactor(false);
//			suggestedInterval = requestedDataSet.getSuggestedInterval(false);
//			staticTiming = suggestedInterval.leftBound();	
			graphName = argv[1];
		}		
		
		for(int i=0; i<argv.length; i++) {
			try {
				switch(AvailableDrawingOption.parse(argv[i].split("-")[1])) {
				case delta:  {
					try {				
						double possibleDelta = Double.parseDouble(argv[i+1]);
						delta = possibleDelta > 0 ? possibleDelta : defaultDelta;
						System.out.println("Set delta from CLI " + delta);
					} catch (Exception e) {
						//					System.err.println("Cannot parse delta correctly. \n");
						//					showHelp();
					} 				
					break;
				}
				case tau: {
					try {
						double possibleTau = Double.parseDouble(argv[i+1]);
						tau = possibleTau >= 0 ? possibleTau : defaultTau;
						cliTau = true;
						System.out.println("Set tau from CLI");
					} catch (Exception e) {
						System.err.println("Cannot parse CLI tau correctly - switching to computed Tau. \n");
						cliTau = false;
						//showHelp();								
					}
					break;
				}
				case text: {
					output = argv[i+1];
				}
				case autoTau: {
					autoTau = false;
				}
				//			case o: {
				//				output = argv[i+1];
				//			}
				default: break;
				}
			}catch (IndexOutOfBoundsException ie) {}
		}
			
		if(cliTau) {
			this.tau = tau;
			this.suggestedInterval = requestedDataSet.getSuggestedInterval(autoTau);
		}else
			if(!autoTau) {
				this.suggestedInterval = requestedDataSet.getSuggestedInterval(false);
				this.tau = requestedDataSet.getSuggestedTimeFactor(false);
			} else {
				double computedTau = requestedDataSet.getSuggestedTimeFactor(true);
				System.out.println("Auto Computed Tau\tSuggestedTau");			
				System.out.println(computedTau + "\t" + requestedDataSet.getSuggestedInterval(false));	
				this.tau = computedTau;
				suggestedInterval = requestedDataSet.getSuggestedInterval(true);
			}
				
		this.delta = delta;
		this.staticTiming = this.suggestedInterval.leftBound();
		this.output = output;

		completeSetup();

	}

	public void computeDrawing() {
		System.out.println("Beginning Layout");
		this.drawnGraph = run();
	}

	protected abstract String getDescription();

	protected abstract void completeSetup();

	protected abstract DyGraph run();

	public void animateGraph() {
		System.out.println("Opening animation window...");		
		animateGraphOnWindow(drawnGraph, staticTiming, suggestedInterval, graphName);
	}

	public void plotSpaceTimeCube() {
		System.out.println("Loading space-time cube...");				
		showGraphOnWindow(drawnGraph, staticTiming, graphName);
	}

	protected static void animateGraphOnWindow(DyGraph graph, double timing, Interval interval, String graphName) {

		DyQuickView dyWindow = new DyQuickView(graph, timing, graphName + " animation");
		dyWindow.setAnimation(new Animation(interval, Duration.ofSeconds(30)));
		dyWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		dyWindow.showNewWindow();
	}

	protected void showGraphOnWindow(DyGraph graph, double timing, String graphName) {

		SpaceTimeCubeSynchroniser stcs = new StcsBuilder(graph, timing).build();
		QuickView window = new QuickView(stcs.mirrorGraph(), graphName + " space-time cube");
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);		
		window.showNewWindow();
	}	

	/**
	 * Checks if the node appearances are in correct order (no earlier
	 * appearance of a node is later in the list) and there are no overlaps
	 * between any two appearances of the same node.
	 *
	 * @param nodeDataSet the node data set.
	 */
	public static void checkNodeAppearanceCorrectness(List<NodeAppearance> nodeDataSet) {
		for (int i = 0; i < nodeDataSet.size(); i++) {
			for (int j = i + 1; j < nodeDataSet.size(); j++) {
				NodeAppearance first = nodeDataSet.get(i);
				NodeAppearance second = nodeDataSet.get(j);
				if (first.id.equals(second.id)) {
					if (first.startTime >= second.startTime) {
						String error = "The appearances of node " + first.id
								+ " at time " + first.startTime + " and "
								+ second.startTime + " are not in the correct order.";
						throw new RuntimeException(error);
					} else if (first.startTime + first.duration >= second.startTime) {
						String error = "The appearances of node " + first.id
								+ " at time " + first.startTime + " and duration "
								+ first.duration + " overlaps with appearance at time "
								+ second.startTime + ".";
						throw new RuntimeException(error);
					}
				}
			}
		}
	}

	/**
	 * Checks if all edges are not loop (e.g. a-a) or do not have source and
	 * target in alphabetical order (e.g. b-a). Also, checks that the edge
	 * appearances are in correct order (no earlier appearance of an edge is
	 * later in the list) and there are no overlaps between any two appearances
	 * of the same edge.
	 *
	 * @param edgeDataSet the edge data set.
	 */
	public static void checkEdgeAppearanceCorrectness(List<EdgeAppearance> edgeDataSet) {
		for (EdgeAppearance appearance : edgeDataSet) {
			if (appearance.sourceId.compareTo(appearance.targetId) >= 0) {
				String error = "An appearance with source node " + appearance.sourceId
						+ " and target node " + appearance.targetId + " either identifies"
						+ " a loop or does not have source and target in alphabetical order.";
				throw new RuntimeException(error);

			}
		}
		for (int i = 0; i < edgeDataSet.size(); i++) {
			for (int j = i + 1; j < edgeDataSet.size(); j++) {
				EdgeAppearance first = edgeDataSet.get(i);
				EdgeAppearance second = edgeDataSet.get(j);
				if (first.sourceId.equals(second.sourceId) && first.targetId.equals(second.targetId)) {
					if (first.startTime >= second.startTime) {
						String error = "The appearances of edge " + first.sourceId + " - "
								+ first.targetId + " at time " + first.startTime + " and "
								+ second.startTime + " are not in the correct order.";
						throw new RuntimeException(error);
					} else if (first.startTime + first.duration >= second.startTime) {
						String error = "The appearances of edge " + first.sourceId + " - "
								+ first.targetId + " at time " + first.startTime + " and duration "
								+ first.duration + " overlaps with appearance at time "
								+ second.startTime + ".";
						throw new RuntimeException(error);
					}
				}
			}
		}
	}

	public void saveOutput() {
		if(output != null) {
			System.out.println("Saving graph on file");
			saveOutput(drawnGraph);
		}
	}

	/**
	 * Saves the output in a given file.
	 *
	 * @param graph the dyGraph to save.
	 */
	public void saveOutput(DyGraph graph) {
		List<String> outputLines = new ArrayList<>();
		DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
		for (Node node : graph.nodes()) {
			Evolution<Coordinates> evolution = position.get(node);
			double previousEntryTime = Double.NEGATIVE_INFINITY;
			for (Function<Coordinates> function : evolution) {
				Interval interval = function.interval();

				if (interval.leftBound() != previousEntryTime) {
					Coordinates startPosition = function.leftValue();
					outputLines.add(node + "," + startPosition.x() + ","
							+ startPosition.y() + "," + interval.leftBound());
				}
				previousEntryTime = interval.leftBound();

				if (interval.rightBound() != previousEntryTime) {
					Coordinates endPosition = function.rightValue();
					outputLines.add(node + "," + endPosition.x() + ","
							+ endPosition.y() + "," + interval.rightBound());
				}
				previousEntryTime = interval.rightBound();
			}
		}
		ParserTools.writeFileLines(outputLines, new File(output));
	}    

	/**
	 * Creates the dynamic dyGraph.
	 *
	 * @return the dynamic dyGraph.
	 */
	protected DyGraph createDynamicGraph(List<NodeAppearance> nodeDataSet, List<EdgeAppearance> edgeDataSet) {
		DyGraph graph = new DyGraph();
		DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
		DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
		DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);

		for (NodeAppearance appearance : nodeDataSet) {
			if (!graph.hasNode(appearance.id)) {
				Node node = graph.newNode(appearance.id);
				label.set(node, new Evolution<>(appearance.id));
				presence.set(node, new Evolution<>(false));
				position.set(node, new Evolution<>(new Coordinates(0, 0)));
			}
			Node node = graph.getNode(appearance.id);
			Interval presenceInterval = Interval.newClosed(appearance.startTime,
					appearance.startTime + appearance.duration);
			presence.get(node).insert(new FunctionConst<>(presenceInterval, true));
		}

		for (EdgeAppearance appearance : edgeDataSet) {
			Node source = graph.getNode(appearance.sourceId);
			Node target = graph.getNode(appearance.targetId);
			if (graph.betweenEdge(source, target) == null) { 
				Edge edge = graph.newEdge(source, target);
				edgePresence.set(edge, new Evolution<>(false));
			}
			Edge edge = graph.betweenEdge(source, target);
			Interval presenceInterval = Interval.newClosed(appearance.startTime,
					appearance.startTime + appearance.duration);
			edgePresence.get(edge).insert(new FunctionConst<>(presenceInterval, true));

		}

		double graphDiameterEstimate = Math.sqrt(graph.nodeCount() * delta);
		Commons.scatterNodes(graph, graphDiameterEstimate);
		return graph;
	}    

	/**
	 * Shows the command line help.
	 */
	public static void showHelp() {
		System.out.println("When a custom graph is provided, the following must be provided in this order:");
		System.out.println("nodeDataSetPath:       the path to the node dataset (csv file with nodeId,startTime,duration)");
		System.out.println("edgeDataSetPath:       the path to the edge dataset (csv file with sourceId,targetId,startTime,duration)");
		System.out.println("Command example:");
		System.out.println("java -jar /path/to/multidynnos.jar custom <path-to-nodeset> <path-to-edgeset> [OPTIONS]");
		System.out.println("Node dataset example:");
		System.out.println("Alice,1,5");
		System.out.println("Bob,2,4.6");
		System.out.println("Carol,1.5,3");
		System.out.println("");
		System.out.println("Edge dataset example:");
		System.out.println("Alice,Bob,2.5,1");
		System.out.println("Bob,Carol,2.1,0.6");

	}

	public enum AvailableDrawingOption {
		delta,
		//		nodes,
		//		edges,
		text,
		autoTau,
		tau;

		public static void printHelp() {

			for(AvailableDrawingOption m : AvailableDrawingOption.values()) {
				if(m != null)
					System.out.println(AvailableDrawingOption.toString(m));
			}

		}

		public static CMDLineOption toString(AvailableDrawingOption option) {

			switch(option) {
			case delta: return new CMDLineOption("Delta", "-d", "Specifies a user defined delta value.");
			case tau: 
				return new CMDLineOption("Manual Tau", "-t", "Specifies a user defined tau value on the command line. Overrides the Semi-Automatic Tau option");	
			case text: 
				return new CMDLineOption("Text-Out", "-o", "If present specifies the path in which to save the output graph to a text file");    	
			case autoTau:
				return new CMDLineOption("Semi-Automatic Tau", "-T", "If included in the dataset code, that specific TAU will be used."
										+ " If absent, tau will be calculated automatically.");    	
				//			case nodes: 
				//				return new CMDLineOption("Nodeset", "-n", "Specifies the path to the user specified node set");
				//			case edges: 
				//				return new CMDLineOption("Edgeset", "-e", "Specifies the path to the user specified edge set");				
			default: return null;
			}
		}

		public static AvailableDrawingOption parse(String arg) {
			switch(arg) {
			case "-d": return delta;
			case "-t": return tau;
			//			case "-n:": return nodes;
			//			case "-e:": return edges;
			case "-o": return text;
			case "-T": return autoTau;
			default: return null;
			}
		}
	}


}
