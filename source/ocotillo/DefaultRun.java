/**
 * Copyright © 2020 Alessio Arleo
 * Copyright © 2014-2017 Paolo Simonetto
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

package ocotillo;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.extra.DyGraphDiscretiser;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl.DyModularFdlBuilder;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing;
import ocotillo.dygraph.layout.fdl.modular.DyModularPreMovement;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.geometry.Geom;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.run.DynNoSliceRun;
import ocotillo.run.MultiDynNoSliceRun;
import ocotillo.run.Run;
import ocotillo.run.Run.AvailableDrawingOption;
import ocotillo.run.SFDPRun;
import ocotillo.samples.DyGraphSamples;
import ocotillo.samples.parsers.BitcoinAlpha;
import ocotillo.samples.parsers.BitcoinOTC;
import ocotillo.samples.parsers.CollegeMsg;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;
import ocotillo.samples.parsers.DialogSequences;
import ocotillo.samples.parsers.InfoVisCitations;
import ocotillo.samples.parsers.Mooc;
import ocotillo.samples.parsers.NewcombFraternity;
import ocotillo.samples.parsers.RampInfectionMap;
import ocotillo.samples.parsers.RealityMining;
import ocotillo.samples.parsers.RugbyTweets;
import ocotillo.samples.parsers.VanDeBunt;
import ocotillo.serialization.ParserTools;

/**
 * Default code for run target.
 */
public class DefaultRun {

	protected static HashMap<String, Integer> preloadedGraphs;

	public static class CMDLineOption{

		public final String readableName;
		public final String argument;
		public final String description;

		public CMDLineOption(String readableName, String argument, String description) {
			this.readableName = readableName;
			this.argument = argument;
			this.description = description;			
		}

		public String toString() {
			return readableName + "\t\t" + argument + "\t\t" + description;
		}

	}

	private enum AvailableMode {

		animate,
		showcube,
		computeMetrics,
		help;

		public static void printHelp() {

			for(AvailableMode m : AvailableMode.values()) {
				if(m != null)
					System.out.println(AvailableMode.toString(m));
			}

		}

		public static CMDLineOption toString(AvailableMode option) {

			switch(option) {
			case help: return new CMDLineOption("Help", "help", "Displays global help message.");
			case animate: 
				return new CMDLineOption("Animate", "animate", "Provides a 30 seconds animation of the layout.");
			case showcube: 
				return new CMDLineOption("Cube", "showcube", "Shows the trajectories of the nodes in a space time cube.");    			
			case computeMetrics: 
				return new CMDLineOption("Metrics", "metrics", "Computes the experiment metrics reported in the paper. Add \"--help\" argument for help on experiment settings.");
			default: return null;
			}
		}

		public static AvailableMode parse(String s) {

			switch(s) {
			case "help": return AvailableMode.help;
			case "animate": 
				return AvailableMode.animate;
			case "showcube": 
				return AvailableMode.showcube;    			
			case "metrics": 
				return AvailableMode.computeMetrics;
			default: return null;			}

		}

	}

	private enum AvailableDataset {

		infovis,
		rugby,
		pride,
		vandebunt,
		newcomb,
		mooc,
		bitalpha,
		bitotc,
		reality,
		college,
		ramp;

		public static void printHelp() {

			System.out.println("The available datasets are the following:");
			for(AvailableDataset m : AvailableDataset.values())
				if(m != null)
					System.out.print(m + " ");
		}
	}

	private enum AvailableMethods{
		help,
		dynnos,
		sfdp,
		multi;

		public static void printHelp() {

			for(AvailableMethods m : AvailableMethods.values()) {
				if(m != null)
					System.out.println(AvailableMethods.toString(m));
			}

		}


		public static CMDLineOption toString(AvailableMethods option) {

			switch(option) {
			case help: return new CMDLineOption("Help", "help", "Shows help message for drawing methods");						
			case dynnos: 
				return new CMDLineOption("DynNoSlice", "single", "Uses DynNoSlice for the drawing");			
			case multi: 
				return new CMDLineOption("MultiDynNoSlice", "multi", "Uses MultiDynNoSlice for the drawing");		
			case sfdp: 
				return new CMDLineOption("SFDP", "sfdp", "Uses SFDP for the drawing");					
			default: return null;
			}
		}

		public static AvailableMethods parse(String s) {

			switch(s) {
			case "help": return AvailableMethods.help;
			case "single": 
				return AvailableMethods.dynnos;
			case "multi": 
				return AvailableMethods.multi;    
			case "sfdp":
				return AvailableMethods.sfdp;
			default: return null;			
			}			
		}
	}

	private enum MetricsCalculationOptions{
		smaller,
		larger,
		multi,
		single,
		visone,		
		sfdp,
		help,
		verbose,
		output;

