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
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class NodeTest {

    @Test
    public void testEqual() {
        Node a = new Node("MyID");
        Node b = new Node("MyID");
        Node c = new Node("MyID?");
        assertThat(a, is(b));
        assertThat(a, is(not(c)));
    }

    @Test
    public void testConstructor() {
        Node a = new Node("MyID");
        assertThat(a.id(), is("MyID"));
    }
}
