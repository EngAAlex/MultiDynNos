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
package ocotillo.dygraph.extra;

import java.util.List;
import ocotillo.dygraph.*;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests interval functions.
 */
public class EvolutionAnalyserTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testConvertToConstFunctions() {
        Evolution<Boolean> evolution = EvoBuilder.defaultAt(false)
                .withConst(Interval.newOpen(2, 12), false)
                .withRect(Interval.newRightClosed(14, 32), true, false, Interpolation.Std.constant)
                .withRect(Interval.newLeftClosed(30, 52), true, false, Interpolation.Std.step)
                .withRect(Interval.newRightClosed(70, 82), false, true, Interpolation.Std.step)
                .build();
        List<FunctionConst<Boolean>> convertedFunc = EvolutionAnalyser.convertToConstFunctions(evolution);
        assertThat(convertedFunc, contains(
                new FunctionConst<>(Interval.newOpen(2, 12), false),
                new FunctionConst<>(Interval.newOpen(14, 32), true),
                new FunctionConst<>(Interval.newClosed(32, 32), false),
                new FunctionConst<>(Interval.newClosed(30, 41), true),
                new FunctionConst<>(Interval.newOpen(41, 52), false),
                new FunctionConst<>(Interval.newRightClosed(70, 76), false),
                new FunctionConst<>(Interval.newRightClosed(76, 82), true)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMergeFunctions() {
        Evolution<Boolean> evolution = EvoBuilder.defaultAt(false)
                .withConst(Interval.newOpen(2, 12), false)
                .withRect(Interval.newLeftClosed(14, 32), true, false, Interpolation.Std.constant)
                .withRect(Interval.newLeftClosed(30, 52), true, false, Interpolation.Std.step)
                .withRect(Interval.newRightClosed(70, 82), false, true, Interpolation.Std.step)
                .build();
        Evolution<Boolean> mergedEvolution = EvolutionAnalyser.mergeFunctions(evolution);
        assertThat(mergedEvolution.getDefaultValue(), is(false));
        assertThat(mergedEvolution, contains(
                new FunctionConst<>(Interval.newClosed(14, 41), true),
                new FunctionConst<>(Interval.newRightClosed(76, 82), true)));

        evolution = EvoBuilder.defaultAt(true)
                .withConst(Interval.newOpen(2, 12), false)
                .withRect(Interval.newLeftClosed(14, 32), true, false, Interpolation.Std.constant)
                .withRect(Interval.newLeftClosed(30, 52), true, false, Interpolation.Std.step)
                .withRect(Interval.newRightClosed(70, 82), false, true, Interpolation.Std.step)
                .build();
        mergedEvolution = EvolutionAnalyser.mergeFunctions(evolution);
        assertThat(mergedEvolution.getDefaultValue(), is(true));
        assertThat(mergedEvolution, contains(
                new FunctionConst<>(Interval.newOpen(2, 12), false),
                new FunctionConst<>(Interval.newOpen(41, 52), false),
                new FunctionConst<>(Interval.newRightClosed(70, 76), false)));
    }

    @Test(expected = IllegalStateException.class)
    public void testMergeFunctionsWithConflict() {
        Evolution<Boolean> evolution = EvoBuilder.defaultAt(false)
                .withRect(Interval.newClosed(14, 32), true, false, Interpolation.Std.constant)
                .withRect(Interval.newLeftClosed(30, 52), true, false, Interpolation.Std.step)
                .build();
        EvolutionAnalyser.mergeFunctions(evolution);
    }

    @Test
    public void testGetIntervalsWithValue() {
        Evolution<Integer> evolution = EvoBuilder.defaultAt(0)
                .withConst(Interval.newOpen(2, 12), 1)
                .withRect(Interval.newLeftClosed(14, 32), 1, 1, Interpolation.Std.constant)
                .withRect(Interval.newLeftClosed(30, 52), 1, 2, Interpolation.Std.step)
                .withRect(Interval.newRightClosed(70, 82), 3, 1, Interpolation.Std.step)
                .build();

        List<Interval> intervals = EvolutionAnalyser.getIntervalsWithValue(evolution, 1);
        assertThat(intervals, contains(
                Interval.newOpen(2, 12),
                Interval.newClosed(14, 41),
                Interval.newRightClosed(76, 82)));

        intervals = EvolutionAnalyser.getIntervalsWithValue(evolution, 0);
        assertThat(intervals, contains(
                Interval.newRightClosed(Double.NEGATIVE_INFINITY, 2),
                Interval.newLeftClosed(12, 14),
                Interval.newClosed(52, 70),
                Interval.newOpen(82, Double.POSITIVE_INFINITY)));

        evolution = EvoBuilder.defaultAt(0)
                .withConst(Interval.newOpen(2, 12), 1)
                .withRect(Interval.newLeftClosed(14, 32), 1, 1, Interpolation.Std.constant)
                .withRect(Interval.newLeftClosed(30, 52), 1, 0, Interpolation.Std.step)
                .withRect(Interval.newRightClosed(70, 82), 0, 1, Interpolation.Std.step)
                .build();
        intervals = EvolutionAnalyser.getIntervalsWithValue(evolution, 0);
        assertThat(intervals, contains(
                Interval.newRightClosed(Double.NEGATIVE_INFINITY, 2),
                Interval.newLeftClosed(12, 14),
                Interval.newRightClosed(41, 76),
                Interval.newOpen(82, Double.POSITIVE_INFINITY)));
    }
}
