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
import ocotillo.geometry.Geom;
import ocotillo.graph.Graph;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.graph.layout.fdl.modular.ModularMetric;
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;
import ocotillo.graph.layout.fdl.modular.ModularPreMovement;
import ocotillo.graph.layout.fdl.modular.ModularStatistics;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.SfdpBuilder;
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

public class MultiLevelDynNoSlice {

	private final double tau;
	private final double delta;

	public static final String NOT_NUKE_HIERARCHY = "notNuke";
	public static final String DESIRED_DISTANCE = "ddistance";
	//public static final double DESIRED_DISTANCE_DEFAULT = CustomRun.defaultDelta;
	public static final String INITIAL_MAX_MOVEMENT = "mMovement";
	//public static final double INITIAL_MAX_MOVEMENT_DEFAULT = 2*CustomRun.defaultDelta;
	public static final String CONTRACT_DISTANCE = "cdistance";
	//public static final double CONTRACT_DISTANCE_DEFAULT = 1.5*CustomRun.defaultDelta;	
	public static final String EXPAND_DISTANCE = "edistance";
	//public static final double EXPAND_DISTANCE_DEFAULT = 2*CustomRun.defaultDelta;	
	public static final String MAX_ITERATIONS = "mIterations";	
	public static final double MAX_ITERATIONS_DEFAULT = 50;	

	public static final String LOG_OPTION = "LogProgressInConsole";
	public static final String FLEXIBLE_TRAJECTORIES = "FLEXIBLE_TRAJECTORIES";

	DyGraph dynamicGraph;

	protected HashMap<String, DynamicLayoutParameter> parametersMap;
	protected HashMap<String, Object> optionsMap;

	protected GraphCoarsener gc;
	protected MultilevelNodePlacementStrategy placement;
	protected DyGraphFlattener flattener;

	protected HashSet<MultiLevelDrawingOption<ModularPostProcessing>> fdlPostProcessingOptions;
	protected HashSet<MultiLevelDrawingOption<ModularPreMovement>> fdlPreMovementOptions;

	private int current_iteration;	

	private ModularStatistics lastRoundStats;
	private DyModularFdl currentAlgorithm;
    protected SpaceTimeCubeSynchroniser synchronizer;
    protected DyGraph drawnGraph;

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

	public MultiLevelDynNoSlice addLayerPreMovementDrawingOption(MultiLevelDrawingOption<ModularPreMovement> opt) {
		if(fdlPostProcessingOptions == null)
			fdlPreMovementOptions = new HashSet<MultiLevelDrawingOption<ModularPreMovement>>();
		fdlPreMovementOptions.add(opt);
		return this;

	}	

