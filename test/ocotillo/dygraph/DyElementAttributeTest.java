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
package ocotillo.dygraph;

import ocotillo.graph.*;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class DyElementAttributeTest {

    @Test
    public void testSetGet() {
        Node a = new Node("a");
        DyNodeAttribute<String> attr = new DyNodeAttribute<>("");
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.get(a).valueAt(0), is(""));
        Evolution<String> aLabel = new Evolution<>("aLabel");
        attr.set(a, aLabel);
        assertThat(attr.isDefault(a), is(false));
        assertThat(attr.get(a), is(aLabel));
        assertThat(attr.get(a).valueAt(0), is("aLabel"));
        aLabel.insert(new FunctionRect.String(Interval.newClosed(-10, 10), "justA", "justA", Interpolation.Std.constant));
        assertThat(attr.get(a).valueAt(0), is("justA"));
    }

    @Test
    public void testSetGetWithDefault() {
        Node a = new Node("a");
        DyNodeAttribute<String> attr = new DyNodeAttribute<>("");
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.get(a).valueAt(0), is(""));

        attr.setDefault(new Evolution<>("newDefault"));
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.get(a).getDefaultValue(), is("newDefault"));

        attr.set(a, attr.getDefault());
        assertThat(attr.isDefault(a), is(false));
        attr.setDefault(new Evolution<>("newestDefault"));
        assertThat(attr.get(a).getDefaultValue(), is("newDefault"));
        assertThat(attr.getDefault().getDefaultValue(), is("newestDefault"));
    }

    @Test
    public void testClear() {
        Node a = new Node("a");
        DyNodeAttribute<Integer> attr = new DyNodeAttribute<>(0);
        attr.set(a, new Evolution<>(1));

        assertThat(attr.isDefault(a), is(false));
        assertThat(attr.get(a).getDefaultValue(), is(1));
        attr.clear(a);
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.get(a).getDefaultValue(), is(0));
    }

    @Test
    public void testReset() {
        Node a = new Node("a");
        Node b = new Node("b");
        DyNodeAttribute<Integer> attr = new DyNodeAttribute<>(0);
        attr.set(a, new Evolution<>(1));
        attr.set(b, new Evolution<>(2));

        assertThat(attr.isDefault(a), is(false));
        assertThat(attr.isDefault(b), is(false));
        assertThat(attr.get(a).getDefaultValue(), is(1));
        assertThat(attr.get(b).getDefaultValue(), is(2));
        attr.reset();
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.isDefault(b), is(true));
        assertThat(attr.get(a).getDefaultValue(), is(0));
        assertThat(attr.get(b).getDefaultValue(), is(0));
        attr.reset(new Evolution<>(-1));
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.isDefault(b), is(true));
        assertThat(attr.get(a).getDefaultValue(), is(-1));
        assertThat(attr.get(b).getDefaultValue(), is(-1));
    }

    @Test
    public void testNonDefaultEntryCount() {
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
        DyNodeAttribute<Integer> attr = new DyNodeAttribute<>(0);
        attr.set(a, new Evolution<>(1));
        attr.set(b, new Evolution<>(2));
        assertThat(attr.nonDefaultElements().size(), is(2));
        attr.set(c, new Evolution<>(3));
        assertThat(attr.nonDefaultElements().size(), is(3));
        attr.clear(a);
        assertThat(attr.nonDefaultElements().size(), is(2));
        attr.reset();
        assertThat(attr.nonDefaultElements().size(), is(0));
    }

}
