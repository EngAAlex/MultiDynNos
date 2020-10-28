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

import ocotillo.Experiment;
import ocotillo.dygraph.DyGraph;
import ocotillo.graph.Graph;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.SfdpBuilder;
import ocotillo.multilevel.flattener.DyGraphFlattener.StaticSumPresenceFlattener;
import ocotillo.samples.parsers.Commons.DyDataSet;

public class SFDPRun extends Run {

	public SFDPRun(String[] argv, DyDataSet requestedDataSet) {
		super(argv, requestedDataSet);
	}

	@Override
	protected String getDescription() {
		return "SFDP";
	}

	@Override
	protected void completeSetup() {
		// NO-OP
	}

	@Override
	protected DyGraph run() {		
		StaticSumPresenceFlattener dyg = new StaticSumPresenceFlattener();
		Graph flattened = dyg.flattenDyGraph(dygraph);
		SfdpBuilder sfdp = new SfdpBuilder();
		SfdpExecutor sfdpInstance = sfdp.build();
		System.out.println("Flattened graph has " + flattened.nodeCount() + " nodes and " + flattened.edgeCount() + " edges");

		sfdpInstance.execute(flattened);

		Experiment.copyNodeLayoutFromTo(flattened, dygraph);
				
		//		MultiLevelCustomRun.showGraphOnWindow(sfdpCont, dataset.suggestedInterval.leftBound(), name + " SFDP");
		//		MultiLevelCustomRun.animateGraphOnWindow(sfdpCont, dataset.suggestedInterval.leftBound(), dataset.suggestedInterval, name + " SFDP");

		return dygraph;
	}

}
