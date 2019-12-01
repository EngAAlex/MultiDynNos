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
package ocotillo.graph.layout.locator;

import ocotillo.geometry.Box.Box2D;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Base test class, not instantiable")
public abstract class ElementLocatorTestBase {

    protected Graph graph;
    protected NodeAttribute<Coordinates> positions;
    protected NodeAttribute<Coordinates> sizes;

    @Before
    public void setUp() {
        graph = new Graph();
        positions = graph.newNodeAttribute(StdAttribute.nodePosition, new Coordinates(0, 0));
        sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(0, 0));
    }

    protected abstract ElementLocator createInstance();

    @Test
    public final void getNodesInBox() {
        Node a = graph.newNode();
        positions.set(a, new Coordinates(11, 11));
        sizes.set(a, new Coordinates(2, 2));

        Node b = graph.newNode();
        positions.set(b, new Coordinates(-7, 11));
        sizes.set(b, new Coordinates(2, 2));

        Node c = graph.newNode();
        positions.set(c, new Coordinates(0, -20));
        sizes.set(c, new Coordinates(10, 10));

        Node d = graph.newNode();
        positions.set(d, new Coordinates(2, 1));
        sizes.set(d, new Coordinates(2, 2));

        Node e = graph.newNode();
        positions.set(e, new Coordinates(2, 15));
        sizes.set(e, new Coordinates(2, 2));

        ElementLocator locator = createInstance();

        Box2D boxA = new Box2D(-6.2, 15, -16, 10.1);
        assertThat(locator.getNodesFullyInBox(boxA), hasItem(d));
        assertThat(locator.getNodesPartiallyInBox(boxA), hasItem(d));
        assertThat(locator.getNodesPartiallyInBox(boxA), hasItem(a));
        assertThat(locator.getNodesPartiallyInBox(boxA), hasItem(b));
        assertThat(locator.getNodesPartiallyInBox(boxA), hasItem(c));

        Box2D boxB = new Box2D(-7.5, 11, 0.5, 20);
        assertThat(locator.getNodesFullyInBox(boxB), hasItem(e));
        assertThat(locator.getNodesPartiallyInBox(boxB), hasItem(e));
        assertThat(locator.getNodesPartiallyInBox(boxB), hasItem(a));
        assertThat(locator.getNodesPartiallyInBox(boxB), hasItem(b));
        assertThat(locator.getNodesPartiallyInBox(boxB), hasItem(d));
    }

    @Test
    public final void getEdgesInBox() {
        Node a = graph.newNode();
        positions.set(a, new Coordinates(11, 11));
        sizes.set(a, new Coordinates(2, 2));

        Node b = graph.newNode();
        positions.set(b, new Coordinates(-7, 11));
        sizes.set(b, new Coordinates(2, 2));

        Node c = graph.newNode();
        positions.set(c, new Coordinates(0, -20));
        sizes.set(c, new Coordinates(10, 10));

        Node d = graph.newNode();
        positions.set(d, new Coordinates(2, 1));
        sizes.set(d, new Coordinates(2, 2));

        Node e = graph.newNode();
        positions.set(e, new Coordinates(2, 15));
        sizes.set(e, new Coordinates(2, 2));

        Edge ab = graph.newEdge(a, b);
        Edge dc = graph.newEdge(d, c);
        Edge da = graph.newEdge(d, a);
        Edge db = graph.newEdge(d, b);
        Edge de = graph.newEdge(d, e);

        ElementLocator locator = createInstance();

        Box2D boxA = new Box2D(-7, 15, -25, 5);
        assertThat(locator.getEdgesFullyInBox(boxA), hasItem(dc));
        assertThat(locator.getEdgesPartiallyInBox(boxA), hasItem(dc));
        assertThat(locator.getEdgesPartiallyInBox(boxA), hasItem(da));
        assertThat(locator.getEdgesPartiallyInBox(boxA), hasItem(db));
        assertThat(locator.getEdgesPartiallyInBox(boxA), hasItem(de));

        Box2D boxB = new Box2D(-9, 13, -0.5, 20);
        assertThat(locator.getEdgesFullyInBox(boxB), hasItem(ab));
        assertThat(locator.getEdgesFullyInBox(boxB), hasItem(da));
        assertThat(locator.getEdgesFullyInBox(boxB), hasItem(db));
        assertThat(locator.getEdgesFullyInBox(boxB), hasItem(de));
        assertThat(locator.getEdgesPartiallyInBox(boxB), hasItem(ab));
        assertThat(locator.getEdgesPartiallyInBox(boxB), hasItem(da));
        assertThat(locator.getEdgesPartiallyInBox(boxB), hasItem(db));
        assertThat(locator.getEdgesPartiallyInBox(boxB), hasItem(de));
        assertThat(locator.getEdgesPartiallyInBox(boxB), hasItem(dc));
    }

    @Test
    public final void getCloseNodesFromPoint() {
        sizes.setDefault(new Coordinates(1, 1));
        Coordinates pointZero = new Coordinates(0, 0);

        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        positions.set(a, pointZero);
        positions.set(b, new Coordinates(1, 0));
        positions.set(c, new Coordinates(3, 0));
        positions.set(d, new Coordinates(9, 0));
        positions.set(e, new Coordinates(27, 0));
        positions.set(f, new Coordinates(81, 0));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseNodes(pointZero, 0), hasItem(a));

        assertThat(locator.getCloseNodes(pointZero, 2), hasItem(a));
        assertThat(locator.getCloseNodes(pointZero, 2), hasItem(b));

        assertThat(locator.getCloseNodes(pointZero, 5), hasItem(a));
        assertThat(locator.getCloseNodes(pointZero, 5), hasItem(b));
        assertThat(locator.getCloseNodes(pointZero, 5), hasItem(c));

        assertThat(locator.getCloseNodes(pointZero, 10), hasItem(a));
        assertThat(locator.getCloseNodes(pointZero, 10), hasItem(b));
        assertThat(locator.getCloseNodes(pointZero, 10), hasItem(c));
        assertThat(locator.getCloseNodes(pointZero, 10), hasItem(d));

        assertThat(locator.getCloseNodes(pointZero, 30), hasItem(a));
        assertThat(locator.getCloseNodes(pointZero, 30), hasItem(b));
        assertThat(locator.getCloseNodes(pointZero, 30), hasItem(c));
        assertThat(locator.getCloseNodes(pointZero, 30), hasItem(d));
        assertThat(locator.getCloseNodes(pointZero, 30), hasItem(e));

        assertThat(locator.getCloseNodes(pointZero, 100), hasItem(a));
        assertThat(locator.getCloseNodes(pointZero, 100), hasItem(b));
        assertThat(locator.getCloseNodes(pointZero, 100), hasItem(c));
        assertThat(locator.getCloseNodes(pointZero, 100), hasItem(d));
        assertThat(locator.getCloseNodes(pointZero, 100), hasItem(e));
        assertThat(locator.getCloseNodes(pointZero, 100), hasItem(f));

        assertThat(locator.getCloseNodes(pointZero, 100), hasSize(6));
    }

    @Test
    public final void getCloseNodesFromNode() {
        sizes.setDefault(new Coordinates(1, 1));

        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        positions.set(a, new Coordinates(0, 0));
        positions.set(b, new Coordinates(1, 0));
        positions.set(c, new Coordinates(3, 0));
        positions.set(d, new Coordinates(9, 0));
        positions.set(e, new Coordinates(27, 0));
        positions.set(f, new Coordinates(81, 0));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseNodes(a, 0), not(hasItem(a)));

        assertThat(locator.getCloseNodes(a, 2), not(hasItem(a)));
        assertThat(locator.getCloseNodes(a, 2), hasItem(b));

        assertThat(locator.getCloseNodes(a, 5), not(hasItem(a)));
        assertThat(locator.getCloseNodes(a, 5), hasItem(b));
        assertThat(locator.getCloseNodes(a, 5), hasItem(c));

        assertThat(locator.getCloseNodes(a, 10), not(hasItem(a)));
        assertThat(locator.getCloseNodes(a, 10), hasItem(b));
        assertThat(locator.getCloseNodes(a, 10), hasItem(c));
        assertThat(locator.getCloseNodes(a, 10), hasItem(d));

        assertThat(locator.getCloseNodes(a, 30), not(hasItem(a)));
        assertThat(locator.getCloseNodes(a, 30), hasItem(b));
        assertThat(locator.getCloseNodes(a, 30), hasItem(c));
        assertThat(locator.getCloseNodes(a, 30), hasItem(d));
        assertThat(locator.getCloseNodes(a, 30), hasItem(e));

        assertThat(locator.getCloseNodes(a, 100), not(hasItem(a)));
        assertThat(locator.getCloseNodes(a, 100), hasItem(b));
        assertThat(locator.getCloseNodes(a, 100), hasItem(c));
        assertThat(locator.getCloseNodes(a, 100), hasItem(d));
        assertThat(locator.getCloseNodes(a, 100), hasItem(e));
        assertThat(locator.getCloseNodes(a, 100), hasItem(f));
    }

    @Test
    public final void getCloseNodesFromEdge() {
        sizes.setDefault(new Coordinates(1, 1));

        Node a1 = graph.newNode();
        Node a2 = graph.newNode();
        Edge a = graph.newEdge(a1, a2);

        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        positions.set(a1, new Coordinates(0, 0));
        positions.set(a2, new Coordinates(0, 1));
        positions.set(b, new Coordinates(1, 0));
        positions.set(c, new Coordinates(3, 0));
        positions.set(d, new Coordinates(9, 0));
        positions.set(e, new Coordinates(27, 0));
        positions.set(f, new Coordinates(81, 0));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseNodes(a, 0), hasItem(a1));
        assertThat(locator.getCloseNodes(a, 0), hasItem(a2));

        assertThat(locator.getCloseNodes(a, 2), hasItem(a1));
        assertThat(locator.getCloseNodes(a, 2), hasItem(a2));
        assertThat(locator.getCloseNodes(a, 2), hasItem(b));

        assertThat(locator.getCloseNodes(a, 5), hasItem(a1));
        assertThat(locator.getCloseNodes(a, 5), hasItem(a2));
        assertThat(locator.getCloseNodes(a, 5), hasItem(b));
        assertThat(locator.getCloseNodes(a, 5), hasItem(c));

        assertThat(locator.getCloseNodes(a, 10), hasItem(a1));
        assertThat(locator.getCloseNodes(a, 10), hasItem(a2));
        assertThat(locator.getCloseNodes(a, 10), hasItem(b));
        assertThat(locator.getCloseNodes(a, 10), hasItem(c));
        assertThat(locator.getCloseNodes(a, 10), hasItem(d));

        assertThat(locator.getCloseNodes(a, 30), hasItem(a1));
        assertThat(locator.getCloseNodes(a, 30), hasItem(a2));
        assertThat(locator.getCloseNodes(a, 30), hasItem(b));
        assertThat(locator.getCloseNodes(a, 30), hasItem(c));
        assertThat(locator.getCloseNodes(a, 30), hasItem(d));
        assertThat(locator.getCloseNodes(a, 30), hasItem(e));

        assertThat(locator.getCloseNodes(a, 100), hasItem(a1));
        assertThat(locator.getCloseNodes(a, 100), hasItem(a2));
        assertThat(locator.getCloseNodes(a, 100), hasItem(b));
        assertThat(locator.getCloseNodes(a, 100), hasItem(c));
        assertThat(locator.getCloseNodes(a, 100), hasItem(d));
        assertThat(locator.getCloseNodes(a, 100), hasItem(e));
        assertThat(locator.getCloseNodes(a, 100), hasItem(f));
    }

    @Test
    public final void getCloseNodesWithPositionUpdate() {
        sizes.setDefault(new Coordinates(1, 1));
        Coordinates pointZero = new Coordinates(0, 0);

        Node a1 = graph.newNode();
        Node a2 = graph.newNode();
        Edge a = graph.newEdge(a1, a2);

        Node b = graph.newNode();

        positions.set(a1, new Coordinates(0, 0));
        positions.set(a2, new Coordinates(0, 1));
        positions.set(b, new Coordinates(1, 0));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseNodes(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a1, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a, 2), hasItem(b));

        positions.set(b, new Coordinates(100, 100));
        locator.rebuild();

        assertThat(locator.getCloseNodes(pointZero, 2), not(hasItem(b)));
        assertThat(locator.getCloseNodes(a1, 2), not(hasItem(b)));
        assertThat(locator.getCloseNodes(a, 2), not(hasItem(b)));

        positions.set(b, new Coordinates(1, 1));
        locator.rebuild();

        assertThat(locator.getCloseNodes(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a1, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a, 2), hasItem(b));
    }

    @Test
    public final void getCloseEdgesFromPoint() {
        sizes.setDefault(new Coordinates(1, 1));
        Coordinates pointZero = new Coordinates(0, 0);

        Node a1 = graph.newNode();
        Node a2 = graph.newNode();
        Edge a = graph.newEdge(a1, a2);

        Node b1 = graph.newNode();
        Node b2 = graph.newNode();
        Edge b = graph.newEdge(b1, b2);

        Node c1 = graph.newNode();
        Node c2 = graph.newNode();
        Edge c = graph.newEdge(c1, c2);

        Node d1 = graph.newNode();
        Node d2 = graph.newNode();
        Edge d = graph.newEdge(d1, d2);

        Node e1 = graph.newNode();
        Node e2 = graph.newNode();
        Edge e = graph.newEdge(e1, e2);

        Node f1 = graph.newNode();
        Node f2 = graph.newNode();
        Edge f = graph.newEdge(f1, f2);

        positions.set(a1, new Coordinates(0, 0));
        positions.set(a2, new Coordinates(0, 1));
        positions.set(b1, new Coordinates(1, 0));
        positions.set(b2, new Coordinates(1, 1));
        positions.set(c1, new Coordinates(3, 0));
        positions.set(c2, new Coordinates(3, 1));
        positions.set(d1, new Coordinates(9, 0));
        positions.set(d2, new Coordinates(9, 1));
        positions.set(e1, new Coordinates(27, 0));
        positions.set(e2, new Coordinates(27, 1));
        positions.set(f1, new Coordinates(81, 0));
        positions.set(f2, new Coordinates(81, 1));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseEdges(pointZero, 0), hasItem(a));

        assertThat(locator.getCloseEdges(pointZero, 2), hasItem(a));
        assertThat(locator.getCloseEdges(pointZero, 2), hasItem(b));

        assertThat(locator.getCloseEdges(pointZero, 5), hasItem(a));
        assertThat(locator.getCloseEdges(pointZero, 5), hasItem(b));
        assertThat(locator.getCloseEdges(pointZero, 5), hasItem(c));

        assertThat(locator.getCloseEdges(pointZero, 10), hasItem(a));
        assertThat(locator.getCloseEdges(pointZero, 10), hasItem(b));
        assertThat(locator.getCloseEdges(pointZero, 10), hasItem(c));
        assertThat(locator.getCloseEdges(pointZero, 10), hasItem(d));

        assertThat(locator.getCloseEdges(pointZero, 30), hasItem(a));
        assertThat(locator.getCloseEdges(pointZero, 30), hasItem(b));
        assertThat(locator.getCloseEdges(pointZero, 30), hasItem(c));
        assertThat(locator.getCloseEdges(pointZero, 30), hasItem(d));
        assertThat(locator.getCloseEdges(pointZero, 30), hasItem(e));

        assertThat(locator.getCloseEdges(pointZero, 100), hasItem(a));
        assertThat(locator.getCloseEdges(pointZero, 100), hasItem(b));
        assertThat(locator.getCloseEdges(pointZero, 100), hasItem(c));
        assertThat(locator.getCloseEdges(pointZero, 100), hasItem(d));
        assertThat(locator.getCloseEdges(pointZero, 100), hasItem(e));
        assertThat(locator.getCloseEdges(pointZero, 100), hasItem(f));

        assertThat(locator.getCloseEdges(pointZero, 100), hasSize(6));
    }

    @Test
    public final void getCloseEdgesFromNode() {
        sizes.setDefault(new Coordinates(1, 1));

        Node a1 = graph.newNode();
        Node a2 = graph.newNode();
        Edge a = graph.newEdge(a1, a2);

        Node b1 = graph.newNode();
        Node b2 = graph.newNode();
        Edge b = graph.newEdge(b1, b2);

        Node c1 = graph.newNode();
        Node c2 = graph.newNode();
        Edge c = graph.newEdge(c1, c2);

        Node d1 = graph.newNode();
        Node d2 = graph.newNode();
        Edge d = graph.newEdge(d1, d2);

        Node e1 = graph.newNode();
        Node e2 = graph.newNode();
        Edge e = graph.newEdge(e1, e2);

        Node f1 = graph.newNode();
        Node f2 = graph.newNode();
        Edge f = graph.newEdge(f1, f2);

        positions.set(a1, new Coordinates(0, 0));
        positions.set(a2, new Coordinates(0, 1));
        positions.set(b1, new Coordinates(1, 0));
        positions.set(b2, new Coordinates(1, 1));
        positions.set(c1, new Coordinates(3, 0));
        positions.set(c2, new Coordinates(3, 1));
        positions.set(d1, new Coordinates(9, 0));
        positions.set(d2, new Coordinates(9, 1));
        positions.set(e1, new Coordinates(27, 0));
        positions.set(e2, new Coordinates(27, 1));
        positions.set(f1, new Coordinates(81, 0));
        positions.set(f2, new Coordinates(81, 1));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseEdges(a1, 0), hasItem(a));

        assertThat(locator.getCloseEdges(a1, 2), hasItem(a));
        assertThat(locator.getCloseEdges(a1, 2), hasItem(b));

        assertThat(locator.getCloseEdges(a1, 5), hasItem(a));
        assertThat(locator.getCloseEdges(a1, 5), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 5), hasItem(c));

        assertThat(locator.getCloseEdges(a1, 10), hasItem(a));
        assertThat(locator.getCloseEdges(a1, 10), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 10), hasItem(c));
        assertThat(locator.getCloseEdges(a1, 10), hasItem(d));

        assertThat(locator.getCloseEdges(a1, 30), hasItem(a));
        assertThat(locator.getCloseEdges(a1, 30), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 30), hasItem(c));
        assertThat(locator.getCloseEdges(a1, 30), hasItem(d));
        assertThat(locator.getCloseEdges(a1, 30), hasItem(e));

        assertThat(locator.getCloseEdges(a1, 100), hasItem(a));
        assertThat(locator.getCloseEdges(a1, 100), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 100), hasItem(c));
        assertThat(locator.getCloseEdges(a1, 100), hasItem(d));
        assertThat(locator.getCloseEdges(a1, 100), hasItem(e));
        assertThat(locator.getCloseEdges(a1, 100), hasItem(f));
    }

    @Test
    public final void getCloseEdgesFromEdge() {
        sizes.setDefault(new Coordinates(1, 1));

        Node a1 = graph.newNode();
        Node a2 = graph.newNode();
        Edge a = graph.newEdge(a1, a2);

        Node b1 = graph.newNode();
        Node b2 = graph.newNode();
        Edge b = graph.newEdge(b1, b2);

        Node c1 = graph.newNode();
        Node c2 = graph.newNode();
        Edge c = graph.newEdge(c1, c2);

        Node d1 = graph.newNode();
        Node d2 = graph.newNode();
        Edge d = graph.newEdge(d1, d2);

        Node e1 = graph.newNode();
        Node e2 = graph.newNode();
        Edge e = graph.newEdge(e1, e2);

        Node f1 = graph.newNode();
        Node f2 = graph.newNode();
        Edge f = graph.newEdge(f1, f2);

        positions.set(a1, new Coordinates(0, 0));
        positions.set(a2, new Coordinates(0, 1));
        positions.set(b1, new Coordinates(1, 0));
        positions.set(b2, new Coordinates(1, 1));
        positions.set(c1, new Coordinates(3, 0));
        positions.set(c2, new Coordinates(3, 1));
        positions.set(d1, new Coordinates(9, 0));
        positions.set(d2, new Coordinates(9, 1));
        positions.set(e1, new Coordinates(27, 0));
        positions.set(e2, new Coordinates(27, 1));
        positions.set(f1, new Coordinates(81, 0));
        positions.set(f2, new Coordinates(81, 1));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseEdges(a, 0), not(hasItem(a)));

        assertThat(locator.getCloseEdges(a, 2), not(hasItem(a)));
        assertThat(locator.getCloseEdges(a, 2), hasItem(b));

        assertThat(locator.getCloseEdges(a, 5), not(hasItem(a)));
        assertThat(locator.getCloseEdges(a, 5), hasItem(b));
        assertThat(locator.getCloseEdges(a, 5), hasItem(c));

        assertThat(locator.getCloseEdges(a, 10), not(hasItem(a)));
        assertThat(locator.getCloseEdges(a, 10), hasItem(b));
        assertThat(locator.getCloseEdges(a, 10), hasItem(c));
        assertThat(locator.getCloseEdges(a, 10), hasItem(d));

        assertThat(locator.getCloseEdges(a, 30), not(hasItem(a)));
        assertThat(locator.getCloseEdges(a, 30), hasItem(b));
        assertThat(locator.getCloseEdges(a, 30), hasItem(c));
        assertThat(locator.getCloseEdges(a, 30), hasItem(d));
        assertThat(locator.getCloseEdges(a, 30), hasItem(e));

        assertThat(locator.getCloseEdges(a, 100), not(hasItem(a)));
        assertThat(locator.getCloseEdges(a, 100), hasItem(b));
        assertThat(locator.getCloseEdges(a, 100), hasItem(c));
        assertThat(locator.getCloseEdges(a, 100), hasItem(d));
        assertThat(locator.getCloseEdges(a, 100), hasItem(e));
        assertThat(locator.getCloseEdges(a, 100), hasItem(f));
    }

    @Test
    public final void getCloseEdgesWithPositionUpdate() {
        sizes.setDefault(new Coordinates(1, 1));
        Coordinates pointZero = new Coordinates(0, 0);

        Node a1 = graph.newNode();
        Node a2 = graph.newNode();
        Edge a = graph.newEdge(a1, a2);

        Node b1 = graph.newNode();
        Node b2 = graph.newNode();
        Edge b = graph.newEdge(b1, b2);

        positions.set(a1, new Coordinates(0, 0));
        positions.set(a2, new Coordinates(0, 1));
        positions.set(b1, new Coordinates(1, 0));
        positions.set(b2, new Coordinates(1, 1));

        ElementLocator locator = createInstance();

        assertThat(locator.getCloseEdges(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a, 2), hasItem(b));

        positions.set(b1, new Coordinates(100, 100));
        positions.set(b2, new Coordinates(100, 101));
        locator.rebuild();

        assertThat(locator.getCloseEdges(pointZero, 2), not(hasItem(b)));
        assertThat(locator.getCloseEdges(a1, 2), not(hasItem(b)));
        assertThat(locator.getCloseEdges(a, 2), not(hasItem(b)));

        positions.set(b1, new Coordinates(1, 0));
        positions.set(b2, new Coordinates(1, 1));
        locator.rebuild();

        assertThat(locator.getCloseEdges(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a, 2), hasItem(b));
    }
}
