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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class DyGraphAttributeTest {

    private class GraphAttrObserverTest extends Observer.GraphAttributeChanges {

        public int updateCount = 0;

        public GraphAttrObserverTest(GraphAttribute<?> attributeObserved) {
            super(attributeObserved);
        }

        public void clear() {
            updateCount = 0;
        }

        @Override
        public void update() {
            updateCount++;
        }
    }

    @Test
    public void testSetSet() {
        Graph graph = new Graph();
        GraphAttribute<String> attribute = graph.newGraphAttribute("graphName", "Aldo");
        assertThat(attribute.get(), is("Aldo"));
        attribute.set("Bepi");
        assertThat(attribute.get(), is("Bepi"));
    }

    @Test
    public void testObservable() {
    }

    @Test
    public void testAttributeType() {
        Graph graph = new Graph();
        GraphAttribute<String> name = graph.newGraphAttribute("graphName", "Aldo");
        assertThat(name.getAttributeType(), is(Attribute.Type.graph));
    }
}
