package ocotillo.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.run.customrun.EdgeAppearance;
import ocotillo.run.customrun.NodeAppearance;
import ocotillo.serialization.ParserTools;

public abstract class Run {

	public static final double defaultDelta = 5.0;
	public static final double defaultTau = 1.0;
	public static final int defaultNumberOfIterations = 100;
	public static final String defaultOutput = "output.txt";

	protected final List<NodeAppearance> nodeDataSet;
	protected final List<EdgeAppearance> edgeDataSet;

	protected final double delta;
	protected final double tau;
	protected final String output;

	public Run(String[] argv) {
		if (argv.length < 2) {
			showHelp();
		}

		File nodeDataSetFile = new File(argv[0]);
		if (!nodeDataSetFile.exists()) {
			System.err.println("The node data set file \"" + argv[0] + "\" does not exist. \n");
			showHelp();
		}
		List<String> nodeDataSetLines = ParserTools.readFileLines(nodeDataSetFile);

		File edgeDataSetFile = new File(argv[1]);
		if (!edgeDataSetFile.exists()) {
			System.err.println("The node edge set file \"" + argv[1] + "\" does not exist. \n");
			showHelp();
		}
		List<String> edgeDataSetLines = ParserTools.readFileLines(edgeDataSetFile);

		double delta = defaultDelta;
		if (argv.length >= 3) {
			try {
				double possibleDelta = Double.parseDouble(argv[2]);
				delta = possibleDelta > 0 ? possibleDelta : defaultDelta;
			} catch (Exception e) {
				System.err.println("Cannot parse delta correctly. \n");
				showHelp();
			} 
		}

		double tau = defaultTau;
		if (argv.length >= 4) {
			try {
				double possibleTau = Double.parseDouble(argv[3]);
				tau = possibleTau >= 0 ? possibleTau : defaultTau;
			} catch (Exception e) {
				System.err.println("Cannot parse tau correctly. \n");
				showHelp();
			}
		}

		if (argv.length >= 5) {
			System.out.println("Carramba");
			this.output = argv[4];
		}else
			this.output = defaultOutput;

        this.nodeDataSet = NodeAppearance.parseDataSet(nodeDataSetLines);
        this.edgeDataSet = EdgeAppearance.parseDataSet(edgeDataSetLines);  
		this.tau = tau;
		this.delta = delta;
        
		completeSetup();
        
	}

	protected abstract void completeSetup();

	protected abstract void run();

    /**
     * Checks if the node appearances are in correct order (no earlier
     * appearance of a node is later in the list) and there are no overlaps
     * between any two appearances of the same node.
     *
     * @param nodeDataSet the node data set.
     */
	protected static void checkNodeAppearanceCorrectness(List<NodeAppearance> nodeDataSet) {
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
    protected static void checkEdgeAppearanceCorrectness(List<EdgeAppearance> edgeDataSet) {
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
	
    /**
     * Saves the output in a given file.
     *
     * @param graph the dyGraph to save.
     */
    protected void saveOutput(DyGraph graph) {
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
	 * Shows the command line help.
	 */
	protected static void showHelp() {
		System.out.println("Custom run needs the following parameters in this order:");
		System.out.println("nodeDataSetPath:       the path to the node dataset (csv file with nodeId,startTime,duration)");
		System.out.println("edgeDataSetPath:       the path to the edge dataset (csv file with sourceId,targetId,startTime,duration)");
		System.out.println("delta (optional):      the desired node distance on the plane.");
		System.out.println("tau (optional):        the conversion factor of time into space.");
		System.out.println("ouputFile (optional):  the output file (csv file with node,xCoord,yCoord,time).");
		System.out.println("");
		System.out.println("Node dataset example:");
		System.out.println("Alice,1,5");
		System.out.println("Bob,2,4.6");
		System.out.println("Carol,1.5,3");
		System.out.println("");
		System.out.println("Edge dataset example:");
		System.out.println("Alice,Bob,2.5,1");
		System.out.println("Bob,Carol,2.1,0.6");
		System.exit(0);
	}


}
