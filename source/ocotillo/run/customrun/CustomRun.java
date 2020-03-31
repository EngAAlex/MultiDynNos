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
package ocotillo.run.customrun;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing.FlexibleTimeTrajectories;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.run.Run;
import ocotillo.samples.parsers.Commons;

/**
 * DynnoSlice execution with custom data.
 */
public class CustomRun extends Run{

    /**
     * Executes the custom run.
     *
     * @param argv the parameters.
     */
    public static void main(String[] argv) {
    	
        CustomRun customRun = new CustomRun(argv);        
        customRun.run();
        
    }
    
	@Override
	protected void completeSetup() {    
		if(nodeDataSet != null && edgeDataSet != null) {
			checkNodeAppearanceCorrectness(nodeDataSet);
        	checkEdgeAppearanceCorrectness(edgeDataSet);
		}
	}

	@Override
	protected void run() {
        DyGraph dyGraph = createDynamicGraph();
        runDynnoSlice(dyGraph);
        saveOutput(dyGraph);		
	}
	
    /**
     * Custom run constructor.
     *
     * @param nodeDataSetLines the lines of the node data set.
     * @param edgeDataSetLines the lines of the edge data set.
     * @param delta the delta parameter.
     * @param tau the tau parameter.
     * @param output the path of the output file.
     */
    public CustomRun(String[] argv) {
    	super(argv);
    }
    
    /**
     * Custom run constructor.
     *
     * @param the preloaded graph dataset.
     * @param delta the delta parameter.
     * @param tau the tau parameter.
     * @param output the path of the output file.
     * @param graphpreloaded if the graph has already been loaded (no need to parse from command line input)
     */
    public CustomRun(String[] argv, boolean graphpreloaded) {
    	super(argv, graphpreloaded);
    }


    /**
     * Creates the dynamic dyGraph.
     *
     * @return the dynamic dyGraph.
     */
    public DyGraph createDynamicGraph() {
        DyGraph graph = new DyGraph();
        DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);

        for (NodeAppearance appearance : nodeDataSet) {
            if (!graph.hasNode(appearance.id)) {
                Node node = graph.newNode(appearance.id);
                label.set(node, new Evolution<>(appearance.id));
                presence.set(node, new Evolution<>(false));
                position.set(node, new Evolution<>(new Coordinates(0, 0)));
            }
            Node node = graph.getNode(appearance.id);
            Interval presenceInterval = Interval.newClosed(appearance.startTime,
                    appearance.startTime + appearance.duration);
            presence.get(node).insert(new FunctionConst<>(presenceInterval, true));
        }

        for (EdgeAppearance appearance : edgeDataSet) {
            Node source = graph.getNode(appearance.sourceId);
            Node target = graph.getNode(appearance.targetId);
            if (graph.betweenEdge(source, target) == null) { 
                Edge edge = graph.newEdge(source, target);
                edgePresence.set(edge, new Evolution<>(false));
            }
            Edge edge = graph.betweenEdge(source, target);
            Interval presenceInterval = Interval.newClosed(appearance.startTime,
                    appearance.startTime + appearance.duration);
            edgePresence.get(edge).insert(new FunctionConst<>(presenceInterval, true));

        }

        double graphDiameterEstimate = Math.sqrt(graph.nodeCount() * delta);
        Commons.scatterNodes(graph, graphDiameterEstimate);
        return graph;
    }

    /**
     * Runs the layout algorithm.
     *
     * @param graph the dynamic dyGraph.
     */
    public void runDynnoSlice(DyGraph graph) {
        DyModularFdl algorithm = new DyModularFdl.DyModularFdlBuilder(graph, tau)
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
