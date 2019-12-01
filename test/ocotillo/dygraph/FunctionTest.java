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

import ocotillo.geometry.Coordinates;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Tests interval functions.
 */
public class FunctionTest {

    private final Interval interval = Interval.newClosed(30, 40);

    @Test
    public void testGetSet() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.linear);
        assertThat(function.leftValue(), is(10.0));
        assertThat(function.rightValue(), is(20.0));
        assertThat(function.interval(), is(interval));
    }

    @Test
    public void testDoubleFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.linear);
        assertThat(function.valueAt(31), isAlmost(11.0));
        assertThat(function.valueAt(32.4), isAlmost(12.4));
        assertThat(function.valueAt(37.64), isAlmost(17.64));
    }

    @Test
    public void testCoordinatesFunction() {
        FunctionRect<Coordinates> function = new FunctionRect.Coordinates(interval,
                new Coordinates(10, 10), new Coordinates(5, 10), Interpolation.Std.linear);
        assertThat(function.valueAt(31), isAlmost(new Coordinates(9.5, 10)));
        assertThat(function.valueAt(32.4), isAlmost(new Coordinates(8.8, 10)));
        assertThat(function.valueAt(37.64), isAlmost(new Coordinates(6.18, 10)));
    }

    @Test
    public void testStringFunction() {
        FunctionRect<String> function = new FunctionRect.String(interval,
                "alpha", "beta", Interpolation.Std.step);
        assertThat(function.valueAt(31), is("alpha"));
        assertThat(function.valueAt(32.4), is("alpha"));
        assertThat(function.valueAt(37.64), is("beta"));
    }

    @Test
    public void testConstantInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.constant);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(10.0));
        assertThat(function.valueAt(32.4), isAlmost(10.0));
        assertThat(function.valueAt(35), isAlmost(10.0));
        assertThat(function.valueAt(37.64), isAlmost(10.0));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testStepInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.step);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(10.0));
        assertThat(function.valueAt(32.4), isAlmost(10.0));
        assertThat(function.valueAt(35), isAlmost(10.0));
        assertThat(function.valueAt(37.64), isAlmost(20.0));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testLinearInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.linear);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(11.0));
        assertThat(function.valueAt(32.4), isAlmost(12.4));
        assertThat(function.valueAt(35), isAlmost(15));
        assertThat(function.valueAt(37.64), isAlmost(17.64));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testSmoothStepInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.smoothStep);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(10.28));
        assertThat(function.valueAt(32.4), isAlmost(11.45152));
        assertThat(function.valueAt(35), isAlmost(15));
        assertThat(function.valueAt(37.64), isAlmost(18.59200512));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testSmootherStepInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.smootherStep);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(10.0856));
        assertThat(function.valueAt(32.4), isAlmost(10.93251174));
        assertThat(function.valueAt(35), isAlmost(15));
        assertThat(function.valueAt(37.64), isAlmost(19.10695611));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testLargeGaussianInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.largeGaussian);
        assertThat(function.valueAt(30), isAlmost(10.0386592014));
        assertThat(function.valueAt(31), isAlmost(10.2856550078));
        assertThat(function.valueAt(32.4), isAlmost(12.2263486588));
        assertThat(function.valueAt(35), isAlmost(20.0));
        assertThat(function.valueAt(37.64), isAlmost(12.1250282428));
        assertThat(function.valueAt(40), isAlmost(10.0386592014));
    }

    @Test
    public void testMediumGaussianInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.mediumGaussian);
        assertThat(function.valueAt(30), isAlmost(10.0000372665));
        assertThat(function.valueAt(31), isAlmost(10.0033546263));
        assertThat(function.valueAt(32.4), isAlmost(10.3404745473));
        assertThat(function.valueAt(35), isAlmost(20.0));
        assertThat(function.valueAt(37.64), isAlmost(10.3065988979));
        assertThat(function.valueAt(40), isAlmost(10.0000372665));
    }

    @Test
    public void testSmallGaussianInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.smallGaussian);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(10.0));
        assertThat(function.valueAt(32.4), isAlmost(10.0000134381));
        assertThat(function.valueAt(35), isAlmost(20.0));
        assertThat(function.valueAt(37.64), isAlmost(10.0000088365));
        assertThat(function.valueAt(40), isAlmost(10.0));
    }

    @Test
    public void testSlowChargeInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.slowCharge);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(13.9346934029));
        assertThat(function.valueAt(32.4), isAlmost(16.9880578809));
        assertThat(function.valueAt(35), isAlmost(19.1791500138));
        assertThat(function.valueAt(37.64), isAlmost(19.7807219911));
        assertThat(function.valueAt(40), isAlmost(19.93262053));
    }

    @Test
    public void testMediumChargeInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.mediumCharge);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(16.3212055883));
        assertThat(function.valueAt(32.4), isAlmost(19.0928204671));
        assertThat(function.valueAt(35), isAlmost(19.93262053));
        assertThat(function.valueAt(37.64), isAlmost(19.9951917155));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testFastChargeInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.fastCharge);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(18.6466471676));
        assertThat(function.valueAt(32.4), isAlmost(19.9177025295));
        assertThat(function.valueAt(35), isAlmost(20.0));
        assertThat(function.valueAt(37.64), isAlmost(20.0));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testSlowEndChargeInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.slowEndCharge);
        assertThat(function.valueAt(30), isAlmost(10.06737947));
        assertThat(function.valueAt(31), isAlmost(10.1110899654));
        assertThat(function.valueAt(32.4), isAlmost(10.2237077186));
        assertThat(function.valueAt(35), isAlmost(10.8208499862));
        assertThat(function.valueAt(37.64), isAlmost(13.072787386));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testMediumEndChargeInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.mediumEndCharge);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(10.001234098));
        assertThat(function.valueAt(32.4), isAlmost(10.0050045143));
        assertThat(function.valueAt(35), isAlmost(10.06737947));
        assertThat(function.valueAt(37.64), isAlmost(10.944202232));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }

    @Test
    public void testFastEndChargeInterpolationFunction() {
        FunctionRect<Double> function = new FunctionRect.Double(interval, 10, 20, Interpolation.Std.fastEndCharge);
        assertThat(function.valueAt(30), isAlmost(10.0));
        assertThat(function.valueAt(31), isAlmost(10.0));
        assertThat(function.valueAt(32.4), isAlmost(10.0));
        assertThat(function.valueAt(35), isAlmost(10.0));
        assertThat(function.valueAt(37.64), isAlmost(10.0891517855));
        assertThat(function.valueAt(40), isAlmost(20.0));
    }
}
