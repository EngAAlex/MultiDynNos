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
import ocotillo.geometry.IntervalBox;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.junit.Test;

public class MultidimIntervalTreeTest {

    @EqualsAndHashCode
    private static class DataClass implements MultidimIntervalTree.Data {

        private final IntervalBox intervalBox;
        private final int id;

        public DataClass(IntervalBox intervalBox) {
            this.intervalBox = intervalBox;
            this.id = 0;
        }

        public DataClass(Interval... intervals) {
            this.intervalBox = IntervalBox.newInstance(intervals);
            this.id = 0;
        }

        public DataClass(int id, Interval... intervals) {
            this.intervalBox = IntervalBox.newInstance(intervals);
            this.id = id;
        }

        public DataClass(IntervalBox intervalBox, int id) {
            this.intervalBox = intervalBox;
            this.id = id;
        }

        @Override
        public IntervalBox intervalBox() {
            return intervalBox;
        }

        @Override
        public String toString() {
            return id + "";
        }
    }

    private IntervalBox first = IntervalBox.newInstance(
            Interval.newClosed(3, 4), Interval.newOpen(5, 6), Interval.newLeftClosed(8, 9));
    private IntervalBox second = IntervalBox.newInstance(
            Interval.newClosed(1, 2), Interval.newOpen(2, 3), Interval.newLeftClosed(3, 4));

    private DataClass firstData = new DataClass(first);
    private DataClass secondData = new DataClass(second);

