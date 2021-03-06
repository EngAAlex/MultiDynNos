/**
 * Copyright ? 2020 Alessio Arleo
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

package ocotillo.graph.multilevel.layout;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl.DyModularFdlBuilder;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPreMovement;
import ocotillo.geometry.Geom;
import ocotillo.graph.Graph;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.graph.layout.fdl.modular.ModularMetric;
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;
import ocotillo.graph.layout.fdl.modular.ModularStatistics;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.AVAILABLE_STATIC_LAYOUTS;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.SfdpBuilder;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.multilevel.MultilevelMetrics.CoarseningTime;
import ocotillo.multilevel.MultilevelMetrics.HierarchyDepth;
import ocotillo.multilevel.MultilevelMetrics.MultiLevelPreProcessTime;
import ocotillo.multilevel.MultilevelMetrics.PlacementTime;
import ocotillo.multilevel.coarsening.GraphCoarsener;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy.LinearCoolingStrategy;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.logger.Logger;
import ocotillo.multilevel.options.MultiLevelDrawingOption;
import ocotillo.multilevel.placement.MultilevelNodePlacementStrategy;
import ocotillo.run.Run;

public class MultiLevelDynNoSlice {
	
	public enum LIMIT_MINIMUM_TUNING {
		
		NO_LIMIT,
		LIMITED;
		
		public double getSelectedMinimum(String option) {
			if(this.equals(NO_LIMIT))
				return Double.MIN_VALUE;
			else
				switch(option) {
				case INITIAL_MAX_MOVEMENT: return 3;
				case MAX_ITERATIONS: return MIN_ITERATIONS_DEFAULT;
				default: return Double.MIN_VALUE;
				}
		}
		
	}

	public final double tau;
	private final double delta;

	public static final String NOT_NUKE_HIERARCHY = "notNuke";
	public static final String DESIRED_DISTANCE = "ddistance";
	public static final String INITIAL_MAX_MOVEMENT = "mMovement";
	public static final String CONTRACT_DISTANCE = "cdistance";
	public static final String EXPAND_DISTANCE = "edistance";
	public static final String MAX_ITERATIONS = "mIterations";	
	public static final double MAX_ITERATIONS_DEFAULT = 75;	
	public static final double MIN_ITERATIONS_DEFAULT = 20;	
	
	public static final double DEFAULT_TUNING_SLOPE = -0.07;
	
	public static final String LOG_OPTION = "LogProgressInConsole";
	public static final String FLEXIBLE_TRAJECTORIES = "FLEXIBLE_TRAJECTORIES";
	public static final int TRAJECTORY_OPTIMIZATION_INTERVAL = 30;
	
	public static final String BEND_TRANSFER = "EnableBendTransfer";
	public static final String LIMIT_MINIMUM_TUNING = "MinimumTuning";

	DyGraph dynamicGraph;

	protected HashMap<String, DynamicLayoutParameter> parametersMap;
	protected HashMap<String, Object> optionsMap;

	protected GraphCoarsener gc;
	protected MultilevelNodePlacementStrategy placement;
	protected DyGraphFlattener flattener;

	protected HashSet<MultiLevelDrawingOption<ModularPostProcessing>> fdlPostProcessingOptions;
	protected HashSet<MultiLevelDrawingOption<DyModularPreMovement>> fdlPreMovementOptions;

	private int current_iteration;	

	private ModularStatistics computationStats;
	private DyModularFdl currentAlgorithm;
    protected SpaceTimeCubeSynchroniser synchronizer;
    protected DyGraph drawnGraph;
    
    protected AVAILABLE_STATIC_LAYOUTS singleLevelLayout = SfdpExecutor.DEFAULT_COMMAND_LINE;

	Logger logger;

	public MultiLevelDynNoSlice(DyGraph original, double tau, double delta) {
		dynamicGraph = original;
		parametersMap = new HashMap<String, DynamicLayoutParameter>();
		optionsMap = new HashMap<String, Object>();
		this.tau = tau;
		this.delta = delta;		
	}

	public MultiLevelDynNoSlice addLayoutParameter(String key, DynamicLayoutParameter par) {
		parametersMap.put(key, par);
		return this;
	}

	public MultiLevelDynNoSlice addLayerPostProcessingDrawingOption(MultiLevelDrawingOption<ModularPostProcessing> opt) {
		if(fdlPostProcessingOptions == null)
			fdlPostProcessingOptions = new HashSet<MultiLevelDrawingOption<ModularPostProcessing>>();
		fdlPostProcessingOptions.add(opt);
		return this;

	}

	public MultiLevelDynNoSlice addLayerPreMovementDrawingOption(MultiLevelDrawingOption<DyModularPreMovement> opt) {
		if(fdlPostProcessingOptions == null)
			fdlPreMovementOptions = new HashSet<MultiLevelDrawingOption<DyModularPreMovement>>();
		fdlPreMovementOptions.add(opt);
		return this;

	}	

	public MultiLevelDynNoSlice defaultLayoutParameters(LIMIT_MINIMUM_TUNING limit) {
		addLayoutParameter(DESIRED_DISTANCE, new DynamicLayoutParameter(delta, new MultiLevelCoolingStrategy.IdentityCoolingStrategy()))
		.addLayoutParameter(INITIAL_MAX_MOVEMENT, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(DEFAULT_TUNING_SLOPE), limit.getSelectedMinimum(INITIAL_MAX_MOVEMENT)))
		.addLayoutParameter(CONTRACT_DISTANCE, new DynamicLayoutParameter(1.5*delta, new MultiLevelCoolingStrategy.IdentityCoolingStrategy()))
		.addLayoutParameter(EXPAND_DISTANCE, new DynamicLayoutParameter(2*delta, new MultiLevelCoolingStrategy.IdentityCoolingStrategy()))
		.addLayoutParameter(MAX_ITERATIONS, new DynamicLayoutParameter(MAX_ITERATIONS_DEFAULT, new LinearCoolingStrategy(DEFAULT_TUNING_SLOPE), limit.getSelectedMinimum(MAX_ITERATIONS)));
		return this;
	}
	
	public MultiLevelDynNoSlice withSingleLevelLayout(AVAILABLE_STATIC_LAYOUTS commandLine) {
		this.singleLevelLayout = commandLine;
		return this;
	}

	public MultiLevelDynNoSlice addOption(String key, Object value) {
		optionsMap.put(key, value);
		return this;
	}

	public MultiLevelDynNoSlice setCoarsener(GraphCoarsener gc) {
		this.gc = gc;
		return this;
	}

	public MultiLevelDynNoSlice setPlacementStrategy(MultilevelNodePlacementStrategy mpc) {
		placement = mpc;	
		return this;
	}

	public MultiLevelDynNoSlice setFlattener(DyGraphFlattener flattener) {
		this.flattener = flattener;
		return this;
	}

	public MultiLevelDynNoSlice build() {
		placement.setCoarsener(gc);

		if(optionsMap.containsKey(LOG_OPTION)) {
			Object eleme = optionsMap.get(LOG_OPTION);
			if(eleme instanceof Boolean)
				Logger.setLog((boolean)eleme);
			else
				Logger.setLog(false);
		}
		
		logger = Logger.getInstance();
		
		logger.log("Coarsening | Selected " + this.gc.getDescription());
		logger.log("Placement | Selected " + this.placement.getDescription());	

		return this;
	}

	protected void preprocess() {
		gc.setGraph(dynamicGraph);
	}

	public Graph runFlattener() {
		preprocess();

		return computeStaticLayout(flattener.flattenDyGraph(gc.getCoarsestGraph()));
	}

	public DyGraph runCoarsening() {
		preprocess();

		gc.computeCoarsening();
		return gc.getCoarsestGraph();
	}

	public DyGraph getCoarsestGraph() {
		return gc.getCoarsestGraph();
	}

	public DyGraph placeVertices(DyGraph finerGraph, DyGraph currentGraph) {
		placement.placeVertices(finerGraph, currentGraph);
		return finerGraph;
	}

	public void nodesFirstPlacement() {
		DyGraph currentGraph = gc.getCoarsestGraph();		
		Graph initialPositionedGraph = computeStaticLayout(flattener.flattenDyGraph(currentGraph));
		placement.transferCoordinatesFromStaticGraph(currentGraph, initialPositionedGraph);
	}

	public DyGraph runMultiLevelLayout() {

		computationStats = new ModularStatistics(new HashSet<ModularMetric>());
		long addedNanos = 0;
		//HashSet<ModularMetric> computationMetrics = new HashSet<ModularMetric>();
		//ModularStatistics iterationTimes = new ModularStatistics(new HashSet<ModularMetric>());

		long startTime = System.nanoTime();
		//double initialTime = startTime;

		logger.log("Preprocessing...");
		preprocess();
		MultiLevelPreProcessTime mp = new MultiLevelPreProcessTime();
		long endTime = System.nanoTime();
		addedNanos += endTime - startTime;
		mp.values().add(endTime - startTime);
		computationStats.addMetric(mp);
		startTime = endTime;

		current_iteration = 1;

		logger.log("Executing Coarsening");
		CoarseningTime cp = new CoarseningTime();
		gc.computeCoarsening();
		endTime = System.nanoTime();
		addedNanos += endTime - startTime;
		cp.values().add(endTime - startTime);
		computationStats.addMetric(cp);
		startTime = endTime;
		HierarchyDepth hd = new HierarchyDepth();
		hd.values().add(gc.getHierarchyDepth());
		computationStats.addMetric(hd);

		PlacementTime pt = new PlacementTime();
		computationStats.addMetric(pt);

		logger.log("Computing default node positioning");
		nodesFirstPlacement();
		endTime = System.nanoTime();
		addedNanos += endTime - startTime;		
		pt.values().add(endTime - startTime);
		startTime = endTime;

		Iterator<DyGraph> hierarchy = gc.getGraphIterator();

		DyGraph currentGraph = hierarchy.next(); 

		logger.log("Working on level " + (gc.getHierarchyDepth() - current_iteration));
		printParameters();
		//Run.animateGraphOnWindow(currentGraph, dynamicGraph.getComputedSuggestedInterval().leftBound(), dynamicGraph.getComputedSuggestedInterval(), "Level " + (gc.getHierarchyDepth() - current_iteration));

		if(currentGraph.nodes().size() > 1)		
			computeDynamicLayout(currentGraph);
		
		//Run.animateGraphOnWindow(currentGraph, dynamicGraph.getComputedSuggestedInterval().leftBound(), dynamicGraph.getComputedSuggestedInterval(), "Level " + (gc.getHierarchyDepth() - current_iteration));

		endTime = System.nanoTime();
		addedNanos += endTime - startTime;				
		computationStats.runAtIterationEnd(Duration.ofNanos(endTime - startTime));
		logger.log("Elapsed: " + new DecimalFormat("#.00").format((endTime - startTime)/Math.pow(10, 9)) + "s");		
		startTime = endTime;
		//Run.animateGraphOnWindow(currentGraph, dynamicGraph.getComputedSuggestedInterval().leftBound(), dynamicGraph.getComputedSuggestedInterval(), "Level " + (gc.getHierarchyDepth() - current_iteration));

		current_iteration++;
		logger.log("Round complete!");		
		
		while(hierarchy.hasNext()) {
			updateThermostats();
			DyGraph finerGraph = placeVertices(/*currentGraph.parentGraph()*/ hierarchy.next(), currentGraph);
			
			//Run.animateGraphOnWindow(finerGraph, dynamicGraph.getComputedSuggestedInterval().leftBound(), dynamicGraph.getComputedSuggestedInterval(), "Level " + (gc.getHierarchyDepth() - current_iteration));
			
			endTime = System.nanoTime();
			addedNanos += endTime - startTime;    		
			pt.values().add(endTime - startTime);
			startTime = endTime;

			logger.log("Working on level " + (gc.getHierarchyDepth() - current_iteration));
			printParameters();

			computeDynamicLayout(finerGraph);    
			endTime = System.nanoTime();
			addedNanos += endTime - startTime;		    		
			computationStats.runAtIterationEnd(Duration.ofNanos(endTime - startTime));
			logger.log("Elapsed: " + new DecimalFormat("#.00").format((endTime - startTime)/Math.pow(10, 9)) + "s");
			startTime = endTime;

			current_iteration++;
			logger.log("Round complete!");
			currentGraph = finerGraph;

		}

		computationStats.runAtComputationEnd(Duration.ofNanos(addedNanos));

		//if(!parametersMap.containsKey(NOT_NUKE_HIERARCHY))
		drawnGraph = currentGraph;
		return currentGraph;
		/*else
			return gc.getCoarsestGraph();*/

	}

	private void printParameters() {
		//double iterations = Math.max(Math.ceil(parametersMap.get(MAX_ITERATIONS).getCurrentValue()), MIN_ITERATIONS_DEFAULT);
		logger.log("\tParameters:\n"
				+"\t\tDecreasingMaxMovement: " + parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue() + "\n"
				+"\t\tMovementAcceleration: " + parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue() + "\n"
				+"\t\tFlexibleTimeTrajectories: " + parametersMap.get(CONTRACT_DISTANCE).getCurrentValue() + " - " + parametersMap.get(EXPAND_DISTANCE).getCurrentValue() + "\n"
				+"\t\tMAX_ITERATIONS: " + Math.ceil(parametersMap.get(MAX_ITERATIONS).getCurrentValue())); 
	}

	private Graph computeStaticLayout(Graph currentGraph) {
		SfdpBuilder sfdp = new SfdpBuilder().withCommandLine(singleLevelLayout);
		logger.log("Using " + AVAILABLE_STATIC_LAYOUTS.toString(singleLevelLayout) + " for first layout");
		SfdpExecutor sfdpInstance = sfdp.build();
		sfdpInstance.execute(currentGraph);	
		return currentGraph;
	}

	private void updateThermostats() {
		for(DynamicLayoutParameter mt : parametersMap.values())
			mt.coolDown(current_iteration);
	}

	private void computeDynamicLayout(DyGraph currentGraph) {
				
		DyModularFdlBuilder algorithmBuilder = new DyModularFdl.DyModularFdlBuilder(currentGraph, tau)
				.withForce(new DyModularForce.TimeStraightning(delta))
				.withForce(new DyModularForce.Gravity())
				.withForce(new DyModularForce.ConnectionAttraction(delta))
				.withForce(new DyModularForce.EdgeRepulsion(delta))
				.withConstraint(new ModularConstraint.DecreasingMaxMovement(parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue()))
				.withConstraint(new ModularConstraint.MovementAcceleration(parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue(), Geom.e3D));

		if(fdlPostProcessingOptions != null)
			for(MultiLevelDrawingOption<ModularPostProcessing> opt : fdlPostProcessingOptions){
				if(opt.active(gc.getHierarchyDepth() - current_iteration)/* gc.getHierarchyDepth() - current_iteration == 0 */)
					algorithmBuilder.withPostProcessing(opt.getValue(current_iteration, gc.getHierarchyDepth() - current_iteration, delta, tau, parametersMap.get(INITIAL_MAX_MOVEMENT), parametersMap.get(CONTRACT_DISTANCE), parametersMap.get(EXPAND_DISTANCE)));
			}

		if(fdlPreMovementOptions != null)
			for(MultiLevelDrawingOption<DyModularPreMovement> opt : fdlPreMovementOptions){
				if(opt.active(gc.getHierarchyDepth() - current_iteration))
					algorithmBuilder.withPreMovmement(opt.getValue(current_iteration, gc.getHierarchyDepth() - current_iteration, delta, tau, parametersMap.get(INITIAL_MAX_MOVEMENT), parametersMap.get(CONTRACT_DISTANCE), parametersMap.get(EXPAND_DISTANCE)));
			}

		currentAlgorithm = algorithmBuilder.build();
		
		currentAlgorithm.iterate((int) Math.ceil(parametersMap.get(MAX_ITERATIONS).getCurrentValue()));		
		
		synchronizer = currentAlgorithm.getSyncro();
	}

	public ModularStatistics getComputationStatistics() {
		return computationStats;
	}

	public DyGraph getDrawnGraph() {
		return drawnGraph;
	}

	public void showMirrorGraph() {
		currentAlgorithm.showMirrorGraph();
	}

	public SpaceTimeCubeSynchroniser getSyncro() {
		return this.synchronizer;
	}

}
