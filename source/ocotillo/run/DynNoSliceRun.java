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
package ocotillo.run;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing.FlexibleTimeTrajectories;
import ocotillo.geometry.Geom;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.samples.parsers.Commons.DyDataSet;

/**
 * DynnoSlice execution with custom data.
 */
public class DynNoSliceRun extends Run{
    
	/**
     * Custom run constructor.
     *
     * @param the preloaded graph dataset.
     * @param delta the delta parameter.
     * @param tau the tau parameter.
     * @param output the path of the output file.
     * @param graphpreloaded if the graph has already been loaded (no need to parse from command line input)
     */
    public DynNoSliceRun(String[] argv, DyDataSet selectedDataSet) {    	
    	super(argv, selectedDataSet);
    }
	
	@Override
	protected DyGraph run() {
        DyModularFdl algorithm = new DyModularFdl.DyModularFdlBuilder(dygraph, tau)
                .withForce(new DyModularForce.TimeStraightning(delta))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.ConnectionAttraction(delta))
                .withForce(new DyModularForce.EdgeRepulsion(delta))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
                .withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
                .withPostProcessing(new FlexibleTimeTrajectories(delta * 1.5, delta * 2.0, Geom.e3D))
                .build();

        	algorithm.iterate(defaultNumberOfIterations);
        	
        	return dygraph;
        }


	@Override
	protected void completeSetup() {
		// TODO Auto-generated method stub		
	}

	@Override
	protected String getDescription() {
		return "DynNoSlice";
	}

}