    @Test
    public void testInsert() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        assertThat(tree.size(), is(0));
        tree.insert(firstData);
        assertThat(tree.size(), is(1));
        assertThat(tree.contains(firstData), is(true));
        assertThat(tree.contains(new DataClass(first)), is(true));
    }

    @Test
    public void testInsertDuplicates() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        assertThat(tree.size(), is(0));
        tree.insert(firstData);
        assertThat(tree.size(), is(1));
        tree.insert(firstData);
        assertThat(tree.size(), is(1));
        tree.insert(new DataClass(first));
        assertThat(tree.size(), is(1));
        tree.insert(new DataClass(first, 3));
        assertThat(tree.size(), is(2));
    }

    @Test
    public void testDelete() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        tree.insert(new DataClass(first));
        tree.insert(new DataClass(second));
        assertThat(tree.size(), is(2));
        assertThat(tree.contains(new DataClass(second)), is(true));
        tree.delete(new DataClass(second));
        assertThat(tree.size(), is(1));
        assertThat(tree.contains(new DataClass(second)), is(false));
        tree.delete(new DataClass(second));
        assertThat(tree.size(), is(1));
    }

    @Test
    public void testIsEmpty() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        assertThat(tree.isEmpty(), is(true));
        tree.insert(firstData);
        assertThat(tree.isEmpty(), is(false));
    }

    @Test
    public void testClear() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        tree.insert(firstData);
        tree.insert(secondData);
        assertThat(tree.isEmpty(), is(false));
        tree.clear();
        assertThat(tree.isEmpty(), is(true));
        tree.insert(secondData);
        tree.insert(firstData);
        assertThat(tree.isEmpty(), is(false));
    }

    @Test
    public void testGetAllEqual() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        DataClass a = new DataClass(1, Interval.newClosed(0, 10), Interval.newClosed(0, 10), Interval.newClosed(0, 10));
        DataClass b = new DataClass(2, Interval.newClosed(9, 10), Interval.newClosed(0, 6), Interval.newClosed(4, 7));
        DataClass c = new DataClass(3, Interval.newClosed(5, 13), Interval.newClosed(1, 7), Interval.newClosed(2, 6));
        DataClass d = new DataClass(4, Interval.newClosed(2, 7), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass e = new DataClass(5, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass f = new DataClass(6, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        DataClass g = new DataClass(7, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        tree.insert(a);
        tree.insert(b);
        tree.insert(c);
        tree.insert(d);
        tree.insert(e);
        tree.insert(f);
        tree.insert(g);
        assertThat(tree.getAllEqual(
                IntervalBox.newInstance(Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5))),
                containsInAnyOrder(f, g));
        assertThat(tree.getAllEqual(
                IntervalBox.newInstance(Interval.newClosed(5, 13), Interval.newClosed(1, 7), Interval.newClosed(2, 6))),
                containsInAnyOrder(c));
    }

    @Test
    public void testGetAllOverlapping() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        DataClass a = new DataClass(1, Interval.newClosed(0, 10), Interval.newClosed(0, 10), Interval.newClosed(0, 10));
        DataClass b = new DataClass(2, Interval.newClosed(9, 10), Interval.newClosed(0, 6), Interval.newClosed(4, 7));
        DataClass c = new DataClass(3, Interval.newClosed(5, 13), Interval.newClosed(1, 7), Interval.newClosed(2, 6));
        DataClass d = new DataClass(4, Interval.newClosed(2, 7), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass e = new DataClass(5, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass f = new DataClass(6, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        DataClass g = new DataClass(7, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        tree.insert(a);
        tree.insert(b);
        tree.insert(c);
        tree.insert(d);
        tree.insert(e);
        tree.insert(f);
        tree.insert(g);
        assertThat(tree.getAllOverlapping(
                IntervalBox.newInstance(Interval.newClosed(2, 4), Interval.newClosed(2, 7), Interval.newClosed(4, 5))),
                containsInAnyOrder(a, d, e, f, g));
        assertThat(tree.getAllOverlapping(
                IntervalBox.newInstance(Interval.newClosed(0, 1), Interval.newClosed(8, 9), Interval.newClosed(2, 6))),
                containsInAnyOrder(a));
    }

    @Test
    public void testGetAllContainedIn() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        DataClass a = new DataClass(1, Interval.newClosed(0, 10), Interval.newClosed(0, 10), Interval.newClosed(0, 10));
        DataClass b = new DataClass(2, Interval.newClosed(9, 10), Interval.newClosed(0, 6), Interval.newClosed(4, 7));
        DataClass c = new DataClass(3, Interval.newClosed(5, 13), Interval.newClosed(1, 7), Interval.newClosed(2, 6));
        DataClass d = new DataClass(4, Interval.newClosed(2, 7), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass e = new DataClass(5, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass f = new DataClass(6, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        DataClass g = new DataClass(7, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        tree.insert(a);
        tree.insert(b);
        tree.insert(c);
        tree.insert(d);
        tree.insert(e);
        tree.insert(f);
        tree.insert(g);
        assertThat(tree.getAllContainedIn(
                IntervalBox.newInstance(Interval.newClosed(2, 10), Interval.newClosed(0, 8), Interval.newClosed(-1, 10))),
                containsInAnyOrder(b, d));
        assertThat(tree.getAllContainedIn(
                IntervalBox.newInstance(Interval.newClosed(0, 10), Interval.newClosed(0, 10), Interval.newLeftClosed(0, 10))),
                containsInAnyOrder(b, f, g));
    }

    @Test
    public void testGetAllContaining() {
        MultidimIntervalTree<DataClass> tree = new MultidimIntervalTree<>(3);
        DataClass a = new DataClass(1, Interval.newClosed(0, 10), Interval.newClosed(0, 10), Interval.newClosed(0, 10));
        DataClass b = new DataClass(2, Interval.newClosed(9, 10), Interval.newClosed(0, 6), Interval.newClosed(4, 7));
        DataClass c = new DataClass(3, Interval.newClosed(5, 13), Interval.newClosed(1, 7), Interval.newClosed(2, 6));
        DataClass d = new DataClass(4, Interval.newClosed(2, 7), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass e = new DataClass(5, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(0, 10));
        DataClass f = new DataClass(6, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        DataClass g = new DataClass(7, Interval.newClosed(0, 10), Interval.newClosed(4, 7), Interval.newClosed(3, 5));
        tree.insert(a);
        tree.insert(b);
        tree.insert(c);
        tree.insert(d);
        tree.insert(e);
        tree.insert(f);
        tree.insert(g);
        assertThat(tree.getAllContaining(
                IntervalBox.newInstance(Interval.newClosed(1, 4), Interval.newClosed(4, 4), Interval.newClosed(4, 4))),
                containsInAnyOrder(a, e, f, g));
        assertThat(tree.getAllContaining(
                IntervalBox.newInstance(Interval.newClosed(9, 10), Interval.newClosed(6, 6), Interval.newClosed(8, 8))),
                containsInAnyOrder(a, e));
    }
}
