/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ocotillo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.FunctionRect;
import ocotillo.dygraph.Interpolation;
import ocotillo.dygraph.extra.DyClustering;
import ocotillo.dygraph.extra.DyGraphDiscretiser;
import ocotillo.dygraph.extra.DyGraphMetric;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.extra.StcGraphMetric;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing;
import ocotillo.dygraph.layout.fdl.modular.DyModularPreMovement;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.export.GMLOutputWriter;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.extra.GraphMetric;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.graph.layout.fdl.modular.ModularMetric;
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;
import ocotillo.graph.layout.fdl.modular.ModularStatistics;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.SfdpBuilder;
import ocotillo.graph.multilevel.layout.MultiLevelDynNoSlice;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.multilevel.MultilevelMetrics.CoarseningTime;
import ocotillo.multilevel.MultilevelMetrics.HierarchyDepth;
import ocotillo.multilevel.coarsening.GraphCoarsener;
import ocotillo.multilevel.coarsening.IndependentSet;
import ocotillo.multilevel.coarsening.SolarMerger;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.flattener.DyGraphFlattener.StaticSumPresenceFlattener;
import ocotillo.multilevel.options.MultiLevelDrawingOption;
import ocotillo.multilevel.placement.MultilevelNodePlacementStrategy;
import ocotillo.multilevel.placement.WeightedBarycenterPlacementStrategy;
import ocotillo.run.Run;
import ocotillo.samples.parsers.BitcoinAlpha;
import ocotillo.samples.parsers.BitcoinOTC;
import ocotillo.samples.parsers.CollegeMsg;
import ocotillo.samples.parsers.Commons;
import ocotillo.samples.parsers.Commons.Mode;
import ocotillo.samples.parsers.DialogSequences;
import ocotillo.samples.parsers.InfoVisCitations;
import ocotillo.samples.parsers.Mooc;
import ocotillo.samples.parsers.NewcombFraternity;
import ocotillo.samples.parsers.PreloadedGraphParser;
import ocotillo.samples.parsers.RealityMining;
import ocotillo.samples.parsers.RugbyTweets;
import ocotillo.samples.parsers.VanDeBunt;
import ocotillo.serialization.ParserTools;
import ocotillo.various.ColorCollection;

/**
 * Experiment on dynamic graph drawing.
 */
public abstract class Experiment {

	private static final long TIMEOUT = 9000;

	protected final String name;
	protected final String directory;
	protected final Commons.DyDataSet dataset;
	protected final PreloadedGraphParser parserInstance;
	protected final Commons.Mode loadMode;
	protected final double delta;

	protected HashSet<Callable<ModularStatistics>> callables = new HashSet<Callable<ModularStatistics>>();

	private final static String STAT_SEPARATOR = ";";


	/**
	 * Builds the experiment.
	 *
	 * @param name the name of the experiment.
	 * @param directory the dataset directory.
	 * @param dataset the dataset to use.
	 * @param delta the default edge length.
	 */
	//	public Experiment(String name, String directory, Commons.DyDataSet dataset, double delta) throws URISyntaxException{
	//		this.name = name;
	//		this.directory = directory;
	//		this.dataset = dataset;
	//		this.delta = delta;
	//	}

	/**
	 * Builds the experiment.
	 *
	 * @param name the name of the experiment.
	 * @param directory the dataset directory.
	 * @param parserClass the parser class of the dataset.
	 * @param loadMode how to load the dataset.
	 * @param delta the default edge length.
	 */
	public Experiment(String name, String directory, Class<? extends PreloadedGraphParser> parserClass, Mode loadMode, double delta) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, URISyntaxException {
		this.name = name;
		this.directory = directory;
		this.delta = delta;		
		this.parserInstance = ((PreloadedGraphParser)(parserClass.getDeclaredConstructor().newInstance()));
		this.loadMode = loadMode;
		this.dataset = this.parserInstance.parse(this.loadMode);
	}

	/**
	 * Runs the layout algorithm treating the graph in continuous time.
	 *
	 * @param k the number of clusters. Negative for no clustering.
	 */
	public void runContinuous(int k) {
		DyModularFdl algorithm = getContinuousLayoutAlgorithm(dataset.dygraph, new ModularPostProcessing.DisplayCurrentIteration());

		algorithm.showMirrorGraph();
		ModularStatistics stats = algorithm.iterate(100);
		stats.saveCsv(new File("build/" + name + "_Continuous.csv"));
		System.out.println("Total running time: " + stats.getTotalRunningTime().getSeconds());

		if (k > 1) {
			DyClustering clustering = new DyClustering.Stc.KMeans3D(
					dataset.dygraph, dataset.suggestedTimeFactor, delta / 3.0, k,
					ColorCollection.cbQualitativePastel);
			clustering.colorGraph();
		}

		DyQuickView view = new DyQuickView(dataset.dygraph, dataset.suggestedInterval.leftBound());
		view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
		view.showNewWindow();
	}

	/**
	 * Runs the layout algorithm treating the graph in discrete time.
	 *
	 * @param k the number of clusters. Negative for no clustering.
	 */
	public void runDiscrete(int k) {
		DyGraph discreteGraph = discretise();
		DyModularFdl algorithm = getDiscreteLayoutAlgorithm(discreteGraph, new ModularPostProcessing.DisplayCurrentIteration());

		algorithm.showMirrorGraph();
		ModularStatistics stats = algorithm.iterate(100);
		stats.saveCsv(new File("build/" + name + "_Discrete.csv"));
		System.out.println("Total running time: " + stats.getTotalRunningTime().getSeconds());

		if (k > 1) {
			DyClustering clustering = new DyClustering.Stc.KMeans3D(
					dataset.dygraph, dataset.suggestedTimeFactor, delta / 3.0, k,
					ColorCollection.cbQualitativePastel);
			clustering.colorGraph();
		}

		DyQuickView view = new DyQuickView(discreteGraph, dataset.suggestedInterval.leftBound());
		view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
		view.showNewWindow();
	}

