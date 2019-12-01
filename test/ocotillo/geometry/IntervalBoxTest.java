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
package ocotillo.geometry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

public class IntervalBoxTest {

    @Test
    public void testNewInstance() {
        IntervalBox box = IntervalBox.newInstance(
                Interval.newClosed(3, 4), Interval.newOpen(5, 6),
                Interval.newLeftClosed(8, 9), Interval.newRightClosed(12, 14));

        assertThat(box.left(), is(3.0));
        assertThat(box.right(), is(4.0));
        assertThat(box.leftClosed(), is(true));
        assertThat(box.rightClosed(), is(true));

        assertThat(box.bottom(), is(5.0));
        assertThat(box.top(), is(6.0));
        assertThat(box.bottomClosed(), is(false));
        assertThat(box.topClosed(), is(false));

        assertThat(box.near(), is(8.0));
        assertThat(box.far(), is(9.0));
        assertThat(box.nearClosed(), is(true));
        assertThat(box.farClosed(), is(false));

        assertThat(box.leftBound(3), is(12.0));
        assertThat(box.rightBound(3), is(14.0));
        assertThat(box.leftStatus(3), is(false));
        assertThat(box.rightStatus(3), is(true));
    }

    @Test
    public void testInterval() {
        IntervalBox box = IntervalBox.newInstance(
                Interval.newClosed(3, 4), Interval.newOpen(5, 6),
                Interval.newLeftClosed(8, 9), Interval.newRightClosed(12, 14));

        assertThat(box.interval(0), is(Interval.newClosed(3, 4)));
        assertThat(box.interval(1), is(Interval.newOpen(5, 6)));
        assertThat(box.interval(2), is(Interval.newLeftClosed(8, 9)));
        assertThat(box.interval(3), is(Interval.newRightClosed(12, 14)));
    }

    @Test
    public void testEquals() {
        IntervalBox box = IntervalBox.newInstance(
                Interval.newClosed(3, 4), Interval.newOpen(5, 6),
                Interval.newLeftClosed(8, 9), Interval.newRightClosed(12, 14));
        assertThat(box, is(box));
        assertThat(box, is(IntervalBox.newInstance(
                Interval.newClosed(3, 4), Interval.newOpen(5, 6),
                Interval.newLeftClosed(8, 9), Interval.newRightClosed(12, 14))));
    }
}
