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
package ocotillo.graph.layout.locator.intervaltree;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.layout.locator.ElementLocator;
import ocotillo.graph.layout.locator.ElementLocator.EdgePolicy;
import ocotillo.graph.layout.locator.ElementLocator.NodePolicy;
import ocotillo.graph.layout.locator.ElementLocatorAbstTestBase;
import ocotillo.graph.layout.locator.intervaltree.IntervalTreeLocator.ItlBuilder;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class IntervalTreeLocatorTest extends ElementLocatorAbstTestBase {

    @Override
    protected IntervalTreeLocator createInstance() {
        return new ItlBuilder(graph, NodePolicy.nodesAsGlyphs, EdgePolicy.edgesAsGlyphs).build();
    }

    @Test
    public final void getCloseNodesWithAutoPositionUpdate() {
        sizes.setDefault(new Coordinates(1, 1));
        Coordinates pointZero = new Coordinates(0, 0);

        Node a1 = graph.newNode();
        Node a2 = graph.newNode();
        Edge a = graph.newEdge(a1, a2);

        Node b = graph.newNode();

        positions.set(a1, new Coordinates(0, 0));
        positions.set(a2, new Coordinates(0, 1));
        positions.set(b, new Coordinates(1, 0));

        ElementLocator locator = new ItlBuilder(graph, NodePolicy.nodesAsGlyphs, EdgePolicy.edgesAsGlyphs).enableAutoSync().build();

        assertThat(locator.getCloseNodes(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a1, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a, 2), hasItem(b));

        positions.set(b, new Coordinates(100, 100));

        assertThat(locator.getCloseNodes(pointZero, 2), not(hasItem(b)));
        assertThat(locator.getCloseNodes(a1, 2), not(hasItem(b)));
        assertThat(locator.getCloseNodes(a, 2), not(hasItem(b)));

        positions.set(b, new Coordinates(1, 1));

        assertThat(locator.getCloseNodes(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a1, 2), hasItem(b));
        assertThat(locator.getCloseNodes(a, 2), hasItem(b));

        locator.close();
    }

    @Test
    public final void getCloseEdgesWithAutoPositionUpdate() {
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

        ElementLocator locator = new ItlBuilder(graph, NodePolicy.nodesAsGlyphs, EdgePolicy.edgesAsGlyphs).enableAutoSync().build();

        assertThat(locator.getCloseEdges(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a, 2), hasItem(b));

        positions.set(b1, new Coordinates(100, 100));
        positions.set(b2, new Coordinates(100, 101));

        assertThat(locator.getCloseEdges(pointZero, 2), not(hasItem(b)));
        assertThat(locator.getCloseEdges(a1, 2), not(hasItem(b)));
        assertThat(locator.getCloseEdges(a, 2), not(hasItem(b)));

        positions.set(b1, new Coordinates(1, 0));
        positions.set(b2, new Coordinates(1, 1));

        assertThat(locator.getCloseEdges(pointZero, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a1, 2), hasItem(b));
        assertThat(locator.getCloseEdges(a, 2), hasItem(b));

        locator.close();
    }

}
