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

import ocotillo.serialization.dot.DotReader;
import ocotillo.serialization.dot.DotTools;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Polygon;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.serialization.ParserTools;
import ocotillo.serialization.ParserTools.EscapedString;
import ocotillo.serialization.dot.DotReader.ColoredPolygon;
import ocotillo.serialization.dot.DotReader.DotReaderBuilder;
import ocotillo.serialization.dot.DotTools.DotAttributes;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class DotReaderTest {

    @Test
    public void combineLines() throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("graph {   ");
        lines.add("  \t  graph [ _draw_=\" \\  ");
        lines.add("   \t \t  polygon \" ];");
        lines.add("    node [ label=\"mango\",");
        lines.add("           position=\"upperleft\"];");
        lines.add("  }  ");
        lines.add("");
        lines.add("");

        List<String> combinedLines = Whitebox.<List<String>>invokeMethod(DotReader.class, "combineLines", lines);

        assertThat(combinedLines.size(), is(4));
        assertThat(combinedLines.get(0), is("graph {"));
        assertThat(combinedLines.get(1), is("graph [ _draw_=\" \\ polygon \" ];"));
        assertThat(combinedLines.get(2), is("node [ label=\"mango\", position=\"upperleft\"];"));
        assertThat(combinedLines.get(3), is("}"));
    }

    @Test
    public void escapeString() throws Exception {
        String line = "label=\" \\\"ugly\\\" purple\"";
        EscapedString eString = Whitebox.<EscapedString>invokeMethod(DotReader.class, "escapeLine", line);

        assertThat(eString.substitutionChar, is("§"));
        assertThat(eString.withSubstitutions, is("label=§0§"));
        assertThat(eString.extractedStrings.size(), is(1));
        assertThat(eString.extractedStrings.get(0), is(" \\\"ugly\\\" purple"));
    }

    @Test
    public void splitLine() throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("graph {");
        lines.add("graph [ _draw_=\" \\ polygon \"] ;");
        lines.add("node [ label=\"mango\", position=\"upperleft\"] \t ;");
        lines.add("edge [ ] ;");
        lines.add("apple [label=\"melinda\"]    ;");
        lines.add("\"bana--na\" [label=\"chiquita\"];");
        lines.add("apple -- \"bana--na\";");
        lines.add("}");

        List<List<String>> tokensList = new ArrayList<>();
        for (String line : lines) {
            EscapedString eString = new EscapedString(line, "\"");
            List<String> tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
            tokensList.add(tokens);
        }

        assertThat(tokensList.get(0).size(), is(1));
        assertThat(tokensList.get(0).get(0), is("graph {"));
        assertThat(tokensList.get(1).size(), is(2));
        assertThat(tokensList.get(1).get(0), is("graph"));
        assertThat(tokensList.get(1).get(1), is("_draw_=§0§"));
        assertThat(tokensList.get(2).size(), is(2));
        assertThat(tokensList.get(2).get(0), is("node"));
        assertThat(tokensList.get(2).get(1), is("label=§0§, position=§1§"));
        assertThat(tokensList.get(3).size(), is(1));
        assertThat(tokensList.get(3).get(0), is("edge"));
        assertThat(tokensList.get(4).size(), is(2));
        assertThat(tokensList.get(4).get(0), is("apple"));
        assertThat(tokensList.get(4).get(1), is("label=§0§"));
        assertThat(tokensList.get(5).size(), is(2));
        assertThat(tokensList.get(5).get(0), is("§0§"));
        assertThat(tokensList.get(5).get(1), is("label=§1§"));
        assertThat(tokensList.get(6).size(), is(1));
        assertThat(tokensList.get(6).get(0), is("apple -- §0§"));
        assertThat(tokensList.get(7).size(), is(1));
        assertThat(tokensList.get(7).get(0), is("}"));
    }

    @Test
    public void extractAttributes() throws Exception {
        ParserTools.EscapedString eString = new ParserTools.EscapedString("4 [cluster=\"1\", clustercolour=\"#fbb4ae\", colour=\"#a442a0\", fontsize=\"14\", height=\"0.31944\", label=\"ugly purple\", pos=\"564.53,298.75\", width=\"1.3194\"];", "\"");
        List<String> tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        DotAttributes attributes = Whitebox.<DotAttributes>invokeMethod(DotReader.class, "extractAttributes", eString, tokens.get(1));

        assertThat(attributes.size(), is(8));
        assertThat(attributes.containsKey("cluster"), is(true));
        assertThat(attributes.containsKey("clustercolour"), is(true));
        assertThat(attributes.containsKey("colour"), is(true));
        assertThat(attributes.containsKey("fontsize"), is(true));
        assertThat(attributes.containsKey("height"), is(true));
        assertThat(attributes.containsKey("label"), is(true));
        assertThat(attributes.containsKey("pos"), is(true));
        assertThat(attributes.containsKey("width"), is(true));
        assertThat(attributes.get("cluster"), is("1"));
        assertThat(attributes.get("clustercolour"), is("#fbb4ae"));
        assertThat(attributes.get("colour"), is("#a442a0"));
        assertThat(attributes.get("fontsize"), is("14"));
        assertThat(attributes.get("height"), is("0.31944"));
        assertThat(attributes.get("label"), is("ugly purple"));
        assertThat(attributes.get("pos"), is("564.53,298.75"));
        assertThat(attributes.get("width"), is("1.3194"));
    }

    @Test
    public void parseOpeningLine() throws Exception {
        String line;
        EscapedString eString;
        List<String> tokens;
        DotAttributes attributes = new DotAttributes();

        line = "graph {";
        eString = new EscapedString(line, "\"");
        tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        Whitebox.invokeMethod(DotReader.class, "parseOpeningLine", attributes, eString, tokens);

        assertThat(attributes.size(), is(1));
        assertThat(attributes, hasKey(DotTools.directedAttr));
        assertThat(attributes.get(DotTools.directedAttr), is("false"));
        attributes.clear();

        line = "strict graph franco {";
        eString = new EscapedString(line, "\"");
        tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        Whitebox.invokeMethod(DotReader.class, "parseOpeningLine", attributes, eString, tokens);

        assertThat(attributes.size(), is(3));
        assertThat(attributes, hasKey(DotTools.directedAttr));
        assertThat(attributes, hasKey(DotTools.strictAttr));
        assertThat(attributes, hasKey(DotTools.graphNameAttr));
        assertThat(attributes.get(DotTools.directedAttr), is("false"));
        assertThat(attributes.get(DotTools.strictAttr), is("true"));
        assertThat(attributes.get(DotTools.graphNameAttr), is("franco"));
        attributes.clear();

        line = "digraph mario {";
        eString = new EscapedString(line, "\"");
        tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        Whitebox.invokeMethod(DotReader.class, "parseOpeningLine", attributes, eString, tokens);

        assertThat(attributes.size(), is(2));
        assertThat(attributes, hasKey(DotTools.directedAttr));
        assertThat(attributes, hasKey(DotTools.graphNameAttr));
        assertThat(attributes.get(DotTools.directedAttr), is("true"));
        assertThat(attributes.get(DotTools.graphNameAttr), is("mario"));
        attributes.clear();

        line = "strict digraph \"luigi\" {";
        eString = new EscapedString(line, "\"");
        tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        Whitebox.invokeMethod(DotReader.class, "parseOpeningLine", attributes, eString, tokens);

        assertThat(attributes.size(), is(3));
        assertThat(attributes, hasKey(DotTools.directedAttr));
        assertThat(attributes, hasKey(DotTools.strictAttr));
        assertThat(attributes, hasKey(DotTools.graphNameAttr));
        assertThat(attributes.get(DotTools.directedAttr), is("true"));
        assertThat(attributes.get(DotTools.strictAttr), is("true"));
        assertThat(attributes.get(DotTools.graphNameAttr), is("luigi"));
        attributes.clear();
    }

    @Test
    public void parseGlobalLine() throws Exception {
        String line;
        EscapedString eString;
        List<String> tokens;
        DotAttributes attributes = new DotAttributes();

        line = "graph [];";
        eString = new EscapedString(line, "\"");
        tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        Whitebox.invokeMethod(DotReader.class, "parseGlobalLine", attributes, eString, tokens);

        assertThat(attributes.size(), is(0));
        attributes.clear();

        line = "node   [attr=value];";
        eString = new EscapedString(line, "\"");
        tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        Whitebox.invokeMethod(DotReader.class, "parseGlobalLine", attributes, eString, tokens);

        assertThat(attributes.size(), is(1));
        assertThat(attributes, hasKey("attr"));
        assertThat(attributes.get("attr"), is("value"));
        attributes.clear();

        line = "edge ;";
        eString = new EscapedString(line, "\"");
        tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
        Whitebox.invokeMethod(DotReader.class, "parseGlobalLine", attributes, eString, tokens);

        assertThat(attributes.size(), is(0));
        attributes.clear();
    }

    @Test
    public void parseNodeLine() throws Exception {
        Map<String, DotAttributes> attributes = new HashMap<>();
        List<String> lines = new ArrayList<>();
        lines.add("franco [];");
        lines.add("\"mario -- plumber\"   [attr=value, attr2=\"10, 20, 30\"];");
        lines.add("\"node\" ;");

        for (String line : lines) {
            EscapedString eString = new EscapedString(line, "\"");
            List<String> tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
            Whitebox.invokeMethod(DotReader.class, "parseNodeLine", attributes, eString, tokens);
        }

        assertThat(attributes.size(), is(3));
        assertThat(attributes, hasKey("franco"));
        assertThat(attributes, hasKey("mario -- plumber"));
        assertThat(attributes, hasKey("node"));
        assertThat(attributes.get("franco").size(), is(0));
        assertThat(attributes.get("mario -- plumber").size(), is(2));
        assertThat(attributes.get("node").size(), is(0));
        assertThat(attributes.get("mario -- plumber"), hasKey("attr"));
        assertThat(attributes.get("mario -- plumber"), hasKey("attr2"));
        assertThat(attributes.get("mario -- plumber").get("attr"), is("value"));
        assertThat(attributes.get("mario -- plumber").get("attr2"), is("10, 20, 30"));
    }

    @Test
    public void parseEdgeLine() throws Exception {
        List<DotAttributes> attributes = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        lines.add("mario -- franco [];");
        lines.add("\"mario\" -- \"franco\"   [attr=value];");
        lines.add("\"node\" -> \"edge\" -- \"graph\" [colour=\"blue\"];");

        for (String line : lines) {
            EscapedString eString = new EscapedString(line, "\"");
            List<String> tokens = Whitebox.<List<String>>invokeMethod(DotReader.class, "splitLine", eString);
            Whitebox.invokeMethod(DotReader.class, "parseEdgeLine", attributes, eString, tokens);
        }

        assertThat(attributes.size(), is(4));
        assertThat(attributes.get(0).size(), is(3));
        assertThat(attributes.get(0).get(DotTools.edgeSourceAttr), is("mario"));
        assertThat(attributes.get(0).get(DotTools.edgeTargetAttr), is("franco"));
        assertThat(attributes.get(0).get(DotTools.directedAttr), is("false"));
        assertThat(attributes.get(1).size(), is(4));
        assertThat(attributes.get(1).get(DotTools.edgeSourceAttr), is("mario"));
        assertThat(attributes.get(1).get(DotTools.edgeTargetAttr), is("franco"));
        assertThat(attributes.get(1).get(DotTools.directedAttr), is("false"));
        assertThat(attributes.get(1).get("attr"), is("value"));
        assertThat(attributes.get(2).size(), is(4));
        assertThat(attributes.get(2).get(DotTools.edgeSourceAttr), is("node"));
        assertThat(attributes.get(2).get(DotTools.edgeTargetAttr), is("edge"));
        assertThat(attributes.get(2).get(DotTools.directedAttr), is("true"));
        assertThat(attributes.get(2).get("colour"), is("blue"));
        assertThat(attributes.get(3).size(), is(4));
        assertThat(attributes.get(3).get(DotTools.edgeSourceAttr), is("edge"));
        assertThat(attributes.get(3).get(DotTools.edgeTargetAttr), is("graph"));
        assertThat(attributes.get(3).get(DotTools.directedAttr), is("false"));
        assertThat(attributes.get(3).get("colour"), is("blue"));
    }

    @Test
    public void parseGraphWithGlobalAttributes() {
        List<String> lines = new ArrayList<>();
        lines.add("strict digraph mario {");
        lines.add("node [label=Franco, pos=\"0.2,4.6\"];");
        lines.add("edge [colour=blue, size=big  ];");
        lines.add("node [colour=red];");
        lines.add("}");

        DotReaderBuilder builder = new DotReader.DotReaderBuilder();
        builder.saveHardcodedAttributes(true);
        builder.nodeAttributes.convert("pos", "position", Coordinates.class)
                .saveUnspecified(true);
        builder.edgeAttributes.convert("colour", "colour", String.class)
                .saveUnspecified(false);

        DotReader reader = builder.build();
        Graph graph = reader.parseFile(lines);
        
        assertThat(graph.nodeCount(), is(0));
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.<Boolean>graphAttribute("strict").get(), is(true));
        assertThat(graph.<Boolean>graphAttribute("directed").get(), is(true));
        assertThat(graph.<String>graphAttribute("name").get(), is("mario"));
        assertThat(graph.<Coordinates>nodeAttribute("position").getDefault(), is(new Coordinates(0.2, 4.6, 0)));
        assertThat(graph.<String>nodeAttribute("label").getDefault(), is("Franco"));
        assertThat(graph.<String>nodeAttribute("colour").getDefault(), is("red"));
    }

    @Test
    public void parseGraphWithNodes() {
        List<String> lines = new ArrayList<>();
        lines.add("graph {");
        lines.add("apple [colour=red];");
        lines.add("banana [colour=yellow];");
        lines.add("kiwi [colour=green, price=10.2];");
        lines.add("}");
        
        DotReaderBuilder builder = new DotReader.DotReaderBuilder();
        builder.nodeAttributes.convert("price", "Price", Double.class)
                .saveUnspecified(true);

        DotReader reader = builder.build();
        Graph graph = reader.parseFile(lines);

        assertThat(graph.nodeCount(), is(3));
        assertThat(graph.hasNode("apple"), is(true));
        assertThat(graph.hasNode("banana"), is(true));
        assertThat(graph.hasNode("kiwi"), is(true));
        assertThat(graph.hasNodeAttribute("colour"), is(true));
        assertThat(graph.hasNodeAttribute("Price"), is(true));

        NodeAttribute<String> colour = graph.nodeAttribute("colour");
        assertThat(colour.getDefault(), is(""));
        assertThat(colour.get(graph.getNode("apple")), is("red"));
        assertThat(colour.get(graph.getNode("banana")), is("yellow"));
        assertThat(colour.get(graph.getNode("kiwi")), is("green"));

        NodeAttribute<Double> price = graph.nodeAttribute("Price");
        assertThat(price.getDefault(), is(0.0));
        assertThat(price.get(graph.getNode("apple")), is(0.0));
        assertThat(price.get(graph.getNode("banana")), is(0.0));
        assertThat(price.get(graph.getNode("kiwi")), is(10.2));
    }

    @Test
    public void parseGraphWithEdges() {
        List<String> lines = new ArrayList<>();
        lines.add("graph {");
        lines.add("apple [colour=red];");
        lines.add("banana [colour=yellow];");
        lines.add("kiwi [colour=green, price=10.2];");
        lines.add("kiwi -- banana [weight=1];");
        lines.add("banana -- apple -> kiwi [weight=4];");
        lines.add("}");

        DotReaderBuilder builder = new DotReader.DotReaderBuilder();
        builder.edgeAttributes.convert("weight", "weight", Integer.class);

        DotReader reader = builder.build();
        Graph graph = reader.parseFile(lines);

        assertThat(graph.edgeCount(), is(3));
        assertThat(graph.hasEdgeAttribute("weight"), is(true));

        for (Edge edge : graph.edges()) {
            if (edge.source() == graph.getNode("kiwi")
                    && edge.target() == graph.getNode("banana")) {
                assertThat(graph.<Integer>edgeAttribute("weight").get(edge), is(1));
            } else {
                assertThat(graph.<Integer>edgeAttribute("weight").get(edge), is(4));
            }
        }
    }

    @Test
    public void parseGraphWithCombinedAttributes() {
        List<String> lines = new ArrayList<>();
        lines.add("graph {");
        lines.add("nodes [colour=blue, size=big  ];");
        lines.add("}");
 
        DotReaderBuilder builder = new DotReader.DotReaderBuilder();
        builder.nodeAttributes.convert("colour|size", "first", String.class)
                .convert("size,colour-colour", "second", String.class);

        DotReader reader = builder.build();
        Graph graph = reader.parseFile(lines);

        assertThat(graph.<String>nodeAttribute("first").getDefault(), is("blue|big"));
        assertThat(graph.<String>nodeAttribute("second").getDefault(), is("big,blue-blue"));
    }

    @Test
    public void parseGraphWithClusters() {
        List<String> lines = new ArrayList<>();
        lines.add("graph {");
        lines.add("apple [colour=red, colourIdx=1];");
        lines.add("banana [colour=yellow, colourIdx=2];");
        lines.add("kiwi [colour=green, colourIdx=3, price=10.2];");
        lines.add("mango [colour=red, colourIdx=1, price=6.6];");
        lines.add("}");

        DotReaderBuilder builder = new DotReader.DotReaderBuilder();
        builder.clusterBy("colour")
                .convert("colour", "clusterId", String.class)
                .convert("colourIdx", "colourIdx", Integer.class);

        DotReader reader = builder.build();
        Graph graph = reader.parseFile(lines);

        assertThat(graph.subGraphs().size(), is(3));
        for (Graph cluster : graph.subGraphs()) {
            String clusterId = cluster.<String>graphAttribute("clusterId").get();
            switch (clusterId) {
                case "red":
                    assertThat(cluster.<Integer>graphAttribute("colourIdx").get(), is(1));
                    break;
                case "green":
                    assertThat(cluster.<Integer>graphAttribute("colourIdx").get(), is(3));
                    break;
                case "yellow":
                    assertThat(cluster.<Integer>graphAttribute("colourIdx").get(), is(2));
                    break;
                default:
                    assertThat(false, is(true));
            }
        }
    }

    @Test
    public void extractPolygonsTest() throws Exception {
        String polygonString = "\\ c 9 -#ff4d4d88 C 9 -#ff4d4d88 P 4  240.0 442.0 264.0 450.0 296.0 450.0 324.0 446.0 \\  c 9 -#ff4d4d88 C 9 -#ff4d4d88 P 2 240.0 442.0 264.0 450.0";

        DotReaderBuilder builder = new DotReader.DotReaderBuilder();
        builder.nodeAttributes.convert("pos", StdAttribute.nodePosition, Coordinates.class);
        DotReader reader = builder.build();

        List<ColoredPolygon> polygons = Whitebox.<List<ColoredPolygon>>invokeMethod(reader, "extractPolygons", polygonString);
        assertThat(polygons.size(), is(2));
        assertThat(polygons.get(0).polygon.size(), is(4));
        assertThat(polygons.get(0).polygon.get(0), is(new Coordinates(240.0, 442.0)));
        assertThat(polygons.get(0).polygon.get(1), is(new Coordinates(264.0, 450.0)));
        assertThat(polygons.get(0).polygon.get(2), is(new Coordinates(296.0, 450.0)));
        assertThat(polygons.get(0).polygon.get(3), is(new Coordinates(324.0, 446.0)));
        assertThat(polygons.get(1).polygon.size(), is(2));
        assertThat(polygons.get(1).polygon.get(0), is(new Coordinates(240.0, 442.0)));
        assertThat(polygons.get(1).polygon.get(1), is(new Coordinates(264.0, 450.0)));

        assertThat(polygons.get(0).fillColor, is(new Color(255, 77, 77, 136)));
        assertThat(polygons.get(1).fillColor, is(new Color(255, 77, 77, 136)));
    }

    @Test
    public void parsingTest() {
        List<String> lines = new ArrayList<>();
        lines.add("graph {");
        lines.add("	graph [ outputorder=edgesfirst, _draw_=\" \\");
        lines.add("		c 9 -#ff4d4d88 C 9 -#ff4d4d88 P 3  0.0 0.0 200.0 0.0 100.0 100.0  \\");
        lines.add("		c 9 -#ff4d4d88 C 9 -#ff4d4d88 P 3  -0.0 -0.0 -200.0 -0.0 -100.0 -100.0  \"];");
        lines.add("  graph [bb=\"0,0,831.05,647.24\", bgcolour=\"#dae2ff\", charset=\"latin1\", outputorder=\"edgesfirst\", overlap=\"prism\"];");
        lines.add("    4 [cluster=\"1\", clustercolour=\"#fbb4ae\", colour=\"#a442a0\", fontsize=\"14\", height=\"0.31944\", label=\"ugly purple\", pos=\"100.53,50.75\", width=\"1.3194\"];");
        lines.add("    11 [cluster=\"2\", clustercolour=\"#decbe4\", colour=\"#3b5b92\", fontsize=\"14\", height=\"0.31944\", label=\"denim blue\", pos=\"-100.97,-50.64\", width=\"1.2917\"];");
        lines.add("    4 -- 11 [len=\"0.09877587\", pos=\"574.79,310.37 589.82,327.4 617.68,358.98 632.71,376.01\", weight=\"0.01179\", weight_from_length=\"10.12393\"];");
        lines.add("}");
        lines.add("");

        DotReaderBuilder builder = new DotReader.DotReaderBuilder();
        builder.nodeAttributes.convert("pos", StdAttribute.nodePosition, Coordinates.class);
        builder.clusterBy("cluster")
                .convert("cluster", "cluster", String.class);

        DotReader reader = builder.build();
        Graph graph = reader.parseFile(lines);

        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.hasNode("4"), is(true));
        assertThat(graph.hasNode("11"), is(true));
        assertThat(graph.edgeCount(), is(1));

        Edge edge = graph.edges().iterator().next();
        assertThat(edge.source().id(), is("4"));
        assertThat(edge.target().id(), is("11"));

        assertThat(graph.subGraphs().size(), is(2));
        for (Graph subGraph : graph.subGraphs()) {
            String clusterId = subGraph.<String>graphAttribute("cluster").get();
            List<Polygon> polygons = subGraph.<List<Polygon>>graphAttribute(DotTools.polygonIdAttr).get();
            switch (clusterId) {
                case "1":
                    assertThat(subGraph.nodeCount(), is(1));
                    assertThat(subGraph.hasNode("4"), is(true));
                    assertThat(polygons.size(), is(1));
                    polygons.get(0).contains(new Coordinates(0, 0, 0));
                    polygons.get(0).contains(new Coordinates(200, 0, 0));
                    polygons.get(0).contains(new Coordinates(100, 100, 0));
                    break;
                case "2":
                    assertThat(subGraph.nodeCount(), is(1));
                    assertThat(subGraph.hasNode("11"), is(true));
                    assertThat(polygons.size(), is(1));
                    polygons.get(0).contains(new Coordinates(0, 0, 0));
                    polygons.get(0).contains(new Coordinates(-200, 0, 0));
                    polygons.get(0).contains(new Coordinates(-100, -100, 0));
                    break;
            }
        }
    }
}