	/**
	 * Builds the layout algorithm for the given dynamic graph.
	 *
	 * @param dyGraph the dynamic graph.
	 * @param postProcessing eventual post processing.
	 * @return the graph drawing algorithm.
	 */
	public DyModularFdl getContinuousLayoutAlgorithm(DyGraph dyGraph, ModularPostProcessing postProcessing) {
		DyModularFdl.DyModularFdlBuilder builder = new DyModularFdl.DyModularFdlBuilder(dyGraph, dataset.suggestedTimeFactor)
				.withForce(new DyModularForce.TimeStraightning(delta))
				.withForce(new DyModularForce.Gravity())
				.withForce(new DyModularForce.ConnectionAttraction(delta))
				.withForce(new DyModularForce.EdgeRepulsion(delta))
				.withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
				.withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
				.withPostProcessing(new DyModularPostProcessing.FlexibleTimeTrajectories(delta * 1.5, delta * 2.0, Geom.e3D));

		if (postProcessing != null) {
			builder.withPostProcessing(postProcessing);
		}

		return builder.build();
	}

	/**
	 * Builds the layout algorithm for the given dynamic graph.
	 *
	 * @param dyGraph the dynamic graph.
	 * @param postProcessing eventual post processing.
	 * @return the graph drawing algorithm.
	 */
	public DyModularFdl getDiscreteLayoutAlgorithm(DyGraph dyGraph, ModularPostProcessing postProcessing) {
		DyModularFdl.DyModularFdlBuilder builder = new DyModularFdl.DyModularFdlBuilder(dyGraph, dataset.suggestedTimeFactor)
				.withForce(new DyModularForce.TimeStraightning(delta))
				.withForce(new DyModularForce.Gravity())
				.withForce(new DyModularForce.ConnectionAttraction(delta))
				.withForce(new DyModularForce.EdgeRepulsion(delta))
				.withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
				.withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
				.withPreMovmement(new DyModularPreMovement.ForbidTimeShitfing());

		if (postProcessing != null) {
			builder.withPostProcessing(postProcessing);
		}

		return builder.build();
	}

	public MultiLevelDynNoSlice getMultiLevelDiscreteLayoutAlgorithm(DyGraph dyGraph, GraphCoarsener gc, MultilevelNodePlacementStrategy ps, ModularPostProcessing postProcessing) {
		MultiLevelDynNoSlice multiDyn = 
				new MultiLevelDynNoSlice(dyGraph, dataset.suggestedTimeFactor, Run.defaultDelta)
				.setCoarsener(gc) //WalshawIndependentSet
				.setPlacementStrategy(ps)
				.setFlattener(new DyGraphFlattener.StaticSumPresenceFlattener())
				.defaultLayoutParameters()
				.addLayerPreMovementDrawingOption(new MultiLevelDrawingOption<DyModularPreMovement>(new DyModularPreMovement.ForbidTimeShitfing()));
		//.addOption(MultiLevelDynNoSlice.LOG_OPTION, true).build();


		if (postProcessing != null) 
			multiDyn.addLayerPostProcessingDrawingOption(
					new MultiLevelDrawingOption<ModularPostProcessing>(postProcessing));

		multiDyn.build();    
		return multiDyn;
	}

	public MultiLevelDynNoSlice getMultiLevelContinuousLayoutAlgorithm(DyGraph dyGraph, GraphCoarsener gc, MultilevelNodePlacementStrategy ps, ModularPostProcessing postProcessing) {
		MultiLevelDynNoSlice multiDyn = 
				new MultiLevelDynNoSlice(dyGraph, dataset.suggestedTimeFactor, Run.defaultDelta)
				.setCoarsener(gc) 
				.setPlacementStrategy(ps)
				.setFlattener(new DyGraphFlattener.StaticSumPresenceFlattener())
				.addLayerPostProcessingDrawingOption(new MultiLevelDrawingOption.FlexibleTimeTrajectoriesPostProcessing(2))
				.addOption(MultiLevelDynNoSlice.LOG_OPTION, true)
				.defaultLayoutParameters();
		//.addOption(MultiLevelDynNoSlice.LOG_OPTION, true).build();


		if (postProcessing != null) 
			multiDyn.addLayerPostProcessingDrawingOption(
					new MultiLevelDrawingOption<ModularPostProcessing>(postProcessing));

		multiDyn.build();    
		return multiDyn;
	}

	/**
	 * Compute the metrics for the current experiment for VisOne.
	 * @param visoneTime
	 * @param runMultiDyn if to run the experiment for Multi-DynNoSlice as well. 
	 * @return
	 */
	public List<String> computeVisoneMetrics(String visoneTime) {
		List<String> lines = new ArrayList<>();

		DyGraph visoneGraph = null;
		DyGraph contVisone = null;

		try {
			List<Double> snapTimes = readSnapTimes(discretise());

			visoneGraph = exportImportVisone(directory);
			contVisone = getContinuousCopy();
			copyNodeLayoutFromTo(visoneGraph, contVisone);

			double visoneScaling = computeIdealScaling(visoneGraph, snapTimes);
			applyIdealScaling(visoneGraph, visoneScaling);
			lines.add(name + STAT_SEPARATOR + "v" + STAT_SEPARATOR + visoneTime + STAT_SEPARATOR + 1 / visoneScaling + STAT_SEPARATOR
					+ computeOtherMetrics(visoneGraph, contVisone, snapTimes, null));
		}catch (URISyntaxException uri) {
			System.err.println("Could not load graph!");
		}

		return lines;
	}

	//	public List<String> provideSnapshotSize(){
	//		List<String> lines = new ArrayList<>();
	//		DyGraph discGraph = discretise();  
	//		return lines;
	//	}