		public static void printHelp() {

			for(MetricsCalculationOptions m : MetricsCalculationOptions.values()) {
				if(m != null)
					System.out.println(MetricsCalculationOptions.toString(m));
			}

		}

		public static CMDLineOption toString(MetricsCalculationOptions option) {

			switch(option) {
			case help: return new CMDLineOption("Show help", "--help", "Displays experiment help message.");			
			case smaller: 
				return new CMDLineOption("Smaller graphs", "--smaller", "Executes the experiment on the smaller graphs");
			case larger: 
				return new CMDLineOption("Larger graphs", "--larger", "Executes the experiment on the larger graphs");
			case visone: 
				return new CMDLineOption("Visone", "--visone", "Computes metrics for stored Visone graphs");    			
			case multi: 
				return new CMDLineOption("MultiDynNoS", "--multi", "Executes the experiment using MultiDynNoS");    			
			case single: 
				return new CMDLineOption("DynNoS", "--single", "Executes the experiment using DynNoS");    			
			case sfdp: 
				return new CMDLineOption("SFDP", "--sfdp", "Flattens graphs and executes the experiment using SFDP"); 
			case output:
				return new CMDLineOption("Output", "--out", "The path where to save the statistics file");		
			case verbose:
				return new CMDLineOption("Verbose", "--verbose", "Extra output on console during computation");	
			default: return null;
			}
		}

