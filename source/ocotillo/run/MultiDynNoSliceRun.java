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
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.AVAILABLE_STATIC_LAYOUTS;
import ocotillo.graph.multilevel.layout.MultiLevelDynNoSlice;
import ocotillo.graph.multilevel.layout.MultiLevelDynNoSlice.LIMIT_MINIMUM_TUNING;
import ocotillo.multilevel.coarsening.IndependentSet;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;
import ocotillo.multilevel.flattener.DyGraphFlattener;
import ocotillo.multilevel.logger.Logger;
import ocotillo.multilevel.options.MultiLevelDrawingOption;
import ocotillo.multilevel.placement.WeightedBarycenterPlacementStrategy;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;

public class MultiDynNoSliceRun extends Run {
	
	protected Graph appearanceGraph;
	private DecimalFormat secondFormat = new DecimalFormat("#.00");

	@Override
	protected DyGraph run() {
		
//		MultiLevelDrawingOption<ModularPostProcessing> mdo = vanillaTuning ?  
//										new MultiLevelDrawingOption.FlexibleTimeTrajectoriesPostProcessing(2, new MultiLevelCoolingStrategy.LinearCoolingStrategy(1, 0)) :
//										new MultiLevelDrawingOption.FlexibleTimeTrajectoriesPostProcessing(0, MultiLevelDynNoSlice.TRAJECTORY_OPTIMIZATION_INTERVAL); 
		
		MultiLevelDynNoSlice multiDyn = 
				new MultiLevelDynNoSlice(dygraph, tau, Run.defaultDelta)
				.setCoarsener(new IndependentSet()) //SolarMerger
				.setPlacementStrategy(new WeightedBarycenterPlacementStrategy(bendTransfer))
				.setFlattener(new DyGraphFlattener.StaticSumPresenceFlattener())
				.defaultLayoutParameters(LIMIT_MINIMUM_TUNING.LIMITED)
				.addLayerPostProcessingDrawingOption(new MultiLevelDrawingOption.FlexibleTimeTrajectoriesPostProcessing(0, MultiLevelDynNoSlice.TRAJECTORY_OPTIMIZATION_INTERVAL))
				.withSingleLevelLayout(AVAILABLE_STATIC_LAYOUTS.sfdp)
				.addOption(MultiLevelDynNoSlice.LOG_OPTION, true).build();

		DyGraph result = multiDyn.runMultiLevelLayout();

		Logger.getInstance().log("Algorithm elapsed time: " + secondFormat.format((multiDyn.getComputationStatistics().getTotalRunningTime().toMillis())/Math.pow(10, 3)) + "s");
		
		return result;
	}

	public MultiDynNoSliceRun(String[] argv, DyDataSet selectedDataset, Mode loadMode) {
		super(argv, selectedDataset, loadMode);
	}		

	@Override
	protected void completeSetup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getDescription() {
		return "MultiDynNoS";
	}

}
