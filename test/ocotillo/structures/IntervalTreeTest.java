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

import lombok.EqualsAndHashCode;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import org.junit.Test;

public class IntervalTreeTest {

    @EqualsAndHashCode
    private static class DataClass implements IntervalTree.Data {

        private final Interval interval;
        private final int id;

        public DataClass(Interval interval) {
            this.interval = interval;
            this.id = 0;
        }

        public DataClass(Interval interval, int id) {
            this.interval = interval;
            this.id = id;
        }

        @Override
        public Interval interval() {
            return interval;
        }
    }

    @Test
    public void testInsert() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        DataClass intervalToAdd = new DataClass(Interval.newClosed(5, 7));
        assertThat(tree.size(), is(0));
        tree.insert(intervalToAdd);
        assertThat(tree.size(), is(1));
        assertThat(tree.contains(intervalToAdd), is(true));
        assertThat(tree.contains(new DataClass(Interval.newClosed(5, 7))), is(true));
    }

    @Test
    public void testInsertDuplicates() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        DataClass intervalToAdd = new DataClass(Interval.newClosed(5, 7));
        tree.insert(intervalToAdd);
        tree.insert(intervalToAdd);
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        assertThat(tree.size(), is(1));
        tree.insert(new DataClass(Interval.newClosed(5, 7), 3));
        assertThat(tree.size(), is(2));
    }

    @Test
    public void testInsertSimilar() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newLeftClosed(5, 7)));
        assertThat(tree.size(), is(2));
    }

    @Test
    public void testDelete() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(6, 17)));
        assertThat(tree.size(), is(2));
        assertThat(tree.contains(new DataClass(Interval.newOpen(6, 17))), is(true));
        tree.delete(new DataClass(Interval.newOpen(6, 17)));
        assertThat(tree.size(), is(1));
        assertThat(tree.contains(new DataClass(Interval.newOpen(6, 17))), is(false));
    }

    @Test
    public void testIsEmpty() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        assertThat(tree.isEmpty(), is(true));
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        assertThat(tree.isEmpty(), is(false));
    }

    @Test
    public void testClear() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(6, 17)));
        assertThat(tree.isEmpty(), is(false));
        tree.clear();
        assertThat(tree.isEmpty(), is(true));
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(6, 17)));
        assertThat(tree.isEmpty(), is(false));
    }

    @Test
    public void testInOrderTraversal() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(7, 17)));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));
        assertThat(tree.inOrderTraversal(), contains(new DataClass(Interval.newClosed(0, 3)),
                new DataClass(Interval.newRightClosed(3, 4)), new DataClass(Interval.newOpen(4, 5)),
                new DataClass(Interval.newClosed(5, 7)), new DataClass(Interval.newOpen(7, 17))));
    }

    @Test
    public void testGetAnyEqual() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7), 0));
        tree.insert(new DataClass(Interval.newClosed(5, 7), 8));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));
        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));
        assertThat(tree.getAnyEqual(Interval.newClosed(5, 7)), anyOf(
                is(new DataClass(Interval.newClosed(5, 7), 0)), is(new DataClass(Interval.newClosed(5, 7), 8))));
        assertThat(tree.getAnyEqual(Interval.newClosed(0, 3)),
                is(new DataClass(Interval.newClosed(0, 3))));
    }

    @Test
    public void testGetAllEqual() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7), 0));
        tree.insert(new DataClass(Interval.newClosed(5, 7), 8));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));
        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));
        assertThat(tree.getAllEqual(Interval.newClosed(5, 7)), containsInAnyOrder(
                new DataClass(Interval.newClosed(5, 7), 0), new DataClass(Interval.newClosed(5, 7), 8)));
        assertThat(tree.getAllEqual(Interval.newClosed(0, 3)), containsInAnyOrder(
                new DataClass(Interval.newClosed(0, 3))));
    }

    @Test
    public void testGetAnyOverlappingWithPoint() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(7, 17)));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));
        assertThat(tree.getAnyContaining(4.9), is(new DataClass(Interval.newOpen(4, 5))));
        assertThat(tree.getAnyContaining(1.5), is(new DataClass(Interval.newClosed(0, 3))));
        assertThat(tree.getAnyContaining(19.0), is(nullValue()));
        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));
        assertThat(tree.getAnyContaining(4.9), anyOf(
                is(new DataClass(Interval.newOpen(4, 5))), is(new DataClass(Interval.newLeftClosed(2, 10)))));
        assertThat(tree.getAnyContaining(5), anyOf(
                is(new DataClass(Interval.newClosed(5, 7))), is(new DataClass(Interval.newLeftClosed(2, 10)))));
    }

    @Test
    public void testGetAnyOverlappingWithInterval() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(7, 17)));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));

        assertThat(tree.getAnyOverlapping(Interval.newClosed(3, 7)), anyOf(
                is(new DataClass(Interval.newClosed(0, 3))), is(new DataClass(Interval.newRightClosed(3, 4))),
                is(new DataClass(Interval.newOpen(4, 5))), is(new DataClass(Interval.newClosed(5, 7)))));
        assertThat(tree.getAnyOverlapping(Interval.newOpen(5, 8)), anyOf(
                is(new DataClass(Interval.newClosed(5, 7))), is(new DataClass(Interval.newOpen(7, 17)))));
        assertThat(tree.getAnyOverlapping(Interval.newRightClosed(17, 23)), is(nullValue()));

        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));

        assertThat(tree.getAnyOverlapping(Interval.newClosed(3, 7)), anyOf(
                is(new DataClass(Interval.newLeftClosed(2, 10))), is(new DataClass(Interval.newClosed(0, 3))),
                is(new DataClass(Interval.newRightClosed(3, 4))), is(new DataClass(Interval.newOpen(4, 5))),
                is(new DataClass(Interval.newClosed(5, 7)))));
        assertThat(tree.getAnyOverlapping(Interval.newOpen(5, 8)), anyOf(
                is(new DataClass(Interval.newLeftClosed(2, 10))), is(new DataClass(Interval.newClosed(5, 7))),
                is(new DataClass(Interval.newOpen(7, 17)))));
    }

    @Test
    public void testGetAllOverlappingWithPoint() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(7, 17)));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));
        assertThat(tree.getAllContaining(4.9), contains(new DataClass(Interval.newOpen(4, 5))));
        assertThat(tree.getAllContaining(1.5), contains(new DataClass(Interval.newClosed(0, 3))));
        assertThat(tree.getAllContaining(19.0), is(empty()));
        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));
        assertThat(tree.getAllContaining(4.9), containsInAnyOrder(
                new DataClass(Interval.newOpen(4, 5)), new DataClass(Interval.newLeftClosed(2, 10))));
        assertThat(tree.getAllContaining(5), containsInAnyOrder(
                new DataClass(Interval.newClosed(5, 7)), new DataClass(Interval.newLeftClosed(2, 10))));
    }

    @Test
    public void testGetAllOverlappingWithInterval() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7)));
        tree.insert(new DataClass(Interval.newOpen(7, 17)));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));

        assertThat(tree.getAllOverlapping(Interval.newClosed(3, 7)), containsInAnyOrder(
                new DataClass(Interval.newClosed(0, 3)), new DataClass(Interval.newRightClosed(3, 4)),
                new DataClass(Interval.newOpen(4, 5)), new DataClass(Interval.newClosed(5, 7))));
        assertThat(tree.getAllOverlapping(Interval.newOpen(5, 8)), containsInAnyOrder(
                new DataClass(Interval.newClosed(5, 7)), new DataClass(Interval.newOpen(7, 17))));
        assertThat(tree.getAllOverlapping(Interval.newRightClosed(17, 23)), is(empty()));

        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));

        assertThat(tree.getAllOverlapping(Interval.newClosed(3, 7)), containsInAnyOrder(
                new DataClass(Interval.newLeftClosed(2, 10)), new DataClass(Interval.newClosed(0, 3)),
                new DataClass(Interval.newRightClosed(3, 4)), new DataClass(Interval.newOpen(4, 5)),
                new DataClass(Interval.newClosed(5, 7))));
        assertThat(tree.getAllOverlapping(Interval.newOpen(5, 8)), containsInAnyOrder(
                new DataClass(Interval.newLeftClosed(2, 10)), new DataClass(Interval.newClosed(5, 7)),
                new DataClass(Interval.newOpen(7, 17))));
    }

    @Test
    public void testGetContaning() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7), 0));
        tree.insert(new DataClass(Interval.newClosed(5, 7), 8));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));
        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));
        assertThat(tree.getAllContaining(Interval.newClosed(4.5, 4.8)), containsInAnyOrder(
                new DataClass(Interval.newOpen(4, 5)), new DataClass(Interval.newLeftClosed(2, 10))));
        assertThat(tree.getAllContaining(Interval.newClosed(2, 3)), containsInAnyOrder(
                new DataClass(Interval.newClosed(0, 3)), new DataClass(Interval.newLeftClosed(2, 10))));
    }

    @Test
    public void testGetContainedIn() {
        IntervalTree<DataClass> tree = new IntervalTree<>();
        tree.insert(new DataClass(Interval.newClosed(5, 7), 0));
        tree.insert(new DataClass(Interval.newClosed(5, 7), 8));
        tree.insert(new DataClass(Interval.newOpen(4, 5)));
        tree.insert(new DataClass(Interval.newClosed(0, 3)));
        tree.insert(new DataClass(Interval.newRightClosed(3, 4)));
        tree.insert(new DataClass(Interval.newLeftClosed(2, 10)));
        assertThat(tree.getAllContainedIn(Interval.newClosed(4, 7)), containsInAnyOrder(
                new DataClass(Interval.newClosed(5, 7), 0), new DataClass(Interval.newClosed(5, 7), 8),
                new DataClass(Interval.newOpen(4, 5))));
        assertThat(tree.getAllContainedIn(Interval.newOpen(0, 5)), containsInAnyOrder(
                new DataClass(Interval.newOpen(4, 5)), new DataClass(Interval.newRightClosed(3, 4))));
    }
}
