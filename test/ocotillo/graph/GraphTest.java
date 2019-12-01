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
package ocotillo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class GraphTest {

    /////////////  Nodes  ////////////
    @Test
    public void testNodes() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        Node c = graph.newNode();

        Set<Node> currentNodes = new HashSet<>(graph.nodes());

        assertThat(currentNodes.contains(a), is(true));
        assertThat(currentNodes.contains(b), is(true));
        assertThat(currentNodes.contains(c), is(true));
    }

    @Test
    public void testNewNode() {
        Graph graph = new Graph();
        assertThat(graph.nodeCount(), is(0));

        Node a = graph.newNode();
        assertThat(graph.nodeCount(), is(1));

        Node b = graph.newNode("MyID");
        assertThat(graph.nodeCount(), is(2));
        assertThat(b.id(), is("MyID"));

        Node c = graph.newNode();
        Node d = graph.newNode();
        assertThat(graph.nodeCount(), is(4));

    }

    @Test
    public void testAddNode() {
        Graph graph = new Graph();
        Node a = new Node("MyID");
        graph.add(a);
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.has(a), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSameNode() {
        Graph graph = new Graph();
        Node a = new Node("MyID");
        graph.add(a);
        graph.add(a);
    }

    @Test
    public void testForcedAddSameNode() {
        Graph graph = new Graph();
        Node a = new Node("MyID");
        graph.forcedAdd(a);
        graph.forcedAdd(a);
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.has(a), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeWithSameId() {
        Graph graph = new Graph();
        Node a = new Node("MyID");
        Node b = new Node("MyID");
        graph.add(a);
        graph.add(b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForcedAddNodeWithSameId() {
        Graph graph = new Graph();
        Node a = new Node("MyID");
        Node b = new Node("MyID");
        graph.forcedAdd(a);
        graph.forcedAdd(b);
    }

    @Test
    public void testHasNode() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        assertThat(graph.hasNode(a.id()), is(true));
        assertThat(graph.hasNode(b.id()), is(true));
    }

    @Test
    public void testGetNode() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        assertThat(graph.getNode(a.id()) == a, is(true));
        assertThat(graph.getNode(b.id()) == b, is(true));
    }

    @Test
    public void testRemoveNode() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        assertThat(graph.nodeCount(), is(2));

        graph.remove(b);
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.nodes().contains(a), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNodeThatDoesNotExist() {
        Graph graph = new Graph();
        Node a = new Node("a");
        assertThat(graph.has(a), is(false));

        graph.remove(a);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNodeWithIncidentEdges() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        Edge ab = graph.newEdge(a, b);
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.edgeCount(), is(1));

        graph.remove(b);
    }

    @Test
    public void testForcedRemoveNodeThatDoesNotExist() {
        Graph graph = new Graph();
        Node a = new Node("a");
        assertThat(graph.has(a), is(false));

        graph.forcedRemove(a);
        assertThat(graph.nodeCount(), is(0));
    }

    @Test
    public void testForcedRemoveNodeWithIncidentEdges() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        Edge ab = graph.newEdge(a, b);
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.edgeCount(), is(1));

        graph.forcedRemove(b);
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.edgeCount(), is(0));
    }

    /////////////  Edges  ////////////
    @Test
    public void testEdges() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab1 = graph.newEdge("ab", a, b);
        Edge ab2 = graph.newEdge(a, b);
        Edge ba1 = graph.newEdge("3", b, a);

        Set<Edge> currentEdges = new HashSet<>(graph.edges());

        assertThat(currentEdges.contains(ab1), is(true));
        assertThat(currentEdges.contains(ab2), is(true));
        assertThat(currentEdges.contains(ba1), is(true));
    }

    @Test
    public void testNewEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        assertThat(graph.edgeCount(), is(0));

        Edge ab = graph.newEdge(a, b);
        assertThat(graph.edgeCount(), is(1));

        Edge aa = graph.newEdge("aa", a, a);
        assertThat(graph.edgeCount(), is(2));
        assertThat(aa.id(), is("aa"));

        Edge ab2 = graph.newEdge(a, b);
        Edge ab3 = graph.newEdge(a, b);
        assertThat(graph.edgeCount(), is(4));
    }

    @Test
    public void testAddEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        assertThat(graph.edgeCount(), is(0));

        Edge ab = new Edge("MyID", a, b);
        graph.add(ab);
        assertThat(graph.edgeCount(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSameEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = new Node("b");
        assertThat(graph.edgeCount(), is(0));

        Edge ab = new Edge("MyID", a, b);
        graph.add(ab);
        graph.add(ab);
    }

    @Test
    public void testForcedAddSameEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = new Node("b");
        assertThat(graph.edgeCount(), is(0));

        Edge ab = new Edge("MyID", a, b);
        graph.forcedAdd(ab);
        graph.forcedAdd(ab);
        assertThat(graph.edgeCount(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEdgeWithSameId() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = new Node("b");
        assertThat(graph.edgeCount(), is(0));

        Edge ab1 = new Edge("MyID", a, b);
        Edge ab2 = new Edge("MyID", a, a);
        graph.add(ab1);
        graph.add(ab2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForcedAddEdgeWithSameId() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = new Node("b");
        assertThat(graph.edgeCount(), is(0));

        Edge ab1 = new Edge("MyID", a, b);
        Edge ab2 = new Edge("MyID", a, a);
        graph.forcedAdd(ab1);
        graph.forcedAdd(ab2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEdgeWhenMissingNode() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = new Node("b");
        assertThat(graph.edgeCount(), is(0));

        Edge ab = new Edge("MyID", a, b);
        graph.add(ab);
    }

    @Test
    public void testForcedAddEdgeWithSameID() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = new Edge("same", a, b);
        graph.forcedAdd(ab);
        graph.forcedAdd(ab);
        assertThat(graph.edgeCount(), is(1));
        assertThat(graph.has(ab), is(true));
    }

    @Test
    public void testForcedAddEdgeWhenMissingNode() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = new Node("b");
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.edgeCount(), is(0));

        Edge ab = new Edge("MyID", a, b);
        graph.forcedAdd(ab);
        assertThat(graph.nodeCount(), is(2));
        assertThat(graph.edgeCount(), is(1));
        assertThat(graph.has(b), is(true));
    }

    @Test
    public void testHasEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab1 = graph.newEdge("ab", a, b);
        Edge ab2 = graph.newEdge(a, b);
        assertThat(graph.hasEdge(ab1.id()), is(true));
        assertThat(graph.hasEdge(ab2.id()), is(true));
    }

    @Test
    public void testGetEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab1 = graph.newEdge("ab", a, b);
        Edge ab2 = graph.newEdge(a, b);
        assertThat(graph.getEdge(ab1.id()) == ab1, is(true));
        assertThat(graph.getEdge(ab2.id()) == ab2, is(true));
    }

    @Test
    public void testRemoveEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        Edge ab = graph.newEdge(a, b);
        assertThat(graph.edgeCount(), is(1));
        assertThat(graph.degree(a), is(1));
        assertThat(graph.inDegree(b), is(1));

        graph.remove(ab);
        assertThat(graph.edgeCount(), is(0));
        assertThat(graph.outDegree(a), is(0));
        assertThat(graph.inDegree(b), is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEdgeThatDoesNotExist() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        Edge ab = new Edge("ab", a, b);
        assertThat(graph.has(ab), is(false));

        graph.remove(ab);
    }

    @Test
    public void testForcedRemoveEdgeThatDoesNotExist() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode("MyID");
        Edge ab = new Edge("ab", a, b);
        assertThat(graph.has(ab), is(false));
        assertThat(graph.edgeCount(), is(0));

        graph.forcedRemove(ab);
        assertThat(graph.edgeCount(), is(0));
    }

    /////////////  Nodes and Edges  ////////////
    @Test
    public void testNodeEdgeSameId() {
        Graph graph = new Graph();
        Node a = graph.newNode("MyID");
        Edge aa = graph.newEdge("MyID", a, a);
        assertThat(graph.nodeCount(), is(1));
        assertThat(graph.edgeCount(), is(1));
    }

    @Test
    public void testInOutEdgesAndDegree() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge bc = graph.newEdge(b, c);
        assertThat(graph.inDegree(b), is(1));
        assertThat(graph.inEdges(b).contains(ab), is(true));
        assertThat(graph.outDegree(b), is(1));
        assertThat(graph.outEdges(b).contains(bc), is(true));
        assertThat(graph.degree(b), is(2));
        assertThat(graph.inOutEdges(b).contains(ab), is(true));
        assertThat(graph.inOutEdges(b).contains(bc), is(true));
    }

    @Test
    public void testFromToEdges() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        assertThat(graph.fromToEdges(a, b), containsInAnyOrder(ab));
        Edge ab2 = graph.newEdge(a, b);
        Edge ba = graph.newEdge(b, a);
        assertThat(graph.fromToEdges(a, b), containsInAnyOrder(ab, ab2));
        assertThat(graph.fromToEdges(b, a), containsInAnyOrder(ba));
    }

    @Test
    public void testFromToEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        assertThat(graph.fromToEdge(a, b), is(ab));
        assertThat(graph.fromToEdge(b, a), is(nullValue()));
    }

    @Test
    public void testBetweenEdges() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        assertThat(graph.betweenEdges(a, b), containsInAnyOrder(ab));
        assertThat(graph.betweenEdges(a, b), is(graph.betweenEdges(b, a)));
        Edge ab2 = graph.newEdge(a, b);
        Edge ba = graph.newEdge(b, a);
        assertThat(graph.betweenEdges(a, b), containsInAnyOrder(ab, ab2, ba));
        assertThat(graph.betweenEdges(a, b), is(graph.betweenEdges(b, a)));
    }

    @Test
    public void testBetweenEdge() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        assertThat(graph.betweenEdge(a, b), is(ab));
        assertThat(graph.betweenEdge(b, a), is(ab));
        assertThat(graph.betweenEdge(b, c), is(nullValue()));
    }

    @Test
    public void testSubGraphs() {
        Graph graph = new Graph();
        assertThat(graph.rootGraph(), is(graph));
        assertThat(graph.parentGraph(), is(nullValue()));

        Graph subGraph = graph.newSubGraph();
        assertThat(subGraph.parentGraph(), is(graph));
        assertThat(subGraph.rootGraph(), is(graph));
        assertThat(graph.subGraphs().size(), is(1));
        assertThat(graph.subGraphs().iterator().next(), is(subGraph));

        Graph subSubGraph = subGraph.newSubGraph();
        assertThat(subSubGraph.parentGraph(), is(subGraph));
        assertThat(subSubGraph.rootGraph(), is(graph));
    }

    @Test
    public void testNewSubGraphWithElements() {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        Edge bc = graph.newEdge(b, c);
        Edge ca = graph.newEdge(c, a);

        Collection<Node> selectedNodes = new ArrayList<>();
        Collection<Edge> selectedEdges = new ArrayList<>();
        selectedNodes.add(a);
        selectedNodes.add(b);
        selectedNodes.add(c);
        selectedEdges.add(bc);
        selectedEdges.add(ca);
        Graph subGraph = graph.newSubGraph(selectedNodes, selectedEdges);
        assertThat(subGraph.nodeCount(), is(3));
        assertThat(subGraph.edgeCount(), is(2));
        assertThat(subGraph.has(bc), is(true));
        assertThat(subGraph.has(ca), is(true));

        selectedNodes = new ArrayList<>();
        selectedNodes.add(a);
        selectedNodes.add(b);
        Graph inducedSubGraph = graph.newInducedSubGraph(selectedNodes);
        assertThat(inducedSubGraph.nodeCount(), is(2));
        assertThat(inducedSubGraph.edgeCount(), is(1));
        assertThat(inducedSubGraph.has(a), is(true));
        assertThat(inducedSubGraph.has(b), is(true));
        assertThat(inducedSubGraph.has(ab), is(true));
    }

    @Test
    public void testRemoveSubGraphs() {
        Graph graph = new Graph();
        Graph subGraphA = graph.newSubGraph();
        Graph subGraphB = graph.newSubGraph();
        graph.removeSubGraph(subGraphA);
        assertThat(graph.subGraphs().size(), is(1));
        assertThat(graph.subGraphs().iterator().next(), is(subGraphB));
    }

    /////////////  Graph attributes  ////////////
    @Test
    public void testGetSetGraphAttribute() {
        Graph graph = new Graph();

        graph.newGraphAttribute("myAttrStr", "attrValue");
        assertThat(graph.hasGraphAttribute("myAttrStr"), is(true));
        assertThat(graph.<String>graphAttribute("myAttrStr").get(), is("attrValue"));

        graph.newGraphAttribute("myAttrInt", 12);
        assertThat(graph.hasGraphAttribute("myAttrInt"), is(true));
        assertThat(graph.<Integer>graphAttribute("myAttrInt").get(), is(12));
    }

    @Test
    public void testGetSetGraphAttributeWithHierachy() {
        Graph graph = new Graph();
        Graph subgraph = graph.newSubGraph();

        graph.newGraphAttribute("first", 1.0);
        subgraph.newGraphAttribute("second", 2.0);

        assertThat(graph.hasGraphAttribute("first"), is(true));
        assertThat(graph.hasGraphAttribute("second"), is(true));
        assertThat(graph.<Double>graphAttribute("first").get(), is(1.0));
        assertThat(graph.<Double>graphAttribute("second").get(), is(2.0));

        assertThat(subgraph.hasGraphAttribute("first"), is(true));
        assertThat(subgraph.hasGraphAttribute("second"), is(true));
        assertThat(subgraph.<Double>graphAttribute("first").get(), is(1.0));
        assertThat(subgraph.<Double>graphAttribute("second").get(), is(2.0));

        graph.newLocalGraphAttribute("third", 3.0);
        subgraph.newLocalGraphAttribute("forth", 4.0);

        assertThat(graph.hasGraphAttribute("third"), is(true));
        assertThat(graph.hasGraphAttribute("forth"), is(false));
        assertThat(graph.hasLocalGraphAttribute("third"), is(true));
        assertThat(graph.hasLocalGraphAttribute("forth"), is(false));

        assertThat(subgraph.hasGraphAttribute("third"), is(true));
        assertThat(subgraph.hasGraphAttribute("forth"), is(true));
        assertThat(subgraph.hasLocalGraphAttribute("third"), is(false));
        assertThat(subgraph.hasLocalGraphAttribute("forth"), is(true));

        subgraph.newLocalGraphAttribute("third", 33.0);

        assertThat(subgraph.hasGraphAttribute("third"), is(true));
        assertThat(subgraph.hasLocalGraphAttribute("third"), is(true));
        assertThat(graph.<Double>graphAttribute("third").get(), is(3.0));
        assertThat(subgraph.<Double>graphAttribute("third").get(), is(33.0));

        subgraph.removeGraphAttribute("third");

        assertThat(subgraph.hasGraphAttribute("third"), is(true));
        assertThat(subgraph.hasLocalGraphAttribute("third"), is(false));
        assertThat(graph.<Double>graphAttribute("third").get(), is(3.0));
        assertThat(subgraph.<Double>graphAttribute("third").get(), is(3.0));
    }

    @Test
    public void testStdGraphAttribute() {
        Graph graph = new Graph();
        graph.newGraphAttribute("label", "I'm a graph");
        assertThat(graph.<String>graphAttribute(StdAttribute.label).get(), is("I'm a graph"));
    }

    @Test(expected = ClassCastException.class)
    public void testStdGraphAttributeWrongCast() {
        Graph graph = new Graph();
        graph.<Integer>newGraphAttribute(StdAttribute.label, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnexistingGraphAttribute() {
        Graph graph = new Graph();
        graph.graphAttribute("attr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveUnexistingGraphAttribute() {
        Graph graph = new Graph();
        graph.removeGraphAttribute("attr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWringTypeGraphAttribute() {
        Graph graph = new Graph();
        graph.newGraphAttribute("attr", 1);
        assertThat(graph.<Integer>graphAttribute("attr").get(), is(1));
        graph.newGraphAttribute("attr", "string");
    }

    /////////////  Node attributes  ////////////
    @Test
    public void testGetNewNodeAttribute() {
        Graph graph = new Graph();
        NodeAttribute<String> attr = graph.newNodeAttribute("myAttr", "");

        assertThat(graph.hasNodeAttribute("myAttr"), is(true));
        assertThat(graph.<String>nodeAttribute("myAttr"), is(attr));
        assertThat(attr.getDefault(), is(""));

        Node a = graph.newNode();
        assertThat(attr.get(a), is(""));
        attr.set(a, "I'm node a");
        assertThat(attr.get(a), is("I'm node a"));
    }

    @Test
    public void testGetSetNodeAttributeWithHierachy() {
        Graph graph = new Graph();
        Graph subgraph = graph.newSubGraph();

        NodeAttribute<Double> first = graph.newNodeAttribute("first", 1.0);
        NodeAttribute<Double> second = subgraph.newNodeAttribute("second", 2.0);

        assertThat(graph.hasNodeAttribute("first"), is(true));
        assertThat(graph.hasNodeAttribute("second"), is(true));
        assertThat(graph.<Double>nodeAttribute("first"), is(first));
        assertThat(graph.<Double>nodeAttribute("second"), is(second));
        assertThat(first.getDefault(), is(1.0));
        assertThat(second.getDefault(), is(2.0));

        assertThat(subgraph.hasNodeAttribute("first"), is(true));
        assertThat(subgraph.hasNodeAttribute("second"), is(true));
        assertThat(subgraph.<Double>nodeAttribute("first"), is(first));
        assertThat(subgraph.<Double>nodeAttribute("second"), is(second));
        assertThat(first.getDefault(), is(1.0));
        assertThat(second.getDefault(), is(2.0));

        graph.newLocalNodeAttribute("third", 3.0);
        subgraph.newLocalNodeAttribute("forth", 4.0);

        assertThat(graph.hasNodeAttribute("third"), is(true));
        assertThat(graph.hasNodeAttribute("forth"), is(false));
        assertThat(graph.hasLocalNodeAttribute("third"), is(true));
        assertThat(graph.hasLocalNodeAttribute("forth"), is(false));

        assertThat(subgraph.hasNodeAttribute("third"), is(true));
        assertThat(subgraph.hasNodeAttribute("forth"), is(true));
        assertThat(subgraph.hasLocalNodeAttribute("third"), is(false));
        assertThat(subgraph.hasLocalNodeAttribute("forth"), is(true));

        subgraph.newLocalNodeAttribute("third", 33.0);

        assertThat(subgraph.hasNodeAttribute("third"), is(true));
        assertThat(subgraph.hasLocalNodeAttribute("third"), is(true));
        assertThat(graph.<Double>nodeAttribute("third").getDefault(), is(3.0));
        assertThat(subgraph.<Double>nodeAttribute("third").getDefault(), is(33.0));

        subgraph.removeNodeAttribute("third");

        assertThat(subgraph.hasNodeAttribute("third"), is(true));
        assertThat(subgraph.hasLocalNodeAttribute("third"), is(false));
        assertThat(graph.<Double>nodeAttribute("third").getDefault(), is(3.0));
        assertThat(subgraph.<Double>nodeAttribute("third").getDefault(), is(3.0));
    }

    @Test
    public void testStdNodeAttribute() {
        Graph graph = new Graph();
        graph.newNodeAttribute(StdAttribute.label, "attrValue");
        assertThat(graph.<String>nodeAttribute(StdAttribute.label).getDefault(), is("attrValue"));
    }

    @Test(expected = ClassCastException.class)
    public void testStdNodeAttributeWrongCast() {
        Graph graph = new Graph();
        graph.<Integer>newNodeAttribute(StdAttribute.label, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnexistingNodeAttribute() {
        Graph graph = new Graph();
        graph.nodeAttribute("attr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveUnexistingNodeAttribute() {
        Graph graph = new Graph();
        graph.removeNodeAttribute("attr");
    }

    @Test(expected = ClassCastException.class)
    public void testSetWringTypeNodeAttribute() {
        Graph graph = new Graph();
        graph.newNodeAttribute("attr", 1);
        graph.nodeAttribute("attr").setDefault(3.14);
    }

    /////////////  Edge attributes  ////////////
    @Test
    public void testGetNewEdgeAttribute() {
        Graph graph = new Graph();
        EdgeAttribute<String> attr = graph.newEdgeAttribute("myAttr", "");

        assertThat(graph.hasEdgeAttribute("myAttr"), is(true));
        assertThat(graph.<String>edgeAttribute("myAttr"), is(attr));
        assertThat(attr.getDefault(), is(""));

        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);
        assertThat(attr.get(ab), is(""));
        attr.set(ab, "I'm edge ab");
        assertThat(attr.get(ab), is("I'm edge ab"));
    }

    @Test
    public void testGetSetEdgeAttributeWithHierachy() {
        Graph graph = new Graph();
        Graph subgraph = graph.newSubGraph();

        EdgeAttribute<Double> first = graph.newEdgeAttribute("first", 1.0);
        EdgeAttribute<Double> second = subgraph.newEdgeAttribute("second", 2.0);

        assertThat(graph.hasEdgeAttribute("first"), is(true));
        assertThat(graph.hasEdgeAttribute("second"), is(true));
        assertThat(graph.<Double>edgeAttribute("first"), is(first));
        assertThat(graph.<Double>edgeAttribute("second"), is(second));
        assertThat(first.getDefault(), is(1.0));
        assertThat(second.getDefault(), is(2.0));

        assertThat(subgraph.hasEdgeAttribute("first"), is(true));
        assertThat(subgraph.hasEdgeAttribute("second"), is(true));
        assertThat(subgraph.<Double>edgeAttribute("first"), is(first));
        assertThat(subgraph.<Double>edgeAttribute("second"), is(second));
        assertThat(first.getDefault(), is(1.0));
        assertThat(second.getDefault(), is(2.0));

        graph.newLocalEdgeAttribute("third", 3.0);
        subgraph.newLocalEdgeAttribute("forth", 4.0);

        assertThat(graph.hasEdgeAttribute("third"), is(true));
        assertThat(graph.hasEdgeAttribute("forth"), is(false));
        assertThat(graph.hasLocalEdgeAttribute("third"), is(true));
        assertThat(graph.hasLocalEdgeAttribute("forth"), is(false));

        assertThat(subgraph.hasEdgeAttribute("third"), is(true));
        assertThat(subgraph.hasEdgeAttribute("forth"), is(true));
        assertThat(subgraph.hasLocalEdgeAttribute("third"), is(false));
        assertThat(subgraph.hasLocalEdgeAttribute("forth"), is(true));

        subgraph.newLocalEdgeAttribute("third", 33.0);

        assertThat(subgraph.hasEdgeAttribute("third"), is(true));
        assertThat(subgraph.hasLocalEdgeAttribute("third"), is(true));
        assertThat(graph.<Double>edgeAttribute("third").getDefault(), is(3.0));
        assertThat(subgraph.<Double>edgeAttribute("third").getDefault(), is(33.0));

        subgraph.removeEdgeAttribute("third");

        assertThat(subgraph.hasEdgeAttribute("third"), is(true));
        assertThat(subgraph.hasLocalEdgeAttribute("third"), is(false));
        assertThat(graph.<Double>edgeAttribute("third").getDefault(), is(3.0));
        assertThat(subgraph.<Double>edgeAttribute("third").getDefault(), is(3.0));
    }

    @Test
    public void testStdEdgeAttribute() {
        Graph graph = new Graph();
        graph.newEdgeAttribute(StdAttribute.label, "attrValue");
        assertThat(graph.<String>edgeAttribute(StdAttribute.label).getDefault(), is("attrValue"));
    }

    @Test(expected = ClassCastException.class)
    public void testStdEdgeAttributeWrongCast() {
        Graph graph = new Graph();
        graph.<Integer>newEdgeAttribute(StdAttribute.label, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnexistingEdgeAttribute() {
        Graph graph = new Graph();
        graph.edgeAttribute("attr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveUnexistingEdgeAttribute() {
        Graph graph = new Graph();
        graph.removeEdgeAttribute("attr");
    }

    @Test(expected = ClassCastException.class)
    public void testSetWringTypeEdgeAttribute() {
        Graph graph = new Graph();
        graph.newEdgeAttribute("attr", 1);
        graph.edgeAttribute("attr").setDefault(3.14);
    }

    private class GraphObserverTester {

        public int updateElementsCount = 0;
        public int updateSubGraphCount = 0;
        public int updateAttributeCount = 0;
        public Collection<Element> lastChangedElememnts = new HashSet<>();
        public Collection<GraphWithHierarchy<?>> lastChangedSubGraphs = new HashSet<>();
        public Collection<Attribute<?>> lastChangedAttributes = new HashSet<>();

        public GraphObserverTester(Graph graphObserved) {
            new Observer.GraphElements(graphObserved) {
                @Override
                public void theseElementsChanged(Collection<Element> changedElements) {
                    updateElementsCount++;
                    lastChangedElememnts = new HashSet<>(changedElements);
                }
            };
            new Observer.GraphHierarchy(graphObserved) {
                @Override
                public void theseSubGraphsChanged(Collection<GraphWithHierarchy<?>> changedSubGraphs) {
                    updateSubGraphCount++;
                    lastChangedSubGraphs = new HashSet<>(changedSubGraphs);
                }
            };
            new Observer.LocalAttributeList(graphObserved) {
                @Override
                public void theseLocalAttributesAddedOrRemoved(Collection<Attribute<?>> changedAttributes) {
                    updateAttributeCount++;
                    lastChangedAttributes = new HashSet<>(changedAttributes);
                }
            };
        }

        public void clear() {
            updateElementsCount = 0;
            updateSubGraphCount = 0;
            updateAttributeCount = 0;
            lastChangedElememnts = new HashSet<>();
            lastChangedSubGraphs = new HashSet<>();
            lastChangedAttributes = new HashSet<>();
        }
    }

    @Test
    public void testObservable() {
        Graph graph = new Graph();
        GraphObserverTester observer = new GraphObserverTester(graph);

        Node a = new Node("a");
        graph.add(a);
        assertThat(observer.updateElementsCount, is(1));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));
        assertThat(observer.lastChangedElememnts.size(), is(1));
        assertThat(observer.lastChangedElememnts, hasItem(a));
        observer.clear();

        Node b = graph.newNode();
        assertThat(observer.updateElementsCount, is(1));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));
        assertThat(observer.lastChangedElememnts.size(), is(1));
        assertThat(observer.lastChangedElememnts, hasItem(b));
        observer.clear();

        Edge ab = graph.newEdge(a, b);
        assertThat(observer.updateElementsCount, is(1));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));
        assertThat(observer.lastChangedElememnts.size(), is(1));
        assertThat(observer.lastChangedElememnts, hasItem(ab));
        observer.clear();

        graph.remove(ab);
        assertThat(observer.updateElementsCount, is(1));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));
        assertThat(observer.lastChangedElememnts.size(), is(1));
        assertThat(observer.lastChangedElememnts, hasItem(ab));
        observer.clear();

        graph.remove(b);
        assertThat(observer.updateElementsCount, is(1));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));
        assertThat(observer.lastChangedElememnts.size(), is(1));
        assertThat(observer.lastChangedElememnts, hasItem(b));
        observer.clear();

        Graph subGraphA = graph.newSubGraph();
        assertThat(observer.updateElementsCount, is(0));
        assertThat(observer.updateSubGraphCount, is(1));
        assertThat(observer.lastChangedElememnts.size(), is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(1));
        assertThat(observer.lastChangedSubGraphs, hasItem(subGraphA));
        observer.clear();

        Set<Node> nodeSet = new HashSet<>();
        nodeSet.add(a);

        Graph subGraphB = graph.newInducedSubGraph(nodeSet);
        assertThat(observer.updateElementsCount, is(0));
        assertThat(observer.updateSubGraphCount, is(1));
        assertThat(observer.lastChangedElememnts.size(), is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(1));
        assertThat(observer.lastChangedSubGraphs, hasItem(subGraphB));
        observer.clear();

        Graph subGraphC = graph.newSubGraph(nodeSet, new HashSet<>());
        assertThat(observer.updateElementsCount, is(0));
        assertThat(observer.updateSubGraphCount, is(1));
        assertThat(observer.lastChangedElememnts.size(), is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(1));
        assertThat(observer.lastChangedSubGraphs, hasItem(subGraphC));
        observer.clear();

        graph.removeSubGraph(subGraphC);
        assertThat(observer.updateElementsCount, is(0));
        assertThat(observer.updateSubGraphCount, is(1));
        assertThat(observer.lastChangedElememnts.size(), is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(1));
        assertThat(observer.lastChangedSubGraphs, hasItem(subGraphC));
        observer.clear();
    }

    @Test
    public void testAttributeObservable() {
        Graph graph = new Graph();
        GraphObserverTester observer = new GraphObserverTester(graph);

        GraphAttribute<String> graphName = graph.newGraphAttribute("graphName", "Aldo");
        assertThat(observer.updateElementsCount, is(0));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedElememnts.size(), is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));
        assertThat(observer.lastChangedAttributes, hasItem(graphName));
        observer.clear();

        graph.removeGraphAttribute("graphName");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(graphName));
        observer.clear();

        NodeAttribute<String> nodeLabel = graph.newNodeAttribute("nodeLabel", "");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(nodeLabel));
        observer.clear();

        graph.removeNodeAttribute("nodeLabel");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(nodeLabel));
        observer.clear();

        EdgeAttribute<String> edgeLabel = graph.newEdgeAttribute("edgeLabel", "");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(edgeLabel));
        observer.clear();

        graph.removeEdgeAttribute("edgeLabel");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(edgeLabel));
        observer.clear();
    }

    @Test
    public void testAttributeObservableOnSubGraph() {
        Graph graph = new Graph();
        Graph subGraph = graph.newSubGraph();

        GraphObserverTester observer = new GraphObserverTester(subGraph);

        subGraph.newGraphAttribute("graphName", "Aldo");
        assertThat(subGraph.hasGraphAttribute("graphName"), is(true));
        assertThat(observer.updateAttributeCount, is(0));
        observer.clear();

        GraphAttribute<String> graphNameSub = subGraph.newLocalGraphAttribute("graphName", "Luigi");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(graphNameSub));
        observer.clear();

        subGraph.newNodeAttribute("nodeMetric", 0);
        assertThat(observer.updateAttributeCount, is(0));
        observer.clear();

        NodeAttribute<Integer> nodeMetricSub = subGraph.newLocalNodeAttribute("nodeMetric", 0);
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(nodeMetricSub));
        observer.clear();

        subGraph.newEdgeAttribute("edgeMetric", 0);
        assertThat(observer.updateAttributeCount, is(0));
        observer.clear();

        EdgeAttribute<Integer> edgeMetricSub = subGraph.newLocalEdgeAttribute("edgeMetric", 0);
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(edgeMetricSub));
        observer.clear();

        subGraph.removeGraphAttribute("graphName");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(graphNameSub));
        observer.clear();

        subGraph.removeNodeAttribute("nodeMetric");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(nodeMetricSub));
        observer.clear();

        subGraph.removeEdgeAttribute("edgeMetric");
        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(edgeMetricSub));
        observer.clear();
    }

    @Test
    public void testObservableWithBulkNotification() {
        Graph graph = new Graph();
        GraphObserverTester observer = new GraphObserverTester(graph);

        graph.startBulkNotification();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Edge ab = graph.newEdge(a, b);

        assertThat(observer.updateElementsCount, is(0));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.lastChangedElememnts.size(), is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));

        graph.stopBulkNotification();
        assertThat(observer.updateElementsCount, is(1));
        assertThat(observer.updateSubGraphCount, is(0));
        assertThat(observer.lastChangedSubGraphs.size(), is(0));
        assertThat(observer.lastChangedElememnts.size(), is(3));
        assertThat(observer.lastChangedElememnts, hasItem(a));
        assertThat(observer.lastChangedElememnts, hasItem(b));
        assertThat(observer.lastChangedElememnts, hasItem(ab));
        observer.clear();

        graph.startBulkNotification();
        Node c = new Node("c");
        Node d = new Node("d");
        Edge cd = new Edge("cd", c, d);
        graph.forcedAdd(cd);
        Graph subGraph = graph.newSubGraph();

        graph.stopBulkNotification();
        assertThat(observer.updateElementsCount, is(1));
        assertThat(observer.updateSubGraphCount, is(1));
        assertThat(observer.lastChangedElememnts.size(), is(3));
        assertThat(observer.lastChangedElememnts, hasItem(c));
        assertThat(observer.lastChangedElememnts, hasItem(d));
        assertThat(observer.lastChangedElememnts, hasItem(cd));
        assertThat(observer.lastChangedSubGraphs.size(), is(1));
        assertThat(observer.lastChangedSubGraphs, hasItem(subGraph));
        observer.clear();
    }

    @Test
    public void testAttributeObservableWithBulkNotification() {
        Graph graph = new Graph();
        GraphObserverTester observer = new GraphObserverTester(graph);

        graph.startBulkNotification();

        GraphAttribute<String> graphName = graph.newGraphAttribute("graphName", "Aldo");
        assertThat(observer.updateAttributeCount, is(0));

        NodeAttribute<Integer> nodeMetric = graph.newNodeAttribute("nodeMetric", 0);
        assertThat(observer.updateAttributeCount, is(0));

        EdgeAttribute<Integer> edgeeMetric = graph.newEdgeAttribute("edgeMetric", 0);
        assertThat(observer.updateAttributeCount, is(0));

        graph.stopBulkNotification();

        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(graphName));
        assertThat(observer.lastChangedAttributes, hasItem(nodeMetric));
        assertThat(observer.lastChangedAttributes, hasItem(edgeeMetric));
        observer.clear();

        graph.startBulkNotification();

        graph.removeGraphAttribute("graphName");
        assertThat(observer.updateAttributeCount, is(0));

        graph.removeNodeAttribute("nodeMetric");
        assertThat(observer.updateAttributeCount, is(0));

        graph.removeEdgeAttribute("edgeMetric");
        assertThat(observer.updateAttributeCount, is(0));

        graph.stopBulkNotification();

        assertThat(observer.updateAttributeCount, is(1));
        assertThat(observer.lastChangedAttributes, hasItem(graphName));
        assertThat(observer.lastChangedAttributes, hasItem(nodeMetric));
        assertThat(observer.lastChangedAttributes, hasItem(edgeeMetric));
        observer.clear();
    }

}
