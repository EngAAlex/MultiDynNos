/**
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.SwingUtilities;

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
import ocotillo.run.customrun.CustomRun;
import ocotillo.samples.DyGraphSamples;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.serialization.ParserTools;

/**
 * Default code for run target.
 */
public class DefaultRun {

	private static final long TIMEOUT = 86400;
	
    private enum AvailableMode {

        discretisationTest,
        infovis,
        infovisDisc,
        infovisAndDiscrete,
        rugby,
        rugbyDisc,
        rugbyAndDiscrete,
        pride,
        prideDisc,
        prideAndDiscrete,
        vanDeBunt,
        vanDeBuntAndDiscrete,
        computeMetrics,
        gui,
        custom;
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        AvailableMode mode = null;

        if (args.length == 0) {
            showHelp();
            return;
        } else {
            for (AvailableMode availableMode : AvailableMode.values()) {
                if (availableMode.name().equals(args[0])) {
                    mode = availableMode;
                }
            }
            if (mode == null) {
                System.out.println("Mode " + args[0] + " not available.\n");
                showHelp();
                return;
            }
        }

        Experiment experiment;

        switch (mode) {
            case discretisationTest:
                discretisationTest();
                break;
            case infovis:
                experiment = new Experiment.InfoVis();
                experiment.runContinuous(5);
                break;
            case infovisDisc:
                experiment = new Experiment.InfoVis();
                experiment.runDiscrete(5);
                break;
            case infovisAndDiscrete:
                experiment = new Experiment.InfoVis();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case rugby:
                experiment = new Experiment.Rugby();
                experiment.runContinuous(5);
                break;
            case rugbyDisc:
                experiment = new Experiment.Rugby();
                experiment.runDiscrete(5);
                break;
            case rugbyAndDiscrete:
                experiment = new Experiment.Rugby();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case pride:
                experiment = new Experiment.Pride();
                experiment.runContinuous(5);
                break;
            case prideDisc:
                experiment = new Experiment.Pride();
                experiment.runDiscrete(5);
                break;
            case prideAndDiscrete:
                experiment = new Experiment.Pride();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case vanDeBunt:
                experiment = new Experiment.Bunt();
                experiment.runContinuous(0);
                break;
            case vanDeBuntAndDiscrete:
                experiment = new Experiment.Bunt();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case computeMetrics:
                List<String> lines = new ArrayList<>();
                lines.add("Graph,Type,Time,Scaling,StressOn(d),StressOff(d),StressOn(c),StressOff(c),Movement,Crowding,Coarsening_Depth,Coarsening_Time,Events_Processed");
                String outputFolder = "."+File.separator;
                Boolean executeMulti = false;
                Boolean executeSFDP = false;
                Boolean executeSingle = false;
                Boolean executeVisone = false;
                Boolean executeDiscrete = false;
                
                HashSet<String> expNames = new HashSet<String>();
                HashSet<String> smallerDatasets = new HashSet<String>();
                smallerDatasets.add("Bunt");
                smallerDatasets.add("Newcomb");
                smallerDatasets.add("InfoVis");
                smallerDatasets.add("Rugby");
                smallerDatasets.add("Pride");
                
                HashSet<String> largerDatasets = new HashSet<String>();
                largerDatasets.add("RealMining");
                largerDatasets.add("BitOTC");
                largerDatasets.add("MOOC");
                largerDatasets.add("BitAlpha");  
                largerDatasets.add("College");
                
                HashMap<String, String> visoneTimes = new HashMap<String, String>();
                
                HashSet<String> discreteExperiment = new HashSet<String>();
                
                for(int i = 0; i < args.length; i++) {
                	if(args[i].equals("--smaller"))
                		expNames.addAll(smallerDatasets);
                	else if(args[i].equals("--larger"))
                		expNames.addAll(largerDatasets);
                	else if(args[i].equals("--visone")) {
                		executeVisone = true;
                    	visoneTimes.put("Bunt", "0.128");
                    	visoneTimes.put("Newcomb", "0.109");
                    	visoneTimes.put("InfoVis", "77.430");
                    	visoneTimes.put("Rugby", "0.079");
                    	visoneTimes.put("Pride", "3.391");                		
                	}
                	else if(args[i].equals("--single"))
                		executeSingle = true;
                	else if(args[i].equals("--multi"))
                		executeMulti = true;
                	else if(args[i].equals("--sfdp"))
                		executeSFDP = true;             
                	else if(args[i].equals("-d")) {
                    	discreteExperiment.add("Bunt");
                    	discreteExperiment.add("Newcomb");
                    	discreteExperiment.add("InfoVis");
                    	discreteExperiment.add("Rugby");
                    	discreteExperiment.add("Pride");                	
                	}else if(args[i].equals("-o"))
                		if(i+1 < args.length) {
                			i++;
                			outputFolder = args[i];
                		}                
                }               
                
                LocalDateTime ld = LocalDateTime.now();
                String date = ld.format(DateTimeFormatter.BASIC_ISO_DATE);
                String time = ld.format(DateTimeFormatter.ISO_LOCAL_TIME);
                time = time.replace(':', '-');
                
                String fileName = "Experiment_" + date + "_" + time + (executeMulti ? "_wMulti" : "") + "_data.csv";

                HashSet<Callable<List<String>>> callables = new HashSet<Callable<List<String>>>();
                
                for(String graphName : expNames) {
                    System.out.println("Starting " + graphName + " Experiment");
                	if(executeVisone && visoneTimes.containsKey(graphName)) {
                		callables.clear();
                        callables.add(new Callable<List<String>>() {
                            public List<String> call() throws Exception {
                            	return ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).newInstance()).computeVisoneMetrics(visoneTimes.get(graphName));
                            }
                        });
                        try {
                            ExecutorService exec = Executors.newSingleThreadExecutor();
							lines.addAll(exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS));
						}catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}catch (TimeoutException timeout) {
							System.out.println("Timeout reached!");
						} 
                	}
                	if(executeSingle) {
                		callables.clear();                		
                        callables.add(new Callable<List<String>>() {
                            public List<String> call() throws Exception {
                            	return ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).newInstance()).computeDynNoSliceMetrics(discreteExperiment.contains(graphName));
                            }
                        });
                        try {
                            ExecutorService exec = Executors.newSingleThreadExecutor();
							lines.addAll(exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS));
						}catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}catch (TimeoutException timeout) {
							System.out.println("Timeout reached!");
						} 
                	}if(executeMulti) {
                		callables.clear();                		
                        callables.add(new Callable<List<String>>() {
                            public List<String> call() throws Exception {
                            	return ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).newInstance()).computeMultiLevelMetrics(discreteExperiment.contains(graphName));
                            }
                        });
                        try {
                            ExecutorService exec = Executors.newSingleThreadExecutor();                       	
							lines.addAll(exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS));
						}catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}catch (TimeoutException timeout) {
							System.out.println("Timeout reached!");
						} 
                	}
                	if(executeSFDP) {
                		callables.clear();                		
                        callables.add(new Callable<List<String>>() {
                            public List<String> call() throws Exception {
                            	return ((Experiment) Class.forName("ocotillo.Experiment$"+graphName).newInstance()).computeSFDPMetrics();
                            }
                        });
                        try {
                            ExecutorService exec = Executors.newSingleThreadExecutor();
							//lines.addAll(exec.invokeAny(callables, TIMEOUT, TimeUnit.SECONDS));
                            lines.addAll(((Experiment) Class.forName("ocotillo.Experiment$"+graphName).newInstance()).computeSFDPMetrics());                                                      
						}catch (IllegalAccessException | InstantiationException | ClassNotFoundException e ) {
							e.printStackTrace();
						}
//						}catch (TimeoutException timeout) {
//							System.out.println("Timeout reached!");
//						} 
                	}
                		
                }
                                
