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
public class EvoBuilderTest {

    private final Interval firstInterval = Interval.newClosed(30, 40);
    private final Interval secondInterval = Interval.newRightClosed(40, 60);
    private final Interval thirdInterval = Interval.newRightClosed(60, 70);

    private final Function<Double> firstFunction = new FunctionRect.Double(firstInterval, 10, 20, Interpolation.Std.linear);
    private final Function<Double> secondFunction = new FunctionRect.Double(secondInterval, 20, 40, Interpolation.Std.linear);
    private final Function<Double> thirdFunction = new FunctionConst<>(thirdInterval, 19.0);

    @Test
    public void testConstruction() {
        Evolution<Double> evolution = EvoBuilder.defaultAt(6.0)
                .withRect(Interval.newClosed(30, 40), 10.0, 20.0, Interpolation.Std.linear)
                .withRect(Interval.newRightClosed(40, 60), 20.0, 40.0, Interpolation.Std.linear)
                .withConst(Interval.newRightClosed(60, 70), 19.0)
                .build();
        assertThat(evolution.getDefaultValue(), is(6.0));
        assertThat(evolution.contains(firstFunction), is(true));
        assertThat(evolution.contains(secondFunction), is(true));
        assertThat(evolution.contains(thirdFunction), is(true));
    }
}
