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
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Geom1DTest {

    @Test
    public void testBoundingBoxInt() {
        assertThat(Geom.e1D.rangesIntersection(0, 5, -3, -2), is(nullValue()));
        assertThat(Geom.e1D.rangesIntersection(0, 5, -1, 2), is(new int[]{0, 2}));
        assertThat(Geom.e1D.rangesIntersection(0, 5, 2, 3), is(new int[]{2, 3}));
        assertThat(Geom.e1D.rangesIntersection(0, 5, 4, 6), is(new int[]{4, 5}));
        assertThat(Geom.e1D.rangesIntersection(0, 5, 7, 8), is(nullValue()));

        assertThat(Geom.e1D.rangesIntersection(0, 5, 2, 2), is(new int[]{2, 2}));
        assertThat(Geom.e1D.rangesIntersection(0, 5, -1, 0), is(new int[]{0, 0}));
        assertThat(Geom.e1D.rangesIntersection(0, 5, 5, 5), is(new int[]{5, 5}));
    }

    @Test
    public void testBoundingBoxDouble() {
        assertThat(Geom.e1D.rangesIntersection(0.0, 5, -3, -2), is(nullValue()));
        assertThat(Geom.e1D.rangesIntersection(0.0, 5, -1, 2), is(new double[]{0, 2}));
        assertThat(Geom.e1D.rangesIntersection(0.0, 5, 2, 3), is(new double[]{2, 3}));
        assertThat(Geom.e1D.rangesIntersection(0.0, 5, 4, 6), is(new double[]{4, 5}));
        assertThat(Geom.e1D.rangesIntersection(0.0, 5, 7, 8), is(nullValue()));

        assertThat(Geom.e1D.rangesIntersection(0.0, 5, 2, 2), is(new double[]{2, 2}));
        assertThat(Geom.e1D.rangesIntersection(0.0, 5, -1, 0), is(new double[]{0, 0}));
        assertThat(Geom.e1D.rangesIntersection(0.0, 5, 5, 5), is(new double[]{5, 5}));
    }

}
