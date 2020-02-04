package ocotillo.graph.multilevel.layout;

import java.util.HashMap;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing.FlexibleTimeTrajectories;
import ocotillo.geometry.Geom;
import ocotillo.graph.Graph;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.SfdpBuilder;
import ocotillo.multilevel.coarsening.GraphCoarsener;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy.LinearCoolingStrategy;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.placement.MultilevelNodePlacementStrategy;
import ocotillo.run.customrun.CustomRun;

public class MultiLevelDynNoSlice {
	
	private final double tau;
	private final double delta;
	
	public static final String DESIRED_DISTANCE = "ddistance";
	//public static final double DESIRED_DISTANCE_DEFAULT = CustomRun.defaultDelta;
	public static final String INITIAL_MAX_MOVEMENT = "mMovement";
	//public static final double INITIAL_MAX_MOVEMENT_DEFAULT = 2*CustomRun.defaultDelta;
	public static final String CONTRACT_DISTANCE = "cdistance";
	//public static final double CONTRACT_DISTANCE_DEFAULT = 1.5*CustomRun.defaultDelta;	
	public static final String EXPAND_DISTANCE = "edistance";
	//public static final double EXPAND_DISTANCE_DEFAULT = 2*CustomRun.defaultDelta;	
	public static final String MAX_ITERATIONS = "mIterations";	
	public static final double MAX_ITERATIONS_DEFAULT = CustomRun.defaultNumberOfIterations;	
	
	
	DyGraph dynamicGraph;
	
	protected HashMap<String, DynamicLayoutParameter> parametersMap;
	
	protected GraphCoarsener gc;
	protected MultilevelNodePlacementStrategy placement;
	protected DyGraphFlattener flattener;

	private int current_iteration;	

	public MultiLevelDynNoSlice(DyGraph original, double tau, double delta) {
		dynamicGraph = original;
		parametersMap = new HashMap<String, DynamicLayoutParameter>();
		this.tau = tau;
		this.delta = delta;		
	}
	
	public MultiLevelDynNoSlice addOption(String key, DynamicLayoutParameter par) {
		parametersMap.put(key, par);
		return this;
	}
	
	public MultiLevelDynNoSlice defaultOptions() {
		addOption(DESIRED_DISTANCE, new DynamicLayoutParameter(delta, new LinearCoolingStrategy(-.1)))
			.addOption(INITIAL_MAX_MOVEMENT, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(-.1)))
			.addOption(CONTRACT_DISTANCE, new DynamicLayoutParameter(1.5*delta, new LinearCoolingStrategy(-.1)))
			.addOption(EXPAND_DISTANCE, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(-.1)))
			.addOption(MAX_ITERATIONS, new DynamicLayoutParameter(MAX_ITERATIONS_DEFAULT, new LinearCoolingStrategy(-.1)));
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
	
	protected void preprocess() {
		gc.setGraph(dynamicGraph);
	}
	
	public Graph runFlattener() {
		preprocess();

		return computeStaticLayout(flattener.flattenDyGraph(gc.getCoarserGraph()));
	}
	
	public DyGraph runCoarsening() {
		preprocess();

		gc.computeCoarsening();
		return gc.getCoarserGraph();
	}
		
	public DyGraph placeVertices(DyGraph finerGraph, DyGraph currentGraph) {
		placement.placeVertices(finerGraph, currentGraph, gc);
		return finerGraph;
	}
	
	public DyGraph runMultiLevelLayout() {
	
		preprocess();
		
		current_iteration = 0;
		
		gc.computeCoarsening();				
		DyGraph currentGraph = gc.getCoarserGraph();		
		Graph initialPositionedGraph = computeStaticLayout(flattener.flattenDyGraph(currentGraph));
    	placement.placeVertices(currentGraph, initialPositionedGraph, gc);		
		
		do {			       	       
        	DyGraph finerGraph = placeVertices(currentGraph.parentGraph(), currentGraph);
        	
        	updateThermostats();
        	
    		computeDynamicLayout(finerGraph);
    		
    		finerGraph.nukeSubgraph(currentGraph);

    		currentGraph = finerGraph;
    		        	
	    }while(currentGraph.parentGraph() != null);
		
		return gc.getCoarserGraph();
		
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
		DyModularFdl algorithm = new DyModularFdl.DyModularFdlBuilder(currentGraph, tau)
                .withForce(new DyModularForce.TimeStraightning(delta))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.ConnectionAttraction(delta))
                .withForce(new DyModularForce.EdgeRepulsion(delta))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue()))
                .withConstraint(new ModularConstraint.MovementAcceleration(parametersMap.get(INITIAL_MAX_MOVEMENT).getCurrentValue(), Geom.e3D))
                .withPostProcessing(new FlexibleTimeTrajectories(parametersMap.get(CONTRACT_DISTANCE).getCurrentValue(), parametersMap.get(EXPAND_DISTANCE).getCurrentValue(), Geom.e3D))
                .build();

        algorithm.iterate((int) parametersMap.get(MAX_ITERATIONS).getCurrentValue());		
	}

	
}
