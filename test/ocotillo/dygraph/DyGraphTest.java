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
package ocotillo.dygraph;

import ocotillo.geometry.Coordinates;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.junit.Test;

/**
 * Test for the Dynamic Graph class.
 */
public class DyGraphTest {

    @Test
    public void testSnapshotAtWithEmptyGraph() {
        DyGraph dyGraph = new DyGraph();
        Graph staticGraph = dyGraph.snapshotAt(0);
        assertThat(staticGraph.nodeCount(), is(0));
        assertThat(staticGraph.edgeCount(), is(0));
    }

    @Test
    public void testSnapshotAtWithSingleNodeGraph() {
        DyGraph dyGraph = new DyGraph();
        Node alpha = dyGraph.newNode("alpha");
        Graph staticGraph = dyGraph.snapshotAt(0);
        assertThat(staticGraph.nodeCount(), is(1));
        assertThat(staticGraph.nodes(), hasItem(alpha));
        assertThat(staticGraph.edgeCount(), is(0));
    }

    @Test
    public void testSnapshotAtWithSingleEdgeGraph() {
        DyGraph dyGraph = new DyGraph();
        Node a = dyGraph.newNode("a");
        Node b = dyGraph.newNode("b");
        Edge ab = dyGraph.newEdge("ab", a, b);
        Graph staticGraph = dyGraph.snapshotAt(0);
        assertThat(staticGraph.nodeCount(), is(2));
        assertThat(staticGraph.nodes(), hasItem(a));
        assertThat(staticGraph.nodes(), hasItem(b));
        assertThat(staticGraph.edgeCount(), is(1));
        assertThat(staticGraph.edges(), hasItem(ab));
    }

    @Test
    public void testSnapshotAtNonPresentNode() {
        DyGraph dyGraph = new DyGraph();
        Node a = dyGraph.newNode("a");
        Evolution<Boolean> aEvo = new Evolution<>(true);
        aEvo.insert(new FunctionRect.Boolean(Interval.newClosed(-10, 10), false, false, Interpolation.Std.constant));
        DyNodeAttribute<Boolean> presence = dyGraph.nodeAttribute(StdAttribute.dyPresence);
        presence.set(a, aEvo);
        Graph staticGraph = dyGraph.snapshotAt(0);
        assertThat(staticGraph.nodeCount(), is(0));
        assertThat(staticGraph.edgeCount(), is(0));
    }

    @Test
    public void testSnapshopOfSampleGraph() {
        DyGraph dyGraph = new DyGraph();
        Node a = dyGraph.newNode("a");
        Node b = dyGraph.newNode("b");

        DyNodeAttribute<Boolean> nodePresence = dyGraph.nodeAttribute(StdAttribute.dyPresence);
        nodePresence.set(a, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 17), true)
                .build());
        nodePresence.set(b, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(5, 15), true)
                .build());

        DyNodeAttribute<Coordinates> positions = dyGraph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, EvoBuilder.defaultAt(new Coordinates(5, 5))
                .build());
        positions.set(b, EvoBuilder.defaultAt(new Coordinates(10, 0))
                .withRect(Interval.newClosed(7, 17), new Coordinates(10, 0), new Coordinates(0, 10), Interpolation.Std.linear)
                .build());

        Edge ab = dyGraph.newEdge(a, b);

        DyEdgeAttribute<Boolean> edgePresence = dyGraph.edgeAttribute(StdAttribute.dyPresence);
        edgePresence.set(ab, EvoBuilder.defaultAt(false)
                .withConst(Interval.newOpen(10, 13), true)
                .withConst(Interval.newOpen(14, 20), true)
                .build());

        Graph graph = dyGraph.snapshotAt(0);
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.nodes(), containsInAnyOrder(a));
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(a), isAlmost(new Coordinates(5, 5)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(b), isAlmost(new Coordinates(10, 0)));

        graph = dyGraph.snapshotAt(8);
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.nodes(), containsInAnyOrder(a, b));
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(a), isAlmost(new Coordinates(5, 5)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(b), isAlmost(new Coordinates(9, 1)));

        graph = dyGraph.snapshotAt(10);
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.nodes(), containsInAnyOrder(a, b));
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(a), isAlmost(new Coordinates(5, 5)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(b), isAlmost(new Coordinates(7, 3)));

        graph = dyGraph.snapshotAt(11);
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.nodes(), containsInAnyOrder(a, b));
        assertThat(graph.edgeCount(), is(1));
        assertThat(graph.edges(), containsInAnyOrder(ab));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(a), isAlmost(new Coordinates(5, 5)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(b), isAlmost(new Coordinates(6, 4)));

        graph = dyGraph.snapshotAt(13);
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.nodes(), containsInAnyOrder(a, b));
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(a), isAlmost(new Coordinates(5, 5)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(b), isAlmost(new Coordinates(4, 6)));

        graph = dyGraph.snapshotAt(16);
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.nodes(), containsInAnyOrder(a));
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(a), isAlmost(new Coordinates(5, 5)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(b), isAlmost(new Coordinates(1, 9)));

        graph = dyGraph.snapshotAt(20);
        assertThat(graph.nodeCount(), is(0));
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(a), isAlmost(new Coordinates(5, 5)));
        assertThat(graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition).get(b), isAlmost(new Coordinates(10, 0)));
    }
}
