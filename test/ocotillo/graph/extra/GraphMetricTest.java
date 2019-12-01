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
import ocotillo.geometry.Geom;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.extra.GraphMetric.LoggedMetric;
import ocotillo.graph.extra.GraphMetric.NodeDistances;
import ocotillo.graph.extra.GraphMetric.NodeSpacialDistancesMetric;
import ocotillo.graph.extra.GraphMetric.NodeTheoreticalDistancesMetric;
import ocotillo.graph.extra.GraphMetric.StressMetric;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class GraphMetricTest {

    private class IncrementMetric implements GraphMetric<Integer> {

        private int value = 0;

        @Override
        public Integer computeMetric(Graph graph) {
            value++;
            return value;
        }
    }

    @Test
    public void testLoggedMetric() {
        Graph graph = new Graph();
        LoggedMetric<Integer> loggedMetric = new LoggedMetric<>(new IncrementMetric());
        assertThat(loggedMetric.computeMetric(graph), is(1));
        assertThat(loggedMetric.computeMetric(graph), is(2));
        assertThat(loggedMetric.computeMetric(graph), is(3));
        assertThat(loggedMetric.computeMetric(graph), is(4));
        assertThat(loggedMetric.getValues(), contains(1, 2, 3, 4));
        loggedMetric.clear();
        assertThat(loggedMetric.getValues(), is(empty()));
    }

    @Test
    public void testNodeTheoreticalDistanceMetricUndirectedUnweighted() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge cb = graph.newEdge(c, b);
        Edge db = graph.newEdge(d, b);
        Edge ed = graph.newEdge(e, d);

        GraphMetric<NodeDistances> metric = new NodeTheoreticalDistancesMetric.Builder().build();
        NodeDistances distances = metric.computeMetric(graph);
        assertThat(distances.get(a, b), is(1.0));
        assertThat(distances.get(b, a), is(1.0));
        assertThat(distances.get(a, c), is(2.0));
        assertThat(distances.get(a, d), is(2.0));
        assertThat(distances.get(a, e), is(3.0));
        assertThat(distances.get(b, e), is(2.0));
        assertThat(distances.get(e, b), is(2.0));
        assertThat(distances.get(a, f), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testNodeTheoreticalDistanceMetricDirectedUnweighted() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge cb = graph.newEdge(c, b);
        Edge db = graph.newEdge(d, b);
        Edge ed = graph.newEdge(e, d);

        GraphMetric<NodeDistances> metric = new NodeTheoreticalDistancesMetric.Builder().withDirectedEdges(true).build();
        NodeDistances distances = metric.computeMetric(graph);
        assertThat(distances.get(a, b), is(1.0));
        assertThat(distances.get(b, a), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(a, c), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(a, d), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(a, e), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(b, e), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(e, b), is(2.0));
        assertThat(distances.get(a, f), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testNodeTheoreticalDistanceMetricUndirectedWeighted() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge cb = graph.newEdge(c, b);
        Edge db = graph.newEdge(d, b);
        Edge ed = graph.newEdge(e, d);

        EdgeAttribute<Double> attribute = new EdgeAttribute<>(1.0);
        attribute.set(ab, 7.0);
        attribute.set(cb, 4.0);
        attribute.set(db, 2.0);
        attribute.set(ed, 8.0);

        GraphMetric<NodeDistances> metric = new NodeTheoreticalDistancesMetric.Builder().withWeight(attribute).build();
        NodeDistances distances = metric.computeMetric(graph);
        assertThat(distances.get(a, b), is(7.0));
        assertThat(distances.get(b, a), is(7.0));
        assertThat(distances.get(a, c), is(11.0));
        assertThat(distances.get(a, d), is(9.0));
        assertThat(distances.get(a, e), is(17.0));
        assertThat(distances.get(b, e), is(10.0));
        assertThat(distances.get(e, b), is(10.0));
        assertThat(distances.get(a, f), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testNodeTheoreticalDistanceMetricDirectedWeighted() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge cb = graph.newEdge(c, b);
        Edge db = graph.newEdge(d, b);
        Edge ed = graph.newEdge(e, d);

        EdgeAttribute<Double> attribute = new EdgeAttribute<>(1.0);
        attribute.set(ab, 7.0);
        attribute.set(cb, 4.0);
        attribute.set(db, 2.0);
        attribute.set(ed, 8.0);

        GraphMetric<NodeDistances> metric = new NodeTheoreticalDistancesMetric.Builder().withWeight(attribute).withDirectedEdges(true).build();
        NodeDistances distances = metric.computeMetric(graph);
        assertThat(distances.get(a, b), is(7.0));
        assertThat(distances.get(b, a), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(a, c), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(a, d), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(a, e), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(b, e), is(Double.POSITIVE_INFINITY));
        assertThat(distances.get(e, b), is(10.0));
        assertThat(distances.get(a, f), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testNodeSpacialDistance() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(1, 0));
        positions.set(c, new Coordinates(0, 1, 1));
        positions.set(d, new Coordinates(1, 1, 1));

        GraphMetric<NodeDistances> metric = new NodeSpacialDistancesMetric();
        NodeDistances distances = metric.computeMetric(graph);
        assertThat(distances.get(a, b), is(1.0));
        assertThat(distances.get(b, a), is(1.0));
        assertThat(distances.get(a, c), is(1.0));
        assertThat(distances.get(a, d), is(Math.sqrt(2.0)));
    }

    @Test
    public void testNodeSpacialDistance3D() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(1, 0));
        positions.set(c, new Coordinates(0, 1, 1));
        positions.set(d, new Coordinates(1, 1, 1));

        GraphMetric<NodeDistances> metric = new NodeSpacialDistancesMetric(Geom.e3D);
        NodeDistances distances = metric.computeMetric(graph);
        assertThat(distances.get(a, b), is(1.0));
        assertThat(distances.get(b, a), is(1.0));
        assertThat(distances.get(a, c), is(Math.sqrt(2.0)));
        assertThat(distances.get(a, d), is(Math.sqrt(3.0)));
    }

    @Test
    public void testStressUnweighted() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge ac = graph.newEdge(a, c);
        Edge ad = graph.newEdge(c, d);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(1, 0));
        positions.set(c, new Coordinates(0, 1, 1));
        positions.set(d, new Coordinates(1, 1, 1));
        positions.set(e, new Coordinates(4, 3, 2));

        GraphMetric<Double> metric = new StressMetric.Builder().build();
        double abContr = Math.pow((1.0 - 1.0) / 1.0, 2.0);
        double acContr = Math.pow((1.0 - 1.0) / 1.0, 2.0);
        double adContr = Math.pow((2.0 - Math.sqrt(2.0)) / 2.0, 2.0);
        double bcContr = Math.pow((2.0 - Math.sqrt(2.0)) / 2.0, 2.0);
        double bdContr = Math.pow((3.0 - 1.0) / 3.0, 2.0);
        double cdContr = Math.pow((1.0 - 1.0) / 1.0, 2.0);
        double stress = abContr + acContr + adContr + bcContr + bdContr + cdContr;
        assertThat(metric.computeMetric(graph), isAlmost(stress));
    }

    @Test
    public void testStressWeighted() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge ac = graph.newEdge(a, c);
        Edge ad = graph.newEdge(c, d);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(1, 0));
        positions.set(c, new Coordinates(0, 1, 1));
        positions.set(d, new Coordinates(1, 1, 1));
        positions.set(e, new Coordinates(4, 3, 2));

        EdgeAttribute<Double> attribute = new EdgeAttribute<>(1.0);
        attribute.set(ab, 3.0);
        attribute.set(ac, 4.0);
        attribute.set(ad, 5.0);

        GraphMetric<Double> metric = new StressMetric.Builder().withWeight(attribute).build();
        double abContr = Math.pow((3.0 - 1.0) / 3.0, 2.0);
        double acContr = Math.pow((4.0 - 1.0) / 4.0, 2.0);
        double adContr = Math.pow((9.0 - Math.sqrt(2.0)) / 9.0, 2.0);
        double bcContr = Math.pow((7.0 - Math.sqrt(2.0)) / 7.0, 2.0);
        double bdContr = Math.pow((12.0 - 1.0) / 12.0, 2.0);
        double cdContr = Math.pow((5.0 - 1.0) / 5.0, 2.0);
        double stress = abContr + acContr + adContr + bcContr + bdContr + cdContr;
        assertThat(metric.computeMetric(graph), isAlmost(stress));
    }
}
