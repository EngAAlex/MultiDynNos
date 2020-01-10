package ocotillo.graph.multilevel.layout;

import java.util.HashMap;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing.FlexibleTimeTrajectories;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.multilevel.coarsening.GraphCoarsener;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy.LinearCoolingStrategy;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy.LinearCoolingStrategy;
import ocotillo.multilevel.options.MultiLevelOption;
import ocotillo.multilevel.placement.MultilevelNodePlacementStrategy;
import ocotillo.run.customrun.CustomRun;

public abstract class MultiLevelDynNoSlice {
	
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

	
	
	protected HashMap<String, DynamicLayoutParameter> optionsMap;
	
	protected GraphCoarsener gc;
	protected MultilevelNodePlacementStrategy placement;

	private int current_iteration;
	

	public MultiLevelDynNoSlice(DyGraph original, double tau, double delta) {
		dynamicGraph = original;
		optionsMap = new HashMap<String, DynamicLayoutParameter>();
		this.tau = tau;
		this.delta = delta;		
	}
	
	public MultiLevelDynNoSlice addOption(String key, DynamicLayoutParameter par) {
		optionsMap.put(key, par);
		return this;
	}
	
	public void defaultOptions() {
		addOption(DESIRED_DISTANCE, new DynamicLayoutParameter(delta, new LinearCoolingStrategy(-.1)))
			.addOption(INITIAL_MAX_MOVEMENT, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(-.1)))
			.addOption(CONTRACT_DISTANCE, new DynamicLayoutParameter(1.5*delta, new LinearCoolingStrategy(-.1)))
			.addOption(EXPAND_DISTANCE, new DynamicLayoutParameter(2*delta, new LinearCoolingStrategy(-.1)))
			.addOption(MAX_ITERATIONS, new DynamicLayoutParameter(MAX_ITERATIONS_DEFAULT, new LinearCoolingStrategy(-.1)));
	}		
	
	public MultiLevelDynNoSlice setCoarsener(GraphCoarsener gc) {
		this.gc = gc;
		return this;
	}
	
	public MultiLevelDynNoSlice setPlacementStrategy(MultilevelNodePlacementStrategy mpc) {
		placement = mpc;
		return this;
	}
	
	protected abstract void preprocess();
		
	public void runMultiLevelLayout() {
	
		preprocess();
		
		current_iteration = 0;
		
		gc.computeCoarsening();				
		Graph currentGraph = gc.getCoarserGraph();		
		computeLayout(currentGraph);			
		
		do {
			NodeAttribute<Coordinates> coarserCoordinatesAttribute = currentGraph.nodeAttribute(StdAttribute.nodePosition);
	        
	        for(Node n: currentGraph.nodes())
	        	coarserCoordinatesAttribute.set(n, new Coordinates(Math.random(), Math.random()));
	        	        
        	Graph finerGraph = currentGraph.parentGraph();
        	placement.placeVertices(finerGraph, currentGraph, gc);
        	
        	updateThermostats();
        	
    		computeLayout(finerGraph);
    		currentGraph = finerGraph;
        	
	    }while(currentGraph.parentGraph() != null);
		
		
	}

	private void updateThermostats() {
		for(DynamicLayoutParameter mt : optionsMap.values())
			mt.coolDown(current_iteration);
	}

	private void computeLayout(Graph currentGraph) {
		DyModularFdl algorithm = new DyModularFdl.DyModularFdlBuilder(currentGraph, tau)
                .withForce(new DyModularForce.TimeStraightning(delta))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.ConnectionAttraction(delta))
                .withForce(new DyModularForce.EdgeRepulsion(delta))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
                .withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
                .withPostProcessing(new FlexibleTimeTrajectories(delta * 1.5, delta * 2.0, Geom.e3D))
                .build();

        algorithm.iterate(defaultNumberOfIterations);		
	}
	
	
	
	
}
