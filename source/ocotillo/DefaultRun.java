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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ocotillo.DefaultRun.MetricsCalculationOptions;
import ocotillo.multilevel.logger.Logger;
import ocotillo.run.DynNoSliceRun;
import ocotillo.run.MultiDynNoSliceRun;
import ocotillo.run.Run;
import ocotillo.run.Run.AvailableDrawingOption;
import ocotillo.run.SFDPRun;
import ocotillo.samples.parsers.BitcoinAlpha;
import ocotillo.samples.parsers.BitcoinOTC;
import ocotillo.samples.parsers.CollegeMsg;
import ocotillo.samples.parsers.Commons;
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

	public static final Commons.Mode DEFAULT_GRAPHLOADING_MODE = Mode.plain;

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
		plotSlices,
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
			case plotSlices: 
				return new CMDLineOption("PlotSlices", "dump", "Dumps 4 GMLs showing different slices of animation.");						
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
			case "dump": 
				return AvailableMode.plotSlices;		
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
		//bitalpha,
		//bitotc,
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

	public static enum MetricsCalculationOptions{
		smaller,
		larger,
		multi,
		single,
		visone,		
		sfdp,
		manualTau, 
		bendTransfer,		
		help,
		verbose,
		output;
		//, vanillaTuning;

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
			case manualTau:
				return new CMDLineOption("Dataset Tau (MultiDynNoS only)", "--manualTau", "Use the time factor suggested in the dataset code (if available)");
			case bendTransfer:	
				return new CMDLineOption("Bend Transfer (MultiDynNoS only)", "--bT", "Enables Bend Transfer (default Disabled).");
//			case vanillaTuning:	
//				return new CMDLineOption("Use Vanilla Tuning (MultiDynNoS only)", "--vT", "Sets layout tuning to vanilla MultiDynNoS.");				
			default: return null;
			}
		}

		public static MetricsCalculationOptions parseMode(String s) {

			switch(s) {
			case "smaller": 
				return smaller;
			case "larger": 
				return larger;
			case "visone": 
				return visone;  				
			case "multi": 
				return multi;    			
			case "single": 
				return single;    			
			case "sfdp": 
				return sfdp;	
			case "out":
				return output;
			case "verbose":
				return verbose;
			case "manualTau":
				return manualTau;	
			case "bT": return bendTransfer;
			//case "vT": return vanillaTuning;				
			default: return null;			
			}			
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("awt.useSystemAAFontSettings", "lcd");
		System.setProperty("swing.aatext", "true");

		System.out.println("MultiDynNoS Demo");

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

			Mode loadMode = Mode.plain;
			String experimentClass = null;
			if(preloadedGraphs.containsKey(selectedGraph)) {
				try {
					switch(preloadedGraphs.get(selectedGraph)) {
					case 0: 
						loadMode = Mode.keepAppearedNode; experimentClass = "Bunt"; data = new VanDeBunt().parse(loadMode); break;
					case 1: 
						loadMode = Mode.keepAppearedNode; experimentClass = "Newcomb"; data = new NewcombFraternity().parse(loadMode); break;
					case 2: 
						experimentClass = "InfoVis"; data = new InfoVisCitations().parse(loadMode); break;
					case 3: loadMode = Mode.keepAppearedNode; experimentClass = "Rugby";  data = new RugbyTweets().parse(loadMode); break;
					case 4: loadMode = Mode.keepAppearedNode; experimentClass = "Pride";  data = new DialogSequences().parse(loadMode); break;
					case 5: loadMode = Mode.keepAppearedNode; experimentClass = "College";  data = new CollegeMsg().parse(loadMode); break;
					case 6: loadMode = Mode.keepAppearedNode; experimentClass = "RealMining";  data = new RealityMining().parse(loadMode); break;
					case 7: loadMode = Mode.keepAppearedNode; experimentClass = "BitAlpha";  data = new BitcoinAlpha().parse(loadMode); break;
					case 8: loadMode = Mode.keepAppearedNode; experimentClass = "BitOTC";  data = new BitcoinOTC().parse(loadMode); break;
					case 9: loadMode = Mode.keepAppearedNode; experimentClass = "MOOC";   data = new Mooc().parse(loadMode); break;
					case 10: loadMode = Mode.keepAppearedNode; experimentClass = "RampInfectionMap";  data = new RampInfectionMap().parse(loadMode); break;
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


			switch(selectedMode) {
			case animate: 
				switch(selectedMethod) {
				case dynnos: drawingAlgorithm = new DynNoSliceRun(args, data, loadMode); break;
				case sfdp: drawingAlgorithm = new SFDPRun(args, data, loadMode); break;
				default: drawingAlgorithm = new MultiDynNoSliceRun(args, data, loadMode); break;
				}
				drawingAlgorithm.computeDrawing();
				drawingAlgorithm.animateGraph(); break;
			case plotSlices: 
				if(customGraph) {
					System.out.println("At the moment the plotSlices option only work with preloaded graphs.");
					System.exit(1);
				}			
				
				String welcomeMessage = "Plot Slices selected -- At the moment only works with MultiLevel Layout";
				String path = ".";

				HashSet<MetricsCalculationOptions> multiLevelOptions = new HashSet<MetricsCalculationOptions>();		

				for(int i = 1; i < args.length; i++) {
					String[] split = args[i].split("--"); 
					if(split.length == 1)
						continue;
					switch(MetricsCalculationOptions.parseMode(split[1])) {			
					case manualTau: {
						multiLevelOptions.add(MetricsCalculationOptions.manualTau); welcomeMessage += "\nSet ManualTau"; break;
					}
					case bendTransfer: {
						multiLevelOptions.add(MetricsCalculationOptions.bendTransfer); welcomeMessage += "\nBend Transfer Enabled"; break;
					}
//					case vanillaTuning: {
//						multiLevelOptions.add(MetricsCalculationOptions.vanillaTuning); welcomeMessage += "\nVanilla Tuning Selected"; break;
//					}
					case verbose: {
						Logger.setLog(true);
					}
					case output: {
						if(i+1 < args.length) {
							i++;
							path = args[i];
						}
						break;
					} 
					default:
						break;	
					}
				}

				Logger.getInstance().log(welcomeMessage);

				((Experiment) Class.forName("ocotillo.Experiment$"+experimentClass).getDeclaredConstructor(new Class[] {HashSet.class}).newInstance(multiLevelOptions)).probeLayout(path);
				break;
			default: 
				switch(selectedMethod) {
				case dynnos: drawingAlgorithm = new DynNoSliceRun(args, data, loadMode); break;
				case sfdp: drawingAlgorithm = new SFDPRun(args, data, loadMode); break;
				default: drawingAlgorithm = new MultiDynNoSliceRun(args, data, loadMode); break;
				}
				drawingAlgorithm.computeDrawing();
				drawingAlgorithm.plotSpaceTimeCube(); break;			
			}

			//drawingAlgorithm.saveOutput();

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

			String experimentPrefix = "";
			String welcomeMessage = "";
			
			HashSet<String> expNames = new HashSet<String>();
			ArrayList<String> smallerDatasets = new ArrayList<String>();
			smallerDatasets.add("Bunt");
			smallerDatasets.add("Newcomb");
			smallerDatasets.add("InfoVis");	
			smallerDatasets.add("Rugby");				
			smallerDatasets.add("Pride");

			HashSet<String> largerDatasets = new HashSet<String>();
			largerDatasets.add("RealMining");
			largerDatasets.add("MOOC");
			largerDatasets.add("College");
			largerDatasets.add("RampInfectionMap");

//			largerDatasets.add("BitOTC");			
//			largerDatasets.add("BitAlpha");  
			
			HashMap<String, String> visoneTimes = new HashMap<String, String>();

			ArrayList<String> discreteExperiment = new ArrayList<String>();
			discreteExperiment.add("Bunt");
			discreteExperiment.add("Newcomb");
			discreteExperiment.add("InfoVis");					

			HashSet<MetricsCalculationOptions> multiLevelOptions = new HashSet<MetricsCalculationOptions>();		

			for(int i = 1; i < args.length; i++) {
				switch(MetricsCalculationOptions.parseMode(args[i].split("--")[1])) {
				case smaller: expNames.addAll(smallerDatasets); break;
				case larger: expNames.addAll(largerDatasets); break;
				case single: {
					if(lines.isEmpty())
						lines.add("Graph;Type;Time;Tau_sugg;Tau_used;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");					
					executeSingle = true; break;
				}
				case multi: {
					if(lines.isEmpty())
						lines.add("Graph;Type;Time;Tau_sugg;Tau_used;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");					
					executeMulti = true; break;
				}
				case sfdp: {
					if(lines.isEmpty())
						lines.add("Graph;Type;Time;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");					
					executeSFDP = true; break;
				}
				case verbose: verbose = true; break;
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
						lines.add("Graph;Type;Time;Tau_sugg;Tau_used;Scaling;StressOn;StressOff;Movement;Crowding;Coarsening_Depth;Coarsening_Time;Events_Processed");					
					break;
				}
				case manualTau: {
					multiLevelOptions.add(MetricsCalculationOptions.manualTau); experimentPrefix += "manualTau_"; welcomeMessage += "\nSet ManualTau"; break;
				}
				case bendTransfer: {
					multiLevelOptions.add(MetricsCalculationOptions.bendTransfer); experimentPrefix += "bendTransfer_"; welcomeMessage += "\nBend Transfer Enabled"; break;
				}
//				case vanillaTuning: {
//					multiLevelOptions.add(MetricsCalculationOptions.vanillaTuning); experimentPrefix += "multiVanillaTuning_"; break;
//				}				
				default: break;
				}               
			}						

			Logger.setLog(verbose);
			Logger.getInstance().log("Selected Options:" + welcomeMessage);

			LocalDateTime ld = LocalDateTime.now();
			String date = ld.format(DateTimeFormatter.BASIC_ISO_DATE);
			String time = ld.format(DateTimeFormatter.ISO_LOCAL_TIME);
			time = time.replace(':', '-');

			String fileName = "Experiment_" + experimentPrefix + "_" + date + "_" + time + (executeMulti ? "_wMulti" : "") + "_data.csv";

			for(String graphName : expNames) {
				System.out.println("\n### Starting " + graphName + " Experiment ###");
				if(executeVisone && visoneTimes.containsKey(graphName)) {
					HashSet<MetricsCalculationOptions> vis_multilevelOptions = new HashSet<>(multiLevelOptions);
					vis_multilevelOptions.add(MetricsCalculationOptions.manualTau); //forces manualTau
					lines.addAll(
							((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor(new Class[] {HashSet.class}).newInstance(vis_multilevelOptions)).computeVisoneMetrics(visoneTimes.get(graphName))
							);
				}
				if(executeSingle) {
					Experiment exp;
					try {
						exp = ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor(new Class[] {HashSet.class}).newInstance(multiLevelOptions));
					}catch(NoSuchMethodException nse) {
						exp = ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor().newInstance());
						Logger.getInstance().log("Reverting to original constructor");
					}
					lines.addAll(
							exp.computeDynNoSliceMetrics(discreteExperiment.contains(graphName))
							);                		
				}if(executeMulti) {
					Experiment exp;
					try {
						exp = ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor(new Class[] {HashSet.class}).newInstance(multiLevelOptions));
					}catch(NoSuchMethodException nse) {
						exp = ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor().newInstance());
						Logger.getInstance().log("Reverting to original constructor");							
					}						
					lines.addAll(
							exp.computeMultiLevelMetrics(discreteExperiment.contains(graphName), verbose)
							);                		
				}
				if(executeSFDP) {
					lines.addAll(
							((Experiment) Class.forName("ocotillo.Experiment$"+graphName).getDeclaredConstructor().newInstance()).computeSFDPMetrics()
							);                		
				}               	

			}

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

}
