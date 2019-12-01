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

import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Test for the Evolution class.
 */
public class EvolutionTest {

    private final Interval firstInterval = Interval.newClosed(30, 40);
    private final Interval secondInterval = Interval.newRightClosed(40, 60);

    private final FunctionRect<Double> firstFunction = new FunctionRect.Double(firstInterval, 10, 20, Interpolation.Std.linear);
    private final FunctionRect<Double> secondFunction = new FunctionRect.Double(secondInterval, 20, 40, Interpolation.Std.linear);

    @Test
    public void testConstruct() {
        Evolution<Double> evolution = new Evolution<>(6.0);
        assertThat(evolution.isEmpty(), is(true));
        assertThat(evolution.size(), is(0));
        assertThat(evolution.getDefaultValue(), is(6.0));
    }

    @Test
    public void testSetDefault() {
        Evolution<Double> evolution = new Evolution<>(6.0);
        assertThat(evolution.getDefaultValue(), is(6.0));
        evolution.setDefaultValue(-10.0);
        assertThat(evolution.getDefaultValue(), is(-10.0));
    }

    @Test
    public void testInsert() {
        Evolution<Double> evolution = new Evolution<>(6.0);
        evolution.insert(secondFunction);
        assertThat(evolution.isEmpty(), is(false));
        assertThat(evolution.size(), is(1));
        assertThat(evolution.contains(firstFunction), is(false));
        assertThat(evolution.contains(secondFunction), is(true));
        evolution.insert(firstFunction);
        assertThat(evolution.isEmpty(), is(false));
        assertThat(evolution.size(), is(2));
        assertThat(evolution.contains(firstFunction), is(true));
        assertThat(evolution.contains(secondFunction), is(true));
    }

    @Test
    public void testDelete() {
        Evolution<Double> evolution = new Evolution<>(6.0);
        evolution.insert(secondFunction);
        evolution.insert(firstFunction);
        evolution.delete(firstFunction);
        assertThat(evolution.isEmpty(), is(false));
        assertThat(evolution.size(), is(1));
        assertThat(evolution.contains(firstFunction), is(false));
        assertThat(evolution.contains(secondFunction), is(true));
        evolution.insert(firstFunction);
        assertThat(evolution.isEmpty(), is(false));
        assertThat(evolution.size(), is(2));
        assertThat(evolution.contains(firstFunction), is(true));
        assertThat(evolution.contains(secondFunction), is(true));
        evolution.delete(secondFunction);
        assertThat(evolution.isEmpty(), is(false));
        assertThat(evolution.size(), is(1));
        assertThat(evolution.contains(firstFunction), is(true));
        assertThat(evolution.contains(secondFunction), is(false));
        evolution.delete(firstFunction);
        assertThat(evolution.isEmpty(), is(true));
        assertThat(evolution.size(), is(0));
        assertThat(evolution.contains(firstFunction), is(false));
        assertThat(evolution.contains(secondFunction), is(false));
    }

    @Test
    public void testIsDefinedAt() {
        Evolution<Double> evolution = new Evolution<>(6.0);
        assertThat(evolution.isDefinedAt(42.0), is(false));
        evolution.insert(secondFunction);
        assertThat(evolution.isDefinedAt(42.0), is(true));
        assertThat(evolution.isDefinedAt(40.0), is(false));
        assertThat(evolution.isDefinedAt(35.0), is(false));
        evolution.insert(firstFunction);
        assertThat(evolution.isDefinedAt(42.0), is(true));
        assertThat(evolution.isDefinedAt(40.0), is(true));
        assertThat(evolution.isDefinedAt(35.0), is(true));
        evolution.delete(secondFunction);
        assertThat(evolution.isDefinedAt(42.0), is(false));
        assertThat(evolution.isDefinedAt(40.0), is(true));
        assertThat(evolution.isDefinedAt(35.0), is(true));
    }

    @Test
    public void testValueAt() {
        Evolution<Double> evolution = new Evolution<>(6.0);
        assertThat(evolution.valueAt(42.0), is(6.0));
        evolution.insert(secondFunction);
        assertThat(evolution.valueAt(42.0), is(22.0));
        assertThat(evolution.valueAt(40.0), is(6.0));
        assertThat(evolution.valueAt(35.0), is(6.0));
        evolution.insert(firstFunction);
        assertThat(evolution.valueAt(42.0), is(22.0));
        assertThat(evolution.valueAt(40.0), is(20.0));
        assertThat(evolution.valueAt(35.0), is(15.0));
        evolution.delete(secondFunction);
        assertThat(evolution.valueAt(42.0), is(6.0));
        assertThat(evolution.valueAt(40.0), is(20.0));
        assertThat(evolution.valueAt(35.0), is(15.0));
    }

    @Test
    public void testEquals() {
        Evolution<Double> first = new Evolution<>(6.0);
        Evolution<Double> second = new Evolution<>(6.0);
        assertThat(first, is(second));
        first.insert(firstFunction);
        first.insert(secondFunction);
        second.insert(firstFunction);
        second.insert(secondFunction);
        assertThat(first, is(second));
    }
}