		public static MetricsCalculationOptions parseMode(String s) {

			switch(s) {
			case "smaller": 
				return MetricsCalculationOptions.smaller;
			case "larger": 
				return MetricsCalculationOptions.larger;
			case "visone": 
				return MetricsCalculationOptions.visone;  				
			case "multi": 
				return MetricsCalculationOptions.multi;    			
			case "single": 
				return MetricsCalculationOptions.single;    			
			case "sfdp": 
				return MetricsCalculationOptions.sfdp;	
			case "out":
				return MetricsCalculationOptions.output;
			case "verbose":
				return MetricsCalculationOptions.verbose;
			default: return null;			
			}			
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("awt.useSystemAAFontSettings", "lcd");
		System.setProperty("swing.aatext", "true");

		System.out.println("MultiDynNoSlyce Demo");

		if (args.length == 0) {
			showHelp();
			return;
		} 

		AvailableMode selectedMode = null;

		selectedMode = AvailableMode.parse(args[0]);

		if (selectedMode == null) {
			System.out.println("Mode " + args[0] + " not available.\n");
			showHelp();
			return;
		}

		switch(selectedMode) {
		case help: showHelp(); return; 
		default: {

			preloadedGraphs = new HashMap<String, Integer>();
			preloadedGraphs.put("vandebunt", 0);
			preloadedGraphs.put("newcomb", 1);
			preloadedGraphs.put("infovis", 2);
			preloadedGraphs.put("rugby", 3);
			preloadedGraphs.put("pride", 4);
			preloadedGraphs.put("college", 5);
			preloadedGraphs.put("reality", 6);
			preloadedGraphs.put("bitalpha", 7);
			preloadedGraphs.put("bitotc", 8);
			preloadedGraphs.put("mooc", 9);
			preloadedGraphs.put("ramp", 10);

			DyDataSet data = null;
			String selectedGraph = args[1];
			boolean customGraph = false;

			if(preloadedGraphs.containsKey(selectedGraph)) {
				try {
					switch(preloadedGraphs.get(selectedGraph)) {
					case 0: 
						data = new VanDeBunt().parse(Mode.keepAppearedNode); break;
					case 1: 
						data = new NewcombFraternity().parse(Mode.keepAppearedNode); break;
					case 2: 
						data = new InfoVisCitations().parse(Mode.plain); break;
					case 3: data = new RugbyTweets().parse(Mode.keepAppearedNode); break;
					case 4: data = new DialogSequences().parse(Mode.keepAppearedNode); break;
					case 5: data = new CollegeMsg().parse(Mode.keepAppearedNode); break;
					case 6: data = new RealityMining().parse(Mode.keepAppearedNode); break;
					case 7: data = new BitcoinAlpha().parse(Mode.keepAppearedNode); break;
					case 8: data = new BitcoinOTC().parse(Mode.keepAppearedNode); break;
					case 9: data = new Mooc().parse(Mode.keepAppearedNode); break;
					case 10: data = new RampInfectionMap().parse(Mode.keepAppearedNode); break;
					default: break;
					}
				}catch (Exception e) {
					System.out.println("Can't load preloaded graph: " + e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			}else if(!selectedGraph.equals("custom")) {
				System.err.println("Graph Dataset not found!"); 
				showHelp();
				System.exit(0);				
			} else
				customGraph = true;

			Run drawingAlgorithm;

			AvailableMethods selectedMethod = AvailableMethods.parse(customGraph ? args[4] : args[2]);

			switch(selectedMethod) {
			case dynnos: drawingAlgorithm = new DynNoSliceRun(args, data); break;
			case sfdp: drawingAlgorithm = new SFDPRun(args, data); break;
			default: drawingAlgorithm = new MultiDynNoSliceRun(args, data); break;
			}

			drawingAlgorithm.computeDrawing();

			switch(selectedMode) {
			case animate: drawingAlgorithm.animateGraph(); break;
			default: drawingAlgorithm.plotSpaceTimeCube(); break;			
			}

			drawingAlgorithm.saveOutput();

			break;

		}
		case computeMetrics: {
			List<String> lines = new ArrayList<>();
			String outputFolder = System.getProperty("user.dir");
			Boolean executeMulti = false;
			Boolean executeSFDP = false;
			Boolean executeSingle = false;
			Boolean executeVisone = false;
			Boolean verbose = false;
			Boolean plotSliceSize = false;
			//			Boolean dumpSlicesPicture = true;

			HashSet<String> expNames = new HashSet<String>();
			HashSet<String> smallerDatasets = new HashSet<String>();
//			smallerDatasets.add("Bunt");
//			smallerDatasets.add("Newcomb");
//			smallerDatasets.add("InfoVis");
			smallerDatasets.add("Rugby");
//			smallerDatasets.add("Pride");

			HashSet<String> largerDatasets = new HashSet<String>();
			largerDatasets.add("RealMining");
			largerDatasets.add("BitOTC");
			largerDatasets.add("MOOC");
			largerDatasets.add("BitAlpha");  
			largerDatasets.add("College");
			largerDatasets.add("RampInfectionMap");

			HashMap<String, String> visoneTimes = new HashMap<String, String>();

			HashSet<String> discreteExperiment = new HashSet<String>();
			discreteExperiment.add("Bunt");
			discreteExperiment.add("Newcomb");
			discreteExperiment.add("InfoVis");

			for(int i = 1; i < args.length; i++) {
				switch(MetricsCalculationOptions.parseMode(args[i].split("--")[1])) {
				case smaller: expNames.addAll(smallerDatasets); break;
				case larger: expNames.addAll(largerDatasets); break;
				case single: {
					if(lines.isEmpty())
						lines.add("Graph;Type;Time;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");					
					executeSingle = true; break;
				}
				case multi: {
					if(lines.isEmpty())
						lines.add("Graph;Type;Time;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");					
					executeMulti = true; break;
				}
				case sfdp: {
					if(lines.isEmpty())
						lines.add("Graph;Type;Time;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");					
					executeSFDP = true; break;
				}
				case verbose: verbose = true; break;
				//				case discrete: {
				//					discreteExperiment.add("Bunt");
				//					discreteExperiment.add("Newcomb");
				//					discreteExperiment.add("InfoVis");
				//					//                    	discreteExperiment.add("Rugby");
				//					//                    	discreteExperiment.add("Pride");     
				//					break;
				//				}
				case output: {
					if(i+1 < args.length) {
						i++;
						outputFolder = args[i];
					}
					break;
				} 
				case visone: {					
					executeVisone = true;
					visoneTimes.put("Bunt", "0.128");
					visoneTimes.put("Newcomb", "0.109");
					visoneTimes.put("InfoVis", "77.430");
					visoneTimes.put("Rugby", "0.079");
					visoneTimes.put("Pride", "3.391");
					if(lines.isEmpty())
						lines.add("Graph;Type;Time;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");										
					break;
				}
				default: break;
				//                	else if(args[i].equals("--dump"))
				//						dumpSlicesPicture = true;


				}               
			}
				LocalDateTime ld = LocalDateTime.now();
				String date = ld.format(DateTimeFormatter.BASIC_ISO_DATE);
				String time = ld.format(DateTimeFormatter.ISO_LOCAL_TIME);
				time = time.replace(':', '-');

				String fileName = "Experiment_" + date + "_" + time + (executeMulti ? "_wMulti" : "") + "_data.csv";

				for(String graphName : expNames) {
					System.out.println("\n### Starting " + graphName + " Experiment ###");
					if(executeVisone && visoneTimes.containsKey(graphName)) {
						lines.addAll(
								((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor().newInstance()).computeVisoneMetrics(visoneTimes.get(graphName))
								);
					}
					if(executeSingle) {
						lines.addAll(
								((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor().newInstance()).computeDynNoSliceMetrics(discreteExperiment.contains(graphName))
								);                		
					}if(executeMulti) {
						lines.addAll(
								((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor().newInstance()).computeMultiLevelMetrics(discreteExperiment.contains(graphName), verbose)
								);                		
					}
					if(executeSFDP) {
						lines.addAll(
								((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor().newInstance()).computeSFDPMetrics()
								);                		
					}               	

				}

				//			for (String line : lines) {
				//				System.out.println(line);
				//			}

				if(outputFolder.charAt(outputFolder.length() - 1) != File.separatorChar)
					outputFolder += File.separator; 

				ParserTools.writeFileLines(lines,
						new File(outputFolder + fileName));

				System.out.println("\n##### Experiments complete! #####");

				System.exit(0);
				return;
			}

		}
		
	}

	private static void showHelp() {
		System.out.println("This software is distributed as demo of the approach detailed at: ");
		System.out.println("Contains DynNoSlice software by Paolo Simonetto et al.: http://cs.swan.ac.uk/~dynnoslice/software.html ");//
		System.out.println("");
		System.out.println("General Usage: <Mode> <Dataset> <Layout Method> [OPTIONS]");
		System.out.println("Experiment mode: metrics [OPTIONS]");
		System.out.println("Modes and layout methods may have other specific options.");
		System.out.println("\nAvailable Modes:");
		System.out.println("#NAME\t#COMMAND\t#DESCRIPTION");	
		AvailableMode.printHelp();
		System.out.println("");

		System.out.println("In custom, animate, and showcube modes the following options are available:");
		System.out.println("#NAME\t#OPTION\t#DESCRIPTION");	
		AvailableDrawingOption.printHelp();
		System.out.println("");

		System.out.println("\nAvailable Layout Methods:");
		System.out.println("#NAME\t#COMMAND\t#DESCRIPTION");	
		AvailableMethods.printHelp();
		System.out.println("");

		System.out.println("\nAvailable Datasets:");	
		AvailableDataset.printHelp();
		System.out.println("");

		System.out.println("The user can specify a dataset using the custom mode.");
		Run.showHelp();
		System.out.println("");		

		System.out.println("\nExperiment Options:");
		System.out.println("The following options define how to perform the experiment.");				
		System.out.println("#NAME\t#OPTION\t#DESCRIPTION");	
		MetricsCalculationOptions.printHelp();		
	}

	public static void discretisationTest() {
		DyDataSet dataset = DyGraphSamples.discretisationExample();

		DyQuickView initialView = new DyQuickView(dataset.dygraph, dataset.suggestedInterval.leftBound());
		initialView.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(10)));
		initialView.showNewWindow();

		List<Double> snapshotTimes = new ArrayList<>();
		snapshotTimes.add(25.0);
		snapshotTimes.add(50.0);
		snapshotTimes.add(75.0);
		snapshotTimes.add(100.0);
		DyGraph discreteGraph = DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes);

		DyModularFdl discreteAlgorithm = new DyModularFdlBuilder(discreteGraph, dataset.suggestedTimeFactor)
				.withForce(new DyModularForce.TimeStraightning(5))
				.withForce(new DyModularForce.Gravity())
				.withForce(new DyModularForce.MentalMapPreservation(2))
				.withForce(new DyModularForce.ConnectionAttraction(5))
				.withForce(new DyModularForce.EdgeRepulsion(5))
				.withConstraint(new ModularConstraint.DecreasingMaxMovement(10))
				.withConstraint(new ModularConstraint.MovementAcceleration(10, Geom.e3D))
				.withPreMovmement(new DyModularPreMovement.ForbidTimeShitfing())
				.build();

		discreteAlgorithm.iterate(100);

		DyQuickView discreteView = new DyQuickView(discreteGraph, dataset.suggestedInterval.leftBound());
		discreteView.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(10)));
		discreteView.showNewWindow();

		DyModularFdl algorithm = new DyModularFdlBuilder(dataset.dygraph, dataset.suggestedTimeFactor)
				.withForce(new DyModularForce.TimeStraightning(5))
				.withForce(new DyModularForce.Gravity())
				.withForce(new DyModularForce.MentalMapPreservation(2))
				.withForce(new DyModularForce.ConnectionAttraction(5))
				.withForce(new DyModularForce.EdgeRepulsion(5))
				.withConstraint(new ModularConstraint.DecreasingMaxMovement(10))
				.withConstraint(new ModularConstraint.MovementAcceleration(10, Geom.e3D))
				.withPostProcessing(new DyModularPostProcessing.FlexibleTimeTrajectories(7, 8))
				.build();

		algorithm.iterate(100);

		DyQuickView view = new DyQuickView(dataset.dygraph, dataset.suggestedInterval.leftBound());
		view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(10)));
		view.showNewWindow();
	}
}
