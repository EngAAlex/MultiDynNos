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
package ocotillo.graph.layout.fdl.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.layout.fdl.modular.ModularFdl.ModularFdlBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class ModularForceTest {

    @Test
    public void testEdgeAttraction() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0, 9));
        positions.set(b, new Coordinates(0, 3, -7));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.EdgeAttraction2D(5))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(modularFdl, "forces");

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));

        double originalMagnitude = Geom.e2D.magnitude(forces.get(a));

        positions.set(a, new Coordinates(0, 0, 100));
        positions.set(b, new Coordinates(0, 2, 20));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));
        assertThat(Geom.e2D.magnitude(forces.get(a)), is(lessThan(originalMagnitude)));

        positions.set(a, new Coordinates(0, 0, -4));
        positions.set(b, new Coordinates(0, 7, 9));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));
        assertThat(Geom.e2D.magnitude(forces.get(a)), is(greaterThan(originalMagnitude)));
    }

    @Test
    public void testNodeNodeRepulsion() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0, 56));
        positions.set(b, new Coordinates(0, 3, 13));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.NodeNodeRepulsion2D(5))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(modularFdl, "forces");

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));

        double originalMagnitude = Geom.e2D.magnitude(forces.get(a));

        positions.set(a, new Coordinates(0, 0, 23));
        positions.set(b, new Coordinates(0, 2, 56));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));
        assertThat(Geom.e2D.magnitude(forces.get(a)), is(greaterThan(originalMagnitude)));

        positions.set(a, new Coordinates(0, 0, -34));
        positions.set(b, new Coordinates(0, 7, 8));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));
        assertThat(Geom.e2D.magnitude(forces.get(a)), is(lessThan(originalMagnitude)));
    }

    @Test
    public void testNodeNodeRepulsionVersusEdgeAttraction() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0, 45));
        positions.set(b, new Coordinates(0, 5, 4));

        NodeAttribute<Coordinates> sizes = graph.newNodeAttribute(StdAttribute.nodeSize, new Coordinates(0, 0));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.EdgeAttraction2D(5))
                .withForce(new ModularForce.NodeNodeRepulsion2D(5))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(modularFdl, "forces");

        modularFdl.iterate(1);
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(0.0));
        assertThat(Geom.e2D.magnitude(forces.get(b)), isAlmost(0.0));

        positions.set(a, new Coordinates(0, 0, 0));
        positions.set(b, new Coordinates(0, 5, 3));
        positions.set(b, new Coordinates(0, 2, -5));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));

        positions.set(a, new Coordinates(0, 0, 6));
        positions.set(b, new Coordinates(0, 5, 19));
        positions.set(b, new Coordinates(0, 7, -1));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(Geom.e2D.magnitude(forces.get(b))));

        positions.set(a, new Coordinates(0, 0, 5));
        positions.set(b, new Coordinates(0, 7, 18));
        sizes.set(a, new Coordinates(2, 2));
        sizes.set(b, new Coordinates(2, 2));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.magnitude(forces.get(a)), isAlmost(0.0));
        assertThat(Geom.e2D.magnitude(forces.get(b)), isAlmost(0.0));
    }

    @Test
    public void testEdgeNodeRepulsion() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0, 23));
        positions.set(b, new Coordinates(0, 5, 3));
        positions.set(c, new Coordinates(1, 3, 12));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.EdgeNodeRepulsion2D(5))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(modularFdl, "forces");

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom.e2D.unitVector(forces.get(c)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), is(lessThan(Geom.e2D.magnitude(forces.get(b)))));

        positions.set(a, new Coordinates(0, 0, 13));
        positions.set(b, new Coordinates(0, 5, 43));
        positions.set(c, new Coordinates(-1, 2, -4));

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.unitVector(forces.get(c)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), is(greaterThan(Geom.e2D.magnitude(forces.get(b)))));
    }

    @Test
    public void testSelectedEdgeNodeRepulsion() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge bc = graph.newEdge(b, c);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0, -5));
        positions.set(b, new Coordinates(0, 5, 34));
        positions.set(c, new Coordinates(1, 3, 55));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.SelectedEdgeNodeRepulsion2D(5, Arrays.asList(ab)))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(modularFdl, "forces");

        modularFdl.iterate(1);
        assertThat(Geom.e2D.unitVector(forces.get(a)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom.e2D.unitVector(forces.get(b)), isAlmost(new Coordinates(-1, 0)));
        assertThat(Geom.e2D.unitVector(forces.get(c)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.magnitude(forces.get(a)), is(lessThan(Geom.e2D.magnitude(forces.get(b)))));
    }

    @Test
    public void testCurveSmoothing() {
        Graph graph = new Graph();
        Node a = graph.newNode("a");
        Node b = graph.newNode("b");
        Node c = graph.newNode("c");
        Edge ab = graph.newEdge(a, b);
        Edge cb = graph.newEdge(c, b);
        Edge ac = graph.newEdge(a, c);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(0, 0, 0));
        positions.set(b, new Coordinates(0, 5, 5));
        positions.set(c, new Coordinates(1, 3, 8));

        EdgeAttribute<ControlPoints> edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);
        edgePoints.set(ab, new ControlPoints(new Coordinates(0, 1, 1), new Coordinates(-1, 5, 5)));
        edgePoints.set(cb, new ControlPoints(new Coordinates(2, 4, 7), new Coordinates(1, 5, 5)));
        edgePoints.set(ac, new ControlPoints(new Coordinates(1, 0, 1), new Coordinates(2, 2, 9)));

        List<Edge> curve = new ArrayList<>();
        curve.add(ab);
        curve.add(cb);
        curve.add(ac);

        List<List<Edge>> curves = new ArrayList<>();
        curves.add(curve);

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.CurveSmoothing(curves))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(modularFdl, "forces");

        modularFdl.iterate(1);
        assertThat(forces.get(a), isAlmost(new Coordinates(1.0 / 3.0, 1.0 / 3.0, 2.0 / 3.0)));
        assertThat(forces.get(b), isAlmost(new Coordinates(0, 0, 0)));
        assertThat(forces.get(c), isAlmost(new Coordinates(2.0 / 3.0, 0, 0)));
    }

    @Test
    public void testNodeAttractionToPoint() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(-1, 0, 6));
        positions.set(b, new Coordinates(0, 1, 7));
        positions.set(c, new Coordinates(1, 0, 10));

        NodeAttribute<Coordinates> attrPoint = graph.newNodeAttribute("attraction", new Coordinates(0, 0));
        attrPoint.set(a, new Coordinates(0, 0, 2));
        attrPoint.set(b, new Coordinates(0, 0, 8));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.NodeAttractionToPoint2D(attrPoint, false))
                .build();

        NodeAttribute<Coordinates> forces = Whitebox.getInternalState(modularFdl, "forces");

        modularFdl.iterate(1);
        assertThat(forces.get(a).dim(), is(2));
        assertThat(forces.get(b).dim(), is(2));
        assertThat(Geom.eXD.unitVector(forces.get(a)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.eXD.unitVector(forces.get(b)), isAlmost(new Coordinates(0, -1)));
        assertThat(forces.get(c), isAlmost(new Coordinates(0, 0)));

        positions.set(a, new Coordinates(1, 0, 5));
        positions.set(b, new Coordinates(2, 0, 6));
        positions.set(c, new Coordinates(3, 0, 6));
        attrPoint.set(c, new Coordinates(0, 0, 4));

        modularFdl.iterate(1);
        assertThat(Geom.eXD.magnitude(forces.get(a)), is(lessThan(Geom.eXD.magnitude(forces.get(b)))));
        assertThat(Geom.eXD.magnitude(forces.get(b)), is(lessThan(Geom.eXD.magnitude(forces.get(c)))));

        positions.set(a, new Coordinates(1, 0, 6));
        positions.set(b, new Coordinates(2, 0, 7));
        positions.set(c, new Coordinates(3, 0, 2));

        attrPoint.set(a, new Coordinates(1, 1, 5));
        attrPoint.set(b, new Coordinates(2, 1, 8));
        attrPoint.set(c, new Coordinates(3, 1, 1));

        modularFdl.iterate(1);
        assertThat(Geom.eXD.unitVector(forces.get(a)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.eXD.unitVector(forces.get(b)), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.eXD.unitVector(forces.get(c)), isAlmost(new Coordinates(0, 1)));
    }
}
