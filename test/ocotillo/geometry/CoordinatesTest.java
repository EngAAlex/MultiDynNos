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
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class CoordinatesTest {

    @Test
    public void testGetSet() {
        Coordinates a = new Coordinates(4);
        a.setAt(0, 1.0);
        a.setAt(1, 2.0);
        a.setAt(2, 3.0);
        a.setAt(3, 4.0);
        a.setAt(4, 5.0);
        assertThat(a.get(0), is(1.0));
        assertThat(a.get(1), is(2.0));
        assertThat(a.get(2), is(3.0));
        assertThat(a.get(3), is(4.0));
        assertThat(a.get(4), is(5.0));
        assertThat(a.get(5), is(0.0));
        a.setX(10.0);
        a.setY(20.0);
        a.setZ(30.0);
        assertThat(a.x(), is(10.0));
        assertThat(a.y(), is(20.0));
        assertThat(a.z(), is(30.0));
        a.set(8.0, 12.0);
        assertThat(a.x(), is(8.0));
        assertThat(a.y(), is(12.0));
        assertThat(a.z(), is(30.0));
        a.set(new Coordinates(7, 19));
        assertThat(a.x(), is(7.0));
        assertThat(a.y(), is(19.0));
        assertThat(a.z(), is(30.0));
        a.reset();
        assertThat(a.x(), is(0.0));
        assertThat(a.y(), is(0.0));
        assertThat(a.z(), is(0.0));
    }

    @Test
    public void testConstructor() {
        Coordinates a = new Coordinates(2, 3);
        assertThat(a.x(), is(2.0));
        assertThat(a.y(), is(3.0));
        assertThat(a.z(), is(0.0));
        assertThat(a.get(0), is(2.0));
        assertThat(a.get(1), is(3.0));
        assertThat(a.get(2), is(0.0));
        a = new Coordinates(2, 3, 5);
        assertThat(a.x(), is(2.0));
        assertThat(a.y(), is(3.0));
        assertThat(a.z(), is(5.0));
        assertThat(a.get(0), is(2.0));
        assertThat(a.get(1), is(3.0));
        assertThat(a.get(2), is(5.0));
    }

    @Test
    public void testCopyConstructor() {
        Coordinates a = new Coordinates(2, 3, 5);
        Coordinates b = new Coordinates(a);
        assertThat(b.x(), is(2.0));
        assertThat(b.y(), is(3.0));
        assertThat(b.z(), is(5.0));
        assertThat(b.get(0), is(2.0));
        assertThat(b.get(1), is(3.0));
        assertThat(b.get(2), is(5.0));
    }

    @Test
    public void testWithHead() {
        Coordinates a = new Coordinates(2, 3, 4, 5);
        Coordinates b = new Coordinates(1, 2);
        Coordinates c = a.withHead(b);
        assertThat(c.get(0), is(1.0));
        assertThat(c.get(1), is(2.0));
        assertThat(c.get(2), is(4.0));
        assertThat(c.get(3), is(5.0));
    }

    @Test
    public void testRestr() {
        Coordinates a = new Coordinates(2, 3, 4, 5);
        Coordinates b = a.restr(2);
        assertThat(b.get(0), is(2.0));
        assertThat(b.get(1), is(3.0));
        assertThat(b.get(2), is(0.0));
        assertThat(b.get(3), is(0.0));
    }

    @Test
    public void testOpposite() {
        Coordinates a = new Coordinates(2, 3, 5);
        Coordinates r = a.minus();
        assertThat(r.x(), is(-2.0));
        assertThat(r.y(), is(-3.0));
        assertThat(r.z(), is(-5.0));
        a.minusIP();
        assertThat(a.x(), is(-2.0));
        assertThat(a.y(), is(-3.0));
        assertThat(a.z(), is(-5.0));
    }

    @Test
    public void testRestrOpposite() {
        Coordinates a = new Coordinates(2, 3, 5);
        Coordinates r = a.restrMinus(2);
        assertThat(r.x(), is(-2.0));
        assertThat(r.y(), is(-3.0));
        assertThat(r.z(), is(0.0));
        assertThat(r.dim(), is(2));
        a.restrMinusIP(2);
        assertThat(a.x(), is(-2.0));
        assertThat(a.y(), is(-3.0));
        assertThat(a.z(), is(5.0));
        assertThat(a.dim(), is(3));
    }

    @Test
    public void testPlus() {
        Coordinates a = new Coordinates(2, 3, 5);
        Coordinates b = new Coordinates(1, 2, 4);
        Coordinates r = a.plus(b);
        assertThat(r.x(), is(3.0));
        assertThat(r.y(), is(5.0));
        assertThat(r.z(), is(9.0));
        a.plusIP(b);
        assertThat(a.x(), is(3.0));
        assertThat(a.y(), is(5.0));
        assertThat(a.z(), is(9.0));
    }

    @Test
    public void testPlusDiffLenght() {
        Coordinates a = new Coordinates(2, 3);
        Coordinates b = new Coordinates(1, 2, 4);
        Coordinates r = a.plus(b);
        assertThat(r.x(), is(3.0));
        assertThat(r.y(), is(5.0));
        assertThat(r.z(), is(4.0));
        r = b.plus(a);
        assertThat(r.x(), is(3.0));
        assertThat(r.y(), is(5.0));
        assertThat(r.z(), is(4.0));
        a.plusIP(b);
        assertThat(a.x(), is(3.0));
        assertThat(a.y(), is(5.0));
        assertThat(a.z(), is(4.0));
        assertThat(a.dim(), is(3));
    }

    @Test
    public void testRestrPlus() {
        Coordinates a = new Coordinates(2, 3, 4, 5);
        Coordinates b = new Coordinates(1, 2, 4);
        Coordinates r = a.restrPlus(b, 2);
        assertThat(r.x(), is(3.0));
        assertThat(r.y(), is(5.0));
        assertThat(r.z(), is(0.0));
        assertThat(r.dim(), is(2));
        r = b.restrPlus(a, 2);
        assertThat(r.x(), is(3.0));
        assertThat(r.y(), is(5.0));
        assertThat(r.z(), is(0.0));
        assertThat(r.dim(), is(2));
        a.restrPlusIP(b, 2);
        assertThat(a.x(), is(3.0));
        assertThat(a.y(), is(5.0));
        assertThat(a.z(), is(4.0));
        assertThat(a.get(3), is(5.0));
        assertThat(a.dim(), is(4));
    }

    @Test
    public void testMinus() {
        Coordinates a = new Coordinates(2, 3, 5);
        Coordinates b = new Coordinates(1, 2, 4);
        Coordinates r = a.minus(b);
        assertThat(r.x(), is(1.0));
        assertThat(r.y(), is(1.0));
        assertThat(r.z(), is(1.0));
        a.minusIP(b);
        assertThat(a.x(), is(1.0));
        assertThat(a.y(), is(1.0));
        assertThat(a.z(), is(1.0));
    }

    @Test
    public void testMinusDiffLenght() {
        Coordinates a = new Coordinates(2, 3);
        Coordinates b = new Coordinates(1, 2, 4);
        Coordinates r = a.minus(b);
        assertThat(r.x(), is(1.0));
        assertThat(r.y(), is(1.0));
        assertThat(r.z(), is(-4.0));
        r = b.minus(a);
        assertThat(r.x(), is(-1.0));
        assertThat(r.y(), is(-1.0));
        assertThat(r.z(), is(4.0));
        a.minusIP(b);
        assertThat(a.x(), is(1.0));
        assertThat(a.y(), is(1.0));
        assertThat(a.z(), is(-4.0));
        assertThat(a.dim(), is(3));
    }

    @Test
    public void testRestrMinus() {
        Coordinates a = new Coordinates(2, 3, 4, 5);
        Coordinates b = new Coordinates(1, 2, 4);
        Coordinates r = a.restrMinus(b, 2);
        assertThat(r.x(), is(1.0));
        assertThat(r.y(), is(1.0));
        assertThat(r.z(), is(0.0));
        assertThat(r.dim(), is(2));
        r = b.restrMinus(a, 2);
        assertThat(r.x(), is(-1.0));
        assertThat(r.y(), is(-1.0));
        assertThat(r.z(), is(0.0));
        assertThat(r.dim(), is(2));
        a.restrMinusIP(b, 2);
        assertThat(a.x(), is(1.0));
        assertThat(a.y(), is(1.0));
        assertThat(a.z(), is(4.0));
        assertThat(a.get(3), is(5.0));
        assertThat(a.dim(), is(4));
    }

    @Test
    public void testTimes() {
        Coordinates a = new Coordinates(2, 3, 5);
        Coordinates r = a.times(2);
        assertThat(r.x(), is(4.0));
        assertThat(r.y(), is(6.0));
        assertThat(r.z(), is(10.0));
        a.timesIP(2);
        assertThat(a.x(), is(4.0));
        assertThat(a.y(), is(6.0));
        assertThat(a.z(), is(10.0));
    }

    @Test
    public void testRestrTimes() {
        Coordinates a = new Coordinates(2, 3, 5);
        Coordinates r = a.restrTimes(2, 2);
        assertThat(r.x(), is(4.0));
        assertThat(r.y(), is(6.0));
        assertThat(r.z(), is(0.0));
        assertThat(r.dim(), is(2));
        a.restrTimesIP(2, 2);
        assertThat(a.x(), is(4.0));
        assertThat(a.y(), is(6.0));
        assertThat(a.z(), is(5.0));
        assertThat(a.dim(), is(3));
    }

    @Test
    public void testDivide() {
        Coordinates a = new Coordinates(4, 6, 8);
        Coordinates r = a.divide(2);
        assertThat(r.x(), is(2.0));
        assertThat(r.y(), is(3.0));
        assertThat(r.z(), is(4.0));
        a.divideIP(2);
        assertThat(a.x(), is(2.0));
        assertThat(a.y(), is(3.0));
        assertThat(a.z(), is(4.0));
    }

    @Test
    public void testRestrDivide() {
        Coordinates a = new Coordinates(4, 6, 8);
        Coordinates r = a.restrDivide(2, 2);
        assertThat(r.x(), is(2.0));
        assertThat(r.y(), is(3.0));
        assertThat(r.z(), is(0.0));
        assertThat(r.dim(), is(2));
        a.restrDivideIP(2, 2);
        assertThat(a.x(), is(2.0));
        assertThat(a.y(), is(3.0));
        assertThat(a.z(), is(8.0));
        assertThat(a.dim(), is(3));
    }

    @Test
    public void testToString() {
        Coordinates a = new Coordinates(4.1, 6.5);
        Coordinates b = new Coordinates(1.2, 3.4, 5.6);
        assertThat(a.toString(), is("(4.1, 6.5)"));
        assertThat(b.toString(), is("(1.2, 3.4, 5.6)"));
    }

    @Test
    public void testParse() {
        Coordinates a = new Coordinates(4.1, 6.5);
        Coordinates b = new Coordinates(1.2, -3.4, 5.6);
        assertThat(Coordinates.parse(a.toString()), isAlmost(a));
        assertThat(Coordinates.parse(b.toString()), isAlmost(b));
        assertThat(Coordinates.parse("(4,   -3.3)"), isAlmost(new Coordinates(4, -3.3)));
    }
}
