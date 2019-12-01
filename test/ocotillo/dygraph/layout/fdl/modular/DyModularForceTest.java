/**
 * Copyright © 2014-2016 Paolo Simonetto
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
package ocotillo.dygraph.layout.fdl.modular;

import ocotillo.dygraph.*;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl.DyModularFdlBuilder;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.GeomXD;
import ocotillo.geometry.Interval;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Tests interval functions.
 */
public class DyModularForceTest {

    @Test
    public void testTimeStraightningForce() {
        DyGraph graph = new DyGraph();
        Node node = graph.newNode();
        DyNodeAttribute<Boolean> dyPresences = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<Coordinates> dyPositions = graph.nodeAttribute(StdAttribute.nodePosition);
        dyPresences.set(node, EvoBuilder.defaultAt(false)
                .withConst(Interval.newLeftClosed(0, 10), true)
                .build());
        dyPositions.set(node, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newLeftClosed(0, 10), new Coordinates(0, 0), new Coordinates(10, 10), Interpolation.Std.linear)
                .build());

        DyModularFdl dyModularFdl = new DyModularFdlBuilder(graph, 1)
                .withForce(new DyModularForce.TimeStraightning(1))
                .build();

        dyModularFdl.iterate(50);
        assertThat(dyPositions.get(node).valueAt(0), isAlmost(dyPositions.get(node).valueAt(10)));
    }

    @Test
    public void testTimeStraightningForceWithBends() {
        // TODO: write it once the original bends can be considered.
    }

    @Test
    public void testConnectionAttractionForce() {
        DyGraph graph = new DyGraph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        DyNodeAttribute<Boolean> dyPresences = graph.nodeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Boolean> dyEdgePresences = graph.edgeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<Coordinates> dyPositions = graph.nodeAttribute(StdAttribute.nodePosition);

        Coordinates aAt0 = new Coordinates(0, 0);
        Coordinates aAt10 = new Coordinates(10, 10);
        Coordinates bAt0 = new Coordinates(5, 5);
        Coordinates bAt10 = new Coordinates(15, 15);

        dyPresences.set(a, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 10), true)
                .build());
        dyPresences.set(b, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 10), true)
                .build());
        dyEdgePresences.set(ab, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 10), true)
                .build());
        dyPositions.set(a, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(0, 10), aAt0, aAt10, Interpolation.Std.linear)
                .build());
        dyPositions.set(b, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(0, 10), bAt0, bAt10, Interpolation.Std.linear)
                .build());

        DyModularFdl dyModularFdl = new DyModularFdlBuilder(graph, 1)
                .withForce(new DyModularForce.ConnectionAttraction(5))
                .build();

        dyModularFdl.iterate(100);

        Coordinates aAt0_new = dyPositions.get(a).valueAt(0);
        Coordinates aAt10_new = dyPositions.get(a).valueAt(10);
        Coordinates bAt0_new = dyPositions.get(b).valueAt(0);
        Coordinates bAt10_new = dyPositions.get(b).valueAt(10);

        GeomXD tolerantGeom = new GeomXD(0.01);

        assertTrue(tolerantGeom.almostEqual(aAt0_new, bAt0_new));
        assertTrue(tolerantGeom.almostEqual(aAt10_new, bAt10_new));
        assertTrue(tolerantGeom.almostEqual(aAt0_new.minus(aAt0), aAt10_new.minus(aAt10)));
    }
}
