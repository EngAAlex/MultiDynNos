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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class Geom3DTest {

    @Test
    public void testAlmostZeroWithCoord() {
        Coordinates a = new Coordinates(0, 0.0000001, 0, 4);
        Coordinates b = new Coordinates(0.00001, -0.00001, 0.0000002, 3);
        assertThat(Geom.e3D.almostZero(a), is(true));
        assertThat(Geom.e3D.almostZero(b), is(true));

        a = new Coordinates(0.0000001, 0, 4, 0, 0);
        b = new Coordinates(1, 0, 0);
        assertThat(Geom.e3D.almostZero(a), is(false));
        assertThat(Geom.e3D.almostZero(b), is(false));
    }

    @Test
    public void testAlmostEqualWithCoord() {
        Coordinates a = new Coordinates(0, 3.0000001, 4, 111);
        Coordinates b = new Coordinates(0.00001, 2.99999, 4);
        assertThat(Geom.e3D.almostEqual(a, b), is(true));

        a = new Coordinates(72, 34, 5, 232);
        b = new Coordinates(71.99993, 34.00001, 5, 3);
        assertThat(Geom.e3D.almostEqual(a, b), is(true));
    }

    @Test
    public void testModule() {
        assertThat(Geom.e3D.magnitude(new Coordinates(1, 1, 1)), isAlmost(Math.sqrt(3)));
        assertThat(Geom.e3D.magnitude(new Coordinates(3, 4, 5)), isAlmost(Math.sqrt(50)));
        assertThat(Geom.e3D.magnitude(new Coordinates(Math.sqrt(3), Math.sqrt(3), Math.sqrt(3))), isAlmost(3));

        assertThat(Geom.e3D.magnitude(new Coordinates(1, 1, 8, 0)), isAlmost(Math.sqrt(66)));
        assertThat(Geom.e3D.magnitude(new Coordinates(3, 4, -4, 5)), isAlmost(Math.sqrt(41)));
    }

    @Test
    public void testDotProduct() {
        Coordinates a = new Coordinates(2, 1);
        Coordinates b = new Coordinates(3, 4, 5);
        Coordinates c = new Coordinates(-3, 5, 3);
        assertThat(Geom.e3D.dotProduct(a, b), isAlmost(10));
        assertThat(Geom.e3D.dotProduct(b, c), isAlmost(26));
        assertThat(Geom.e3D.dotProduct(a, c), isAlmost(-1));
        assertThat(Geom.e3D.dotProduct(a, c), isAlmost(Geom.e3D.dotProduct(c, a)));

        a = new Coordinates(2, 1, 0, 6);
        b = new Coordinates(3, 4, 5, -4);
        assertThat(Geom.e3D.dotProduct(a, b), isAlmost(10));
    }

    @Test
    public void testCrossProduct() {
        Coordinates a = new Coordinates(3, -3, 1);
        Coordinates b = new Coordinates(4, 9, 2);
        Coordinates c = new Coordinates(-12, 12, -4);
        assertThat(Geom.e3D.crossProduct(a, b), isAlmost(new Coordinates(-15, -2, 39)));
        assertThat(Geom.e3D.crossProduct(b, c), isAlmost(new Coordinates(-60, -8, 156)));
        assertThat(Geom.e3D.crossProduct(a, c), isAlmost(new Coordinates(0, 0, 0)));

        a = new Coordinates(3, -3, 1, 5);
        b = new Coordinates(4, 9, 2, 13);
        assertThat(Geom.e3D.crossProduct(a, b), isAlmost(new Coordinates(-15, -2, 39)));
    }

    @Test
    public void testUnitVector() {
        assertThat(Geom.e3D.unitVector(new Coordinates(12, 0)), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(Geom.e3D.unitVector(new Coordinates(0, -5)), isAlmost(new Coordinates(0, -1, 0)));
        assertThat(Geom.e3D.unitVector(new Coordinates(0, 0, -5)), isAlmost(new Coordinates(0, 0, -1)));
        assertThat(Geom.e3D.unitVector(new Coordinates(0, 4, 4)), isAlmost(new Coordinates(0, Math.sqrt(2) / 2, Math.sqrt(2) / 2)));
        assertThat(Geom.e3D.unitVector(new Coordinates(12, -2, 5), new Coordinates(12, -6, 1)),
                isAlmost(new Coordinates(12, -6 + Math.sqrt(2) / 2, 1 + Math.sqrt(2) / 2)));

        assertThat(Geom.e3D.unitVector(new Coordinates(12, 0, 0, 4)), isAlmost(new Coordinates(1, 0)));
        assertThat(Geom.e3D.unitVector(new Coordinates(12, 0, 0, 8)).dim(), is(3));
        assertThat(Geom.e3D.unitVector(new Coordinates(12, 0, 3, 8), new Coordinates(12, -5, 3, 7)), isAlmost(new Coordinates(12, -4, 3, 7)));
        assertThat(Geom.e3D.unitVector(new Coordinates(12, 0, 8), new Coordinates(12, -5, 3)).dim(), is(3));
    }

    @Test
    public void testBetweenAngle() {
        assertThat(Geom.e3D.betweenAngle(new Coordinates(-1, 0), new Coordinates(1, 0)), isAlmost(Math.PI));
        assertThat(Geom.e3D.betweenAngle(new Coordinates(-1, 0), new Coordinates(0, 1)), isAlmost(Math.PI / 2));
        assertThat(Geom.e3D.betweenAngle(new Coordinates(-11, 10), new Coordinates(11, 10), new Coordinates(10, 10)), isAlmost(Math.PI));

        assertThat(Geom.e3D.betweenAngle(new Coordinates(0, -1, 0), new Coordinates(0, 1, 0)), isAlmost(Math.PI));
        assertThat(Geom.e3D.betweenAngle(new Coordinates(-1, 0, 0), new Coordinates(0, 0, 1)), isAlmost(Math.PI / 2));
        assertThat(Geom.e3D.betweenAngle(new Coordinates(-11, 7, 10), new Coordinates(11, 7, 10), new Coordinates(10, 7, 10)), isAlmost(Math.PI));
    }

    @Test
    public void testIsPointInLine() {
        Coordinates n = new Coordinates(2, 2, 1, 32);
        Coordinates a = new Coordinates(2, -2, 1, 72);
        Coordinates b = new Coordinates(2, -6, 1, 10);
        Coordinates c = new Coordinates(0, 1, 0, 3);
        Coordinates d = new Coordinates(4, 3, 2, 1);
        Coordinates e = new Coordinates(3, 0, -1);
        Coordinates f = new Coordinates(4, -2, -3, 6);
        assertThat(Geom.e3D.isPointInLine(n, a, b), is(true));
        assertThat(Geom.e3D.isPointInLine(n, c, d), is(true));
        assertThat(Geom.e3D.isPointInLine(n, e, f), is(true));
        assertThat(Geom.e3D.isPointInLine(n, a, f), is(false));
    }

    @Test
    public void testIsPointInSegment() {
        Coordinates n = new Coordinates(2, 2, 1, 3, 4);
        Coordinates a = new Coordinates(2, -2, 1, 1);
        Coordinates b = new Coordinates(2, -6, 1, 12, 3);
        Coordinates c = new Coordinates(0, 1, 0);
        Coordinates d = new Coordinates(4, 3, 2, 4);
        Coordinates e = new Coordinates(3, 0, -1, 67);
        Coordinates f = new Coordinates(4, -2, -3, 9);
        Coordinates g = new Coordinates(8, 8, 4, 2);
        assertThat(Geom.e3D.isPointInSegment(n, n, n), is(true));
        assertThat(Geom.e3D.isPointInSegment(n, a, n), is(true));
        assertThat(Geom.e3D.isPointInSegment(n, n, a), is(true));
        assertThat(Geom.e3D.isPointInSegment(n, a, a), is(false));
        assertThat(Geom.e3D.isPointInSegment(n, a, b), is(false));
        assertThat(Geom.e3D.isPointInSegment(n, c, d), is(true));
        assertThat(Geom.e3D.isPointInSegment(n, e, f), is(false));
        assertThat(Geom.e3D.isPointInSegment(n, a, f), is(false));
        assertThat(Geom.e3D.isPointInSegment(n, g, g.minus()), is(true));
    }

    // ******** Point - Line ********
    @Test
    public void testPointLineRelation_Inside() {
        Coordinates o = new Coordinates(0, 0, 0, 4, 6, 7);
        Coordinates x = new Coordinates(1, 0, 0, 3);
        GeomE.PointRelation relation = Geom.e3D.pointLineRelation(x, o, x);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(relation.projection(), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.isPointIncluded(), is(true));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointLineRelation_Outside() {
        Coordinates a = new Coordinates(0, 0, 0, 4);
        Coordinates b = new Coordinates(4, 0, 4, 41);
        Coordinates p = new Coordinates(3, 1, 1, 17);
        GeomE.PointRelation relation = Geom.e3D.pointLineRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(2, 0, 2)));
        assertThat(relation.projection(), isAlmost(relation.closestPoint()));
        assertThat(relation.projectionAsLine(), isAlmost(relation.projection()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(3)));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testPointLineRelation_UndefinedLine() {
        Coordinates a = new Coordinates(0, 0, 0, 42);
        Coordinates b = new Coordinates(0, 0, 0, 2);
        Coordinates x = new Coordinates(1, 0);
        Geom.e3D.pointLineRelation(x, a, b);
    }

    // ******** Point - Segment ********
    @Test
    public void testPointSegmentRelation_SameLineInside() {
        Coordinates a = new Coordinates(0, 0, 1, 4);
        Coordinates b = new Coordinates(1, 0, 1, 32);
        Coordinates p = new Coordinates(0.8, 0, 1, 432);
        GeomE.PointRelation relation = Geom.e3D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(p.restr(3)));
        assertThat(relation.projection(), isAlmost(p.restr(3)));
        assertThat(relation.projectionAsLine(), isAlmost(p.restr(3)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.isPointIncluded(), is(true));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointSegmentRelation_SameLineOutSide() {
        Coordinates a = new Coordinates(0, 0, 7, 22);
        Coordinates b = new Coordinates(1, 0, 7, 9);
        Coordinates p = new Coordinates(2.8, 0, 7);
        GeomE.PointRelation relation = Geom.e3D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(b.restr(3)));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), isAlmost(p.restr(3)));
        assertThat(relation.distance(), isAlmost(1.8));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(false));
    }

    @Test
    public void testPointSegmentRelation_DifferentLineInside() {
        Coordinates a = new Coordinates(0, 0, 2, 44);
        Coordinates b = new Coordinates(1, 0, 2, 85);
        Coordinates p = new Coordinates(0.8, 1);
        GeomE.PointRelation relation = Geom.e3D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(0.8, 0, 2)));
        assertThat(relation.projection(), isAlmost(new Coordinates(0.8, 0, 2)));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(0.8, 0, 2)));
        assertThat(relation.distance(), isAlmost(Math.sqrt(5)));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointSegmentRelation_DifferentLineOutside() {
        Coordinates a = new Coordinates(0, 0, 1, 3);
        Coordinates b = new Coordinates(1, 0, 1, 2);
        Coordinates p = new Coordinates(3, 1, 1, 1);
        GeomE.PointRelation relation = Geom.e3D.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(b.restr(3)));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(3, 0, 1)));
        assertThat(relation.distance(), isAlmost(Math.sqrt(5)));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(false));
    }

    @Test
    public void testPointSegmentRelation_UndefinedSegment() {
        Coordinates o = new Coordinates(0, 0, 5, 6);
        Coordinates x = new Coordinates(1, 0, 9, 3);
        GeomE.PointRelation relation = Geom.e3D.pointSegmentRelation(x, o, o);
        assertThat(relation.closestPoint(), isAlmost(o.restr(3)));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(17)));
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
        GeomE.LineRelation relation = Geom.e3D.lineLineRelation(a, b, c, d);
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
        GeomE.LineRelation relation = Geom.e3D.lineLineRelation(a, b, c, d);
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
        Coordinates a = new Coordinates(0, 1, 0);
        Coordinates b = new Coordinates(1, 1, 2);
        Coordinates c = new Coordinates(2, 1, 0);
        Coordinates d = new Coordinates(-3, 1, 0);
        GeomE.LineRelation relation = Geom.e3D.lineLineRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(a.restr(3)));
        assertThat(relation.intersection(), isAlmost(a.restr(3)));
        assertThat(relation.intersectionAsLines(), isAlmost(a.restr(3)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineLineRelation_Skew() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Coordinates d = new Coordinates(-3, 1, 1);
        GeomE.LineRelation relation = Geom.e3D.lineLineRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(1, 1, 1)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testLineLineRelation_FirstUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.e3D.lineLineRelation(a, a, b, c);
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testLineLineRelation_SecondUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.e3D.lineLineRelation(a, b, c, c);
    }

    // ******** Line - Segment ********
    @Test
    public void testLineSegmentRelation_SameLine() {
        Coordinates a = new Coordinates(0, 0, 0, 53);
        Coordinates b = new Coordinates(1, 0, 2, 2);
        Coordinates c = new Coordinates(2, 0, 4, 12);
        Coordinates d = new Coordinates(-3, 0, -6, 44);
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, d);
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
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, d);
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
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(c.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), isAlmost(c.restr(3)));
        assertThat(relation.intersectionAsLines(), isAlmost(c.restr(3)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(true));
    }

    @Test
    public void testLineSegmentRelation_IntersectingInside() {
        Coordinates a = new Coordinates(0, 1, 0, 89);
        Coordinates b = new Coordinates(1, 1, 2, 1);
        Coordinates c = new Coordinates(2, 1, 0, 42);
        Coordinates d = new Coordinates(-3, 1, 0, 2);
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(a.restr(3)));
        assertThat(relation.intersection(), isAlmost(a.restr(3)));
        assertThat(relation.intersectionAsLines(), isAlmost(a.restr(3)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineSegmentRelation_IntersectingOutside() {
        Coordinates a = new Coordinates(0, 1, 0, 67);
        Coordinates b = new Coordinates(0, 1, 2, 9);
        Coordinates c = new Coordinates(1, 1, 0, 11);
        Coordinates d = new Coordinates(3, 1, 0, 1);
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), isAlmost(a.restr(3)));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineSegmentRelation_Skew() {
        Coordinates a = new Coordinates(1, 0, 0, 9);
        Coordinates b = new Coordinates(1, 1, 0, 8);
        Coordinates c = new Coordinates(2, 1, 1, 11);
        Coordinates d = new Coordinates(8, 1, 1, 34);
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
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
        Geom.e3D.lineSegmentRelation(a, a, b, c);
    }

    @Test
    public void testLineSegmentRelation_SegmentUndefined() {
        Coordinates a = new Coordinates(1, 0, 0, 1);
        Coordinates b = new Coordinates(1, 1, 0, 33);
        Coordinates c = new Coordinates(0, 1, 1, 1);
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, c);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
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
        GeomE.LineRelation relation = Geom.e3D.lineSegmentRelation(a, b, c, c);
        relation.areParallel();
    }

    // ******** Segment - Segment ********
    @Test
    public void testSegmentSegmentRelation_SameLine_Overlapping() {
        Coordinates a = new Coordinates(0, 0, 0, 2);
        Coordinates b = new Coordinates(1, 0, 2, 11);
        Coordinates c = new Coordinates(2, 0, 4, 1);
        Coordinates d = new Coordinates(-3, 0, -6, 11);
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, d);
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
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(5)));
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
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, d);
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
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(6)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(true));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_IntersectingInside() {
        Coordinates a = new Coordinates(-1, 1, -2);
        Coordinates b = new Coordinates(1, 1, 2, 3);
        Coordinates c = new Coordinates(2, 1, 0, 12);
        Coordinates d = new Coordinates(-3, 1, 0);
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(0, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(0, 1, 0)));
        assertThat(relation.intersection(), isAlmost(new Coordinates(0, 1, 0)));
        assertThat(relation.intersectionAsLines(), isAlmost(new Coordinates(0, 1, 0)));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_IntersectingOutside() {
        Coordinates a = new Coordinates(4, 1, 8);
        Coordinates b = new Coordinates(1, 1, 2, 1);
        Coordinates c = new Coordinates(2, 1, 0, 12);
        Coordinates d = new Coordinates(-3, 1, 0, 14);
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), isAlmost(new Coordinates(0, 1, 0)));
        assertThat(relation.distance(), isAlmost(2));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testSegmentSegmentRelation_Skew() {
        Coordinates a = new Coordinates(1, 0, 0, 8);
        Coordinates b = new Coordinates(1, 1, 0, 4);
        Coordinates c = new Coordinates(2, 1, 1, 11);
        Coordinates d = new Coordinates(8, 1, 1, 7);
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
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
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, c);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
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
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, a, b, c);
        relation.areParallel();
    }

    @Test
    public void testSegmentSegmentRelation_SecondUndefined() {
        Coordinates a = new Coordinates(0, 1, 1);
        Coordinates b = new Coordinates(1, 0, 0);
        Coordinates c = new Coordinates(1, 1, 0);
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, a, b, c);
        assertThat(relation.closestPointA(), isAlmost(a.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
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
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, b, c, c);
        relation.areParallel();
    }

    @Test
    public void testSegmentSegmentRelation_BothUndefined() {
        Coordinates a = new Coordinates(0, 1, 1);
        Coordinates b = new Coordinates(1, 0, 0);
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, a, b, b);
        assertThat(relation.closestPointA(), isAlmost(a.restr(3)));
        assertThat(relation.closestPointB(), isAlmost(b.restr(3)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(3)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = GeomE.UndefinedSubspace.class)
    public void testSegmentSegmentRelation_BothUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        GeomE.LineRelation relation = Geom.e3D.segmentSegmentRelation(a, a, b, b);
        relation.areParallel();
    }

}
