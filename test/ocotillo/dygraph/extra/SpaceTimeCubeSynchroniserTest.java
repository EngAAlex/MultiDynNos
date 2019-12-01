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
package ocotillo.dygraph.extra;

import ocotillo.dygraph.*;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.MirrorConnection;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.MirrorLine;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.StcsBuilder;
import ocotillo.geometry.Coordinates;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests interval functions.
 */
public class SpaceTimeCubeSynchroniserTest {

    @Test
    public void testEmptyConstruction() {
        DyGraph graph = new DyGraph();
        SpaceTimeCubeSynchroniser synchroniser = new StcsBuilder(graph, 10).build();
        assertThat(synchroniser.mirrorGraph().nodeCount(), is(0));
        assertThat(synchroniser.mirrorGraph().edgeCount(), is(0));
        assertThat(synchroniser.mirrorLines().isEmpty(), is(true));
        assertThat(synchroniser.mirrorConnections().isEmpty(), is(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSinglePresenceNodeConstruction() {
        DyGraph graph = new DyGraph();
        Node node = graph.newNode();
        DyNodeAttribute<Boolean> dyPresences = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<Coordinates> dyPositions = graph.nodeAttribute(StdAttribute.nodePosition);
        dyPresences.set(node, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(1, 11), true)
                .build());
        dyPositions.set(node, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(1, 11), new Coordinates(2, 3), new Coordinates(12, 13), Interpolation.Std.linear)
                .build());

        SpaceTimeCubeSynchroniser synchroniser = new StcsBuilder(graph, 10).build();
        Graph mirrorGraph = synchroniser.mirrorGraph();

        assertThat(mirrorGraph.nodeCount(), is(2));
        assertThat(mirrorGraph.edgeCount(), is(1));

        Edge mirrorEdge = synchroniser.mirrorGraph().edges().iterator().next();
        Node mirrorSource = mirrorEdge.source();
        Node mirrorTarget = mirrorEdge.target();
        NodeAttribute<Coordinates> mirrorPositions = mirrorGraph.nodeAttribute(StdAttribute.nodePosition);

        assertThat(mirrorPositions.get(mirrorSource), isAlmost(new Coordinates(2, 3, 10)));
        assertThat(mirrorPositions.get(mirrorTarget), isAlmost(new Coordinates(12, 13, 110)));

        assertThat(synchroniser.mirrorLines().size(), is(1));
        assertThat(synchroniser.mirrorConnections().size(), is(0));
        MirrorLine line = synchroniser.mirrorLines().iterator().next();
        assertThat(line.mirrorEdge(), is(mirrorEdge));
        assertThat(line.mirrorSource(), is(mirrorSource));
        assertThat(line.mirrorTarget(), is(mirrorTarget));
        assertThat(line.interval(), is(Interval.newClosed(1, 11)));
        assertThat(line.mirrorInterval(), is(Interval.newClosed(10, 110)));
        assertThat(line.original(), is(node));
        assertThat(line.computeFunctions(), contains(new FunctionRect.Coordinates(
                Interval.newClosed(1, 11), new Coordinates(2, 3), new Coordinates(12, 13), Interpolation.Std.linear)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoublePresenceNodeConstruction() {
        DyGraph graph = new DyGraph();
        Node node = graph.newNode();
        DyNodeAttribute<Boolean> dyPresences = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<Coordinates> dyPositions = graph.nodeAttribute(StdAttribute.nodePosition);
        dyPresences.set(node, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(1, 11), true)
                .withConst(Interval.newClosed(12, 14), true)
                .build());
        dyPositions.set(node, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(1, 11), new Coordinates(2, 3), new Coordinates(12, 13), Interpolation.Std.linear)
                .withRect(Interval.newClosed(12, 16), new Coordinates(7, 12), new Coordinates(7, 16), Interpolation.Std.linear)
                .build());

        SpaceTimeCubeSynchroniser synchroniser = new StcsBuilder(graph, 10).build();
        Graph mirrorGraph = synchroniser.mirrorGraph();

        assertThat(mirrorGraph.nodeCount(), is(4));
        assertThat(mirrorGraph.edgeCount(), is(2));
        assertThat(synchroniser.mirrorLines().size(), is(2));
        assertThat(synchroniser.mirrorConnections().size(), is(0));

        MirrorLine first = synchroniser.mirrorLines().get(0);
        MirrorLine second = synchroniser.mirrorLines().get(1);
        NodeAttribute<Coordinates> mirrorPositions = mirrorGraph.nodeAttribute(StdAttribute.nodePosition);

        assertThat(mirrorPositions.get(first.mirrorSource()), isAlmost(new Coordinates(2, 3, 10)));
        assertThat(mirrorPositions.get(first.mirrorTarget()), isAlmost(new Coordinates(12, 13, 110)));
        assertThat(mirrorPositions.get(second.mirrorSource()), isAlmost(new Coordinates(7, 12, 120)));
        assertThat(mirrorPositions.get(second.mirrorTarget()), isAlmost(new Coordinates(7, 14, 140)));
        assertThat(first.interval(), is(Interval.newClosed(1, 11)));
        assertThat(first.mirrorInterval(), is(Interval.newClosed(10, 110)));
        assertThat(second.interval(), is(Interval.newClosed(12, 14)));
        assertThat(second.mirrorInterval(), is(Interval.newClosed(120, 140)));
        assertThat(first.original(), is(node));
        assertThat(second.original(), is(node));
        assertThat(first.computeFunctions(), contains(new FunctionRect.Coordinates(
                Interval.newClosed(1, 11), new Coordinates(2, 3), new Coordinates(12, 13), Interpolation.Std.linear)));
        assertThat(second.computeFunctions(), contains(new FunctionRect.Coordinates(
                Interval.newClosed(12, 14), new Coordinates(7, 12), new Coordinates(7, 14), Interpolation.Std.linear)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConnection() {
        DyGraph graph = new DyGraph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        DyNodeAttribute<Boolean> dyPresences = graph.nodeAttribute(StdAttribute.dyPresence);
        dyPresences.set(a, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(1, 11), true)
                .build());
        dyPresences.set(b, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 12), true)
                .build());

        DyNodeAttribute<Coordinates> dyPositions = graph.nodeAttribute(StdAttribute.nodePosition);
        dyPositions.set(a, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(1, 11), new Coordinates(4, 5), new Coordinates(6, 7), Interpolation.Std.linear)
                .build());
        dyPositions.set(b, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(0, 12), new Coordinates(2, 12), new Coordinates(8, 3), Interpolation.Std.linear)
                .build());

        DyEdgeAttribute<Boolean> dyEdgePresences = graph.edgeAttribute(StdAttribute.dyPresence);
        dyEdgePresences.set(ab, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(4, 7), true)
                .build());

