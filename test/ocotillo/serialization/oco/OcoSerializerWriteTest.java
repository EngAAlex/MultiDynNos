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
package ocotillo.serialization.oco;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.EvoBuilder;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class OcoSerializerWriteTest {

    @Test
    public void testWriteGraph() {
        Graph graph = new Graph();
        graph.newGraphAttribute(StdAttribute.label, "myGraph");
        graph.newGraphAttribute("myMetric", 2.0);

        OcoSerializer saver = new OcoSerializer();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();

        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute\tlabel\tmyMetric"));
        assertThat(iterator.next(), is("@type\tString\tDouble"));
        assertThat(iterator.next(), is("@default\tmyGraph\t2.0"));
    }

    @Test
    public void testWriteNodes() {
        Graph graph = new Graph();
        Node mario = graph.newNode("mario");
        Node luigi = graph.newNode("luigi");

        NodeAttribute<Integer> ranks = graph.newNodeAttribute("myRank", 10);
        ranks.set(mario, 1);
        ranks.set(luigi, 2);

        NodeAttribute<Coordinates> sizes = graph.nodeAttribute(StdAttribute.nodeSize);
        sizes.set(mario, new Coordinates(2, 3));
        sizes.set(luigi, new Coordinates(4, 5));

        OcoSerializer saver = new OcoSerializer();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();

        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute\tmyRank\tnodeSize"));
        assertThat(iterator.next(), is("@type\tInteger\tCoordinates"));
        assertThat(iterator.next(), is("@default\t10\t(1.0, 1.0)"));
        assertThat(iterator.next(), is("luigi\t2\t(4.0, 5.0)"));
        assertThat(iterator.next(), is("mario\t1\t(2.0, 3.0)"));
    }

    @Test
    public void testWriteEdges() {
        Graph graph = new Graph();
        Node mario = graph.newNode("mario");
        Node luigi = graph.newNode("luigi");
        Node peach = graph.newNode("peach");
        Edge ml = graph.newEdge("ml", mario, luigi);
        Edge lp = graph.newEdge("lp", luigi, peach);

        EdgeAttribute<Double> width = graph.edgeAttribute(StdAttribute.edgeWidth);
        width.set(ml, 1.3);
        width.set(lp, 3.4);

        EdgeAttribute<Color> myColor = graph.newEdgeAttribute("myColor", Color.RED);
        myColor.set(ml, Color.BLACK);
        myColor.set(lp, Color.GREEN);

        OcoSerializer saver = new OcoSerializer();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();

        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("luigi"));
        assertThat(iterator.next(), is("mario"));
        assertThat(iterator.next(), is("peach"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\t@from\t@to\tedgeWidth\tmyColor"));
        assertThat(iterator.next(), is("@type\t\t\tDouble\tColor"));
        assertThat(iterator.next(), is("@default\t\t\t0.2\t#ff0000ff"));
        assertThat(iterator.next(), is("lp\tluigi\tpeach\t3.4\t#00ff00ff"));
        assertThat(iterator.next(), is("ml\tmario\tluigi\t1.3\t#000000ff"));
    }

    @Test
    public void testWriteHierarchy() {
        Graph graph = new Graph();
        Node mario = graph.newNode("mario");
        Node luigi = graph.newNode("luigi");
        Node peach = graph.newNode("peach");
        Edge ml = graph.newEdge("ml", mario, luigi);
        Edge lp = graph.newEdge("lp", luigi, peach);

        EdgeAttribute<Double> width = graph.edgeAttribute(StdAttribute.edgeWidth);
        width.set(ml, 1.3);
        width.set(lp, 3.4);

        Graph subgraph = graph.newSubGraph();
        subgraph.add(mario);
        subgraph.add(luigi);
        subgraph.add(ml);

        EdgeAttribute<Color> myColor = subgraph.newLocalEdgeAttribute("myColor", Color.RED);
        myColor.set(ml, Color.BLACK);

        OcoSerializer saver = new OcoSerializer();
        List<String> lines = saver.write(graph);
        Iterator<String> iterator = lines.iterator();

        assertThat(iterator.next(), is("#graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("luigi"));
        assertThat(iterator.next(), is("mario"));
        assertThat(iterator.next(), is("peach"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\t@from\t@to\tedgeWidth"));
        assertThat(iterator.next(), is("@type\t\t\tDouble"));
        assertThat(iterator.next(), is("@default\t\t\t0.2"));
        assertThat(iterator.next(), is("lp\tluigi\tpeach\t3.4"));
        assertThat(iterator.next(), is("ml\tmario\tluigi\t1.3"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("##graph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("luigi"));
        assertThat(iterator.next(), is("mario"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\tmyColor"));
        assertThat(iterator.next(), is("@type\tColor"));
        assertThat(iterator.next(), is("@default\t#ff0000ff"));
        assertThat(iterator.next(), is("ml\t#000000ff"));
    }

    @Test
    public void testWriteDynamic() {
        DyGraph graph = new DyGraph();
        Node a = graph.newNode("a");
        Node b = graph.newNode("b");
        Edge ab = graph.newEdge("ab", a, b);

        DyNodeAttribute<Boolean> presence = graph.newNodeAttribute(StdAttribute.dyPresence, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 10), true).build());
        DyNodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Double> metric = graph.newNodeAttribute("myMetric", EvoBuilder.defaultAt(3.4)
                .withRect(Interval.newOpen(4, 5), 3.7, 8.0, Interpolation.Std.linear).build());

        DyEdgeAttribute<Color> color = graph.edgeAttribute(StdAttribute.color);

        presence.set(b, EvoBuilder.defaultAt(false).withConst(Interval.newClosed(2, 12), true).build());
        positions.set(a, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newOpen(10, 30), new Coordinates(0, 0), new Coordinates(3, 3), Interpolation.Std.largeGaussian)
                .build());
        metric.set(a, EvoBuilder.defaultAt(14.0).build());
        metric.set(b, EvoBuilder.defaultAt(16.0)
                .withConst(Interval.newLeftClosed(2, 4), 88.0)
                .withRect(Interval.newClosed(4, 14), 70.0, 25.0, Interpolation.Std.fastCharge)
                .build());

        color.set(ab, EvoBuilder.defaultAt(Color.RED)
                .withConst(Interval.newOpen(-5, 10), Color.BLUE).build());

        DyGraph subgraph = graph.newSubGraph();
        subgraph.add(a);
        subgraph.add(b);
        subgraph.add(ab);

        DyEdgeAttribute<Color> subcolor = subgraph.newLocalEdgeAttribute(StdAttribute.color, new Evolution<>(Color.GREEN));
        subcolor.set(ab, EvoBuilder.defaultAt(Color.RED).withConst(Interval.newRightClosed(3.0, 7.0), Color.BLUE).build());

        DyGraph subsubgraph = subgraph.newSubGraph();
        subsubgraph.add(b);

        OcoSerializer serializer = new OcoSerializer();
        Iterator<String> iterator = serializer.write(graph).iterator();

        assertThat(iterator.next(), is("#dygraph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute\tdyPresence\tmyMetric\tnodePosition"));
        assertThat(iterator.next(), is("@type\tBoolean\tDouble\tCoordinates"));
        assertThat(iterator.next(), is("@default\tfalse § const ^ [0.0, 10.0] ^ true\t3.4 § rect ^ (4.0, 5.0) ^ 3.7 ^ 8.0 ^ linear\t(0.0, 0.0)"));
        assertThat(iterator.next(), is("a\t\t14.0\t(0.0, 0.0) § rect ^ (10.0, 30.0) ^ (0.0, 0.0) ^ (3.0, 3.0) ^ largeGaussian"));
        assertThat(iterator.next(), is("b\tfalse § const ^ [2.0, 12.0] ^ true\t16.0 § const ^ [2.0, 4.0) ^ 88.0 § rect ^ [4.0, 14.0] ^ 70.0 ^ 25.0 ^ fastCharge\t"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\t@from\t@to\tcolor"));
        assertThat(iterator.next(), is("@type\t\t\tColor"));
        assertThat(iterator.next(), is("@default\t\t\t#000000ff"));
        assertThat(iterator.next(), is("ab\ta\tb\t#ff0000ff § const ^ (-5.0, 10.0) ^ #0000ffff"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("##dygraph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("a"));
        assertThat(iterator.next(), is("b"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute\tcolor"));
        assertThat(iterator.next(), is("@type\tColor"));
        assertThat(iterator.next(), is("@default\t#00ff00ff"));
        assertThat(iterator.next(), is("ab\t#ff0000ff § const ^ (3.0, 7.0] ^ #0000ffff"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("###dygraph"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#nodes"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is("b"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is("#edges"));
        assertThat(iterator.next(), is("@attribute"));
        assertThat(iterator.next(), is("@type"));
        assertThat(iterator.next(), is("@default"));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.next(), is(""));
        assertThat(iterator.hasNext(), is(false));
    }
}
