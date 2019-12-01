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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class EdgeTest {

    @Test
    public void testEqual() {
        Node a = new Node("a");
        Node b = new Node("b");
        Edge ab = new Edge("ab", a, b);
        Edge ab1 = new Edge("ab", a, b);
        assertThat(ab1, is(ab));
        Node b1 = new Node("b");
        Edge ab2 = new Edge("ab", a, b1);
        assertThat(ab2, is(ab));
        Edge ab3 = new Edge("ab?", a, b);
        assertThat(ab3, is(not(ab)));
        Edge ab4 = new Edge("ab", b, b);
        assertThat(ab4, is(not(ab)));
    }

    @Test
    public void testConstructor() {
        Node a = new Node("a");
        Node b = new Node("b");
        Edge ab = new Edge("ab", a, b);
        assertThat(ab.id(), is("ab"));
    }

    @Test
    public void testSourceTarget() {
        Node a = new Node("a");
        Node b = new Node("b");
        Edge ab = new Edge("ab", a, b);
        assertThat(ab.source() == a, is(true));
        assertThat(ab.target() == b, is(true));
    }

    @Test
    public void testOtherEnd() {
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
        Edge ab = new Edge("ab", a, b);
        assertThat(ab.otherEnd(a) == b, is(true));
        assertThat(ab.otherEnd(b) == a, is(true));
        assertThat(ab.otherEnd(c), is(nullValue()));
    }

    @Test
    public void testIsExtremity() {
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
        Edge ab = new Edge("ab", a, b);
        assertThat(ab.isNodeExtremity(a), is(true));
        assertThat(ab.isNodeExtremity(b), is(true));
        assertThat(ab.isNodeExtremity(c), is(false));
    }
}