	//	public void probeLayout() {
	//		HashSet<String> methodologies = new HashSet<String>();
	//		methodologies.add("wi_id");
	//		methodologies.add("iset_grip");
	//		methodologies.add("sm_sp");
	//		for(String s : methodologies) {
	//			GraphCoarsener gc;
	//			MultilevelNodePlacementStrategy ps;
	//			if(s.equals("wi_id")) {
	//				System.out.println("Setting Walshaw IndependentSet and Identity Placement");
	//				gc = new IndependentSet.WalshawIndependentSet();
	//				ps = new MultilevelNodePlacementStrategy.IdentityNodePlacement();
	//			}else if(s.equals("iset_grip")) {
	//				System.out.println("Setting IndependentSet and GRIP Placement");        			
	//				gc = new IndependentSet();
	//				ps = new WeightedBarycenterPlacementStrategy();
	//			}else{
	//				System.out.println("Setting Solar Merger and Placer");        			        			
	//				gc = new SolarMerger();
	//				ps = new WeightedBarycenterPlacementStrategy.SolarMergerPlacementStrategy();
	//			}
	//			System.out.println("\t\tExecuting Continuous Multi-Level Algorithm");            
	//			MultiLevelDynNoSlice contMultiDyn = getMultiLevelContinuousLayoutAlgorithm(getContinuousCopy(), gc, ps, null);
	//			contMultiDyn.runMultiLevelLayout();
	//			dumpGraphSlices(contMultiDyn.getDrawnGraph(), 3);
	//		}
	//	}

