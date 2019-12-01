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
package ocotillo.serialization.dot;

import ocotillo.serialization.dot.DotTools;
import ocotillo.serialization.dot.DotWriter;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.serialization.dot.DotWriter.DotWriterBuilder;
import ocotillo.serialization.dot.DotValueConverter.CoordDimensionConverter;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class DotWriterTest {

    @Test
    public void writeGraphWithGlobalAttributes() {
        Graph graph = new Graph();
        graph.newGraphAttribute("graphName", "Luigi");
        graph.newGraphAttribute("graphStrict", "true");

        graph.newGraphAttribute("attr1", "Mario");
        graph.newGraphAttribute("attr2", 4.5);
        graph.newNodeAttribute("nodeAttr", "~");
        graph.newEdgeAttribute("edgeAttr", "?");

        DotWriterBuilder builder = new DotWriterBuilder();
        builder.graphAttributes.convert("graphName", DotTools.graphNameAttr)
                .convert("graphStrict", DotTools.strictAttr)
                .convert("attr1", "label")
                .convert("attr2", "weight");
        builder.nodeAttributes.saveUnspecified(true);

        DotWriter writer = builder.build();
        List<String> dotLines = writer.writeGraph(graph);

        assertThat(dotLines.size(), is(5));
        assertThat(dotLines.get(0), is("strict graph Luigi {"));
        assertThat(dotLines.get(1), is("\tgraph [label=\"Mario\", weight=\"4.5\"];"));
        assertThat(dotLines.get(2), is("\tnode [nodeAttr=\"~\"];"));
        assertThat(dotLines.get(3), is("\tedge [ ];"));
        assertThat(dotLines.get(4), is("}"));
    }

    @Test
    public void writeGraphWithNodeAttributes() {
        Graph graph = new Graph();
        graph.newGraphAttribute("graphName", "Luigi");
        graph.newGraphAttribute("graphStrict", "true");
        graph.newGraphAttribute("attr1", "Mario");
        graph.newGraphAttribute("attr2", 4.5);
        graph.newNodeAttribute("nodeAttr", "~");
        graph.newNodeAttribute("nodeSize", new Coordinates(0, 0));
        graph.newEdgeAttribute("edgeAttr", "?");

        Node apple = graph.newNode("apple");
        Node banana = graph.newNode("banana");
        graph.<String>nodeAttribute("nodeAttr").set(apple, "I'm apple");
        graph.<Coordinates>nodeAttribute("nodeSize").set(banana, new Coordinates(3, 4));

        DotWriterBuilder builder = new DotWriterBuilder();
        builder.graphAttributes.convert("graphName", DotTools.graphNameAttr)
                .convert("graphStrict", DotTools.strictAttr)
                .convert("attr1", "label")
                .convert("attr2", "weight");
        builder.nodeAttributes.convert("nodeSize", "width", new CoordDimensionConverter(0))
                .convert("nodeSize", "height", new CoordDimensionConverter(1))
                .saveUnspecified(true);

        DotWriter writer = builder.build();
        List<String> dotLines = writer.writeGraph(graph);

        assertThat(dotLines.size(), is(7));
        assertThat(dotLines.get(0), is("strict graph Luigi {"));
        assertThat(dotLines.get(1), is("\tgraph [label=\"Mario\", weight=\"4.5\"];"));
        assertThat(dotLines.get(2), is("\tnode [height=\"0.00\", nodeAttr=\"~\", width=\"0.00\"];"));
        assertThat(dotLines.get(3), is("\tedge [ ];"));
        assertThat(dotLines.get(4), is("\tapple [nodeAttr=\"I'm apple\"];"));
        assertThat(dotLines.get(5), is("\tbanana [height=\"4.00\", width=\"3.00\"];"));
        assertThat(dotLines.get(6), is("}"));
    }

    @Test
    public void writeGraphWithClusterAttributes() {
        Graph graph = new Graph();
        graph.newGraphAttribute("graphName", "Luigi");
        graph.newGraphAttribute("graphStrict", "true");
        graph.newGraphAttribute("attr1", "Mario");
        graph.newGraphAttribute("attr2", 4.5);
        graph.newNodeAttribute("nodeAttr", "~");
        graph.newEdgeAttribute("edgeAttr", "?");

        Node apple = graph.newNode("apple");
        Node orange = graph.newNode("orange");
        Node banana = graph.newNode("banana");
        graph.<String>nodeAttribute("nodeAttr").set(apple, "I'm apple");

        Graph roundFruit = graph.newSubGraph();
        roundFruit.add(apple);
        roundFruit.add(orange);
        roundFruit.newLocalGraphAttribute("shape", "round");
        roundFruit.newLocalGraphAttribute("name", "Round Fruit!");

        Graph otherFruit = graph.newSubGraph();
        otherFruit.add(banana);
        otherFruit.newLocalGraphAttribute("shape", "other");
        otherFruit.newLocalGraphAttribute("name", "Wierdly Shaped Fruit!");

        DotWriterBuilder builder = new DotWriterBuilder();
        builder.graphAttributes.convert("graphName", DotTools.graphNameAttr)
                .convert("graphStrict", DotTools.strictAttr)
                .convert("attr1", "label")
                .convert("attr2", "weight");
        builder.clusterToNodeAttributes.convert("shape", "cluster")
                .convert("name", "clusterName");
        builder.nodeAttributes.saveUnspecified(true);

        DotWriter writer = builder.build();
        List<String> dotLines = writer.writeGraph(graph);

        assertThat(dotLines.size(), is(8));
        assertThat(dotLines.get(0), is("strict graph Luigi {"));
        assertThat(dotLines.get(1), is("\tgraph [label=\"Mario\", weight=\"4.5\"];"));
        assertThat(dotLines.get(2), is("\tnode [nodeAttr=\"~\"];"));
        assertThat(dotLines.get(3), is("\tedge [ ];"));
        assertThat(dotLines.get(4), is("\tapple [cluster=\"round\", clusterName=\"Round Fruit!\", nodeAttr=\"I'm apple\"];"));
        assertThat(dotLines.get(5), is("\tbanana [cluster=\"other\", clusterName=\"Wierdly Shaped Fruit!\"];"));
        assertThat(dotLines.get(6), is("\torange [cluster=\"round\", clusterName=\"Round Fruit!\"];"));
        assertThat(dotLines.get(7), is("}"));
    }
}
