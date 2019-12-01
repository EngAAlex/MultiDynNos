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
package ocotillo.graph.extra;

import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class SymmetricLookupTableTest {

    private Graph graph;
    private Node a, b;

    @Before
    public void setUp() {
        graph = new Graph();
        a = graph.newNode();
        b = graph.newNode();
    }

    @Test
    public void testGetSet() throws Exception {
        SymmetricLookupTable<Node, Node, Integer> lookup = new SymmetricLookupTable<>();

        assertThat(lookup.get(a, b), is(nullValue()));
        lookup.set(a, b, 3);
        assertThat(lookup.get(a, b), is(3));
        lookup.set(a, b, 5);
        assertThat(lookup.get(a, b), is(5));

        assertThat(lookup.get(a, a), is(nullValue()));
        lookup.set(a, a, 3);
        assertThat(lookup.get(a, a), is(3));
        lookup.set(a, a, 5);
        assertThat(lookup.get(a, a), is(5));
    }

    @Test
    public void testErase() throws Exception {
        SymmetricLookupTable<Node, Node, Integer> lookup = new SymmetricLookupTable<>();

        lookup.set(a, b, 3);
        assertThat(lookup.get(a, b), is(3));
        lookup.erase(a);
        assertThat(lookup.get(a, b), is(nullValue()));

        lookup.set(a, b, 3);
        assertThat(lookup.get(a, b), is(3));
        lookup.erase(b);
        assertThat(lookup.get(a, b), is(nullValue()));

        lookup.set(a, a, 3);
        assertThat(lookup.get(a, a), is(3));
        lookup.erase(a);
        assertThat(lookup.get(a, a), is(nullValue()));
    }

    @Test
    public void testClear() throws Exception {
        SymmetricLookupTable<Node, Node, Integer> lookup = new SymmetricLookupTable<>();

        lookup.set(a, b, 2);
        lookup.set(a, a, 3);
        lookup.clear();
        assertThat(lookup.get(a, b), is(nullValue()));
        assertThat(lookup.get(a, a), is(nullValue()));
    }

    @Test
    public void testGetSetWithSwap() throws Exception {
        SymmetricLookupTable<Node, Node, Integer> lookup = new SymmetricLookupTable<>();

        assertThat(lookup.get(a, b), is(nullValue()));
        lookup.set(a, b, 3);
        assertThat(lookup.get(b, a), is(3));
        lookup.set(b, a, 5);
        assertThat(lookup.get(a, b), is(5));
    }

}
