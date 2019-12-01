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

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ElementRelationLookupTest {

    private Graph graph;
    private Node a, b, c;
    private Edge ab, ac, cc;
    private NodeAttribute<Integer> nodeMetric;
    private EdgeAttribute<Integer> edgeMetric;
    private NodeAttribute<Coordinates> positions;

    private ElementRelationComputator<Integer> sum;
    private ElementRelationComputator<Double> xDiff;

    private ElementRelationComputator<Integer> sumLookup;
    private ElementRelationComputator<Double> xDiffLookup;

    @Before
    public void setUp() {
        graph = new Graph();
        a = graph.newNode();
        b = graph.newNode();
        c = graph.newNode();

        ab = graph.newEdge(a, b);
        ac = graph.newEdge(a, c);
        cc = graph.newEdge(c, c);

        nodeMetric = graph.newNodeAttribute("nodeMetric", 0);
        nodeMetric.set(a, 1);
        nodeMetric.set(b, 2);
        nodeMetric.set(c, 3);

        edgeMetric = graph.newEdgeAttribute("edgeMetric", 0);
        edgeMetric.set(ab, 3);
        edgeMetric.set(ac, 4);
        edgeMetric.set(cc, 6);

        positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        positions.set(a, new Coordinates(3, 1));
        positions.set(b, new Coordinates(6, 2));
        positions.set(c, new Coordinates(9, 3));

        sum = new ElementRelationComputator<Integer>() {

            @Override
            public Integer compute(Node nodeA, Node nodeB) {
                return nodeMetric.get(nodeA) + nodeMetric.get(nodeB);
            }

            @Override
            public Integer compute(Edge edgeA, Edge edgeB) {
                return edgeMetric.get(edgeA) + edgeMetric.get(edgeB);
            }

            @Override
            public Integer compute(Node node, Edge edge) {
                return nodeMetric.get(node) + edgeMetric.get(edge);
            }

            @Override
            public void close() {
            }
        };

        xDiff = new ElementRelationComputator<Double>() {

            @Override
            public Double compute(Node nodeA, Node nodeB) {
                return Math.abs(positions.get(nodeA).x() - positions.get(nodeB).x());
            }

            @Override
            public Double compute(Edge edgeA, Edge edgeB) {
                return Math.min(compute(edgeA.source(), edgeB), compute(edgeA.target(), edgeB));
            }

            @Override
            public Double compute(Node node, Edge edge) {
                return Math.min(compute(node, edge.source()), compute(node, edge.target()));
            }

            @Override
            public void close() {
            }
        };
    }

    @Test
    public void testGetSet() throws Exception {
        sumLookup = new ElementRelationLookup<>(sum);
        xDiffLookup = new ElementRelationLookup<>(xDiff);

        assertThat(sum.compute(a, b), is(3));
        assertThat(sumLookup.compute(a, b), is(3));

        nodeMetric.set(a, 10);
        assertThat(sum.compute(a, b), is(12));
        assertThat(sumLookup.compute(a, b), is(3));

        assertThat(sum.compute(ab, ac), is(7));
        assertThat(sumLookup.compute(ab, ac), is(7));

        edgeMetric.set(ab, 10);
        assertThat(sum.compute(ab, ac), is(14));
        assertThat(sumLookup.compute(ab, ac), is(7));

        assertThat(sum.compute(a, cc), is(16));
        assertThat(sumLookup.compute(a, cc), is(16));

        nodeMetric.set(a, 20);
        assertThat(sum.compute(a, cc), is(26));
        assertThat(sumLookup.compute(a, cc), is(16));

        assertThat(xDiff.compute(a, b), is(3.0));
        assertThat(xDiffLookup.compute(a, b), is(3.0));

        positions.set(a, new Coordinates(-6.0, 3.5));
        assertThat(xDiff.compute(a, b), is(12.0));
        assertThat(xDiffLookup.compute(a, b), is(3.0));

        assertThat(xDiff.compute(c, ab), is(3.0));
        assertThat(xDiffLookup.compute(c, ab), is(3.0));

        positions.set(b, new Coordinates(5, 0));
        assertThat(xDiff.compute(c, ab), is(4.0));
        assertThat(xDiffLookup.compute(c, ab), is(3.0));
    }

    @Test
    public void testGetSetWithManualClear() throws Exception {
        sumLookup = new ElementRelationLookup<>(sum);
        xDiffLookup = new ElementRelationLookup<>(xDiff);

        assertThat(sum.compute(a, b), is(3));
        assertThat(sumLookup.compute(a, b), is(3));

        nodeMetric.set(a, 10);
        ((ElementRelationLookup) sumLookup).clear();

        assertThat(sum.compute(a, b), is(12));
        assertThat(sumLookup.compute(a, b), is(12));

        assertThat(sum.compute(ab, ac), is(7));
        assertThat(sumLookup.compute(ab, ac), is(7));

        edgeMetric.set(ab, 10);
        ((ElementRelationLookup) sumLookup).clear();

        assertThat(sum.compute(ab, ac), is(14));
        assertThat(sumLookup.compute(ab, ac), is(14));

        assertThat(sum.compute(a, cc), is(16));
        assertThat(sumLookup.compute(a, cc), is(16));

        nodeMetric.set(a, 20);
        ((ElementRelationLookup) sumLookup).clear();

        assertThat(sum.compute(a, cc), is(26));
        assertThat(sumLookup.compute(a, cc), is(26));

        assertThat(xDiff.compute(a, b), is(3.0));
        assertThat(xDiffLookup.compute(a, b), is(3.0));

        positions.set(a, new Coordinates(-6.0, 3.5));
        ((ElementRelationLookup) xDiffLookup).clear();

        assertThat(xDiff.compute(a, b), is(12.0));
        assertThat(xDiffLookup.compute(a, b), is(12.0));

        assertThat(xDiff.compute(c, ab), is(3.0));
        assertThat(xDiffLookup.compute(c, ab), is(3.0));

        positions.set(b, new Coordinates(5, 0));
        ((ElementRelationLookup) xDiffLookup).clear();

        assertThat(xDiff.compute(c, ab), is(4.0));
        assertThat(xDiffLookup.compute(c, ab), is(4.0));
    }

    @Test
    public void testGetSetWithAutoUpdate() throws Exception {
        ElementRelationLookup<Integer> sumExplicitLookup = new ElementRelationLookup<>(sum);
        sumExplicitLookup.observe(graph);
        sumExplicitLookup.observe(nodeMetric, false);
        sumExplicitLookup.observe(edgeMetric, false);
        sumLookup = sumExplicitLookup;

        ElementRelationLookup<Double> xDiffExplicitLookup = new ElementRelationLookup<>(xDiff);
        xDiffExplicitLookup.observe(graph);
        xDiffExplicitLookup.observe(positions, true);
        xDiffLookup = xDiffExplicitLookup;

        assertThat(sum.compute(a, b), is(3));
        assertThat(sumLookup.compute(a, b), is(3));

        nodeMetric.set(a, 10);

        assertThat(sum.compute(a, b), is(12));
        assertThat(sumLookup.compute(a, b), is(12));

        assertThat(sum.compute(ab, ac), is(7));
        assertThat(sumLookup.compute(ab, ac), is(7));

        edgeMetric.set(ab, 10);

        assertThat(sum.compute(ab, ac), is(14));
        assertThat(sumLookup.compute(ab, ac), is(14));

        assertThat(sum.compute(a, cc), is(16));
        assertThat(sumLookup.compute(a, cc), is(16));

        nodeMetric.set(a, 20);

        assertThat(sum.compute(a, cc), is(26));
        assertThat(sumLookup.compute(a, cc), is(26));

        assertThat(xDiff.compute(a, b), is(3.0));
        assertThat(xDiffLookup.compute(a, b), is(3.0));

        positions.set(a, new Coordinates(-6.0, 3.5));

        assertThat(xDiff.compute(a, b), is(12.0));
        assertThat(xDiffLookup.compute(a, b), is(12.0));

        assertThat(xDiff.compute(c, ab), is(3.0));
        assertThat(xDiffLookup.compute(c, ab), is(3.0));

        positions.set(b, new Coordinates(5, 0));

        assertThat(xDiff.compute(c, ab), is(4.0));
        assertThat(xDiffLookup.compute(c, ab), is(4.0));

        sumLookup.close();
        xDiffLookup.close();
    }

}