        SpaceTimeCubeSynchroniser synchroniser = new StcsBuilder(graph, 10).build();
        Graph mirrorGraph = synchroniser.mirrorGraph();

        assertThat(mirrorGraph.nodeCount(), is(4));
        assertThat(mirrorGraph.edgeCount(), is(2));
        assertThat(synchroniser.mirrorLines().size(), is(2));
        assertThat(synchroniser.mirrorConnections().size(), is(1));

        MirrorLine aLine, bLine;
        if (synchroniser.mirrorLines().get(0).original() == a) {
            aLine = synchroniser.mirrorLines().get(0);
            bLine = synchroniser.mirrorLines().get(1);
        } else {
            aLine = synchroniser.mirrorLines().get(1);
            bLine = synchroniser.mirrorLines().get(0);
        }
        NodeAttribute<Coordinates> mirrorPositions = mirrorGraph.nodeAttribute(StdAttribute.nodePosition);

        assertThat(mirrorPositions.get(aLine.mirrorSource()), isAlmost(new Coordinates(4, 5, 10)));
        assertThat(mirrorPositions.get(aLine.mirrorTarget()), isAlmost(new Coordinates(6, 7, 110)));
        assertThat(mirrorPositions.get(bLine.mirrorSource()), isAlmost(new Coordinates(2, 12, 0)));
        assertThat(mirrorPositions.get(bLine.mirrorTarget()), isAlmost(new Coordinates(8, 3, 120)));
        assertThat(aLine.interval(), is(Interval.newClosed(1, 11)));
        assertThat(aLine.mirrorInterval(), is(Interval.newClosed(10, 110)));
        assertThat(bLine.interval(), is(Interval.newClosed(0, 12)));
        assertThat(bLine.mirrorInterval(), is(Interval.newClosed(0, 120)));
        assertThat(aLine.original(), is(a));
        assertThat(bLine.original(), is(b));
        assertThat(aLine.computeFunctions(), contains(new FunctionRect.Coordinates(
                Interval.newClosed(1, 11), new Coordinates(4, 5), new Coordinates(6, 7), Interpolation.Std.linear)));
        assertThat(bLine.computeFunctions(), contains(new FunctionRect.Coordinates(
                Interval.newClosed(0, 12), new Coordinates(2, 12), new Coordinates(8, 3), Interpolation.Std.linear)));

        MirrorConnection connection = synchroniser.mirrorConnections().iterator().next();
        assertThat(connection.interval(), is(Interval.newClosed(4, 7)));
        assertThat(connection.mirrorInterval(), is(Interval.newClosed(40, 70)));
        assertThat(connection.original(), is(ab));
        assertThat(connection.sourceMirrorLine(), is(aLine));
        assertThat(connection.targetMirrorLine(), is(bLine));
    }

    @Test
    public void updateOriginalWithBend() {
        DyGraph graph = new DyGraph();
        Node node = graph.newNode();
        DyNodeAttribute<Boolean> dyPresences = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<Coordinates> dyPositions = graph.nodeAttribute(StdAttribute.nodePosition);
        dyPresences.set(node, EvoBuilder.defaultAt(false)
                .withConst(Interval.newLeftClosed(0, 10), true)
                .build());

        SpaceTimeCubeSynchroniser synchroniser = new StcsBuilder(graph, 10).build();
        Graph mirrorGraph = synchroniser.mirrorGraph();

        Edge mirrorEdge = synchroniser.mirrorGraph().edges().iterator().next();
        Node mirrorSource = mirrorEdge.source();
        Node mirrorTarget = mirrorEdge.target();
        NodeAttribute<Coordinates> mirrorPositions = mirrorGraph.nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> bends = mirrorGraph.edgeAttribute(StdAttribute.edgePoints);

        mirrorPositions.set(mirrorSource, new Coordinates(12, 43, 0));
        mirrorPositions.set(mirrorTarget, new Coordinates(8, 3, 100));
        bends.set(mirrorEdge, new ControlPoints(new Coordinates(6, 8, 30), new Coordinates(5, 32, 80)));

        synchroniser.updateOriginal();
        assertThat(dyPositions.get(node), is(EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(0, 3), new Coordinates(12, 43), new Coordinates(6, 8), Interpolation.Std.linear)
                .withRect(Interval.newRightClosed(3, 8), new Coordinates(6, 8), new Coordinates(5, 32), Interpolation.Std.linear)
                .withRect(Interval.newOpen(8, 10), new Coordinates(5, 32), new Coordinates(8, 3), Interpolation.Std.linear)
                .build()));
    }
}
