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
package ocotillo.structures;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Base test class, not instantiable")
public abstract class BinarySearchTreeTestBase {

    protected abstract BinarySearchTree<DataClass, Integer> createInstance();

    protected class DataClass implements BinarySearchTree.Data<Integer> {

        final int intValue;
        final String stringValue;

        public DataClass(int intValue, String stringValue) {
            this.intValue = intValue;
            this.stringValue = stringValue;
        }

        @Override
        public Integer bstKey() {
            return intValue;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    protected final DataClass one = new DataClass(1, "one");
    protected final DataClass two = new DataClass(2, "two");
    protected final DataClass three = new DataClass(3, "three");
    protected final DataClass four = new DataClass(4, "four");
    protected final DataClass five = new DataClass(5, "five");
    protected final DataClass six = new DataClass(6, "six");
    protected final DataClass seven = new DataClass(7, "seven");
    protected final DataClass eight = new DataClass(8, "eight");
    protected final DataClass nine = new DataClass(9, "nine");

    protected final DataClass oneBis = new DataClass(1, "oneBis");
    protected final DataClass oneTris = new DataClass(1, "oneTris");
    protected final DataClass twoBis = new DataClass(2, "twoBis");
    protected final DataClass twoTris = new DataClass(2, "twoTris");

    @Test
    public void testGetAllWithOneElement() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        assertThat(searchTree.getAll(1), hasItem(one));
        assertThat(searchTree.getAll(4), is(empty()));
    }

    @Test
    public void testGetAllWithOneHit() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(seven);
        searchTree.insert(four);
        searchTree.insert(five);
        assertThat(searchTree.getAll(4), contains(four));
        assertThat(searchTree.getAll(6), is(empty()));
    }

    @Test
    public void testGetAllWithTwoHits() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(seven);
        searchTree.insert(four);
        searchTree.insert(twoBis);
        searchTree.insert(twoTris);
        searchTree.insert(oneBis);
        searchTree.insert(five);
        searchTree.insert(two);
        searchTree.insert(five);
        assertThat(searchTree.getAll(1), containsInAnyOrder(one, oneBis));
        assertThat(searchTree.getAll(2), containsInAnyOrder(two, twoBis, twoTris));
    }

    @Test
    public void testGetWithOneElement() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        assertThat(searchTree.get(1), is(one));
        assertThat(searchTree.get(4), is(nullValue()));
    }

    @Test
    public void testGetWithOneHit() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(seven);
        searchTree.insert(four);
        searchTree.insert(five);
        assertThat(searchTree.get(4), is(four));
        assertThat(searchTree.get(6), is(nullValue()));
    }

    @Test
    public void testGetWithTwoHits() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(seven);
        searchTree.insert(four);
        searchTree.insert(twoBis);
        searchTree.insert(twoTris);
        searchTree.insert(oneBis);
        searchTree.insert(five);
        searchTree.insert(two);
        searchTree.insert(five);
        assertThat(searchTree.get(1), anyOf(is(one), is(oneBis)));
        assertThat(searchTree.get(2), anyOf(is(two), is(twoBis), is(twoTris)));
    }

    @Test
    public void testInsertAll() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        List<DataClass> data = new ArrayList<>();
        data.add(one);
        data.add(four);
        data.add(eight);
        searchTree.insertAll(data);
        assertThat(searchTree.get(1), is(one));
        assertThat(searchTree.get(4), is(four));
        assertThat(searchTree.get(8), is(eight));
        assertThat(searchTree.size(), is(3));
    }

    @Test
    public void testDeleteWithOneElement() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        assertThat(searchTree.get(1), is(one));
        assertThat(searchTree.size(), is(1));
        searchTree.delete(one);
        assertThat(searchTree.get(1), is(nullValue()));
        assertThat(searchTree.size(), is(0));
    }

    @Test
    public void testDeleteWithNoHit() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        assertThat(searchTree.get(1), is(one));
        assertThat(searchTree.size(), is(1));
        searchTree.delete(four);
        assertThat(searchTree.get(1), is(one));
        assertThat(searchTree.size(), is(1));
        searchTree.delete(oneBis);
        assertThat(searchTree.get(1), is(one));
        assertThat(searchTree.size(), is(1));
    }

    @Test
    public void testDeleteKey() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(oneBis);
        searchTree.insert(oneTris);
        searchTree.insert(four);
        assertThat(searchTree.getAll(1).size(), is(3));
        assertThat(searchTree.size(), is(4));
        searchTree.deleteKey(1);
        assertThat(searchTree.getAll(1), is(empty()));
        assertThat(searchTree.size(), is(1));
    }

    @Test
    public void testDeleteAll() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(oneBis);
        searchTree.insert(oneTris);
        searchTree.insert(four);
        assertThat(searchTree.getAll(1).size(), is(3));
        assertThat(searchTree.size(), is(4));

        List<DataClass> data = new ArrayList<>();
        data.add(one);
        data.add(oneBis);

        searchTree.deleteAll(data);
        assertThat(searchTree.get(1), is(oneTris));
        assertThat(searchTree.size(), is(2));
    }

    @Test
    public void testContains() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(four);
        searchTree.insert(seven);
        assertThat(searchTree.contains(one), is(true));
        assertThat(searchTree.contains(four), is(true));
        assertThat(searchTree.contains(seven), is(true));
        assertThat(searchTree.contains(nine), is(false));
    }

    @Test
    public void testContainsKey() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(four);
        searchTree.insert(seven);
        assertThat(searchTree.containsKey(1), is(true));
        assertThat(searchTree.containsKey(4), is(true));
        assertThat(searchTree.containsKey(7), is(true));
        assertThat(searchTree.containsKey(9), is(false));
    }

    @Test
    public void testIsEmpty() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        assertThat(searchTree.isEmpty(), is(true));
        searchTree.insert(one);
        assertThat(searchTree.isEmpty(), is(false));
    }

    @Test
    public void testClear() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(one);
        searchTree.insert(two);
        searchTree.insert(three);
        assertThat(searchTree.isEmpty(), is(false));
        searchTree.clear();
        assertThat(searchTree.isEmpty(), is(true));
        searchTree.insert(one);
        searchTree.insert(two);
        assertThat(searchTree.isEmpty(), is(false));
    }

    @Test
    public void testInOrderTraversal() {
        BinarySearchTree<DataClass, Integer> searchTree = createInstance();
        searchTree.insert(four);
        searchTree.insert(one);
        searchTree.insert(eight);
        searchTree.insert(seven);
        searchTree.insert(two);
        searchTree.insert(six);
        assertThat(searchTree.inOrderTraversal(), contains(one, two, four, six, seven, eight));
    }

}
