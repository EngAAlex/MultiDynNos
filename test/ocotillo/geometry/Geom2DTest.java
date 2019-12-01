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
import java.util.List;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Geom2DTest {

    @Test
    public void testAlmostZeroWithCoord() {
        Coordinates a = new Coordinates(0.0000001, 0, 4);
        Coordinates b = new Coordinates(0.00001, -0.00001, 0.0000002, 3);
        assertThat(Geom.e2D.almostZero(a), is(true));
        assertThat(Geom.e2D.almostZero(b), is(true));

        a = new Coordinates(0.0000001, 4, 0, 0);
        b = new Coordinates(1, 0, 0);
        assertThat(Geom.e2D.almostZero(a), is(false));
        assertThat(Geom.e2D.almostZero(b), is(false));
    }

    @Test
    public void testAlmostEqualWithCoord() {
        Coordinates a = new Coordinates(0, 3.0000001, 111);
        Coordinates b = new Coordinates(0.00001, 2.99999, 4);
        assertThat(Geom.e2D.almostEqual(a, b), is(true));

        a = new Coordinates(72, 34, 232);
        b = new Coordinates(71.99993, 34.00001, 3);
        assertThat(Geom.e2D.almostEqual(a, b), is(true));
    }

    @Test
    public void testModule() {
        assertThat(Geom.e2D.magnitude(new Coordinates(1, 1)), isAlmost(Math.sqrt(2)));
        assertThat(Geom.e2D.magnitude(new Coordinates(3, 4)), isAlmost(5));
        assertThat(Geom.e2D.magnitude(new Coordinates(Math.sqrt(2), Math.sqrt(2))), isAlmost(2));
        assertThat(Geom.e2D.magnitude(new Coordinates(7, 3)), isAlmost(7.61577310586));

        assertThat(Geom.e2D.magnitude(new Coordinates(1, 1, 8)), isAlmost(Math.sqrt(2)));
        assertThat(Geom.e2D.magnitude(new Coordinates(3, 4, -4, 5)), isAlmost(5));
    }

    @Test
    public void testDotProduct() {
        Coordinates a = new Coordinates(2, 1);
        Coordinates b = new Coordinates(3, 4);
        Coordinates c = new Coordinates(-3, 5);
        assertThat(Geom.e2D.dotProduct(a, b), isAlmost(10));
        assertThat(Geom.e2D.dotProduct(b, c), isAlmost(11));
        assertThat(Geom.e2D.dotProduct(a, c), isAlmost(-1));
        assertThat(Geom.e2D.dotProduct(a, c), isAlmost(Geom.e2D.dotProduct(c, a)));

        a = new Coordinates(2, 1, 9);
        b = new Coordinates(3, 4, -10);
        assertThat(Geom.e2D.dotProduct(a, b), isAlmost(10));
    }

    @Test
    public void testAngle() {
        Coordinates a = new Coordinates(2, 2);
        Coordinates b = new Coordinates(1, 0);
        Coordinates c = new Coordinates(1, Math.sqrt(3));
        Coordinates d = new Coordinates(-3, 5);
        Coordinates e = new Coordinates(0, 5);
        Coordinates f = new Coordinates(0, -5);
        Coordinates g = new Coordinates(0, 0);
        assertThat(Geom.e2D.angle(a), isAlmost(Math.PI / 4));
        assertThat(Geom.e2D.angle(b), isAlmost(0));
        assertThat(Geom.e2D.angle(c), isAlmost(Math.PI / 3));
        assertThat(Geom.e2D.angle(d), isAlmost(2.11121582707));
        assertThat(Geom.e2D.angle(e), isAlmost(Math.PI / 2));
        assertThat(Geom.e2D.angle(f), isAlmost(3 * Math.PI / 2));
        assertThat(Geom.e2D.angle(g), isAlmost(-1));

        a = new Coordinates(2, 2, 34);
        b = new Coordinates(1, 0, 12);
        assertThat(Geom.e2D.angle(a), isAlmost(Math.PI / 4));
        assertThat(Geom.e2D.angle(b), isAlmost(0));
    }

    @Test
    public void testUnitVector() {
        assertThat(Geom.e2D.unitVector(0), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.unitVector(Math.PI / 2), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.unitVector(Math.PI / 2, new Coordinates(10, 10)), isAlmost(new Coordinates(10, 11)));
        assertThat(Geom.e2D.unitVector(new Coordinates(12, 0)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.unitVector(new Coordinates(0, -5)), isAlmost(new Coordinates(0, -1)));
        assertThat(Geom.e2D.unitVector(new Coordinates(12, 0), new Coordinates(12, -5)), isAlmost(new Coordinates(12, -4)));

        assertThat(Geom.e2D.unitVector(new Coordinates(12, 0, 8)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.unitVector(new Coordinates(12, 0, 8)).dim(), is(2));
        assertThat(Geom.e2D.unitVector(new Coordinates(12, 0, 8), new Coordinates(12, -5, 3)), isAlmost(new Coordinates(12, -4, 3)));
        assertThat(Geom.e2D.unitVector(new Coordinates(12, 0, 8), new Coordinates(12, -5, 3)).dim(), is(3));
    }

    @Test
    public void testRotateVector() {
        assertThat(Geom.e2D.rotateVector(new Coordinates(-1, 0), Math.PI), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e2D.rotateVector(new Coordinates(1, 0), Math.PI / 2), isAlmost(new Coordinates(0, 1)));
        assertThat(Geom.e2D.rotateVector(new Coordinates(11, 10), Math.PI / 2, new Coordinates(10, 10)), isAlmost(new Coordinates(10, 11)));

        assertThat(Geom.e2D.rotateVector(new Coordinates(-1, 0, 4), Math.PI), isAlmost(new Coordinates(1, 0, 4)));
        assertThat(Geom.e2D.rotateVector(new Coordinates(-1, 0, 4), Math.PI).dim(), is(3));
    }

    @Test
    public void testBetweenAngle() {
        assertThat(Geom.e2D.betweenAngle(new Coordinates(-1, 0), new Coordinates(1, 0)), isAlmost(Math.PI));
        assertThat(Geom.e2D.betweenAngle(new Coordinates(-1, 0), new Coordinates(0, 1)), isAlmost(Math.PI / 2));
        assertThat(Geom.e2D.betweenAngle(new Coordinates(-11, 10), new Coordinates(11, 10), new Coordinates(10, 10)), isAlmost(Math.PI));
    }

    @Test
    public void testBisector() {
        assertThat(Geom.e2D.bisector(new Coordinates(1, 0), new Coordinates(0, 1)), isAlmost(new Coordinates(Math.sqrt(2) / 2, Math.sqrt(2) / 2)));
        assertThat(Geom.e2D.bisector(Geom.e2D.unitVector(0.3), Geom.e2D.unitVector(1.1)), isAlmost(Geom.e2D.unitVector(0.7)));
        assertThat(Geom.e2D.bisector(new Coordinates(11, 10), new Coordinates(10, 11), new Coordinates(10, 10)), isAlmost(new Coordinates(10 + Math.sqrt(2) / 2, 10 + Math.sqrt(2) / 2)));
    }

    @Test
    public void testIsPointInLine() {
        Coordinates n = new Coordinates(2, 2, 1, 32);
        Coordinates a = new Coordinates(2, -2, 21, 72);
        Coordinates b = new Coordinates(2, -6, 15, 10);
        Coordinates c = new Coordinates(0, 1, 70, 3);
        Coordinates d = new Coordinates(4, 3, 32, 1);
        Coordinates e = new Coordinates(3, 0, -21);
        Coordinates f = new Coordinates(4, -2, -3, 6);
        assertThat(Geom.e2D.isPointInLine(n, a, b), is(true));
        assertThat(Geom.e2D.isPointInLine(n, c, d), is(true));
        assertThat(Geom.e2D.isPointInLine(n, e, f), is(true));
        assertThat(Geom.e2D.isPointInLine(n, a, f), is(false));
    }

    @Test
    public void testIsPointInSegment() {
        Coordinates n = new Coordinates(2, 2, 11, 3, 4);
        Coordinates a = new Coordinates(2, -2, 31, 1);
        Coordinates b = new Coordinates(2, -6, 71, 12, 3);
        Coordinates c = new Coordinates(0, 1, 70);
        Coordinates d = new Coordinates(4, 3, 25, 4);
        Coordinates e = new Coordinates(3, 0, -21, 67);
        Coordinates f = new Coordinates(4, -2, -32, 9);
        Coordinates g = new Coordinates(8, 8, 4, 2);
        assertThat(Geom.e2D.isPointInSegment(n, n, n), is(true));
        assertThat(Geom.e2D.isPointInSegment(n, a, n), is(true));
        assertThat(Geom.e2D.isPointInSegment(n, n, a), is(true));
        assertThat(Geom.e2D.isPointInSegment(n, a, a), is(false));
        assertThat(Geom.e2D.isPointInSegment(n, a, b), is(false));
        assertThat(Geom.e2D.isPointInSegment(n, c, d), is(true));
        assertThat(Geom.e2D.isPointInSegment(n, e, f), is(false));
        assertThat(Geom.e2D.isPointInSegment(n, a, f), is(false));
        assertThat(Geom.e2D.isPointInSegment(n, g, g.minus()), is(true));
    }

    // ******** Point - Line ********
    @Test
    public void testPointLineRelation_Inside() {
        Coordinates o = new Coordinates(0, 0, 10, 4, 6, 7);
        Coordinates x = new Coordinates(1, 0, 2, 3);
        GeomE.PointRelation relation = Geom.e2D.pointLineRelation(x, o, x);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(relation.projection(), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.isPointIncluded(), is(true));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointLineRelation_Outside() {
        Coordinates a = new Coordinates(0, 0, 1, 4);
        Coordinates b = new Coordinates(4, 0, 3, 41);
        Coordinates p = new Coordinates(3, 1, 5, 17);
        GeomE.PointRelation relation = Geom.e2D.pointLineRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(3, 0)));
        assertThat(relation.projection(), isAlmost(relation.closestPoint()));
        assertThat(relation.projectionAsLine(), isAlmost(relation.projection()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testPointLineRelation_UndefinedLine() {
        Coordinates a = new Coordinates(0, 0, 7, 42);
        Coordinates b = new Coordinates(0, 0, 2, 2);
        Coordinates x = new Coordinates(1, 0);
        Geom.e2D.pointLineRelation(x, a, b);
    }

    // ******** Point - Segment ********
    @Test
    public void testPointSegmentRelation_SameLineInside() {
        Coordinates a = new Coordinates(0, 0, 2, 4);
        Coordinates b = new Coordinates(1, 0, 1, 32);
        Coordinates p = new Coordinates(0.8, 0, 12, 432);
        GeomE.PointRelation relation = Geom.e2D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(p.restr(2)));
        assertThat(relation.projection(), isAlmost(p.restr(2)));
        assertThat(relation.projectionAsLine(), isAlmost(p.restr(2)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.isPointIncluded(), is(true));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointSegmentRelation_SameLineOutSide() {
        Coordinates a = new Coordinates(0, 0, 71, 22);
        Coordinates b = new Coordinates(1, 0, 4, 9);
        Coordinates p = new Coordinates(2.8, 0, 2);
        GeomE.PointRelation relation = Geom.e2D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(b.restr(2)));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), isAlmost(p.restr(2)));
        assertThat(relation.distance(), isAlmost(1.8));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(false));
    }

    @Test
    public void testPointSegmentRelation_DifferentLineInside() {
        Coordinates a = new Coordinates(0, 0, 2, 44);
        Coordinates b = new Coordinates(1, 0, 2, 85);
        Coordinates p = new Coordinates(0.8, 1);
        GeomE.PointRelation relation = Geom.e2D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(0.8, 0)));
        assertThat(relation.projection(), isAlmost(new Coordinates(0.8, 0)));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(0.8, 0)));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointSegmentRelation_DifferentLineOutside() {
        Coordinates a = new Coordinates(0, 0, 1, 3);
        Coordinates b = new Coordinates(1, 0, 1, 2);
        Coordinates p = new Coordinates(3, 1, 1, 1);
        GeomE.PointRelation relation = Geom.e2D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(b.restr(2)));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(3, 0)));
        assertThat(relation.distance(), isAlmost(Math.sqrt(5)));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(false));
    }

    @Test
    public void testPointSegmentRelation_UndefinedSegment() {
        Coordinates o = new Coordinates(0, 0, 5, 6);
        Coordinates x = new Coordinates(1, 0, 9, 3);
        GeomE.PointRelation relation = Geom.e2D.pointSegmentRelation(x, o, o);
        assertThat(relation.closestPoint(), isAlmost(o.restr(2)));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(false));
    }

    // ******** Line - Line ********
    @Test
    public void testLineLineRelation_SameLine() {
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 0, 4);
        Coordinates d = new Coordinates(-3, 0, -6);
        GeomE.LineRelation relation = Geom.e2D.lineLineRelation(a, b, c, d);
        assertThat(relation.closestPointA(), is(nullValue()));
        assertThat(relation.closestPointB(), is(nullValue()));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(true));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineLineRelation_ParallelDistinct() {
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 1, 4);
        Coordinates d = new Coordinates(-3, 1, -6);
        GeomE.LineRelation relation = Geom.e2D.lineLineRelation(a, b, c, d);
        assertThat(relation.closestPointA(), is(nullValue()));
        assertThat(relation.closestPointB(), is(nullValue()));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineLineRelation_Intersecting() {
        Coordinates a = new Coordinates(0, 0, 8);
        Coordinates b = new Coordinates(1, 2, 2);
        Coordinates c = new Coordinates(2, 0, 6);
        Coordinates d = new Coordinates(-3, 0, 15);
        GeomE.LineRelation relation = Geom.e2D.lineLineRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(a.restr(2)));
        assertThat(relation.intersection(), isAlmost(a.restr(2)));
        assertThat(relation.intersectionAsLines(), isAlmost(a.restr(2)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testLineLineRelation_FirstUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.e2D.lineLineRelation(a, a, b, c);
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testLineLineRelation_SecondUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.e2D.lineLineRelation(a, b, c, c);
    }

    // ******** Line - Segment ********
    @Test
    public void testLineSegmentRelation_SameLine() {
        Coordinates a = new Coordinates(0, 0, 0, 53);
        Coordinates b = new Coordinates(1, 0, 2, 2);
        Coordinates c = new Coordinates(2, 0, 4, 12);
        Coordinates d = new Coordinates(-3, 0, -6, 44);
        GeomE.LineRelation relation = Geom.e2D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), is(nullValue()));
        assertThat(relation.closestPointB(), is(nullValue()));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(true));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineSegmentRelation_ParallelDistinct() {
        Coordinates a = new Coordinates(0, 0, 0, 22);
        Coordinates b = new Coordinates(1, 0, 2, 1);
        Coordinates c = new Coordinates(2, 1, 4);
        Coordinates d = new Coordinates(-3, 1, -6);
        GeomE.LineRelation relation = Geom.e2D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), is(nullValue()));
        assertThat(relation.closestPointB(), is(nullValue()));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineSegmentRelation_Adjacent() {
        Coordinates a = new Coordinates(1, 0, 0, 5);
        Coordinates b = new Coordinates(1, 1, 0, 1);
        Coordinates c = new Coordinates(1, 1, 0, 52);
        Coordinates d = new Coordinates(8, 1, 1, 9);
        GeomE.LineRelation relation = Geom.e2D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(c.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(2)));
        assertThat(relation.intersection(), isAlmost(c.restr(2)));
        assertThat(relation.intersectionAsLines(), isAlmost(c.restr(2)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(true));
    }

    @Test
    public void testLineSegmentRelation_IntersectingInside() {
        Coordinates a = new Coordinates(0, 0, 40, 89);
        Coordinates b = new Coordinates(1, 2, 2, 1);
        Coordinates c = new Coordinates(2, 0, 6, 42);
        Coordinates d = new Coordinates(-3, 0, 2, 2);
        GeomE.LineRelation relation = Geom.e2D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(a.restr(2)));
        assertThat(relation.intersection(), isAlmost(a.restr(2)));
        assertThat(relation.intersectionAsLines(), isAlmost(a.restr(2)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineSegmentRelation_IntersectingOutside() {
        Coordinates a = new Coordinates(0, 0, 2, 67);
        Coordinates b = new Coordinates(0, 2, 2, 9);
        Coordinates c = new Coordinates(1, 0, 5, 11);
        Coordinates d = new Coordinates(3, 0, 3, 1);
        GeomE.LineRelation relation = Geom.e2D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(2)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), isAlmost(a.restr(2)));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testLineSegmentRelation_LineUndefined() {
        Coordinates a = new Coordinates(1, 0, 0, 2);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.e2D.lineSegmentRelation(a, a, b, c);
    }

    @Test
    public void testLineSegmentRelation_SegmentUndefined() {
        Coordinates a = new Coordinates(1, 0, 0, 1);
        Coordinates b = new Coordinates(1, 1, 0, 33);
        Coordinates c = new Coordinates(0, 1, 1, 1);
        GeomE.LineRelation relation = Geom.e2D.lineSegmentRelation(a, b, c, c);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(2)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testLineSegmentRelation_SegmentUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0, 55);
        Coordinates b = new Coordinates(1, 1, 0, 1);
        Coordinates c = new Coordinates(0, 1, 1, 94);
        GeomE.LineRelation relation = Geom.e2D.lineSegmentRelation(a, b, c, c);
        relation.areParallel();
    }

    // ******** Segment - Segment ********
    @Test
    public void testSegmentSegmentRelation_SameLine_Overlapping() {
        Coordinates a = new Coordinates(0, 0, 0, 2);
        Coordinates b = new Coordinates(1, 0, 2, 11);
        Coordinates c = new Coordinates(2, 0, 4, 1);
        Coordinates d = new Coordinates(-3, 0, -6, 11);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), is(nullValue()));
        assertThat(relation.closestPointB(), is(nullValue()));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(true));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_SameLine_NonOvelapping() {
        Coordinates a = new Coordinates(0, 0, 0, 1);
        Coordinates b = new Coordinates(1, 0, 2, 9);
        Coordinates c = new Coordinates(2, 0, 4, 1);
        Coordinates d = new Coordinates(3, 0, 6, 33);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(2)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_ParallelDistinct_Overlapping() {
        Coordinates a = new Coordinates(0, 0, 0, 11);
        Coordinates b = new Coordinates(1, 0, 2, 8);
        Coordinates c = new Coordinates(2, 1, 4, 8);
        Coordinates d = new Coordinates(-3, 1, -6, -1);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), is(nullValue()));
        assertThat(relation.closestPointB(), is(nullValue()));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_ParallelDistinct_NonOverlapping() {
        Coordinates a = new Coordinates(0, 0, 0, 2);
        Coordinates b = new Coordinates(1, 0, 2, -5);
        Coordinates c = new Coordinates(2, 1, 4, 2);
        Coordinates d = new Coordinates(3, 1, 6, 1);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(2)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_IntersectingInside() {
        Coordinates a = new Coordinates(-1, -2, -2);
        Coordinates b = new Coordinates(1, 2, 24, 3);
        Coordinates c = new Coordinates(2, 0, 3, 12);
        Coordinates d = new Coordinates(-3, 0, 1);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(0, 0)));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(0, 0)));
        assertThat(relation.intersection(), isAlmost(new Coordinates(0, 0)));
        assertThat(relation.intersectionAsLines(), isAlmost(new Coordinates(0, 0)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_IntersectingOutside() {
        Coordinates a = new Coordinates(4, 8, 18);
        Coordinates b = new Coordinates(1, 2, 2, 1);
        Coordinates c = new Coordinates(2, 0, 2, 12);
        Coordinates d = new Coordinates(-3, 0, 5, 14);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(1, 0)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), isAlmost(new Coordinates(0, 0)));
        assertThat(relation.distance(), isAlmost(2));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_FirstUndefined() {
        Coordinates a = new Coordinates(1, 0, 0, 2);
        Coordinates b = new Coordinates(1, 1, 0, 9);
        Coordinates c = new Coordinates(0, 1, 1, 11);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, c);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(2)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testSegmentSegmentRelation_FirstUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0, 2);
        Coordinates b = new Coordinates(1, 1, 0, 11);
        Coordinates c = new Coordinates(0, 1, 1, 44);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, a, b, c);
        relation.areParallel();
    }

    @Test
    public void testSegmentSegmentRelation_SecondUndefined() {
        Coordinates a = new Coordinates(0, 1, 1);
        Coordinates b = new Coordinates(1, 0, 0);
        Coordinates c = new Coordinates(1, 1, 0);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, a, b, c);
        assertThat(relation.closestPointA(), isAlmost(a.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testSegmentSegmentRelation_SecondUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, b, c, c);
        relation.areParallel();
    }

    @Test
    public void testSegmentSegmentRelation_BothUndefined() {
        Coordinates a = new Coordinates(0, 1, 1);
        Coordinates b = new Coordinates(1, 0, 0);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, a, b, b);
        assertThat(relation.closestPointA(), isAlmost(a.restr(2)));
        assertThat(relation.closestPointB(), isAlmost(b.restr(2)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testSegmentSegmentRelation_BothUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        GeomE.LineRelation relation = Geom.e2D.segmentSegmentRelation(a, a, b, b);
        relation.areParallel();
    }

    @Test
    public void testIsPolygonSimple() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, -1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));

        Polygon polygonD = new Polygon();
        polygonD.add(new Coordinates(0, 0));
        polygonD.add(new Coordinates(0, 1));

        Polygon polygonE = new Polygon();
        polygonE.add(new Coordinates(0, 0));
        polygonE.add(new Coordinates(0, 1));
        polygonE.add(new Coordinates(1, 1));

        Polygon polygonF = new Polygon();
        polygonF.add(new Coordinates(0, 0));
        polygonF.add(new Coordinates(0, 1));
        polygonF.add(new Coordinates(1, 0));
        polygonF.add(new Coordinates(1, 1));

        assertThat(Geom.e2D.isPolygonSimple(polygonA), is(true));
        assertThat(Geom.e2D.isPolygonSimple(polygonB), is(true));
        assertThat(Geom.e2D.isPolygonSimple(polygonC), is(false));
        assertThat(Geom.e2D.isPolygonSimple(polygonD), is(false));
        assertThat(Geom.e2D.isPolygonSimple(polygonE), is(true));
        assertThat(Geom.e2D.isPolygonSimple(polygonF), is(false));
    }

    @Test
    public void testPolygonIsConvex() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, -1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(-2, -3));
        polygonB.add(new Coordinates(-5, 1));
        polygonB.add(new Coordinates(3, 8));
        polygonB.add(new Coordinates(4, -1));
        polygonB.add(new Coordinates(0, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(4, -1));
        polygonC.add(new Coordinates(3, 8));
        polygonC.add(new Coordinates(-5, 1));
        polygonC.add(new Coordinates(-2, -3));

        Polygon polygonD = new Polygon();
        polygonD.add(new Coordinates(4, -1));
        polygonD.add(new Coordinates(3, 8));
        polygonD.add(new Coordinates(0, 0));
        polygonD.add(new Coordinates(-5, 1));
        polygonD.add(new Coordinates(-2, -3));

        Polygon polygonE = new Polygon();
        polygonE.add(new Coordinates(0, 0));
        polygonE.add(new Coordinates(2, 0));
        polygonE.add(new Coordinates(4, 0));
        polygonE.add(new Coordinates(3, 3));

        assertThat(Geom.e2D.isPolygonConvex(polygonA), is(true));
        assertThat(Geom.e2D.isPolygonConvex(polygonB), is(false));
        assertThat(Geom.e2D.isPolygonConvex(polygonC), is(true));
        assertThat(Geom.e2D.isPolygonConvex(polygonD), is(false));
        assertThat(Geom.e2D.isPolygonConvex(polygonE), is(true));
    }

    @Test
    public void testPolygonArea() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, -1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 1));
        polygonC.add(new Coordinates(1, 1));

        assertThat(Geom.e2D.polygonArea(polygonA), isAlmost(54.5));
        assertThat(Geom.e2D.polygonArea(polygonB), isAlmost(1));
        assertThat(Geom.e2D.polygonArea(polygonC), isAlmost(0.5));
    }

    @Test
    public void testPolygonSignedArea() {
        Polygon anticlockwise = new Polygon();
        anticlockwise.add(new Coordinates(0, 0));
        anticlockwise.add(new Coordinates(0, 1));
        anticlockwise.add(new Coordinates(1, 1));
        anticlockwise.add(new Coordinates(1, 0));

        Polygon clockwise = new Polygon();
        clockwise.add(new Coordinates(0, 0));
        clockwise.add(new Coordinates(1, 0));
        clockwise.add(new Coordinates(1, 1));
        clockwise.add(new Coordinates(0, 1));

        assertThat(Geom.e2D.polygonSignedArea(anticlockwise), isAlmost(1));
        assertThat(Geom.e2D.polygonSignedArea(clockwise), isAlmost(-1));
    }

    @Test
    public void testPolygonPerimeter() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(0, 0));
        polygonA.add(new Coordinates(0, 1));
        polygonA.add(new Coordinates(1, 1));
        polygonA.add(new Coordinates(1, 0));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(1, 0));
        polygonB.add(new Coordinates(2, 1));
        polygonB.add(new Coordinates(2, -1));
        polygonB.add(new Coordinates(1, -1));

        assertThat(Geom.e2D.polygonPerimeter(polygonA), isAlmost(4));
        assertThat(Geom.e2D.polygonPerimeter(polygonB), isAlmost(4 + 2 * Math.sqrt(2)));
    }

    @Test(expected = AssertionError.class)
    public void testPolygonAreaIAE1() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        Geom.e2D.polygonArea(polygon);
    }

    @Test(expected = AssertionError.class)
    public void testPolygonAreaIAE2() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        polygon.add(new Coordinates(0, 1));
        Geom.e2D.polygonArea(polygon);
    }

    @Test(expected = AssertionError.class)
    public void testPolygonAreaIAE3() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        polygon.add(new Coordinates(0, 1));
        polygon.add(new Coordinates(1, 0));
        polygon.add(new Coordinates(1, 1));
        Geom.e2D.polygonArea(polygon);
    }

    @Test
    public void testPolygonCentroid() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(5, 1));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 1));
        polygonC.add(new Coordinates(1, 0));

        Polygon polygonD = new Polygon();
        polygonD.add(new Coordinates(0, 0));
        polygonD.add(new Coordinates(1, 0));
        polygonD.add(new Coordinates(0, 1));

        assertThat(Geom.e2D.polygonCentroid(polygonA), isAlmost(new Coordinates(13.0 / 33.0, 2)));
        assertThat(Geom.e2D.polygonCentroid(polygonB), isAlmost(new Coordinates(0.5, 0.5)));
        assertThat(Geom.e2D.polygonCentroid(polygonC), isAlmost(new Coordinates(1.0 / 3.0, 1.0 / 3)));
        assertThat(Geom.e2D.polygonCentroid(polygonD), isAlmost(Geom.e2D.polygonCentroid(polygonC)));
    }

    @Test
    public void testIsPointInPolygonBoundary() {
        Polygon polygon = new Polygon();
        polygon.add(new Coordinates(0, 0));
        polygon.add(new Coordinates(0, 1));
        polygon.add(new Coordinates(1, 0));
        polygon.add(new Coordinates(1, 1));
        assertThat(Geom.e2D.isPointInPolygonBoundary(new Coordinates(0.7, 0.5), polygon), is(false));
        assertThat(Geom.e2D.isPointInPolygonBoundary(new Coordinates(-1, 1), polygon), is(false));
        assertThat(Geom.e2D.isPointInPolygonBoundary(new Coordinates(0, 0.5), polygon), is(true));
        assertThat(Geom.e2D.isPointInPolygonBoundary(new Coordinates(0, 0), polygon), is(true));
    }

    @Test
    public void testIsPointInPolygon() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-2, -3));
        polygonA.add(new Coordinates(-5, 1));
        polygonA.add(new Coordinates(3, 8));
        polygonA.add(new Coordinates(4, 3));
        polygonA.add(new Coordinates(5, 4));
        polygonA.add(new Coordinates(6, 2));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(0, 0));
        polygonB.add(new Coordinates(0, 1));
        polygonB.add(new Coordinates(1, 1));
        polygonB.add(new Coordinates(1, 0));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 0));
        polygonC.add(new Coordinates(0, 1));
        polygonC.add(new Coordinates(1, 1));
        polygonC.add(new Coordinates(1, 0));

        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(1, 1), polygonA), is(true));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(10, 10), polygonA), is(false));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(-10, -10), polygonA), is(false));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(0.7, 0.5), polygonB), is(true));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(-1, 1), polygonB), is(false));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(3, 3), polygonC), is(false));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(-3, -3), polygonC), is(false));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(0, 0.5), polygonB), is(true));
        assertThat(Geom.e2D.isPointInPolygon(new Coordinates(0, 0), polygonB), is(true));
    }

    @Test
    public void testConvexHull() {
        List<Coordinates> pointsA = new ArrayList<>();
        pointsA.add(new Coordinates(-2, -3));
        pointsA.add(new Coordinates(-5, 1));
        pointsA.add(new Coordinates(3, 8));
        pointsA.add(new Coordinates(4, 3));
        pointsA.add(new Coordinates(5, 4));
        pointsA.add(new Coordinates(6, 2));

        List<Coordinates> pointsB = new ArrayList<>();
        pointsB.add(new Coordinates(0, 0));
        pointsB.add(new Coordinates(0, 1));
        pointsB.add(new Coordinates(1, 1));
        pointsB.add(new Coordinates(1, 0));

        List<Coordinates> pointsC = new ArrayList<>();
        pointsC.add(new Coordinates(0, 0));
        pointsC.add(new Coordinates(0, 0));
        pointsC.add(new Coordinates(0, 1));
        pointsC.add(new Coordinates(1, 1));
        pointsC.add(new Coordinates(1, 0));

        List<Coordinates> pointsD = new ArrayList<>();

        List<Coordinates> pointsE = new ArrayList<>();
        pointsE.add(new Coordinates(0, 0));

        List<Coordinates> pointsF = new ArrayList<>();
        pointsF.add(new Coordinates(0, 0));
        pointsF.add(new Coordinates(0, 0));

        List<Coordinates> convexHullA = Geom.e2D.convexHull(pointsA);
        assertThat(convexHullA.size(), is(4));
        assertThat(convexHullA.get(0), is(new Coordinates(-2, -3)));
        assertThat(convexHullA.get(1), is(new Coordinates(6, 2)));
        assertThat(convexHullA.get(2), is(new Coordinates(3, 8)));
        assertThat(convexHullA.get(3), is(new Coordinates(-5, 1)));

        List<Coordinates> convexHullB = Geom.e2D.convexHull(pointsB);
        assertThat(convexHullB.size(), is(4));
        assertThat(convexHullB.get(0), is(new Coordinates(0, 0)));
        assertThat(convexHullB.get(1), is(new Coordinates(1, 0)));
        assertThat(convexHullB.get(2), is(new Coordinates(1, 1)));
        assertThat(convexHullB.get(3), is(new Coordinates(0, 1)));

        List<Coordinates> convexHullC = Geom.e2D.convexHull(pointsC);
        assertThat(convexHullC.size(), is(4));
        assertThat(convexHullC.get(0), is(new Coordinates(0, 0)));
        assertThat(convexHullC.get(1), is(new Coordinates(1, 0)));
        assertThat(convexHullC.get(2), is(new Coordinates(1, 1)));
        assertThat(convexHullC.get(3), is(new Coordinates(0, 1)));

        List<Coordinates> convexHullD = Geom.e2D.convexHull(pointsD);
        assertThat(convexHullD, empty());

        List<Coordinates> convexHullE = Geom.e2D.convexHull(pointsE);
        assertThat(convexHullE.size(), is(1));
        assertThat(convexHullE.get(0), is(new Coordinates(0, 0)));

        List<Coordinates> convexHullF = Geom.e2D.convexHull(pointsF);
        assertThat(convexHullF.size(), is(1));
        assertThat(convexHullF.get(0), is(new Coordinates(0, 0)));
    }

    @Test
    public void testConvexPolygonIntersection() {
        Polygon polygonA = new Polygon();
        polygonA.add(new Coordinates(-1, -1));
        polygonA.add(new Coordinates(6, -1));
        polygonA.add(new Coordinates(6, 5));
        polygonA.add(new Coordinates(-1, 5));

        Polygon polygonB = new Polygon();
        polygonB.add(new Coordinates(1, 6));
        polygonB.add(new Coordinates(5, 6));
        polygonB.add(new Coordinates(5, -2));
        polygonB.add(new Coordinates(1, -2));

        Polygon polygonC = new Polygon();
        polygonC.add(new Coordinates(7, 3));
        polygonC.add(new Coordinates(7, -2));
        polygonC.add(new Coordinates(2, -2));

        Polygon polygonD = new Polygon();
        polygonD.add(new Coordinates(4, 2));
        polygonD.add(new Coordinates(2, 0));
        polygonD.add(new Coordinates(0, 2));
        polygonD.add(new Coordinates(2, 4));

        Polygon intersectionAB = new Polygon();
        intersectionAB.add(new Coordinates(1, -1));
        intersectionAB.add(new Coordinates(5, -1));
        intersectionAB.add(new Coordinates(5, 5));
        intersectionAB.add(new Coordinates(1, 5));
        assertThat(Geom.e2D.convexIntersection(polygonA, polygonB).cyclicEquivalent(intersectionAB), is(true));

        Polygon intersectionAC = new Polygon();
        intersectionAC.add(new Coordinates(3, -1));
        intersectionAC.add(new Coordinates(6, 2));
        intersectionAC.add(new Coordinates(6, -1));
        assertThat(Geom.e2D.convexIntersection(polygonA, polygonC).cyclicEquivalent(intersectionAC), is(true));

        Polygon intersectionBD = new Polygon();
        intersectionBD.add(new Coordinates(2, 0));
        intersectionBD.add(new Coordinates(4, 2));
        intersectionBD.add(new Coordinates(2, 4));
        intersectionBD.add(new Coordinates(1, 3));
        intersectionBD.add(new Coordinates(1, 1));
        assertThat(Geom.e2D.convexIntersection(polygonB, polygonD).cyclicEquivalent(intersectionBD), is(true));

        assertThat(Geom.e2D.convexIntersection(polygonA, polygonD).cyclicEquivalent(polygonD), is(true));
        assertThat(Geom.e2D.convexIntersection(polygonC, polygonD).isEmpty(), is(true));
    }
}
