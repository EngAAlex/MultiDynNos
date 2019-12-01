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

public class CircleTest {

    @Test
    public void testArea() {
        Coordinates center = new Coordinates(0, 0);
        assertThat(new Circle(center, 1).area(), is(Math.PI));
        assertThat(new Circle(center, 2).area(), is(4 * Math.PI));
    }

    @Test
    public void testPerimeter() {
        Coordinates center = new Coordinates(0, 0);
        assertThat(new Circle(center, 1).perimeter(), is(2 * Math.PI));
        assertThat(new Circle(center, 2).perimeter(), is(4 * Math.PI));
    }

    @Test
    public void testFromTwoCircPoints() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(3, 4);
        Coordinates c = new Coordinates(-2, 6);

        Circle ab = Circle.fromCircPoints(a, b);
        Circle bc = Circle.fromCircPoints(b, c);
        Circle ac = Circle.fromCircPoints(a, c);

        assertThat(ab.center(), is(new Coordinates(1.5, 2)));
        assertThat(ab.radius(), is(2.5));
        assertThat(bc.center(), is(new Coordinates(0.5, 5)));
        assertThat(bc.radius(), is(Math.sqrt(29) / 2));
        assertThat(ac.center(), is(new Coordinates(-1, 3)));
        assertThat(ac.radius(), is(Math.sqrt(40) / 2));
    }

    @Test
    public void testFromThreeCircPoints() {
        Coordinates a = new Coordinates(-1, 0);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(0, 1);
        Coordinates d = new Coordinates(0, 0);

        Circle abc = Circle.fromCircPoints(a, b, c);
        Circle bcd = Circle.fromCircPoints(b, c, d);

        assertThat(abc.center(), is(new Coordinates(0, 0)));
        assertThat(abc.radius(), is(1.0));
        assertThat(bcd.center(), is(new Coordinates(0.5, 0.5)));
        assertThat(bcd.radius(), is(Math.sqrt(2) / 2));
    }

    @Test
    public void testBoundingCircle() {
        Coordinates a = new Coordinates(-1, 0);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(0, 1);
        Coordinates d = new Coordinates(0, 0);

        Circle acd = Circle.boundingCircle(a, c, d);
        Circle bcd = Circle.boundingCircle(b, c, d);
        Circle abcd = Circle.boundingCircle(a, b, c, d);

        assertThat(acd.center(), is(new Coordinates(-0.5, 0.5)));
        assertThat(acd.radius(), is(Math.sqrt(2) / 2));
        assertThat(bcd.center(), is(new Coordinates(0.5, 0.5)));
        assertThat(bcd.radius(), is(Math.sqrt(2) / 2));
        assertThat(abcd.center(), is(new Coordinates(0, 0)));
        assertThat(abcd.radius(), is(1.0));
    }

    @Test
    public void testContains() {
        Circle circle = new Circle(new Coordinates(0, 0), 1);
        assertThat(circle.contains(new Coordinates(0, 0)), is(true));
        assertThat(circle.contains(new Coordinates(0, 1)), is(true));
        assertThat(circle.contains(new Coordinates(1, 1)), is(false));
    }

    @Test
    public void testIsPointInCircumference() {
        Circle circle = new Circle(new Coordinates(0, 0), 1);
        assertThat(circle.isPointInCircumference(new Coordinates(0, 0)), is(false));
        assertThat(circle.isPointInCircumference(new Coordinates(0, 1)), is(true));
        assertThat(circle.isPointInCircumference(new Coordinates(1, 1)), is(false));
    }

}