	/**
	 * Compute the metrics for the current experiment for VisOne.
	 * @param visoneTime
	 * @param runMultiDyn if to run the experiment for Multi-DynNoSlice as well. 
	 * @return
	 */
	public List<String> computeDynNoSliceMetrics(boolean discrete) {
		List<String> lines = new ArrayList<>();

		DyGraph discGraph = discretise();        
		List<Double> snapTimes = readSnapTimes(discGraph);


		if(discrete) {
			System.out.println("\tExecuting Discrete DynNoSlice");
			DyModularFdl discAlgorithm = getDiscreteLayoutAlgorithm(discGraph, null);//, new ModularPostProcessing.DisplayCurrentIteration());
			SpaceTimeCubeSynchroniser discSyncro = discAlgorithm.getSyncro();
			callables.clear();
			callables.add(new Callable<ModularStatistics>() {
				public ModularStatistics call() throws Exception {
					return discAlgorithm.iterate(100);
				}
			});
			try {
				ExecutorService exec = Executors.newSingleThreadExecutor();
				ModularStatistics discStats = exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS);
				double discTime = computeRunningTime(discStats);
				double discreteScaling = computeIdealScaling(discGraph, snapTimes);
				applyIdealScaling(discSyncro, discreteScaling);
				DyGraph contDiscrete = getContinuousCopy();
				copyNodeLayoutFromTo(discGraph, contDiscrete);
				lines.add(name + STAT_SEPARATOR + "d" + STAT_SEPARATOR + discTime + STAT_SEPARATOR + 1 / discreteScaling + STAT_SEPARATOR
						+ computeOtherMetrics(discGraph, contDiscrete, snapTimes, discSyncro));
			}catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}catch (TimeoutException timeout) {
				System.out.println("Timeout reached!");
			}catch (URISyntaxException uri) {
				System.out.println("ERROR: Can't load graph!");
			}
		}

		System.out.println("\tExecuting Continuous DynNoSlice");
		DyGraph contGraph = dataset.dygraph;
		DyModularFdl contAlgorithm = getContinuousLayoutAlgorithm(contGraph, null);//, new ModularPostProcessing.DisplayCurrentIteration());
		SpaceTimeCubeSynchroniser contSyncro = contAlgorithm.getSyncro();
		callables.clear();
		callables.add(new Callable<ModularStatistics>() {
			public ModularStatistics call() throws Exception {
				return contAlgorithm.iterate(100);
			}
		});
		try {
			ExecutorService exec = Executors.newSingleThreadExecutor();
			ModularStatistics contStats = exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS);
			double contTime = computeRunningTime(contStats);       

			double continuousScaling = computeIdealScaling(contGraph, snapTimes);
			applyIdealScaling(contSyncro, continuousScaling);

			System.out.println("Applied scaling " + continuousScaling);

			//			MultiLevelCustomRun.animateGraphOnWindow(contGraph, dataset.suggestedInterval.leftBound(), dataset.suggestedInterval, name + " DynNoSlice");

			DyGraph discContinuous = discretise();
			copyNodeLayoutFromTo(contGraph, discContinuous);

			String line = name + STAT_SEPARATOR + "c" + STAT_SEPARATOR + contTime + STAT_SEPARATOR + 1 / continuousScaling + STAT_SEPARATOR
					+ computeOtherMetrics(discContinuous, contGraph, snapTimes, contSyncro);

			//		System.out.println(line);

			lines.add(line);  
		}catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}catch (TimeoutException timeout) {
			System.out.println("Timeout reached!");
		}

		return lines;
	}

	public List<String> computeSFDPMetrics() {
		System.out.println("\tExecuting SFDP flattened");

		List<String> lines = new ArrayList<>();

		try {
			DyGraph contGraph = dataset.dygraph;
			List<Double> snapTimes = readSnapTimes(discretise());

			StaticSumPresenceFlattener dyg = new StaticSumPresenceFlattener();
			Graph flattened = dyg.flattenDyGraph(contGraph);
			SfdpBuilder sfdp = new SfdpBuilder();
			SfdpExecutor sfdpInstance = sfdp.build();
			System.out.println("Flattened graph has " + flattened.nodeCount() + " nodes and " + flattened.edgeCount() + " edges");
			long epochStart = System.currentTimeMillis();
			sfdpInstance.execute(flattened);

			//		GMLOutputWriter.writeOutput(new File("C:\\Users\\Alessio Arleo\\Desktop\\"+name+"-sfdp" +".gml"), flattened);
			//		if(true)
			//			return lines;

			long epochEnd = System.currentTimeMillis();
			System.out.println("\tDone! Computing metrics...");
			DyGraph sfdpCont = getContinuousCopy();
			copyNodeLayoutFromTo(flattened, sfdpCont);

			//		MultiLevelCustomRun.showGraphOnWindow(sfdpCont, dataset.suggestedInterval.leftBound(), name + " SFDP");
			//		MultiLevelCustomRun.animateGraphOnWindow(sfdpCont, dataset.suggestedInterval.leftBound(), dataset.suggestedInterval, name + " SFDP");

			double sfdpScaling = computeIdealScaling(sfdpCont, snapTimes);
			applyIdealScaling(sfdpCont, sfdpScaling);

			System.out.println("Applied " + sfdpScaling);

			DyGraph sfdpDisc = discretise();
			copyNodeLayoutFromTo(sfdpCont, sfdpDisc);

			//		DyGraph sfdpContDisc = getContinuousCopy();
			//		copyNodeLayoutFromTo(sfdpCont, sfdpContDisc);

			String line = name + STAT_SEPARATOR + "sfdp" + STAT_SEPARATOR + (Duration.ofMillis(epochEnd - epochStart).toMillis() / 1000.0d) + STAT_SEPARATOR + 1 / sfdpScaling + STAT_SEPARATOR
					+ computeOtherMetrics(sfdpDisc, sfdpCont, snapTimes, null) + STAT_SEPARATOR + dataset.eventsProcessed;;

					//String lineD = name + "," + "sfdp" + "," + (Duration.ofMillis(epochEnd - epochStart).toMillis() / 1000.0d) + "," + 1 / sfdpScaling + ","
					//		+ computeOtherMetrics(sfdpCont, sfdpContDisc, snapTimes, null);

					//		System.out.println(lineD);

					lines.add(line);
					//		lines.add(lineD);

		}catch(URISyntaxException uri) {
			System.err.println("Can't load graph");
		}

		return lines;
	}


	/**
	 * Compute the metrics for the current experiment for MultiLevelDynNoSlice.
	 * @param discrete if to run the discrete experiment (for timesliced graph) as well. 
	 * @return
	 */
	public List<String> computeMultiLevelMetrics(boolean discrete) {
		List<String> lines = new ArrayList<>();
		List<Double> snapTimes = readSnapTimes(discretise());
		HashSet<String> methodologies = new HashSet<String>();
		methodologies.add("wi_id");
		methodologies.add("iset_grip");
		methodologies.add("sm_sp");
		for(String s : methodologies) {
			GraphCoarsener gc;
			MultilevelNodePlacementStrategy ps;
			if(s.equals("wi_id")) {
				System.out.println("Setting Walshaw IndependentSet and Identity Placement");
				gc = new IndependentSet.WalshawIndependentSet();
				ps = new MultilevelNodePlacementStrategy.IdentityNodePlacement();
			}else if(s.equals("iset_grip")) {
				System.out.println("Setting IndependentSet and GRIP Placement");        			
				gc = new IndependentSet();
				ps = new WeightedBarycenterPlacementStrategy();
			}else{
				System.out.println("Setting Solar Merger and Placer");        			        			
				gc = new SolarMerger();
				ps = new WeightedBarycenterPlacementStrategy.SolarMergerPlacementStrategy();
			}
			System.out.println("\t\tExecuting Continuous Multi-Level Algorithm");     
			
			try {			
				MultiLevelDynNoSlice contMultiDyn = getMultiLevelContinuousLayoutAlgorithm(getContinuousCopy(), gc, ps, null);
				callables.clear();
				callables.add(new Callable<ModularStatistics>() {
					public ModularStatistics call() throws Exception {
						contMultiDyn.runMultiLevelLayout();
						//contMultiDyn.runCoarsening();
						return contMultiDyn.getComputationStatistics();
					}
				});

				ExecutorService exec = Executors.newSingleThreadExecutor();
				ModularStatistics multiContStats = exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS);

				double multiContTime = multiContStats.getTotalRunningTime().toMillis()/1000.0d;

				System.out.println("total running time " + multiContTime);

				SpaceTimeCubeSynchroniser contMultiDynSyncro = contMultiDyn.getSyncro();	

				double multiContinuousScaling = computeIdealScaling(contMultiDyn.getDrawnGraph(), snapTimes);
				applyIdealScaling(contMultiDynSyncro, multiContinuousScaling);

				System.out.println("Applied scaling " + multiContinuousScaling);

				//				MultiLevelCustomRun.animateGraphOnWindow(contMultiDyn.getDrawnGraph(), dataset.suggestedInterval.leftBound(), dataset.suggestedInterval, name + " MultiDynNoSlice");				

				DyGraph multiDiscContinuous = discretise();
				copyNodeLayoutFromTo(contMultiDyn.getDrawnGraph(), multiDiscContinuous);	           

				String extraLines = stringifyMultiLevelMetrics(contMultiDyn.getComputationStatistics().getMetrics()) + STAT_SEPARATOR + dataset.eventsProcessed;
				System.out.println("\tDone! Computing metrics...");
				String line = name + ";" + "multic_" + s + ";" + multiContTime + STAT_SEPARATOR + 1 / multiContinuousScaling + STAT_SEPARATOR
						+ computeOtherMetrics(multiDiscContinuous, contMultiDyn.getDrawnGraph(), snapTimes, contMultiDynSyncro) + STAT_SEPARATOR + extraLines;

				//System.out.println(line);

				lines.add(line); 
			}catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}catch (TimeoutException timeout) {
				System.out.println("Timeout reached!");
			}catch (URISyntaxException uri) {
				System.out.println("Can't load graph");
			}

			//			MultiLevelCustomRun.showGraphOnWindow(contMultiDyn.getDrawnGraph(), dataset.suggestedInterval.leftBound(), name + " Multi-C");
			//			MultiLevelCustomRun.animateGraphOnWindow(contMultiDyn.getDrawnGraph(), dataset.suggestedInterval.leftBound(), dataset.suggestedInterval, name + " Multi-C");

			if(discrete) {
				System.out.println("\t\tExecuting Discrete Multi-Level Algorithm");
				MultiLevelDynNoSlice discMultiDyn = getMultiLevelDiscreteLayoutAlgorithm(discretise(), gc, ps, null);
				callables.clear();
				callables.add(new Callable<ModularStatistics>() {
					public ModularStatistics call() throws Exception {
						discMultiDyn.runMultiLevelLayout();
						return discMultiDyn.getComputationStatistics();
					}
				});
				try {
					ExecutorService exec = Executors.newSingleThreadExecutor();
					ModularStatistics multiDiscStats = exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS);
					System.out.println("\tDone! Computing metrics...");
					SpaceTimeCubeSynchroniser discMultiDynSyncro = discMultiDyn.getSyncro();
					double multiDiscTime = multiDiscStats.getTotalRunningTime().toMillis()/1000.0d;      

					double multiDiscreteScaling = computeIdealScaling(discMultiDyn.getDrawnGraph(), snapTimes);
					applyIdealScaling(discMultiDynSyncro, multiDiscreteScaling);

					DyGraph multiContDiscrete = getContinuousCopy();
					copyNodeLayoutFromTo(discMultiDyn.getDrawnGraph(), multiContDiscrete);

					String extraLines = stringifyMultiLevelMetrics(discMultiDyn.getComputationStatistics().getMetrics());

					String line = name + STAT_SEPARATOR + "multid_" + s + STAT_SEPARATOR + multiDiscTime + STAT_SEPARATOR + 1 / multiDiscreteScaling + STAT_SEPARATOR
							+ computeOtherMetrics(discMultiDyn.getDrawnGraph(), multiContDiscrete, snapTimes, discMultiDynSyncro) + STAT_SEPARATOR + extraLines;

					System.out.println(line);

					lines.add(line);
				}catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}catch (TimeoutException timeout) {
					System.out.println("Timeout reached!");
				}catch (URISyntaxException uri) {
					System.out.println("ERROR: Can't load graph!");
				}
			}
		}

		return lines;
	}

	private String stringifyMultiLevelMetrics(List<ModularMetric> list) {
		String coarseningTime = "0";
		int hierarchyDepth = 0;
		for(ModularMetric m : list) {
			if(m instanceof HierarchyDepth)
				hierarchyDepth = (int) m.values().get(0);
			else if (m instanceof CoarseningTime) {	            		
				coarseningTime = new DecimalFormat("#0.00").format((long)m.values().get(0)/Math.pow(10, 9));
			}
		}
		return hierarchyDepth + STAT_SEPARATOR + coarseningTime;
	}

	/**
	 * Computes the running times for the DyModularFDL algorithm.
	 *
	 * @param stats the algorithm statistics.
	 * @return the running time for the layout computation.
	 */
	public double computeRunningTime(ModularStatistics stats) {
		double time = 0;
		for (ModularMetric metric : stats.getMetrics()) {
			if (metric.metricName().equals("RunningTime")) {
				for (Object value : metric.values()) {
					if (value != null) {
						time += (Double) value;
					}
				}
			}
		}
		return time;
	}

	/**
	 * Computes the other metrics of interest.
	 *
	 * @param discGraph the graph with discrete edges.
	 * @param contGraph the graph with continuous edges.
	 * @param snapTimes the snapshot times.
	 * @param synchro the synchroniser.
	 * @return the metrics text.
	 */
	public String computeOtherMetrics(DyGraph discGraph, DyGraph contGraph, List<Double> snapTimes, SpaceTimeCubeSynchroniser synchro) {
		SpaceTimeCubeSynchroniser synchroniser = synchro != null ? synchro
				: new SpaceTimeCubeSynchroniser.StcsBuilder(
						discGraph, dataset.suggestedTimeFactor).build();

		//        DyQuickView view = new DyQuickView(discGraph, dataset.suggestedInterval.leftBound(), "Disc Graph");
		//        view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
		//        view.showNewWindow();
		//
		//        DyQuickView view2 = new DyQuickView(contGraph, dataset.suggestedInterval.leftBound(), "Cont Graph");
		//        view2.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
		//        view2.showNewWindow();
		//
		int slicesForOff = snapTimes.size() + (snapTimes.size() - 1) * 10;
		Interval interval = Interval.newClosed(snapTimes.get(0), snapTimes.get(snapTimes.size() - 1));

		DyGraphMetric<Double> stressOn = new DyGraphMetric.AverageSnapshotMetricCalculation(
				new GraphMetric.StressMetric.Builder().withScaling(delta).build(), interval, snapTimes.size());
		DyGraphMetric<Double> stressOff = new DyGraphMetric.AverageSnapshotMetricCalculation(
				new GraphMetric.StressMetric.Builder().withScaling(delta).build(), interval, slicesForOff);
		StcGraphMetric<Double> nodeMovement = new StcGraphMetric.AverageNodeMovement2D();
		StcGraphMetric<Integer> crowding = new StcGraphMetric.Crowding(dataset.suggestedInterval, 600);

		//		return /*stressOn.computeMetric(discGraph)*/ -1 + STAT_SEPARATOR + /*stressOff.computeMetric(discGraph)*/ -1 + STAT_SEPARATOR
		//		+ /*stressOn.computeMetric(contGraph)*/ -1 + STAT_SEPARATOR + stressOff.computeMetric(contGraph) + STAT_SEPARATOR
		//		+ nodeMovement.computeMetric(synchroniser) + STAT_SEPARATOR + crowding.computeMetric(synchroniser);

		return stressOn.computeMetric(discGraph)+ STAT_SEPARATOR + stressOff.computeMetric(discGraph) + STAT_SEPARATOR
				+ stressOn.computeMetric(contGraph) + STAT_SEPARATOR + stressOff.computeMetric(contGraph) + STAT_SEPARATOR
				+ nodeMovement.computeMetric(synchroniser) + STAT_SEPARATOR + crowding.computeMetric(synchroniser);

	}

	public double computeIdealScaling(DyGraph graph, List<Double> snapTimes) {
		Interval interval = Interval.newClosed(snapTimes.get(0), snapTimes.get(snapTimes.size() - 1));

		double bestScaling = 0;
		double bestStress = Double.POSITIVE_INFINITY;

		System.out.println("\tIterating to get best scaling");

		for (int i = -20; i <= 20; i++) {

			//System.out.println("\t\tIteration " + i);

			double scaling = Math.pow(1.1, i);
			DyGraphMetric<Double> stressMetric = new DyGraphMetric.AverageSnapshotMetricCalculation(
					new GraphMetric.StressMetric.Builder().withScaling(delta * scaling).build(),
					interval, snapTimes.size());
			double stress = stressMetric.computeMetric(graph);
			//System.out.println("\t\t\tComputed Stress: " + stress);
			if (stress < bestStress) {
				bestStress = stress;
				bestScaling = scaling;
			}
		}
		System.out.println("\tBest scaling: " + bestScaling);
		return bestScaling;
	}

	public void applyIdealScaling(DyGraph graph, double idealScaling) {
		DyNodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
		for (Node node : graph.nodes()) {
			Evolution<Coordinates> evolution = new Evolution<>(new Coordinates(0, 0));
			for (Function<Coordinates> function : positions.get(node)) {
				if (function instanceof FunctionConst) {
					evolution.insert(new FunctionConst<>(function.interval(),
							function.leftValue().divide(idealScaling)));
				} else if (function instanceof FunctionRect) {
					evolution.insert(new FunctionRect.Coordinates(function.interval(),
							function.leftValue().divide(idealScaling),
							function.rightValue().divide(idealScaling),
							((FunctionRect<Coordinates>) function).interpolation()));
				}
			}
			positions.set(node, evolution);
		}
	}

	public void applyIdealScaling(SpaceTimeCubeSynchroniser synchro, double idealScaling) {
		Graph mirror = synchro.mirrorGraph();
		NodeAttribute<Coordinates> positions = mirror.nodeAttribute(StdAttribute.nodePosition);
		for (Node node : mirror.nodes()) {
			Coordinates position = positions.get(node);
			position.setX(position.x() / idealScaling);
			position.setY(position.y() / idealScaling);
		}
		EdgeAttribute<ControlPoints> bends = mirror.edgeAttribute(StdAttribute.edgePoints);
		for (Edge edge : mirror.edges()) {
			for (Coordinates position : bends.get(edge)) {
				position.setX(position.x() / idealScaling);
				position.setY(position.y() / idealScaling);
			}
		}
		synchro.updateOriginal();
	}

	/**
	 * Handles the export and import of the visone data.
	 *
	 * @param directory the visone directory.
	 * @return the discrete graph with the layout computed by visone.
	 */
	public DyGraph exportImportVisone(String directory) throws URISyntaxException{
		DyGraph discreteGraph = discretise();
		//		exportVisone(directory, discreteGraph);
		importVisone(directory, discreteGraph);
		return discreteGraph;
	}

	/**
	 * Exports the graphs in a format that can be processed by visone.
	 *
	 * @param directory the visone directory.
	 * @param discreteGraph the original discrete graph.
	 */
	//	public void exportVisone(String directory, DyGraph discreteGraph) throws URISyntaxException{
	//		NodeMap map = new NodeMap(discreteGraph);
	//
	//		int sliceNumber = 0;
	//		for (Double time : readSnapTimes(discreteGraph)) {
	//			List<String> lines = new ArrayList<>();
	//
	//			Graph snapshot = discreteGraph.snapshotAt(time);
	//			for (String a : map) {
	//				String line = "";
	//				Node aNode = snapshot.hasNode(a) ? snapshot.getNode(a) : null;
	//				for (String b : map) {
	//					Node bNode = snapshot.hasNode(b) ? snapshot.getNode(b) : null;
	//					if (aNode == null || bNode == null || aNode == bNode
	//							|| snapshot.betweenEdge(aNode, bNode) == null) {
	//						line += " 0";
	//					} else {
	//						line += " 1";
	//					}
	//				}
	//				lines.add(line);
	//			}
	//
	//			File dir = new File(Experiment.class.getResource(directory + "visoneIn/").toURI());
	//			try {
	//				dir.mkdir();
	//			} catch (SecurityException se) {
	//				throw new IllegalStateException("Cannot create the directory.");
	//			}
	//			File file = new File(directory + "visoneIn/" + name + String.format("%03d", sliceNumber) + ".csv");
	//			ParserTools.writeFileLines(lines, file);
	//			sliceNumber++;
	//		}
	//	}

	/**
	 * Imports the layout computed by visone and applies it to the given graph.
	 *
	 * @param directory the visone directory.
	 * @param discreteGraph the graph that will receive the visone layout.
	 */
	public void importVisone(String directory, DyGraph discreteGraph) throws URISyntaxException{
		NodeMap map = new NodeMap(discreteGraph);
		List<Double> snapTimes = readSnapTimes(discreteGraph);
		List<Map<Node, Coordinates>> nodePositions = readNodePositions(directory, discreteGraph, map);

		DyNodeAttribute<Coordinates> positions = discreteGraph.nodeAttribute(StdAttribute.nodePosition);
		for (Node node : discreteGraph.nodes()) {
			Evolution<Coordinates> evolution = new Evolution<>(new Coordinates(0, 0));
			evolution.insert(new FunctionConst<>(
					Interval.newRightClosed(Double.NEGATIVE_INFINITY, snapTimes.get(0)),
					nodePositions.get(0).get(node)));
			for (int i = 0; i < snapTimes.size() - 1; i++) {
				evolution.insert(new FunctionRect.Coordinates(
						Interval.newRightClosed(snapTimes.get(i), snapTimes.get(i + 1)),
						nodePositions.get(i).get(node),
						nodePositions.get(i + 1).get(node),
						Interpolation.Std.linear));
			}
			evolution.insert(new FunctionConst<>(
					Interval.newOpen(snapTimes.get(snapTimes.size() - 1), Double.POSITIVE_INFINITY),
					nodePositions.get(snapTimes.size() - 1).get(node)));
			positions.set(node, evolution);
		}
	}

	/**
	 * Reads the suggested snapshot times.
	 *
	 * @param discreteGraph the dynamic graph.
	 * @return the list of snapshot times.
	 */
	private List<Double> readSnapTimes(DyGraph discreteGraph) {
		String snapString = discreteGraph.<String>graphAttribute("SnapTimes").get().getDefaultValue();
		List<Double> snapTimes = new ArrayList<>();
		for (String token : snapString.split(",")) {
			snapTimes.add(Double.parseDouble(token));
		}
		return snapTimes;
	}

	private List<Map<Node, Coordinates>> readNodePositions(String directory, DyGraph discreteGraph, NodeMap map) throws URISyntaxException{
		List<Map<Node, Coordinates>> nodePositions = new ArrayList<>();

		for (File fileEntry : new File(Experiment.class.getResource(directory + "/visoneAfter").toURI()).listFiles()) {

			int sliceNumber = Integer.parseInt(fileEntry.getName().replaceAll("[^\\d]", ""));
			while (nodePositions.size() <= sliceNumber) {
				nodePositions.add(new HashMap<>());
			}

			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fileEntry);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("node");

				for (int i = 0; i < nList.getLength(); i++) {
					org.w3c.dom.Node nNode = nList.item(i);
					Element eElement = (Element) nNode;
					int nodeIndex = Integer.parseInt(eElement.getAttribute("id").replace("n", ""));
					org.w3c.dom.Node nDataD2 = eElement.getElementsByTagName("data").item(2);
					Element dataD2 = (Element) nDataD2;
					assert (dataD2.getAttribute("key").equals("d2")) : "Format mismatch on data d2";
					Element vShapeNode = (Element) dataD2.getElementsByTagName("visone:shapeNode").item(0);
					Element yShapeNode = (Element) vShapeNode.getElementsByTagName("y:ShapeNode").item(0);
					Element geom = (Element) yShapeNode.getElementsByTagName("y:Geometry").item(0);
					double x = Double.parseDouble(geom.getAttribute("x")) / 40.0;
					double y = -Double.parseDouble(geom.getAttribute("y")) / 40.0;
					Coordinates position = new Coordinates(x, y);

					Node node = discreteGraph.getNode(map.get(nodeIndex));
					nodePositions.get(sliceNumber).put(node, position);
				}

			} catch (SAXException | IOException | ParserConfigurationException ex) {
				throw new IllegalStateException("Cannot read the xml file.");
			}
		}
		return nodePositions;
	}

	private void copyNodeLayoutFromTo(DyGraph source, DyGraph target) {
		DyNodeAttribute<Coordinates> sourcePositions = source.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Coordinates> targetPositions = target.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Boolean> sourcePresences = source.nodeAttribute(StdAttribute.dyPresence);
		DyNodeAttribute<Boolean> targetPresences = target.nodeAttribute(StdAttribute.dyPresence);

		for (Node node : source.nodes()) {

			Evolution<Coordinates> newEvolution = new Evolution<>(new Coordinates(0, 0));
			for (Function<Coordinates> function : sourcePositions.get(node)) {
				newEvolution.insert(function);
			}
			targetPositions.set(target.getNode(node.id()), newEvolution);

			Evolution<Boolean> newPresence = new Evolution<>(false);
			for (Function<Boolean> function : sourcePresences.get(node)) {
				newPresence.insert(function);
			}
			targetPresences.set(target.getNode(node.id()), newPresence);
		}
	}

	private void copyNodeLayoutFromTo(Graph source, DyGraph target) {
		NodeAttribute<Coordinates> sourcePositions = source.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Coordinates> targetPositions = target.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Boolean> targetPresences = target.nodeAttribute(StdAttribute.dyPresence);

		for (Node node : source.nodes()) {
			Coordinates coords = sourcePositions.get(node);
			Evolution<Coordinates> newEvolution = new Evolution<>(new Coordinates(coords.x(), coords.y()));
			targetPositions.set(target.getNode(node.id()), newEvolution);

			Evolution<Boolean> newPresence = new Evolution<>(true);
			targetPresences.set(target.getNode(node.id()), newPresence);
		}
	}

	/**
	 * Map that keep the correspondence between nodes in the ocotillo library
	 * and the visone one.
	 */
	private static class NodeMap implements Iterable<String> {

		private final List<String> list = new ArrayList<>();
		private final Map<String, Integer> map = new HashMap<>();

		public NodeMap(DyGraph graph) {
			for (Node node : graph.nodes()) {
				list.add(node.id());
			}
			Collections.sort(list);
			int i = 0;
			for (String string : list) {
				map.put(string, i);
				i++;
			}
		}

		public String get(int index) {
			return list.get(index);
		}

		public int get(String label) {
			return map.get(label);
		}

		@Override
		public Iterator<String> iterator() {
			return list.iterator();
		}
	}

	/**
	 * Creates the discrete version of the dataset.
	 *
	 * @return the discrete dynamic graph.
	 */
	public abstract DyGraph discretise();

	/**
	 * Gets a copy of the continuous graph.
	 *
	 * @return a copy of the continuous graph.
	 */
	public DyGraph getContinuousCopy() throws URISyntaxException{
		return parserInstance.parse(loadMode).dygraph;
	}

	/**
	 * Experiment with the InfoVis dataset.
	 */
	public static class InfoVis extends Experiment {

		public InfoVis() throws Exception{
			super("InfoVis", "data/InfoVis_citations/", InfoVisCitations.class, Commons.Mode.plain, 5.0);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			for (int i = 1995; i <= 2015; i++) {
				snapshotTimes.add((double) i);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return InfoVisCitations.parse(Commons.Mode.plain).dygraph;
		//		}

	}

	/**
	 * Experiment with the Rugby dataset.
	 */
	public static class Rugby extends Experiment {

		public Rugby() throws Exception{
			super("Rugby", "data/Rugby_tweets/", RugbyTweets.class, Commons.Mode.keepAppearedNode, 5.0d);			
			//			super("Rugby", "data/Rugby_tweets/", RugbyTweets.parse(Commons.Mode.keepAppearedNode), 5);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = 20;
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return RugbyTweets.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}

	}

	/**
	 * Experiment with the Pride and Prejudice dataset.
	 */
	public static class Pride extends Experiment {

		public Pride() throws Exception{
			super("Pride", "data/DialogSequences/Pride_and_Prejudice/", DialogSequences.class, Commons.Mode.keepAppearedNode, 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = (int) dataset.suggestedInterval.width();
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return DialogSequences.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}

	}

	/**
	 * Experiment with the van de Bunt dataset.
	 */
	public static class Bunt extends Experiment {

		public Bunt() throws Exception {
			super("VanDeBunt", "data/van_De_Bunt/", VanDeBunt.class, Commons.Mode.keepAppearedNode, 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			for (int i = 0; i <= 6; i++) {
				snapshotTimes.add((double) i);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return VanDeBunt.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}

	/**
	 * Experiment with the Newcomb fraternity dataset.
	 */
	public static class Newcomb extends Experiment {

		public Newcomb() throws Exception {
			super("Newcomb", "data/Newcomb/", NewcombFraternity.class, Commons.Mode.keepAppearedNode, 5);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			for (int i = 1; i <= 15; i++) {
				snapshotTimes.add((double) i);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return NewcombFraternity.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}

	/**
	 * Experiment with the College dataset.
	 */
	public static class College extends Experiment {

		public College() throws Exception {
			super("CollegeMsg", "data/CollegeMsg/", CollegeMsg.class, Commons.Mode.keepAppearedNode, 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = 20;
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return CollegeMsg.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}

	/**
	 * Experiment with the BitcoinAlpha dataset.
	 */
	public static class BitAlpha extends Experiment {

		public BitAlpha() throws Exception {
			super("BitcoinAlpha", "data/BitcoinAlpha/", BitcoinAlpha.class, Commons.Mode.keepAppearedNode, 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = 20;
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return BitcoinAlpha.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}

	/**
	 * Experiment with the BitcoinOTC dataset.
	 */
	public static class BitOTC extends Experiment {

		public BitOTC() throws Exception {
			super("BitcoinOTC", "data/BitcoinOTC/", BitcoinOTC.class, Commons.Mode.keepAppearedNode, 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = 20;
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return BitcoinOTC.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}

	/**
	 * Experiment with the Reality Mining dataset.
	 */
	public static class RealMining extends Experiment {

		public RealMining() throws Exception{
			super("Reality Mining", "data/RealityMining/", RealityMining.class, Commons.Mode.keepAppearedNode , 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = 20;
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return RealityMining.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}


	/**
	 * Experiment with the MOOC dataset.
	 */
	public static class MOOC extends Experiment {

		public MOOC() throws Exception {
			super("MOOC", "data/act-mooc/", Mooc.class, Commons.Mode.keepAppearedNode, 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = 20;
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return Mooc.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}

	/**
	 * Experiment with the MOOC dataset.
	 */
	public static class RampInfectionMap extends Experiment {

		public RampInfectionMap() throws Exception {
			super("RampInfectionMap", "data/RampInfectionMap/", ocotillo.samples.parsers.RampInfectionMap.class, Commons.Mode.keepAppearedNode, 5.0d);
		}

		@Override
		public DyGraph discretise() {
			List<Double> snapshotTimes = new ArrayList<>();
			int slices = 20;
			double gap = dataset.suggestedInterval.width() / slices;
			for (int i = 0; i < slices; i++) {
				double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
				snapshotTimes.add(snapTime);
			}
			return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
		}

		//		@Override
		//		public DyGraph getContinuousCopy() {
		//			return ocotillo.samples.parsers.RampInfectionMap.parse(Commons.Mode.keepAppearedNode).dygraph;
		//		}
	}

	protected void dumpGraphSlices(DyGraph drawnGraph, int snaps) {
		DyGraph discretizedGraph = discretise();
		List<Double> snapTimes = readSnapTimes(discretizedGraph);
		System.out.println("Dumping Slices");
		int lastSnap = 0;
		int step = Double.valueOf(Math.floor(snapTimes.size()/snaps)).intValue();
		boolean stoppingCondition = true;
		int needle = step;
		while(stoppingCondition) {    		
			if(needle >= snapTimes.size()) {
				stoppingCondition = false;
				needle = snapTimes.size() - 1;
			}    		
			System.out.println("Start " + lastSnap + " end " + needle);
			Graph snipGraph = DyGraphDiscretiser.flattenWithinInterval(drawnGraph, Interval.newClosed(snapTimes.get(lastSnap), snapTimes.get(needle)));
			GMLOutputWriter.writeOutput(new File("C:\\Users\\Alessio Arleo\\Desktop\\"+name+"_slices" + lastSnap + "-" + needle + ".gml"), snipGraph);
			lastSnap = needle;
			needle += step;
		}	
	}

}
