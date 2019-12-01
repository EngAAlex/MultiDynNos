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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ElementAttributeTest {

    @Test
    public void testSetGet() {
        Node a = new Node("a");
        ElementAttribute<Node, String> attr = new NodeAttribute<>("");
        assertThat(attr.isDefault(a), is(true));
        attr.set(a, "aLabel");
        assertThat(attr.isDefault(a), is(false));
        assertThat(attr.get(a), is("aLabel"));
    }

    @Test
    public void testSetGetWithDefault() {
        Node a = new Node("a");
        ElementAttribute<Node, String> attr = new NodeAttribute<>("");
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.get(a), is(""));

        attr.setDefault("newDefault");
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.get(a), is("newDefault"));

        attr.set(a, attr.getDefault());
        assertThat(attr.isDefault(a), is(false));
        attr.setDefault("newestDefault");
        assertThat(attr.get(a), is("newDefault"));
        assertThat(attr.getDefault(), is("newestDefault"));
    }

    @Test
    public void testClear() {
        Node a = new Node("a");
        ElementAttribute<Node, Integer> attr = new NodeAttribute<>(0);
        attr.set(a, 1);

        assertThat(attr.isDefault(a), is(false));
        assertThat(attr.get(a), is(1));
        attr.clear(a);
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.get(a), is(0));
    }

    @Test
    public void testReset() {
        Node a = new Node("a");
        Node b = new Node("b");
        ElementAttribute<Node, Integer> attr = new NodeAttribute<>(0);
        attr.set(a, 1);
        attr.set(b, 2);

        assertThat(attr.isDefault(a), is(false));
        assertThat(attr.isDefault(b), is(false));
        assertThat(attr.get(a), is(1));
        assertThat(attr.get(b), is(2));
        attr.reset();
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.isDefault(b), is(true));
        assertThat(attr.get(a), is(0));
        assertThat(attr.get(b), is(0));
        attr.reset(-1);
        assertThat(attr.isDefault(a), is(true));
        assertThat(attr.isDefault(b), is(true));
        assertThat(attr.get(a), is(-1));
        assertThat(attr.get(b), is(-1));
    }

    @Test
    public void testIterator() {
        Node a = new Node("a");
        Node b = new Node("b");
        ElementAttribute<Node, Integer> attr = new NodeAttribute<>(0);
        attr.set(a, 1);
        attr.set(b, 2);

        int entryCount = 0;
        for (Entry<Node, Integer> entry : attr) {
            if (entry.getKey().equals(a)) {
                assertThat(entry.getValue(), is(1));
            } else if (entry.getKey().equals(b)) {
                assertThat(entry.getValue(), is(2));
            } else {
                assertThat(false, is(true));
            }
            entryCount++;
        }
        assertThat(entryCount, is(2));
    }

    @Test
    public void testNonDefaultEntryCount() {
        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
        ElementAttribute<Node, Integer> attr = new NodeAttribute<>(0);
        attr.set(a, 1);
        attr.set(b, 2);
        assertThat(attr.nonDefaultElements().size(), is(2));
        attr.set(c, 3);
        assertThat(attr.nonDefaultElements().size(), is(3));
        attr.clear(a);
        assertThat(attr.nonDefaultElements().size(), is(2));
        attr.reset();
        assertThat(attr.nonDefaultElements().size(), is(0));
    }

    @Test
    public void testCopy() {
        Node a = new Node("a");
        Node b = new Node("b");
        ElementAttribute<Node, Integer> attrA = new NodeAttribute<>(0);
        attrA.set(a, 1);
        attrA.set(b, 2);

        ElementAttribute<Node, Integer> attrB = new NodeAttribute<>(10);
        attrB.copy(attrA);

        assertThat(attrB.getDefault(), is(attrA.getDefault()));
        assertThat(attrB.get(a), is(attrA.get(a)));
        assertThat(attrB.get(b), is(attrA.get(b)));
        assertThat(attrB.nonDefaultElements().size(), is(attrA.nonDefaultElements().size()));
    }

    private class NodeAttrObserverTest extends Observer.ElementAttributeChanges<Node> {

        public int updateCount = 0;
        public int updateAllCount = 0;
        public Collection<Node> lastChangedElem = new HashSet<>();

        public NodeAttrObserverTest(ElementAttribute<Node, ?> attributeObserved) {
            super(attributeObserved);
        }

        public void clear() {
            updateCount = 0;
            updateAllCount = 0;
            lastChangedElem = new HashSet<>();
        }

        @Override
        public void update(Collection<Node> changedElements) {
            updateCount++;
            lastChangedElem = new HashSet<>(changedElements);
        }

        @Override
        public void updateAll() {
            updateAllCount++;
        }
    }

    @Test
    public void testObservable() {
        Node a = new Node("a");
        Node b = new Node("b");

        ElementAttribute<Node, Integer> attr = new NodeAttribute<>(0);
        NodeAttrObserverTest observer = new NodeAttrObserverTest(attr);

        attr.set(a, 3);
        assertThat(observer.updateCount, is(1));
        assertThat(observer.updateAllCount, is(0));
        assertThat(observer.lastChangedElem.size(), is(1));
        assertThat(observer.lastChangedElem, hasItem(a));
        observer.clear();

        attr.set(b, 2);
        assertThat(observer.updateCount, is(1));
        assertThat(observer.updateAllCount, is(0));
        assertThat(observer.lastChangedElem.size(), is(1));
        assertThat(observer.lastChangedElem, hasItem(b));
        observer.clear();

        attr.clear(a);
        assertThat(observer.updateCount, is(1));
        assertThat(observer.updateAllCount, is(0));
        assertThat(observer.lastChangedElem.size(), is(1));
        assertThat(observer.lastChangedElem, hasItem(a));
        observer.clear();

        attr.setDefault(4);
        assertThat(observer.updateCount, is(0));
        assertThat(observer.updateAllCount, is(1));
        assertThat(observer.lastChangedElem.size(), is(0));
        observer.clear();

        attr.reset();
        assertThat(observer.updateCount, is(0));
        assertThat(observer.updateAllCount, is(1));
        assertThat(observer.lastChangedElem.size(), is(0));
        observer.clear();

        attr.reset(6);
        assertThat(observer.updateCount, is(0));
        assertThat(observer.updateAllCount, is(1));
        assertThat(observer.lastChangedElem.size(), is(0));
        observer.clear();
    }

    @Test
    public void testObservableWithBulkNotification() {
        Node a = new Node("a");
        Node b = new Node("b");

        ElementAttribute<Node, Integer> attr = new NodeAttribute<>(0);
        NodeAttrObserverTest observer = new NodeAttrObserverTest(attr);

        attr.startBulkNotification();
        attr.set(a, 3);
        assertThat(observer.updateCount, is(0));
        assertThat(observer.updateAllCount, is(0));
        assertThat(observer.lastChangedElem.size(), is(0));

        attr.set(b, 2);
        assertThat(observer.updateCount, is(0));
        assertThat(observer.updateAllCount, is(0));
        assertThat(observer.lastChangedElem.size(), is(0));

        attr.stopBulkNotification();
        assertThat(observer.updateCount, is(1));
        assertThat(observer.updateAllCount, is(0));
        assertThat(observer.lastChangedElem.size(), is(2));
        assertThat(observer.lastChangedElem, hasItem(a));
        assertThat(observer.lastChangedElem, hasItem(b));
        observer.clear();

        attr.startBulkNotification();
        attr.set(a, 3);
        attr.set(b, 2);
        attr.clear(b);
        attr.stopBulkNotification();
        assertThat(observer.updateCount, is(1));
        assertThat(observer.updateAllCount, is(0));
        assertThat(observer.lastChangedElem.size(), is(2));
        assertThat(observer.lastChangedElem, hasItem(a));
        assertThat(observer.lastChangedElem, hasItem(b));
        observer.clear();

        attr.startBulkNotification();
        attr.set(a, 3);
        attr.reset(21);
        attr.stopBulkNotification();
        assertThat(observer.updateCount, is(0));
        assertThat(observer.updateAllCount, is(1));
        assertThat(observer.lastChangedElem.size(), is(0));
        observer.clear();
    }

    @Test
    public void testAttributeType() {
        ElementAttribute<Node, String> nodeAttr = new NodeAttribute<>("");
        assertThat(nodeAttr.getAttributeType(), is(Attribute.Type.node));
        ElementAttribute<Edge, String> edgeAttr = new EdgeAttribute<>("");
        assertThat(edgeAttr.getAttributeType(), is(Attribute.Type.edge));
    }

}
