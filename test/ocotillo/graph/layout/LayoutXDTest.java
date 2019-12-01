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
package ocotillo.graph.layout;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class LayoutXDTest {

    @Test
    public void testEdgePoints() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));

        positions.set(a, new Coordinates(1, 1));
        positions.set(b, new Coordinates(9, 9));

        List<Coordinates> points = LayoutXD.edgePoints(ab, graph);
        assertThat(points.size(), is(2));
        assertThat(points.get(0), is(new Coordinates(1, 1)));
        assertThat(points.get(1), is(new Coordinates(9, 9)));

        EdgeAttribute<ControlPoints> edgePoints = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());

        points = LayoutXD.edgePoints(ab, graph);
        assertThat(points.size(), is(2));
        assertThat(points.get(0), is(new Coordinates(1, 1)));
        assertThat(points.get(1), is(new Coordinates(9, 9)));

        edgePoints.set(ab, new ControlPoints(new Coordinates(3, 3)));

        points = LayoutXD.edgePoints(ab, graph);
        assertThat(points.size(), is(3));
        assertThat(points.get(0), is(new Coordinates(1, 1)));
        assertThat(points.get(1), is(new Coordinates(3, 3)));
        assertThat(points.get(2), is(new Coordinates(9, 9)));

        edgePoints.set(ab, new ControlPoints(new Coordinates(3, 3), new Coordinates(7, 7)));

        points = LayoutXD.edgePoints(ab, graph);
        assertThat(points.size(), is(4));
        assertThat(points.get(0), is(new Coordinates(1, 1)));
        assertThat(points.get(1), is(new Coordinates(3, 3)));
        assertThat(points.get(2), is(new Coordinates(7, 7)));
        assertThat(points.get(3), is(new Coordinates(9, 9)));
    }

    @Test
    public void testEdgeLength() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        EdgeAttribute<ControlPoints> points = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());

        positions.set(a, new Coordinates(0, 0, 3));
        positions.set(b, new Coordinates(0, 4, 0));

        assertThat(LayoutXD.edgeLength(ab, graph), is(5.0));

        points.set(ab, new ControlPoints(new Coordinates(0, 4, 3)));

        assertThat(LayoutXD.edgeLength(ab, positions, points, null), is(7.0));
    }

}
