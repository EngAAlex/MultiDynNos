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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ocotillo.geometry.Box.Box2D;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class BoxTest {

    @Test
    public void testPoints() {
        Box2D box = new Box2D(1, 5, 0, 2);
        assertThat(box.topLeft(), is(new Coordinates(1, 2)));
        assertThat(box.topRight(), is(new Coordinates(5, 2)));
        assertThat(box.bottomLeft(), is(new Coordinates(1, 0)));
        assertThat(box.bottomRight(), is(new Coordinates(5, 0)));
        assertThat(box.center(), is(new Coordinates(3, 1)));
        assertThat(box.size(), is(new Coordinates(4, 2)));
    }

    @Test
    public void testEquals() {
        assertThat(new Box2D(0, 1, 0, 1), is(equalTo(new Box2D(0, 1, 0, 1))));
        assertThat(new Box2D(0, 1, 0, 1), is(not(equalTo(new Box2D(0, 3, 0, 1)))));
    }

    @Test
    public void testContains() {
        Box2D box = new Box2D(0, 10, 0, 10);
        Coordinates a = new Coordinates(2, 3);
        Coordinates b = new Coordinates(5, 10);
        Coordinates c = new Coordinates(11, 10);
        Coordinates d = new Coordinates(-3, -2);
        assertThat(box.contains(a), is(true));
        assertThat(box.contains(b), is(true));
        assertThat(box.contains(c), is(false));
        assertThat(box.contains(d), is(false));
        assertThat(box.contains(Arrays.asList(a, b)), is(true));
        assertThat(box.contains(Arrays.asList(a, b, c)), is(false));
    }

    @Test
    public void testBoundingBox() {
        List<Coordinates> points = new ArrayList<>();
        points.add(new Coordinates(-2, -3));
        points.add(new Coordinates(-5, 1));
        points.add(new Coordinates(3, 8));
        points.add(new Coordinates(4, 3));
        points.add(new Coordinates(5, 4));
        points.add(new Coordinates(6, 2));

        Box box = Box2D.boundingBox(points);
        assertThat(box, is(new Box2D(-5, 6, -3, 8)));
    }

    @Test
    public void testBoundingBoxWithMargin() {
        List<Coordinates> points = new ArrayList<>();
        points.add(new Coordinates(0, 0));

        Box box = Box.boundingBox(points, new Coordinates(1, 2));
        assertThat(box, is(new Box2D(-1, 1, -2, 2)));
    }

    @Test
    public void testWidthHeightDims() {
        Box2D box = new Box2D(0, 8, -1, 6);
        assertThat(box.width(), is(8.0));
        assertThat(box.height(), is(7.0));
        assertThat(box.maxDim(), is(8.0));
        assertThat(box.minDim(), is(7.0));
    }

    @Test
    public void testScale() {
        Box box = new Box2D(0, 8, -1, 6);
        Box scaled = box.scale(new Coordinates(10, 20));
        assertThat(scaled.bottom(), is(-20.0));
        assertThat(scaled.left(), is(0.0));
        assertThat(scaled.top(), is(120.0));
        assertThat(scaled.right(), is(80.0));
    }

    @Test
    public void testExpand() {
        Box box = new Box2D(0, 8, -1, 6);
        Box scaled = box.expand(new Coordinates(1, 2));
        assertThat(scaled.bottom(), is(-3.0));
        assertThat(scaled.left(), is(-1.0));
        assertThat(scaled.top(), is(8.0));
        assertThat(scaled.right(), is(9.0));
    }

    @Test
    public void testShift() {
        Box box = new Box2D(0, 8, -1, 6);
        Box scaled = box.shift(new Coordinates(2, 1));
        assertThat(scaled.bottom(), is(0.0));
        assertThat(scaled.left(), is(2.0));
        assertThat(scaled.top(), is(7.0));
        assertThat(scaled.right(), is(10.0));
    }

    @Test
    public void testCombineBoxes() {
        List<Box> boxes = new ArrayList<>();
        boxes.add(new Box2D(-1, 1, -1, 1));
        boxes.add(new Box2D(1, 3, 1, 3));

        Box box = Box.combine(boxes);
        assertThat(box, is(new Box2D(-1, 3, -1, 3)));
    }

    @Test
    public void testIntersectBoxes() {
        List<Box> boxes = new ArrayList<>();
        boxes.add(new Box2D(0, 2, 0, 2));
        boxes.add(new Box2D(1, 3, 1, 3));

        Box box = Box.intersect(boxes);
        assertThat(box, is(new Box2D(1, 2, 1, 2)));
    }

    @Test
    public void testIntersectBoxesEmpty() {
        List<Box> boxes = new ArrayList<>();
        boxes.add(new Box2D(0, 2, 0, 2));
        boxes.add(new Box2D(9, 10, 9, 10));

        Box box = Box.intersect(boxes);
        assertThat(box, is(nullValue()));
    }

}
