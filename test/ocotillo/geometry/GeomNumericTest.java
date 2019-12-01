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

import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class GeomNumericTest {

    @Test
    public void testRandomDouble() {
        int[] buckets = new int[10];
        for (int i = 0; i < 200; i++) {
            double random = GeomNumeric.randomDouble(100);
            assertThat(random, greaterThanOrEqualTo(0.0));
            assertThat(random, lessThanOrEqualTo(100.0));
            buckets[(int) random / 10] += 1;
        }

        for (int bucket : buckets) {
            assertThat(bucket, greaterThan(0));
        }

        buckets = new int[10];
        for (int i = 0; i < 200; i++) {
            double random = GeomNumeric.randomDouble(50, 100);
            assertThat(random, greaterThanOrEqualTo(50.0));
            assertThat(random, lessThanOrEqualTo(100.0));
            buckets[(int) random / 10] += 1;
        }

        for (int i = 0; i < 5; ++i) {
            assertThat(buckets[i], is(0));
        }
        for (int i = 5; i < 10; ++i) {
            assertThat(buckets[i], greaterThan(0));
        }
    }

    @Test
    public void testRadiansToDegrees() {
        assertThat(GeomNumeric.radiansToDegrees(1), isAlmost(57.2957795131));
        assertThat(GeomNumeric.radiansToDegrees(Math.PI / 2), isAlmost(90));
        assertThat(GeomNumeric.radiansToDegrees(Math.PI), isAlmost(180));
        assertThat(GeomNumeric.radiansToDegrees(Math.PI * 2), isAlmost(360));
        assertThat(GeomNumeric.radiansToDegrees(2.63), isAlmost(150.687900119));
    }

    @Test
    public void testDegreesToRadians() {
        assertThat(GeomNumeric.degreesToRadians(1), isAlmost(0.0174532925));
        assertThat(GeomNumeric.degreesToRadians(90), isAlmost(Math.PI / 2));
        assertThat(GeomNumeric.degreesToRadians(180), isAlmost(Math.PI));
        assertThat(GeomNumeric.degreesToRadians(360), isAlmost(Math.PI * 2));
        assertThat(GeomNumeric.degreesToRadians(150.687900119), isAlmost(2.63));
    }

    @Test
    public void testNormalizeRadiansAngle() {
        assertThat(GeomNumeric.normalizeRadiansAngle(0), isAlmost(0));
        assertThat(GeomNumeric.normalizeRadiansAngle(Math.PI), isAlmost(-Math.PI));
        assertThat(GeomNumeric.normalizeRadiansAngle(2 * Math.PI), isAlmost(0));
        assertThat(GeomNumeric.normalizeRadiansAngle(1), isAlmost(1));
        assertThat(GeomNumeric.normalizeRadiansAngle(1 - 2 * Math.PI), isAlmost(1));
        assertThat(GeomNumeric.normalizeRadiansAngle(1 - 6 * Math.PI), isAlmost(1));
        assertThat(GeomNumeric.normalizeRadiansAngle(-1 + 2 * Math.PI), isAlmost(-1));
        assertThat(GeomNumeric.normalizeRadiansAngle(-1), isAlmost(-1));
        assertThat(GeomNumeric.normalizeRadiansAngle(-1 - 6 * Math.PI), isAlmost(-1));
    }

    @Test
    public void testNormalizeDegreesAngle() {
        assertThat(GeomNumeric.normalizeDegreesAngle(0), isAlmost(0));
        assertThat(GeomNumeric.normalizeDegreesAngle(180), isAlmost(-180));
        assertThat(GeomNumeric.normalizeDegreesAngle(360), isAlmost(0));
        assertThat(GeomNumeric.normalizeDegreesAngle(1), isAlmost(1));
        assertThat(GeomNumeric.normalizeDegreesAngle(361), isAlmost(1));
        assertThat(GeomNumeric.normalizeDegreesAngle(721), isAlmost(1));
        assertThat(GeomNumeric.normalizeDegreesAngle(359), isAlmost(-1));
        assertThat(GeomNumeric.normalizeDegreesAngle(-1), isAlmost(-1));
        assertThat(GeomNumeric.normalizeDegreesAngle(-361), isAlmost(-1));
    }

    @Test
    public void testPosNormalizeRadiansAngle() {
        assertThat(GeomNumeric.posNormalizeRadiansAngle(0), isAlmost(0));
        assertThat(GeomNumeric.posNormalizeRadiansAngle(Math.PI), isAlmost(Math.PI));
        assertThat(GeomNumeric.posNormalizeRadiansAngle(2 * Math.PI), isAlmost(0));
        assertThat(GeomNumeric.posNormalizeRadiansAngle(1), isAlmost(1));
        assertThat(GeomNumeric.posNormalizeRadiansAngle(1 - 2 * Math.PI), isAlmost(1));
        assertThat(GeomNumeric.posNormalizeRadiansAngle(1 - 6 * Math.PI), isAlmost(1));
        assertThat(GeomNumeric.posNormalizeRadiansAngle(-1), isAlmost(2 * Math.PI - 1));
        assertThat(GeomNumeric.posNormalizeRadiansAngle(-1 - 6 * Math.PI), isAlmost(2 * Math.PI - 1));
    }

    @Test
    public void testPosNormalizeDegreesAngle() {
        assertThat(GeomNumeric.posNormalizeDegreesAngle(0), isAlmost(0));
        assertThat(GeomNumeric.posNormalizeDegreesAngle(180), isAlmost(180));
        assertThat(GeomNumeric.posNormalizeDegreesAngle(360), isAlmost(0));
        assertThat(GeomNumeric.posNormalizeDegreesAngle(1), isAlmost(1));
        assertThat(GeomNumeric.posNormalizeDegreesAngle(361), isAlmost(1));
        assertThat(GeomNumeric.posNormalizeDegreesAngle(721), isAlmost(1));
        assertThat(GeomNumeric.posNormalizeDegreesAngle(-1), isAlmost(359));
        assertThat(GeomNumeric.posNormalizeDegreesAngle(-361), isAlmost(359));
    }

    @Test
    public void testIsDirectionInAngle() {
        assertThat(GeomNumeric.isDirectionInAngle(0.3, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.isDirectionInAngle(1, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.isDirectionInAngle(1, 0.3 + 2 * Math.PI, 1.7 - 2 * Math.PI), is(true));
        assertThat(GeomNumeric.isDirectionInAngle(-1 + 4 * Math.PI, -1.7 - 2 * Math.PI, -0.3 + 2 * Math.PI), is(true));
        assertThat(GeomNumeric.isDirectionInAngle(-1, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.isDirectionInAngle(0.1, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.isDirectionInAngle(2.1, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.isDirectionInAngle(1.0, 1.7, 0.3), is(false));
    }

    @Test
    public void testIsAngleInAngle() {
        assertThat(GeomNumeric.isAngleInAngle(0.3, 0.3, 0.3, 0.3), is(true));
        assertThat(GeomNumeric.isAngleInAngle(0.3, 0.3, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.isAngleInAngle(0.3, 1.7, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.isAngleInAngle(0.5, 0.8, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.isAngleInAngle(5.5, 5.8, 5.3, 6.7), is(true));
        assertThat(GeomNumeric.isAngleInAngle(5.5, 5.8, 5.3, 0.5), is(true));
        assertThat(GeomNumeric.isAngleInAngle(5.5, 5.8 + 2 * Math.PI, 5.3 - 2 * Math.PI, 0.5), is(true));
        assertThat(GeomNumeric.isAngleInAngle(0.2, 0.25, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.isAngleInAngle(0.2, 0.8, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.isAngleInAngle(0.5, 5.8, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.isAngleInAngle(0.2, 5.8, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.isAngleInAngle(0.2 + 6 * Math.PI, 5.8, 0.3 - 4 * Math.PI, 1.7), is(false));
    }

    @Test
    public void testAreAnglesOverlapping() {
        assertThat(GeomNumeric.areAnglesOverlapping(0.3, 0.3, 0.3, 0.3), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(0.3, 0.3, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(0.3, 1.7, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(0.5, 0.8, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(5.5, 5.8, 5.3, 6.7), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(5.5, 5.8, 5.3, 0.5), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(5.5, 5.8 + 2 * Math.PI, 5.3 - 2 * Math.PI, 0.5), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(0.2, 0.25, 0.3, 1.7), is(false));
        assertThat(GeomNumeric.areAnglesOverlapping(0.2, 0.8, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(0.5, 5.8, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(0.2, 5.8, 0.3, 1.7), is(true));
        assertThat(GeomNumeric.areAnglesOverlapping(3.2 + 6 * Math.PI, 5.8, 0.3 - 4 * Math.PI, 1.7), is(false));
    }

    @Test
    public void testCdf() {
        GeomE tolerance = new GeomXD(0.003);
        assertThat(tolerance.almostEqual(GeomNumeric.cdf(-1.0), 0.15865526139567465), is(true));
        assertThat(tolerance.almostEqual(GeomNumeric.cdf(1.0), 0.8413447460), is(true));
        assertThat(tolerance.almostEqual(GeomNumeric.cdf(3.0), 0.9986501019267444), is(true));
        assertThat(tolerance.almostEqual(GeomNumeric.cdf(1.0, 2.0, 1.0), 0.15865526139567465), is(true));
        assertThat(tolerance.almostEqual(GeomNumeric.cdf(3.0, 2.0, 1.0), 0.8413447460), is(true));
        assertThat(tolerance.almostEqual(GeomNumeric.cdf(2.5, 1.0, 0.5), 0.9986501019267444), is(true));
    }
}
