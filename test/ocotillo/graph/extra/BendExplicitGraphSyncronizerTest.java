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
package ocotillo.graph.extra;

import java.util.Arrays;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser.BegsBuilder;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser.MirrorEdge;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class BendExplicitGraphSyncronizerTest {

    Graph graph;
    Node a;
    Node b;
    Edge ab;

    NodeAttribute<Coordinates> positions;
    EdgeAttribute<ControlPoints> controlPoints;
    NodeAttribute<Coordinates> sizes;
    NodeAttribute<Integer> values;
    EdgeAttribute<Double> metrics;

    @Before
    public void setUp() {
        graph = new Graph();
        a = graph.newNode();
        b = graph.newNode();
        ab = graph.newEdge(a, b);

        positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(-10, -10));
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(10, 0));

        controlPoints = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());
        controlPoints.set(ab, new ControlPoints(new Coordinates(2, 5), new Coordinates(6, 3)));

        sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(0, 0));
        sizes.set(a, new Coordinates(1, 1));
        sizes.set(b, new Coordinates(2, 2));

        values = graph.newNodeAttribute("value", 0);
        values.set(a, 1);
        values.set(b, 2);

        metrics = graph.newEdgeAttribute("metric", 0.0);
        metrics.set(ab, 10.0);
    }

    @Test
    public void testBendExpliciGraphCreation() {
        BendExplicitGraphSynchroniser syncronizer = new BegsBuilder(graph).build();
        Graph mirror = syncronizer.getMirrorGraph();
        NodeAttribute<Coordinates> mirrorPositions = syncronizer.getMirrorPositions();

        assertThat(mirror.nodeCount(), is(4));
        assertThat(mirror.edgeCount(), is(3));
        assertThat(mirror.has(a), is(true));
        assertThat(mirror.has(b), is(true));
        assertThat(mirror.has(ab), is(false));
        assertThat(mirror.outDegree(a), is(1));
        assertThat(mirror.inDegree(b), is(1));

        Node bend1 = mirror.outEdges(a).iterator().next().target();
        Node bend2 = mirror.inEdges(b).iterator().next().source();

        assertThat(mirror.inDegree(bend1), is(1));
        assertThat(mirror.inDegree(bend2), is(1));
        assertThat(mirror.outDegree(bend1), is(1));
        assertThat(mirror.outDegree(bend2), is(1));
        assertThat(mirror.outEdges(bend1).iterator().next().target(), is(bend2));

        assertThat(mirrorPositions.get(a), is(new Coordinates(0, 0)));
        assertThat(mirrorPositions.get(b), is(new Coordinates(10, 0)));
        assertThat(mirrorPositions.get(bend1), is(new Coordinates(2, 5)));
        assertThat(mirrorPositions.get(bend2), is(new Coordinates(6, 3)));
    }

    @Test
    public void testMapping() {
        BendExplicitGraphSynchroniser syncronizer = new BegsBuilder(graph).build();
        Graph mirror = syncronizer.getMirrorGraph();

        Edge segment1 = mirror.outEdges(a).iterator().next();
        Node bend1 = segment1.target();
        Edge segment2 = mirror.outEdges(bend1).iterator().next();
        Node bend2 = segment2.target();
        Edge segment3 = mirror.outEdges(bend2).iterator().next();

        MirrorEdge mirrorEdge = syncronizer.getMirrorEdge(ab);
        assertThat(mirrorEdge.originalEdge(), is(ab));
        assertThat(mirrorEdge.source(), is(a));
        assertThat(mirrorEdge.target(), is(b));
        assertEquals(mirrorEdge.bends(), Arrays.asList(bend1, bend2));
        assertEquals(mirrorEdge.segments(), Arrays.asList(segment1, segment2, segment3));

        assertThat(syncronizer.getOriginalEdge(bend1), is(ab));
        assertThat(syncronizer.getOriginalEdge(bend2), is(ab));
        assertThat(syncronizer.getOriginalEdge(segment1), is(ab));
        assertThat(syncronizer.getOriginalEdge(segment2), is(ab));
        assertThat(syncronizer.getOriginalEdge(segment3), is(ab));

        assertThat(syncronizer.isPartOfMirrorEdge(a), is(false));
        assertThat(syncronizer.isPartOfMirrorEdge(b), is(false));
        assertThat(syncronizer.isPartOfMirrorEdge(bend1), is(true));
        assertThat(syncronizer.isPartOfMirrorEdge(bend2), is(true));
        assertThat(syncronizer.isPartOfMirrorEdge(segment1), is(true));
        assertThat(syncronizer.isPartOfMirrorEdge(segment2), is(true));
        assertThat(syncronizer.isPartOfMirrorEdge(segment3), is(true));
    }

    @Test
    public void testAttributePreservation() {
        BendExplicitGraphSynchroniser syncronizer = new BegsBuilder(graph)
                .preserveNodeAttribute(StdAttribute.nodeSize, true)
                .preserveNodeAttribute("value", false)
                .preserveEdgeAttribute("metric")
                .build();

        Graph mirror = syncronizer.getMirrorGraph();
        assertThat(mirror.hasNodeAttribute(StdAttribute.nodeSize), is(true));
        assertThat(mirror.hasNodeAttribute("value"), is(true));
        assertThat(mirror.hasEdgeAttribute("metric"), is(true));
        NodeAttribute<Coordinates> mirrorSizes = mirror.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<Integer> mirrorValues = mirror.nodeAttribute("value");
        EdgeAttribute<Double> mirrorMetric = mirror.edgeAttribute("metric");

        MirrorEdge mirrorEdge = syncronizer.getMirrorEdge(ab);

        assertThat(mirrorSizes.get(a), is(sizes.get(a)));
        assertThat(mirrorSizes.get(b), is(sizes.get(b)));
        assertThat(mirrorValues.get(a), is(values.get(a)));
        assertThat(mirrorValues.get(b), is(values.get(b)));

        for (Node bend : mirrorEdge.bends()) {
            assertThat(mirrorSizes.get(bend), is(sizes.get(a)));
        }
        for (Node bend : mirrorEdge.bends()) {
            assertThat(mirrorValues.get(bend), is(values.getDefault()));
        }
        for (Edge segment : mirrorEdge.segments()) {
            assertThat(mirrorMetric.get(segment), is(metrics.get(ab)));
        }
    }

    @Test
    public void testAddMirrorBend() {
        BendExplicitGraphSynchroniser syncronizer = new BegsBuilder(graph)
                .preserveNodeAttribute(StdAttribute.nodeSize, true)
                .preserveNodeAttribute("value", false)
                .preserveEdgeAttribute("metric")
                .build();

        Graph mirror = syncronizer.getMirrorGraph();
        NodeAttribute<Coordinates> mirrorPositions = syncronizer.getMirrorPositions();
        NodeAttribute<Coordinates> mirrorSizes = mirror.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<Integer> mirrorValues = mirror.nodeAttribute("value");
        EdgeAttribute<Double> mirrorMetric = mirror.edgeAttribute("metric");

        MirrorEdge mirrorEdge = syncronizer.getMirrorEdge(ab);

        Node newBend = syncronizer.addMirrorBend(mirrorEdge, mirrorEdge.segments().get(0));
        Edge newSegmentA = mirror.inEdges(newBend).iterator().next();
        Edge newSegmentB = mirror.outEdges(newBend).iterator().next();

        assertThat(mirror.nodeCount(), is(5));
        assertThat(mirror.edgeCount(), is(4));
        assertThat(mirrorEdge.bends().get(0), is(newBend));
        assertThat(mirrorEdge.segments().get(0), is(newSegmentA));
        assertThat(mirrorEdge.segments().get(1), is(newSegmentB));
        assertThat(mirrorPositions.get(newBend), is(new Coordinates(1, 2.5)));
        assertThat(mirrorSizes.get(newBend), is(sizes.get(a)));
        assertThat(mirrorValues.get(newBend), is(values.getDefault()));
        assertThat(mirrorMetric.get(newSegmentA), is(metrics.get(ab)));

        assertThat(syncronizer.getOriginalEdge(newBend), is(ab));
        assertThat(syncronizer.getOriginalEdge(newSegmentA), is(ab));
        assertThat(syncronizer.getOriginalEdge(newSegmentB), is(ab));

        newBend = syncronizer.addMirrorBend(mirrorEdge, mirrorEdge.segments().get(2));
        newSegmentA = mirror.inEdges(newBend).iterator().next();
        newSegmentB = mirror.outEdges(newBend).iterator().next();
        assertThat(mirror.nodeCount(), is(6));
        assertThat(mirror.edgeCount(), is(5));
        assertThat(mirrorEdge.bends().get(2), is(newBend));
        assertThat(mirrorEdge.segments().get(2), is(newSegmentA));
        assertThat(mirrorEdge.segments().get(3), is(newSegmentB));
    }

    @Test
    public void testRemoveMirrorBend() {
        BendExplicitGraphSynchroniser syncronizer = new BegsBuilder(graph)
                .preserveNodeAttribute(StdAttribute.nodeSize, true)
                .preserveNodeAttribute("value", false)
                .preserveEdgeAttribute("metric")
                .build();

        Graph mirror = syncronizer.getMirrorGraph();
        EdgeAttribute<Double> mirrorMetric = mirror.edgeAttribute("metric");

        MirrorEdge mirrorEdge = syncronizer.getMirrorEdge(ab);

        Edge newSegment = syncronizer.removeMirrorBend(mirrorEdge, mirrorEdge.bends().get(0));

        assertThat(mirror.nodeCount(), is(3));
        assertThat(mirror.edgeCount(), is(2));
        assertThat(mirrorEdge.bends().size(), is(1));
        assertThat(mirrorEdge.segments().get(0), is(newSegment));
        assertThat(mirrorMetric.get(newSegment), is(metrics.get(ab)));

        newSegment = syncronizer.removeMirrorBend(mirrorEdge, mirrorEdge.bends().get(0));

        assertThat(mirror.nodeCount(), is(2));
        assertThat(mirror.edgeCount(), is(1));
        assertThat(mirrorEdge.bends().isEmpty(), is(true));
        assertThat(mirrorEdge.segments().get(0), is(newSegment));
    }

    @Test
    public void testDirectUpdate() {
        BendExplicitGraphSynchroniser syncronizer = new BegsBuilder(graph)
                .preserveNodeAttribute(StdAttribute.nodeSize, true)
                .preserveNodeAttribute("value", false)
                .preserveEdgeAttribute("metric")
                .build();

        Graph mirror = syncronizer.getMirrorGraph();
        NodeAttribute<Coordinates> mirrorPositions = syncronizer.getMirrorPositions();
        NodeAttribute<Coordinates> mirrorSizes = mirror.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<Integer> mirrorValues = mirror.nodeAttribute("value");
        EdgeAttribute<Double> mirrorMetric = mirror.edgeAttribute("metric");

        Node c = graph.newNode();
        Edge cb = graph.newEdge(c, b);
        sizes.set(a, new Coordinates(3, 5));
        values.set(a, 18);
        values.set(c, 13);
        metrics.set(ab, 4.5);

        syncronizer.updateMirror();

        assertThat(mirrorValues.get(a), is(values.get(a)));
        assertThat(mirrorValues.get(b), is(values.get(b)));
        assertThat(mirrorValues.get(c), is(values.get(c)));

        MirrorEdge mirrorAb = syncronizer.getMirrorEdge(ab);
        for (Node bend : mirrorAb.bends()) {
            assertThat(mirrorSizes.get(bend), is(sizes.get(a)));
        }
        for (Node bend : mirrorAb.bends()) {
            assertThat(mirrorValues.get(bend), is(values.getDefault()));
        }
        for (Edge segment : mirrorAb.segments()) {
            assertThat(mirrorMetric.get(segment), is(metrics.get(ab)));
        }

        MirrorEdge mirrorCb = syncronizer.getMirrorEdge(cb);
        assertThat(mirrorCb.bends().size(), is(0));
        assertThat(mirrorCb.segments().size(), is(1));

        controlPoints.set(ab, new ControlPoints());
        controlPoints.set(cb, new ControlPoints(new Coordinates(2, 3)));

        syncronizer.updateMirror();

        assertThat(mirrorAb.bends().size(), is(0));
        assertThat(mirrorAb.segments().size(), is(1));

        assertThat(mirrorCb.bends().size(), is(1));
        assertThat(mirrorCb.segments().size(), is(2));
        assertThat(mirrorPositions.get(mirrorCb.bends().get(0)), is(new Coordinates(2, 3)));

        assertThat(mirror.nodeCount(), is(4));
        assertThat(mirror.edgeCount(), is(3));
    }

    @Test
    public void testReverseUpdate() {
        BendExplicitGraphSynchroniser syncronizer = new BegsBuilder(graph)
                .preserveNodeAttribute(StdAttribute.nodeSize, true)
                .preserveNodeAttribute("value", false)
                .preserveEdgeAttribute("metric")
                .build();

        Graph mirror = syncronizer.getMirrorGraph();
        NodeAttribute<Coordinates> mirrorPositions = syncronizer.getMirrorPositions();
        NodeAttribute<Coordinates> mirrorSizes = mirror.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<Integer> mirrorValues = mirror.nodeAttribute("value");
        EdgeAttribute<Double> mirrorMetric = mirror.edgeAttribute("metric");
        MirrorEdge mirrorEdge = syncronizer.getMirrorEdge(ab);

        mirrorPositions.set(mirrorEdge.bends().get(0), new Coordinates(2, -2));

        syncronizer.updateOriginal();
        assertThat(controlPoints.get(ab), is(Arrays.asList(new Coordinates(2, -2), new Coordinates(6, 3))));

        mirrorSizes.set(a, new Coordinates(0.3, 0.3));
        mirrorValues.set(mirrorEdge.bends().get(0), 14);
        mirrorMetric.set(mirrorEdge.segments().get(0), 6.7);
        mirrorMetric.set(mirrorEdge.segments().get(1), 6.7);
        mirrorMetric.set(mirrorEdge.segments().get(2), 10.7);

        syncronizer.updateOriginal();
        assertThat(sizes.get(a), is(mirrorSizes.get(a)));
        assertThat(values.get(a), is(mirrorValues.get(a)));
        assertThat(metrics.get(ab), is(not(6.7)));
        assertThat(metrics.get(ab), is(not(10.7)));

        mirrorMetric.set(mirrorEdge.segments().get(2), 6.7);

        syncronizer.updateOriginal();
        assertThat(metrics.get(ab), is(6.7));

        syncronizer.addMirrorBend(mirrorEdge, mirrorEdge.segments().get(0), new Coordinates(1, 1));

        syncronizer.updateOriginal();
        assertThat(controlPoints.get(ab), is(Arrays.asList(new Coordinates(1, 1), new Coordinates(2, -2), new Coordinates(6, 3))));

        syncronizer.removeMirrorBend(mirrorEdge, mirrorEdge.bends().get(1));

        syncronizer.updateOriginal();
        assertThat(controlPoints.get(ab), is(Arrays.asList(new Coordinates(1, 1), new Coordinates(6, 3))));

        syncronizer.removeMirrorBend(mirrorEdge, mirrorEdge.bends().get(1));

        syncronizer.updateOriginal();
        assertThat(controlPoints.get(ab), is(Arrays.asList(new Coordinates(1, 1))));
    }
}
