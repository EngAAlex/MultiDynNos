/**
 * Copyright © 2020 Alessio Arleo
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ocotillo.run;

import java.text.DecimalFormat;
import java.util.HashMap;

import ocotillo.dygraph.DyGraph;
import ocotillo.graph.Graph;
import ocotillo.graph.multilevel.layout.MultiLevelDynNoSlice;
import ocotillo.multilevel.coarsening.IndependentSet;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.logger.Logger;
import ocotillo.multilevel.options.MultiLevelDrawingOption;
import ocotillo.multilevel.placement.WeightedBarycenterPlacementStrategy;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;

public class MultiDynNoSliceRun extends Run {

	protected Graph appearanceGraph;
	protected static String[] args;
	protected static HashMap<String, Integer> preloadedGraphs;
	private DecimalFormat secondFormat = new DecimalFormat("#.00");

	@Override
	protected DyGraph run() {

		Logger.getInstance().log("Applying tau " + tau);
		
		MultiLevelDynNoSlice multiDyn = 
				new MultiLevelDynNoSlice(dygraph, tau, Run.defaultDelta)
				.setCoarsener(new IndependentSet()) //SolarMerger
				.setPlacementStrategy(new WeightedBarycenterPlacementStrategy())
				.setFlattener(new DyGraphFlattener.StaticSumPresenceFlattener())
				.defaultLayoutParameters()
				.addLayerPostProcessingDrawingOption(new MultiLevelDrawingOption.FlexibleTimeTrajectoriesPostProcessing(0, MultiLevelDynNoSlice.TRAJECTORY_OPTIMIZATION_INTERVAL))
				.addOption(MultiLevelDynNoSlice.LOG_OPTION, true).build();

		DyGraph result = multiDyn.runMultiLevelLayout();

		Logger.getInstance().log("Algorithm elapsed time: " + secondFormat.format((multiDyn.getComputationStatistics().getTotalRunningTime().toMillis())/Math.pow(10, 3)) + "s");
		
		return result;
	}

	public MultiDynNoSliceRun(String[] argv, DyDataSet selectedDataset, Mode loadMode) {
		super(argv, selectedDataset, loadMode);
	}		

//	public DyGraph testGraphPlacement(MultiLevelDynNoSlice multiDyn, boolean randomCoordinates) {
//		DyGraph coarsenedGraph = testGraphCoarsening(multiDyn);
//
//		if(randomCoordinates) {
//			NodeAttribute<Evolution<Coordinates>> coarserCoordinates = coarsenedGraph.nodeAttribute(StdAttribute.nodePosition);
//
//			for(Node n: coarsenedGraph.nodes())
//				coarserCoordinates.set(n, new Evolution<Coordinates>(new Coordinates(Math.random(), Math.random())));
//		}else
//			multiDyn.nodesFirstPlacement();
//
//		DyGraph parentGraph = coarsenedGraph.parentGraph();
//		do {
//			multiDyn.placeVertices(parentGraph, coarsenedGraph);
//			coarsenedGraph = parentGraph;
//			parentGraph = parentGraph.parentGraph();        	
//		}while(parentGraph != null);
//
//		return multiDyn.getCoarsestGraph();
//	}
//
//	private DyGraph testGraphCoarsening(MultiLevelDynNoSlice multiDyn) {
//		return multiDyn.runCoarsening();		
//	}
	
//	private void outputFullLevels(DyGraph startGraph, Consumer<DyGraph> visualization) {
//		int currentLevel = 0;
//
//		DyGraph currentGraph = startGraph;
//		do {
//			System.out.println("Displaying level " + currentLevel);		
//
//			visualization.accept(currentGraph);
//
//			DyGraph parentGraph = currentGraph.parentGraph();
//			if(parentGraph != null) {
//				parentGraph.nukeSubgraph(currentGraph);        		
//				currentLevel++;
//			}
//			currentGraph = parentGraph;        
//
//		}while(currentGraph != null);
//	}

//	protected void showGraphOnWindow(DyGraph graph, double timing, String graphName) {
//
//		SpaceTimeCubeSynchroniser stcs = new StcsBuilder(graph, timing).build();
//		QuickView window = new QuickView(stcs.mirrorGraph(), graphName);
//		window.showNewWindow();
//	}
//	
//	protected void animateGraphOnWindow(DyGraph graph, double timing, Interval interval, String graphName) {
//
//		DyQuickView dyWindow = new DyQuickView(graph, timing, graphName);
//		dyWindow.setAnimation(new Animation(interval, Duration.ofSeconds(30)));
//		dyWindow.showNewWindow();
//	}
//	
//	private void showGraphOnTerminal(DyGraph currentGraph) {
//		System.out.println("Nodes:");
//
//		DyNodeAttribute<Double> currentLevelNodeWeight = currentGraph.nodeAttribute(StdAttribute.weight);
//		DyEdgeAttribute<Double> currentLevelEdgeWeight = currentGraph.edgeAttribute(StdAttribute.weight);
//
//		DyNodeAttribute<Evolution<Double>> currentLevelNodeCoordinates = currentGraph.nodeAttribute(StdAttribute.nodePosition);
//
//		DyNodeAttribute<Boolean> currentLevelNodePresence = currentGraph.nodeAttribute(StdAttribute.dyPresence);
//		DyEdgeAttribute<Boolean> currentLevelEdgePresence = currentGraph.edgeAttribute(StdAttribute.dyPresence);
//
//		for(Node n : currentGraph.nodes()) {
//			System.out.println(n.id() + "\n\tWeight " + currentLevelNodeWeight.get(n).getDefaultValue() + "\n\tPosition " + currentLevelNodeCoordinates.get(n).getLastValue());
//			Evolution<Boolean> nodePresence = currentLevelNodePresence.get(n);
//			for(Function<Boolean> f : nodePresence.getAllIntervals())
//				System.out.println("Exists from " + f.interval().leftBound() + " to " + f.interval().rightBound());        		
//		}
//		
//		System.out.println("Edges:");
//
//    	for(Edge e: currentGraph.edges()) {        		
//        	System.out.println("From " + e.source().id() + " to " + e.target().id() + "\n\tWeight " + currentLevelEdgeWeight.get(e).getDefaultValue());
//    		Evolution<Boolean> edgePresence = currentLevelEdgePresence.get(e);
//    		for(Function<Boolean> f : edgePresence.getAllIntervals())
//    			System.out.println("Exists from " + f.interval().leftBound() + " to " + f.interval().rightBound());    
//    	}
//
//		System.out.println("\n");
//	}
//
//	public void outputAsGml(DyGraph graph, String filename) {
//
//		System.out.println("Writing output as gml");
//
//		PrintWriter pw = null;
//		try {
//			pw = new PrintWriter(new File(new File(this.output).getParentFile()+File.pathSeparator+filename+".gml"));
//		} catch (FileNotFoundException e1) {
//			System.err.println("Can't find output file");
//			e1.printStackTrace();
//		}
//
//		short indentLevel = 0;
//		pw.println(indentString("graph [", indentLevel));
//		indentLevel++;
//		pw.println(indentString("directed 0", indentLevel));
//
//		DyNodeAttribute<Coordinates> currentLevelNodeCoordinates = graph.nodeAttribute(StdAttribute.nodePosition);
//
//		for(Node n : graph.nodes()) {
//			int id = Integer.parseInt(n.id().split("__")[0]);
//			pw.println(indentString("node [", indentLevel));
//			indentLevel++;
//			double[] coords = new double[] {currentLevelNodeCoordinates.get(n).getLastValue().get(0), currentLevelNodeCoordinates.get(n).getLastValue().get(1)};
//			pw.println(indentString("id " + id, indentLevel));
//			pw.println(indentString("label \"" + n.id() +"\"", indentLevel));			
//			pw.println(indentString("graphics [ ", indentLevel));	
//			indentLevel++;
//			pw.println(indentString("x " + coords[0], indentLevel));
//			pw.println(indentString("y " + coords[1], indentLevel));
//			pw.println(indentString("w 5", indentLevel));
//			pw.println(indentString("h 5", indentLevel));
//			indentLevel--;
//			pw.println(indentString("]", indentLevel));
//			indentLevel--;
//			pw.println(indentString("]", indentLevel));
//		}
//
//		//DyEdgeAttribute<Double> currentLevelEdgeWeight = graph.edgeAttribute(StdAttribute.weight);
//
//		for(Edge e: graph.edges()){
//			pw.println(indentString("edge [", indentLevel));
//			indentLevel++;
//			pw.println(indentString("source " + Integer.parseInt(e.source().id().split("__")[0]), indentLevel));
//			pw.println(indentString("target " + Integer.parseInt(e.target().id().split("__")[0]), indentLevel));
//			pw.println(indentString("graphics [", indentLevel));
//			indentLevel++;
//			pw.println(indentString("type \"line\"", indentLevel));
//			pw.println(indentString("width 1" /*+ (currentLevelEdgeWeight.get(e).getDefaultValue() == -1 ? 1 : currentEdge.getWeight() + 1)*/, indentLevel));		
//			indentLevel--;
//			pw.println(indentString("]", indentLevel));
//			indentLevel--;
//			pw.println(indentString("]", indentLevel));
//		}		
//
//		indentLevel--;
//
//		pw.println(indentString("]", indentLevel));
//		pw.close();
//
//		System.out.println("All done!");
//
//	}
//	
//	protected static String indentString(String in, short indentLevel){
//		String result = "";
//		for(int i=0; i<indentLevel; i++)
//			result += "\t";
//		return result + in;
//	}

	@Override
	protected void completeSetup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getDescription() {
		return "MultiDynNoSlice";
	}

}