//                System.out.println("Starting VanDeBunt Experiment");
//                lines.add("VanDeBunt");
//                experiment = new Experiment.Bunt();
////                lines.addAll(experiment.computeMetrics("0.128"));
////                if(executeMulti)
////                	lines.addAll(experiment.computeMultiLevelMetrics());
//                if(executeSFDP)
//                	lines.addAll(experiment.computeSFDPMetrics());
//                System.out.println("Starting Newcomb Experiment");
//                experiment = new Experiment.Newcomb();
//                lines.addAll(experiment.computeMetrics("0.109", executeMulti));
//                System.out.println("Starting InfoVis Experiment");                
//                experiment = new Experiment.InfoVis();
//                lines.addAll(experiment.computeMetrics("77.430", executeMulti));
//                System.out.println("Starting Rugby Experiment");                
//                experiment = new Experiment.Rugby();
//                lines.addAll(experiment.computeMetrics("0.079", executeMulti));
//                System.out.println("Starting Pride Experiment");                
//                experiment = new Experiment.Pride();
//                lines.addAll(experiment.computeMetrics("3.391", executeMulti));
//                  System.out.println("Starting College Experiment");
//                  experiment = new Experiment.College();
//                  lines.addAll(experiment.computeMetrics(null, executeMulti));

                for (String line : lines) {
                    System.out.println(line);
                }
                
                ParserTools.writeFileLines(lines,
                        new File(outputFolder + File.separator + fileName));
                break;
            case gui:
                Gui gui = new Gui();
                SwingUtilities.invokeLater(() -> {
                    gui.setVisible(true);
                });
                break;
            case custom:
                String[] arguments = new String[args.length - 1];
                for (int i = 0; i < arguments.length; i++) {
                    arguments[i] = args[i + 1];
                }
                CustomRun.main(arguments);
                break;
            default:
                throw new UnsupportedOperationException("Not supported");
        }
        
        System.exit(0);
    }

    private static void showHelp() {
        System.out.println("DynNoSlyce Demo");
        System.out.println("This software is distributed as demo of the approach detailed at: http://cs.swan.ac.uk/~dynnoslice/software.html");
        System.out.println("");
        System.out.println("Append a mode to perform the desired operation:");
        System.out.println("gui          Shows the GUI that allow to run the approach on the default datasets.");
        System.out.println("custom       Allows to execute the software on a custom dataset.");
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
