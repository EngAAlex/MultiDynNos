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
import java.util.Collections;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ElementTest {

    @Test
    public void testToString() {
        Element a = new Node("a");
        Element b = new Node("b");
        assertThat(a.toString(), is("a"));
        assertThat(b.toString(), is("b"));
    }

    @Test
    public void testCompare() {
        Element a = new Node("a");
        Element b = new Node("b");
        assertThat(a.compareTo(b), is(-1));
        assertThat(a.compareTo(a), is(0));
        assertThat(b.compareTo(a), is(1));
    }

    @Test
    public void testOrderOnIds() {
        List<Element> elements = new ArrayList<>();
        Element a = new Node("a");
        Element b = new Node("b");
        Element c = new Node("c");

        elements.add(b);
        elements.add(a);
        elements.add(c);
        Collections.sort(elements);
        assertThat(elements.get(0), is(a));
        assertThat(elements.get(1), is(b));
        assertThat(elements.get(2), is(c));
    }
}
