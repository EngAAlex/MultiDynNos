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

import ocotillo.geometry.Box;
import ocotillo.geometry.Box.Box2D;
import ocotillo.geometry.Coordinates;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.NodeShape;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Layout2DTest {

    @Test
    public void testEdgeLength() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        EdgeAttribute<ControlPoints> points = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());

        positions.set(a, new Coordinates(0, 3, -3));
        positions.set(b, new Coordinates(4, 0, 5));

        assertThat(Layout2D.edgeLength(ab, graph), is(5.0));

        points.set(ab, new ControlPoints(new Coordinates(4, 3)));

        assertThat(Layout2D.edgeLength(ab, positions, points, null), is(7.0));
    }

    @Test
    public void testClosestEdgePoint() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        EdgeAttribute<ControlPoints> points = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(4, 0));
        points.set(ab, new ControlPoints(new Coordinates(0, 2), new Coordinates(4, 2)));

        assertThat(Layout2D.closestEdgePoint(graph, new Coordinates(-2, -2), ab), is(new Coordinates(0, 0)));
        assertThat(Layout2D.closestEdgePoint(graph, new Coordinates(-2, 4), ab), is(new Coordinates(0, 2)));
        assertThat(Layout2D.closestEdgePoint(graph, new Coordinates(2, 3), ab), is(new Coordinates(2, 2)));
        assertThat(Layout2D.closestEdgePoint(graph, new Coordinates(5, 3), ab), is(new Coordinates(4, 2)));
        assertThat(Layout2D.closestEdgePoint(graph, new Coordinates(5, 1), ab), is(new Coordinates(4, 1)));
        assertThat(Layout2D.closestEdgePoint(graph, new Coordinates(3, 0), ab), is(new Coordinates(4, 0)));
    }

    @Test
    public void testNodeBox() {
        Graph graph = new Graph();
        Node node = graph.newNode();
        graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(2, 2));

        Box nodeBox = Layout2D.nodeBox(node, graph);
        assertThat(nodeBox, is(new Box2D(-1, 1, -1, 1)));

        NodeAttribute<Coordinates> otherPositions = graph.newNodeAttribute("otherPos", new Coordinates(3, 3));
        NodeAttribute<Coordinates> otherSizes = graph.newNodeAttribute("otherSize", new Coordinates(4, 4));

        nodeBox = Layout2D.nodeBox(node, otherPositions, otherSizes);
        assertThat(nodeBox, is(new Box2D(1, 5, 1, 5)));
    }

    @Test
    public void testEdgeBox() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        EdgeAttribute<ControlPoints> points = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());

        positions.set(a, new Coordinates(2, 0));
        positions.set(b, new Coordinates(4, 1));
        points.set(ab, new ControlPoints(new Coordinates(2.5, 0.5)));

        Box edgeBox = Layout2D.edgeBox(ab, graph);
        assertThat(edgeBox, is(new Box2D(2, 4, 0, 1)));

        points.set(ab, new ControlPoints(new Coordinates(2.5, 5)));
        edgeBox = Layout2D.edgeBox(ab, graph);
        assertThat(edgeBox, is(new Box2D(2, 4, 0, 5)));

        points.set(ab, new ControlPoints(new Coordinates(2.5, 5), new Coordinates(6, -2)));
        edgeBox = Layout2D.edgeBox(ab, graph);
        assertThat(edgeBox, is(new Box2D(2, 6, -2, 5)));

        NodeAttribute<Coordinates> otherPositions = graph.newNodeAttribute("otherPos", new Coordinates(3, 3));
        EdgeAttribute<ControlPoints> otherPoints = graph.newEdgeAttribute("otherPoints", new ControlPoints());
        otherPositions.set(a, new Coordinates(10, 12));
        otherPositions.set(b, new Coordinates(8, 14));
        otherPoints.set(ab, new ControlPoints(new Coordinates(9, 5)));

        edgeBox = Layout2D.edgeBox(ab, otherPositions, otherPoints, null);
        assertThat(edgeBox, is(new Box2D(8, 10, 5, 14)));
    }

    @Test
    public void testGraphBox() {
        Graph graph = new Graph();
        Node nodeA = graph.newNode();
        Node nodeB = graph.newNode();
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(2, 2));
        positions.set(nodeB, new Coordinates(10, 10));
        sizes.set(nodeB, new Coordinates(4, 4));

        Box nodeBox = Layout2D.graphBox(graph);
        assertThat(nodeBox, is(new Box2D(-1, 12, -1, 12)));

        NodeAttribute<Coordinates> otherPositions = graph.newNodeAttribute("otherPos", new Coordinates(3, 3));
        NodeAttribute<Coordinates> otherSizes = graph.newNodeAttribute("otherSize", new Coordinates(6, 6));
        otherPositions.set(nodeA, new Coordinates(-10, 10));
        otherSizes.set(nodeA, new Coordinates(8, 8));

        nodeBox = Layout2D.graphBox(graph, otherPositions, otherSizes, null, null);
        assertThat(nodeBox, is(new Box2D(-14, 6, 0, 14)));
    }

    @Test
    public void testNodeGlyphRadiusAtAngle() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(2, 2));
        NodeAttribute<NodeShape> shapes = graph.newNodeAttribute(StdAttribute.nodeShape, NodeShape.cuboid);

        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, a, 0), isAlmost(1.0));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, a, Math.PI), isAlmost(1.0));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, a, Math.PI / 2), isAlmost(1.0));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, a, Math.PI / 4), isAlmost(Math.sqrt(2)));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, a, Math.PI / 8), isAlmost(1.08239220029));

        sizes.set(b, new Coordinates(3, 1));

        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, b, 0), isAlmost(1.5));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, b, Math.PI), isAlmost(1.5));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, b, Math.PI / 2), isAlmost(0.5));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, b, Math.PI / 4), isAlmost(Math.sqrt(0.5)));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, b, Math.PI / 8), isAlmost(1.30656296488));

        shapes.set(c, NodeShape.spheroid);

        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, c, 0), isAlmost(1.0));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, c, Math.PI), isAlmost(1.0));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, c, Math.PI / 2), isAlmost(1.0));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, c, Math.PI / 4), isAlmost(1.0));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, c, Math.PI / 8), isAlmost(1.0));

        sizes.set(d, new Coordinates(3, 1));
        shapes.set(d, NodeShape.spheroid);

        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, d, 0), isAlmost(1.5));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, d, Math.PI), isAlmost(1.5));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, d, Math.PI / 2), isAlmost(0.5));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, d, Math.PI / 4), isAlmost(1.11803398875));
        assertThat(Layout2D.nodeGlyphRadiusAtAngle(graph, d, Math.PI / 8), isAlmost(1.39896632596));
    }

    @Test
    public void testPointNodeGlyphDistance() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(3, 1));
        NodeAttribute<NodeShape> shapes = graph.newNodeAttribute(StdAttribute.nodeShape, NodeShape.cuboid);

        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(3, 0), a), isAlmost(1.5));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(0, 3), a), isAlmost(2.5));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(1, 0), a), isAlmost(-0.5));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(3, 1), a), is(greaterThan(1.5)));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(3, 1), a), is(lessThan(2.0)));

        shapes.set(b, NodeShape.spheroid);

        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(3, 0), b), isAlmost(1.5));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(0, 3), b), isAlmost(2.5));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(1, 0), b), isAlmost(-0.5));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(3, 1), b), is(greaterThan(1.5)));
        assertThat(Layout2D.pointNodeGlyphDistance(graph, new Coordinates(3, 1), b), is(lessThan(2.0)));
    }

    @Test
    public void testNodeNodeGlyphDistance() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(2, 2));
        NodeAttribute<NodeShape> shapes = graph.newNodeAttribute(StdAttribute.nodeShape, NodeShape.cuboid);

        positions.set(b, new Coordinates(3, 0));
        sizes.set(b, new Coordinates(3, 3));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, a, b), isAlmost(0.5));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, b, a), isAlmost(0.5));

        positions.set(b, new Coordinates(3, 3));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, a, b), isAlmost(0.707106781187));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, b, a), isAlmost(0.707106781187));

        positions.set(c, new Coordinates(3, 0));
        shapes.set(c, NodeShape.spheroid);
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, a, c), isAlmost(1));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, c, a), isAlmost(1));

        positions.set(c, new Coordinates(3, 3));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, a, c), isAlmost(1.82842712475));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, c, a), isAlmost(1.82842712475));

        positions.set(c, new Coordinates(1.5, 0));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, a, c), isAlmost(-0.5));
        assertThat(Layout2D.nodeNodeGlyphDistance(graph, c, a), isAlmost(-0.5));
    }

    @Test
    public void testPointEdgeGlyphDistance() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        EdgeAttribute<ControlPoints> points = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());
        EdgeAttribute<Double> edgeWidth = graph.newEdgeAttribute(StdAttribute.edgeWidth, 0.2);

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(4, 0));
        points.set(ab, new ControlPoints(new Coordinates(0, 2), new Coordinates(4, 2)));

        assertThat(Layout2D.pointEdgeGlyphDistance(graph, new Coordinates(-2, -2), ab), isAlmost(Math.sqrt(8) - 0.1));
        assertThat(Layout2D.pointEdgeGlyphDistance(graph, new Coordinates(-2, 4), ab), isAlmost(Math.sqrt(8) - 0.1));
        assertThat(Layout2D.pointEdgeGlyphDistance(graph, new Coordinates(2, 3), ab), isAlmost(0.9));
        assertThat(Layout2D.pointEdgeGlyphDistance(graph, new Coordinates(5, 3), ab), isAlmost(Math.sqrt(2) - 0.1));
        assertThat(Layout2D.pointEdgeGlyphDistance(graph, new Coordinates(5, 1), ab), isAlmost(0.9));
        assertThat(Layout2D.pointEdgeGlyphDistance(graph, new Coordinates(3, 0), ab), isAlmost(0.9));
    }

    @Test
    public void testNodeEdgeGlyphDistance() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(2, 2));
        NodeAttribute<NodeShape> shapes = graph.newNodeAttribute(StdAttribute.nodeShape, NodeShape.cuboid);
        EdgeAttribute<ControlPoints> points = graph.newEdgeAttribute(StdAttribute.edgePoints, new ControlPoints());
        EdgeAttribute<Double> edgeWidth = graph.newEdgeAttribute(StdAttribute.edgeWidth, 0.2);

        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(4, 0));
        points.set(ab, new ControlPoints(new Coordinates(0, 2), new Coordinates(4, 2)));

        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Node g = graph.newNode();
        Node h = graph.newNode();

        positions.set(c, new Coordinates(-2, -2));
        positions.set(d, new Coordinates(-2, 4));
        positions.set(e, new Coordinates(2, 3));
        positions.set(f, new Coordinates(5, 3));
        positions.set(g, new Coordinates(5, 1));
        positions.set(h, new Coordinates(3, 0));

        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, c, ab), isAlmost(Math.sqrt(2) - 0.1));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, d, ab), isAlmost(Math.sqrt(2) - 0.1));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, e, ab), isAlmost(-0.1));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, f, ab), isAlmost(-0.1));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, g, ab), isAlmost(-0.1));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, h, ab), isAlmost(-0.1));

        shapes.setDefault(NodeShape.spheroid);

        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, c, ab), is(greaterThan(Math.sqrt(8) - 1.1 - 0.2)));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, c, ab), is(lessThan(Math.sqrt(8) - 1.1 + 0.2)));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, d, ab), is(greaterThan(Math.sqrt(8) - 1.1 - 0.2)));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, d, ab), is(lessThan(Math.sqrt(8) - 1.1 + 0.2)));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, e, ab), isAlmost(-0.1));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, f, ab), is(greaterThan(Math.sqrt(2) - 1.1 - 0.2)));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, f, ab), is(lessThan(Math.sqrt(2) - 1.1 + 0.2)));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, g, ab), isAlmost(-0.1));
        assertThat(Layout2D.nodeEdgeGlyphDistance(graph, h, ab), isAlmost(-0.1));
    }

    @Test
    public void testDoNodesOverlap() {
        Graph graph = new Graph();
        Node nodeA = graph.newNode();
        Node nodeB = graph.newNode();
        NodeAttribute<Coordinates> positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(2, 2));
        positions.set(nodeB, new Coordinates(3, 3));
        sizes.set(nodeB, new Coordinates(4, 4));
        assertThat(Layout2D.doNodesOverlap(graph), is(true));

        positions.set(nodeB, new Coordinates(10, 10));
        assertThat(Layout2D.doNodesOverlap(graph), is(false));

        sizes.set(nodeA, new Coordinates(20, 20));
        assertThat(Layout2D.doNodesOverlap(graph), is(true));
    }

}
