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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

public class IntervalTest {

    @Test
    public void testNewOpen() {
        Interval interval = Interval.newOpen(4, 6);
        assertThat(interval.leftBound(), is(4.0));
        assertThat(interval.rightBound(), is(6.0));
        assertThat(interval.isLeftClosed(), is(false));
        assertThat(interval.isRightClosed(), is(false));
    }

    @Test
    public void testNewLeftClosed() {
        Interval interval = Interval.newLeftClosed(4, 6);
        assertThat(interval.leftBound(), is(4.0));
        assertThat(interval.rightBound(), is(6.0));
        assertThat(interval.isLeftClosed(), is(true));
        assertThat(interval.isRightClosed(), is(false));
    }

    @Test
    public void testNewRightClosed() {
        Interval interval = Interval.newRightClosed(4, 6);
        assertThat(interval.leftBound(), is(4.0));
        assertThat(interval.rightBound(), is(6.0));
        assertThat(interval.isLeftClosed(), is(false));
        assertThat(interval.isRightClosed(), is(true));
    }

    @Test
    public void testNewClosed() {
        Interval interval = Interval.newClosed(4, 6);
        assertThat(interval.leftBound(), is(4.0));
        assertThat(interval.rightBound(), is(6.0));
        assertThat(interval.isLeftClosed(), is(true));
        assertThat(interval.isRightClosed(), is(true));
    }

    @Test
    public void testNewCustom() {
        Interval interval = Interval.newCustom(4, 6, true, false);
        assertThat(interval.leftBound(), is(4.0));
        assertThat(interval.rightBound(), is(6.0));
        assertThat(interval.isLeftClosed(), is(true));
        assertThat(interval.isRightClosed(), is(false));
    }

    @Test
    public void testInfinityIsAlwaysOpen() {
        assertThat(Interval.newClosed(Double.NEGATIVE_INFINITY, 0).isLeftClosed(), is(false));
        assertThat(Interval.newRightClosed(0, Double.POSITIVE_INFINITY).isRightClosed(), is(false));
    }

    @Test
    public void testNullIntervasls() {
        assertThat(Interval.newClosed(6, 2), is(nullValue()));
        assertThat(Interval.newOpen(6, 6), is(nullValue()));
        assertThat(Interval.newLeftClosed(6, 6), is(nullValue()));
        assertThat(Interval.newRightClosed(6, 6), is(nullValue()));
    }

    @Test
    public void testContains() {
        assertThat(Interval.newLeftClosed(4, 6).contains(5), is(true));
        assertThat(Interval.newLeftClosed(4, 6).contains(9), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(4), is(true));
        assertThat(Interval.newLeftClosed(4, 6).contains(6), is(false));
    }

    @Test
    public void testIntersection() {
        assertThat(Interval.newLeftClosed(4, 6).intersection(Interval.newClosed(-2, 1)), is(nullValue()));
        assertThat(Interval.newLeftClosed(4, 6).intersection(Interval.newClosed(-2, 4)), is(Interval.newClosed(4, 4)));
        assertThat(Interval.newLeftClosed(4, 6).intersection(Interval.newClosed(-2, 5)), is(Interval.newClosed(4, 5)));
        assertThat(Interval.newLeftClosed(4, 6).intersection(Interval.newClosed(-2, 8)), is(Interval.newLeftClosed(4, 6)));
        assertThat(Interval.newLeftClosed(4, 6).intersection(Interval.newClosed(5, 8)), is(Interval.newLeftClosed(5, 6)));
        assertThat(Interval.newLeftClosed(4, 6).intersection(Interval.newClosed(6, 8)), is(nullValue()));
    }

    @Test
    public void testFusion() {
        assertThat(Interval.newLeftClosed(4, 6).fusion(Interval.newOpen(-2, 1)), is(Interval.newOpen(-2, 6)));
        assertThat(Interval.newLeftClosed(4, 6).fusion(Interval.newClosed(5, 6)), is(Interval.newClosed(4, 6)));
        assertThat(Interval.newLeftClosed(4, 6).fusion(Interval.newClosed(-2, 8)), is(Interval.newClosed(-2, 8)));
        assertThat(Interval.newLeftClosed(4, 6).fusion(Interval.newLeftClosed(5, 8)), is(Interval.newLeftClosed(4, 8)));
        assertThat(Interval.newLeftClosed(4, 6).fusion(Interval.newClosed(6, 8)), is(Interval.newClosed(4, 8)));
    }

    @Test
    public void testOverlapsWith() {
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(-2, 1)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(-2, 4)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(-2, 5)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(-2, 8)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(4, 6)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newLeftClosed(4, 6)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(5, 6)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(5, 8)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).overlapsWith(Interval.newClosed(6, 8)), is(false));
    }

    @Test
    public void testContainsInterval() {
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(-2, 1)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(-2, 4)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(-2, 5)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(-2, 8)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(4, 6)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newLeftClosed(4, 6)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(5, 6)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(5, 8)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).contains(Interval.newClosed(6, 8)), is(false));
    }

    @Test
    public void testContainedIn() {
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(-2, 1)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(-2, 4)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(-2, 5)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(-2, 8)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(4, 6)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newLeftClosed(4, 6)), is(true));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(5, 6)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(5, 8)), is(false));
        assertThat(Interval.newLeftClosed(4, 6).isContainedIn(Interval.newClosed(6, 8)), is(false));
    }

    @Test
    public void testWidth() {
        assertThat(Interval.newRightClosed(-6, 4).width(), is(10.0));
        assertThat(Interval.newOpen(-6, 4).width(), is(10.0));
        assertThat(Interval.newClosed(-6, -6).width(), is(0.0));
    }

    @Test
    public void testToString() {
        assertThat(Interval.newLeftClosed(4, 6).toString(), is("[4.0, 6.0)"));
        assertThat(Interval.newClosed(-2, 0).toString(), is("[-2.0, 0.0]"));
        assertThat(Interval.newOpen(-2, 40).toString(), is("(-2.0, 40.0)"));
    }

    @Test
    public void testParse() {
        assertThat(Interval.parse("(4,5]"), is(Interval.newRightClosed(4, 5)));
        assertThat(Interval.parse("  [ -4.55, 5.3  )"), is(Interval.newLeftClosed(-4.55, 5.3)));
    }
}
