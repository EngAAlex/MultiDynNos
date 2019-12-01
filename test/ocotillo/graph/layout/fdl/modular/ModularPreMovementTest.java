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

import java.util.List;
import ocotillo.geometry.Coordinates;
import static ocotillo.geometry.matchers.IsAlmostNumber.isAlmost;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularPreMovement.MovementLimiterPerDimension;
import ocotillo.graph.layout.fdl.modular.ModularPreMovement.VectorFieldSmoothing;
import ocotillo.graph.layout.fdl.modular.ModularPreMovement.VectorFieldSmoothing.NodeCellInfo;
import static org.hamcrest.CoreMatchers.is;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class ModularPreMovementTest {

    @Test
    public void testMovementLimiterPerDimension() {
        double xLimit = 0.359;
        double yLimit = 0.712;

        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, 2, 2));
        positions.set(b, new Coordinates(3, 3, 3));

        MovementLimiterPerDimension dimensionLimiter = new MovementLimiterPerDimension(new Coordinates(xLimit, yLimit));

        ModularFdl modularFdl = new ModularFdl.ModularFdlBuilder(graph)
                .withForce(new ModularForce.NodeNodeRepulsion2D(10))
                .withPreMovmement(dimensionLimiter)
                .build();

        modularFdl.iterate(1);

        assertThat(modularFdl.movements.get(a).x(), isAlmost(-xLimit));
        assertThat(modularFdl.movements.get(a).y(), isAlmost(-yLimit));
        assertThat(modularFdl.movements.get(b).x(), isAlmost(xLimit));
        assertThat(modularFdl.movements.get(b).y(), isAlmost(yLimit));
    }

    @Test
    public void testGetCellInfoSingleCell() throws Exception {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, 2));
        positions.set(b, new Coordinates(6, 6));
        positions.set(c, new Coordinates(3, 5));

        ModularPreMovement vectorField = new VectorFieldSmoothing(1);

        ModularFdl modularFdl = new ModularFdl.ModularFdlBuilder(graph)
                .withPreMovmement(vectorField)
                .build();

        modularFdl.iterate(1);

        NodeCellInfo aInfo = Whitebox.<NodeCellInfo>invokeMethod(vectorField, "getNodeCellInfo", a);
        NodeCellInfo bInfo = Whitebox.<NodeCellInfo>invokeMethod(vectorField, "getNodeCellInfo", b);
        NodeCellInfo cInfo = Whitebox.<NodeCellInfo>invokeMethod(vectorField, "getNodeCellInfo", c);

        assertThat(aInfo.node, is(a));
        assertThat(aInfo.cellRow, is(0));
        assertThat(aInfo.cellColumn, is(0));
        assertThat(aInfo.leftBottomCorner, is(0));
        assertThat(aInfo.rightBottomCorner, is(1));
        assertThat(aInfo.leftTopCorner, is(2));
        assertThat(aInfo.rightTopCorner, is(3));
        assertThat(aInfo.xOffsetInCell, is(0.0));
        assertThat(aInfo.yOffsetInCell, is(0.0));

        assertThat(bInfo.node, is(b));
        assertThat(bInfo.cellRow, is(0));
        assertThat(bInfo.cellColumn, is(0));
        assertThat(bInfo.leftBottomCorner, is(0));
        assertThat(bInfo.rightBottomCorner, is(1));
        assertThat(bInfo.leftTopCorner, is(2));
        assertThat(bInfo.rightTopCorner, is(3));
        assertThat(bInfo.xOffsetInCell, is(1.0));
        assertThat(bInfo.yOffsetInCell, is(1.0));

        assertThat(cInfo.node, is(c));
        assertThat(cInfo.cellRow, is(0));
        assertThat(cInfo.cellColumn, is(0));
        assertThat(cInfo.leftBottomCorner, is(0));
        assertThat(cInfo.rightBottomCorner, is(1));
        assertThat(cInfo.leftTopCorner, is(2));
        assertThat(cInfo.rightTopCorner, is(3));
        assertThat(cInfo.xOffsetInCell, is(0.25));
        assertThat(cInfo.yOffsetInCell, is(0.75));
    }

    @Test
    public void testGetCellInfoMultipleCells() throws Exception {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, 2));
        positions.set(b, new Coordinates(5, 6));
        positions.set(c, new Coordinates(3.1, 4.9));

        ModularPreMovement vectorField = new ModularPreMovement.VectorFieldSmoothing(3);

        ModularFdl modularFdl = new ModularFdl.ModularFdlBuilder(graph)
                .withPreMovmement(vectorField)
                .build();

        modularFdl.iterate(1);

        NodeCellInfo aInfo = Whitebox.<NodeCellInfo>invokeMethod(vectorField, "getNodeCellInfo", a);
        NodeCellInfo bInfo = Whitebox.<NodeCellInfo>invokeMethod(vectorField, "getNodeCellInfo", b);
        NodeCellInfo cInfo = Whitebox.<NodeCellInfo>invokeMethod(vectorField, "getNodeCellInfo", c);

        assertThat(aInfo.node, is(a));
        assertThat(aInfo.cellRow, is(0));
        assertThat(aInfo.cellColumn, is(0));
        assertThat(aInfo.leftBottomCorner, is(0));
        assertThat(aInfo.rightBottomCorner, is(1));
        assertThat(aInfo.leftTopCorner, is(4));
        assertThat(aInfo.rightTopCorner, is(5));
        assertThat(aInfo.xOffsetInCell, is(0.0));
        assertThat(aInfo.yOffsetInCell, is(0.0));

        assertThat(bInfo.node, is(b));
        assertThat(bInfo.cellRow, is(3));
        assertThat(bInfo.cellColumn, is(2));
        assertThat(bInfo.leftBottomCorner, is(14));
        assertThat(bInfo.rightBottomCorner, is(15));
        assertThat(bInfo.leftTopCorner, is(18));
        assertThat(bInfo.rightTopCorner, is(19));
        assertThat(bInfo.xOffsetInCell, is(1.0));
        assertThat(bInfo.yOffsetInCell, is(1.0));

        assertThat(cInfo.node, is(c));
        assertThat(cInfo.cellRow, is(2));
        assertThat(cInfo.cellColumn, is(1));
        assertThat(cInfo.leftBottomCorner, is(9));
        assertThat(cInfo.rightBottomCorner, is(10));
        assertThat(cInfo.leftTopCorner, is(13));
        assertThat(cInfo.rightTopCorner, is(14));
        assertThat(cInfo.xOffsetInCell, isAlmost(0.1));
        assertThat(cInfo.yOffsetInCell, isAlmost(0.9));
    }

    @Test
    public void testGetGetNeighbours() throws Exception {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(a, new Coordinates(2, 2));
        positions.set(b, new Coordinates(5, 6));

        ModularPreMovement vectorField = new ModularPreMovement.VectorFieldSmoothing(3);

        ModularFdl modularFdl = new ModularFdl.ModularFdlBuilder(graph)
                .withPreMovmement(vectorField)
                .build();

        modularFdl.iterate(1);

        assertThat(Whitebox.<Integer>getInternalState(vectorField, "xDim"), is(3));
        assertThat(Whitebox.<Integer>getInternalState(vectorField, "yDim"), is(4));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 0), Matchers.containsInAnyOrder(1, 4));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 1), Matchers.containsInAnyOrder(0, 2, 5));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 8), Matchers.containsInAnyOrder(4, 9, 12));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 10), Matchers.containsInAnyOrder(6, 9, 11, 14));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 11), Matchers.containsInAnyOrder(7, 10, 15));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 16), Matchers.containsInAnyOrder(12, 17));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 18), Matchers.containsInAnyOrder(14, 17, 19));
        assertThat(Whitebox.<List<Integer>>invokeMethod(vectorField, "getNeighborCorners", 19), Matchers.containsInAnyOrder(15, 18));
    }
}
