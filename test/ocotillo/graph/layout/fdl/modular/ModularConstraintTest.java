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
import java.util.Collection;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.Observer;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularFdl.ModularFdlBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class ModularConstraintTest {

    @Test
    public void testEdgeAttraction() {
        Graph graph = new Graph();
        final Node a = graph.newNode();

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(5))
                .build();

        final NodeAttribute<Double> constraints = Whitebox.getInternalState(modularFdl, "constraints");

        new Observer.ElementAttributeChanges<Node>(constraints) {

            double previousValue = 5.1;

            @Override
            public void update(Collection<Node> changedElements) {
                Node changedElement = changedElements.iterator().next();
                double currentValue = constraints.get(changedElement);
                assertThat(changedElement, is(a));
                assertThat(currentValue, is(lessThan(previousValue)));
                previousValue = currentValue;
            }

            @Override
            public void updateAll() {
            }
        };

        modularFdl.iterate(10);
    }

    @Test
    public void SurroundingEdgesProjectionInside() {
        Graph graph = new Graph();

        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Node g = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, -2));
        positions.set(b, new Coordinates(2, 2));

        NodeAttribute<Coordinates> moveTo = new NodeAttribute<>(new Coordinates(0, 0));
        moveTo.set(a, new Coordinates(0, 0));
        moveTo.set(b, new Coordinates(0, 2));
        moveTo.set(c, Geom.e2D.unitVector(0));
        moveTo.set(d, Geom.e2D.unitVector(Math.PI / 4));
        moveTo.set(e, Geom.e2D.unitVector(Math.PI / 3));
        moveTo.set(f, Geom.e2D.unitVector(Math.PI / 2));
        moveTo.set(g, Geom.e2D.unitVector(Math.PI));

        NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<>(Arrays.asList(ab));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.NodeAttractionToPoint2D(moveTo, true))
                .withConstraint(new ModularConstraint.SurroundingEdges(surroundingEdges))
                .build();

        modularFdl.iterate(1);

        NodeAttribute<Double> constraints = Whitebox.getInternalState(modularFdl, "constraints");

        assertThat(constraints.get(a), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(b), isAlmost(1));
        assertThat(constraints.get(c), isAlmost(1));
        assertThat(constraints.get(d), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(e), isAlmost(2));
        assertThat(constraints.get(f), is(Double.POSITIVE_INFINITY));
        assertThat(constraints.get(g), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void SurroundingEdgesProjectionOutside() {
        Graph graph = new Graph();

        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Node g = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, 2));
        positions.set(b, new Coordinates(2, 4));

        NodeAttribute<Coordinates> moveTo = new NodeAttribute<>(new Coordinates(0, 0));
        moveTo.set(a, new Coordinates(0, 0));
        moveTo.set(b, new Coordinates(0, 2));
        moveTo.set(c, Geom.e2D.unitVector(0));
        moveTo.set(d, Geom.e2D.unitVector(Math.PI / 4));
        moveTo.set(e, Geom.e2D.unitVector(Math.PI / 2));
        moveTo.set(f, Geom.e2D.unitVector(Math.PI));

        NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<>(Arrays.asList(ab));

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withForce(new ModularForce.NodeAttractionToPoint2D(moveTo, true))
                .withConstraint(new ModularConstraint.SurroundingEdges(surroundingEdges))
                .build();

        modularFdl.iterate(1);

        NodeAttribute<Double> constraints = Whitebox.getInternalState(modularFdl, "constraints");

        assertThat(constraints.get(a), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(b), isAlmost(Math.sqrt(2) * 2));
        assertThat(constraints.get(c), isAlmost(2));
        assertThat(constraints.get(d), isAlmost(Math.sqrt(2)));
        assertThat(constraints.get(e), isAlmost(2));
        assertThat(constraints.get(f), is(Double.POSITIVE_INFINITY));
    }

    @Test
    public void testPinnedNodes() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Boolean> pinnedNodesA = new NodeAttribute<>(false);
        pinnedNodesA.set(a, true);

        Collection<Node> pinnedNodesB = new ArrayList<>();
        pinnedNodesB.add(b);

        ModularFdl modularFdl = new ModularFdlBuilder(graph)
                .withConstraint(new ModularConstraint.PinnedNodes(pinnedNodesA))
                .withConstraint(new ModularConstraint.PinnedNodes(pinnedNodesB))
                .build();

        NodeAttribute<Double> constraints = Whitebox.getInternalState(modularFdl, "constraints");

        modularFdl.iterate(1);

        assertThat(constraints.get(a), is(0.0));
        assertThat(constraints.get(b), is(0.0));
        assertThat(constraints.get(c), is(Double.POSITIVE_INFINITY));
    }
}
