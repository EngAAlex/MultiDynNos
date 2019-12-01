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
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class PolygonTest {

    @Test
    public void testCyclycEquivalent() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, -1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(4, -1));
        polygonB.add(new Coordinates(3, 8));
        polygonB.add(new Coordinates(-5, 1));
        polygonB.add(new Coordinates(-2, -3));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(-5, 1));
        polygonC.add(new Coordinates(-2, -3));
        polygonC.add(new Coordinates(4, -1));
        polygonC.add(new Coordinates(3, 8));

        Polygon polygonD = new Polygon();
        polygonD.add(new Coordinates(-5, 1));
        polygonD.add(new Coordinates(-2, -3));
        polygonD.add(new Coordinates(126, -1));
        polygonD.add(new Coordinates(3, 8));

        assertThat(polygonA.cyclicEquivalent(polygonB), is(true));
        assertThat(polygonA.cyclicEquivalent(polygonC), is(true));
        assertThat(polygonB.cyclicEquivalent(polygonC), is(true));
        assertThat(polygonA.cyclicEquivalent(polygonD), is(false));
    }
}