	public MultiLevelDynNoSlice defaultLayoutParameters() {
		addLayoutParameter(DESIRED_DISTANCE, new DynamicLayoutParameter(delta, new MultiLevelCoolingStrategy.IdentityCoolingStrategy()))
		.addLayoutParameter(INITIAL_MAX_MOVEMENT, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(-.07)))
		.addLayoutParameter(CONTRACT_DISTANCE, new DynamicLayoutParameter(1.5*delta, new MultiLevelCoolingStrategy.IdentityCoolingStrategy()))
		.addLayoutParameter(EXPAND_DISTANCE, new DynamicLayoutParameter(2*delta, new MultiLevelCoolingStrategy.IdentityCoolingStrategy()))
		.addLayoutParameter(MAX_ITERATIONS, new DynamicLayoutParameter(MAX_ITERATIONS_DEFAULT, new LinearCoolingStrategy(-.07)));
		
//		addLayoutParameter(DESIRED_DISTANCE, new DynamicLayoutParameter(delta, new LinearCoolingStrategy(-.07)))
//		.addLayoutParameter(INITIAL_MAX_MOVEMENT, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(-.07)))
//		.addLayoutParameter(CONTRACT_DISTANCE, new DynamicLayoutParameter(1.5*delta, new LinearCoolingStrategy(-.07)))
//		.addLayoutParameter(EXPAND_DISTANCE, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(-.07)))
//		.addLayoutParameter(MAX_ITERATIONS, new DynamicLayoutParameter(MAX_ITERATIONS_DEFAULT, new LinearCoolingStrategy(-.07)));
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
				logger = Logger.getInstance((boolean)eleme);
			else
				logger = Logger.getInstance();
		}else
			logger = Logger.getInstance();

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
		placement.placeVertices(currentGraph, initialPositionedGraph);
	}

	public DyGraph runMultiLevelLayout() {

		lastRoundStats = new ModularStatistics(new HashSet<ModularMetric>());
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
		lastRoundStats.addMetric(mp);
		startTime = endTime;

		current_iteration = 1;

		logger.log("Executing Coarsening");
		CoarseningTime cp = new CoarseningTime();
		gc.computeCoarsening();
		endTime = System.nanoTime();
		addedNanos += endTime - startTime;
		cp.values().add(endTime - startTime);
		lastRoundStats.addMetric(cp);
		startTime = endTime;
		HierarchyDepth hd = new HierarchyDepth();
		hd.values().add(gc.getHierarchyDepth());
		lastRoundStats.addMetric(hd);

		PlacementTime pt = new PlacementTime();
		lastRoundStats.addMetric(pt);

		logger.log("Computing default node positioning");
		nodesFirstPlacement();
		endTime = System.nanoTime();
		addedNanos += endTime - startTime;		
		pt.values().add(endTime - startTime);
		startTime = endTime;

		Iterator<DyGraph> hierarchy = gc.getGraphIterator();

		DyGraph currentGraph = hierarchy.next(); //gc.getCoarsestGraph();	

		//LayoutTime lt = new LayoutTime(); 

		logger.log("Working on level " + (gc.getHierarchyDepth() - current_iteration));
		printParameters();
		computeDynamicLayout(currentGraph);
		endTime = System.nanoTime();
		addedNanos += endTime - startTime;				
		lastRoundStats.runAtIterationEnd(Duration.ofNanos(endTime - startTime));
		logger.log("Elapsed: " + new DecimalFormat("#.00").format((endTime - startTime)/Math.pow(10, 9)) + "s");
		startTime = endTime;
		current_iteration++;
		logger.log("Round complete!");
		
		//while(currentGraph.parentGraph() != null) {
		while(hierarchy.hasNext()) {
			updateThermostats();
			DyGraph finerGraph = placeVertices(/*currentGraph.parentGraph()*/ hierarchy.next(), currentGraph);
			endTime = System.nanoTime();
			addedNanos += endTime - startTime;    		
			pt.values().add(endTime - startTime);
			startTime = endTime;

			logger.log("Working on level " + (gc.getHierarchyDepth() - current_iteration));
			printParameters();

			computeDynamicLayout(finerGraph);    
			endTime = System.nanoTime();
			addedNanos += endTime - startTime;		    		
			lastRoundStats.runAtIterationEnd(Duration.ofNanos(endTime - startTime));
			logger.log("Elapsed: " + new DecimalFormat("#.00").format((endTime - startTime)/Math.pow(10, 9)) + "s");
			startTime = endTime;

			current_iteration++;
			logger.log("Round complete!");
			currentGraph = finerGraph;

		}

		lastRoundStats.runAtComputationEnd(Duration.ofNanos(addedNanos));

		//if(!parametersMap.containsKey(NOT_NUKE_HIERARCHY))
		drawnGraph = currentGraph;
		return currentGraph;
		/*else
			return gc.getCoarsestGraph();*/

	}

	private void printParameters() {
		logger.log("\tParameters:\n"
				+"\t\tDecreasingMaxMovement: " + parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue() + "\n"
				+"\t\tMovementAcceleration: " + parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue() + "\n"
				+"\t\tFlexibleTimeTrajectories: " + parametersMap.get(CONTRACT_DISTANCE).getCurrentValue() + " - " + parametersMap.get(EXPAND_DISTANCE).getCurrentValue() + "\n"
				+"\t\tMAX_ITERATIONS: " + Math.ceil(parametersMap.get(MAX_ITERATIONS).getCurrentValue())); 
	}

	private Graph computeStaticLayout(Graph currentGraph) {
		SfdpBuilder sfdp = new SfdpBuilder();
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
				if(opt.active(gc.getHierarchyDepth() - current_iteration))
					algorithmBuilder.withPostProcessing(opt.getValue(current_iteration, gc.getHierarchyDepth() - current_iteration, delta, tau, parametersMap.get(INITIAL_MAX_MOVEMENT), parametersMap.get(CONTRACT_DISTANCE), parametersMap.get(EXPAND_DISTANCE)));
			}

		if(fdlPreMovementOptions != null)
			for(MultiLevelDrawingOption<ModularPreMovement> opt : fdlPreMovementOptions){
				if(opt.active(gc.getHierarchyDepth() - current_iteration))
					algorithmBuilder.withPreMovmement(opt.getValue(current_iteration, gc.getHierarchyDepth() - current_iteration, delta, tau, parametersMap.get(INITIAL_MAX_MOVEMENT), parametersMap.get(CONTRACT_DISTANCE), parametersMap.get(EXPAND_DISTANCE)));
			}

		currentAlgorithm = algorithmBuilder.build();

		currentAlgorithm.iterate((int) Math.ceil(parametersMap.get(MAX_ITERATIONS).getCurrentValue()));		
		
		synchronizer = currentAlgorithm.getSyncro();
	}

	public ModularStatistics getLastRoundStatistics() {
		return lastRoundStats;
	}

	public DyGraph getDrawnGraph() {
		return drawnGraph;
	}

	public void showMirrorGraph() {
		currentAlgorithm.showMirrorGraph();;
	}

	public SpaceTimeCubeSynchroniser getSyncro() {
		return this.synchronizer;
	}

}
