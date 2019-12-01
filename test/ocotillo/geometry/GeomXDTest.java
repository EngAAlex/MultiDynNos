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

import ocotillo.geometry.GeomE.LineRelation;
import ocotillo.geometry.GeomE.PointRelation;
import ocotillo.geometry.GeomE.UndefinedSubspace;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class GeomXDTest {

    @Test
    public void testAlmostZeroWithDoubles() {
        assertThat(Geom.eXD.almostZero(0), is(true));
        assertThat(Geom.eXD.almostZero(-0.000001), is(true));

        assertThat(Geom.eXD.almostZero(0.01), is(false));
        assertThat(Geom.eXD.almostEqual(10, 1), is(false));
    }

    @Test
    public void testAlmostZeroWithCoord() {
        Coordinates a = new Coordinates(0, 0.0000001);
        Coordinates b = new Coordinates(0.00001, -0.00001);
        assertThat(Geom.eXD.almostZero(a), is(true));
        assertThat(Geom.eXD.almostZero(b), is(true));

        a = new Coordinates(0.0000001, 0, 0, 0, 0, 4);
        b = new Coordinates(1, 0, 0);
        assertThat(Geom.eXD.almostZero(a), is(false));
        assertThat(Geom.eXD.almostZero(b), is(false));
    }

    @Test
    public void testAlmostEqualWithDoubles() {
        assertThat(Geom.eXD.almostEqual(0, 0), is(true));
        assertThat(Geom.eXD.almostEqual(10, 10), is(true));
        assertThat(Geom.eXD.almostEqual(10.000000001, 9.99999999), is(true));

        assertThat(Geom.eXD.almostEqual(001, 00011), is(false));
    }

    @Test
    public void testAlmostEqualWithCoord() {
        Coordinates a = new Coordinates(0, 0.0000001);
        Coordinates b = new Coordinates(0.00001, -0.00001);
        assertThat(Geom.eXD.almostEqual(a, b), is(true));

        a = new Coordinates(72, 34);
        b = new Coordinates(71.99993, 34.00001);
        assertThat(Geom.eXD.almostEqual(a, b), is(true));
    }

    @Test
    public void testDotProduct() {
        Coordinates a = new Coordinates(2, 1);
        Coordinates b = new Coordinates(3, 4, 5);
        Coordinates c = new Coordinates(-3, 5, 3);
        Coordinates d = new Coordinates(2, 1, 0, 6);
        Coordinates e = new Coordinates(3, 4, 5, -4);

        assertThat(Geom.eXD.dotProduct(a, b), isAlmost(10));
        assertThat(Geom.eXD.dotProduct(b, c), isAlmost(26));
        assertThat(Geom.eXD.dotProduct(a, c), isAlmost(-1));
        assertThat(Geom.eXD.dotProduct(d, e), isAlmost(-14));
        assertThat(Geom.eXD.dotProduct(a, c), isAlmost(Geom.eXD.dotProduct(c, a)));
    }

    @Test
    public void testScaleVector() {
        assertThat(Geom.eXD.scaleVector(new Coordinates(1, 0, 4), 2), isAlmost(new Coordinates(2, 0, 8)));
        assertThat(Geom.eXD.scaleVector(new Coordinates(0, -1), 3), isAlmost(new Coordinates(0, -3)));
        Geom.eXD.scaleVector(new Coordinates(10, 11, 2, -1), 2, new Coordinates(10, 10));
        assertThat(Geom.eXD.scaleVector(new Coordinates(10, 11, 2, -1), 2, new Coordinates(10, 10)), isAlmost(new Coordinates(10, 12, 4, -2)));
    }

    @Test
    public void testUnitVector() {
        assertThat(Geom.eXD.unitVector(new Coordinates(12, 0, 0)), isAlmost(new Coordinates(1, 0, 0)));
        assertThat(Geom.eXD.unitVector(new Coordinates(0, 5, 5)), isAlmost(new Coordinates(0, Math.sqrt(2) / 2, Math.sqrt(2) / 2)));
        assertThat(Geom.eXD.unitVector(new Coordinates(12, 5, 7), new Coordinates(12, -5, 7)), isAlmost(new Coordinates(12, -4, 7)));
    }

    @Test
    public void testMidPoint() {
        assertThat(Geom.eXD.midPoint(new Coordinates(0, 0), new Coordinates(10, 10)), is(new Coordinates(5, 5)));
        assertThat(Geom.eXD.midPoint(new Coordinates(2, 3, 10), new Coordinates(8, 5, 4)), is(new Coordinates(5, 4, 7)));
        assertThat(Geom.eXD.midPoint(new Coordinates(2, 3), new Coordinates(8, 5, 4)), is(new Coordinates(5, 4, 2)));
    }

    @Test
    public void testInBetweenPoint() {
        assertThat(Geom.eXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 0), is(new Coordinates(0, 0)));
        assertThat(Geom.eXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 1), is(new Coordinates(10, 10)));
        assertThat(Geom.eXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 0.5), is(new Coordinates(5, 5)));
        assertThat(Geom.eXD.inBetweenPoint(new Coordinates(0, 0), new Coordinates(10, 10), 0.4), is(new Coordinates(4, 4)));
    }

    @Test
    public void testIsPointInLine() {
        Coordinates n = new Coordinates(2, 2, 1);
        Coordinates a = new Coordinates(2, -2, 1);
        Coordinates b = new Coordinates(2, -6, 1);
        Coordinates c = new Coordinates(0, 1, 0);
        Coordinates d = new Coordinates(4, 3, 2);
        Coordinates e = new Coordinates(3, 0, -1);
        Coordinates f = new Coordinates(4, -2, -3);
        assertThat(Geom.eXD.isPointInLine(n, a, b), is(true));
        assertThat(Geom.eXD.isPointInLine(n, c, d), is(true));
        assertThat(Geom.eXD.isPointInLine(n, e, f), is(true));
        assertThat(Geom.eXD.isPointInLine(n, a, f), is(false));
    }

    @Test
    public void testIsPointInSegment() {
        Coordinates n = new Coordinates(2, 2, 1);
        Coordinates a = new Coordinates(2, -2, 1);
        Coordinates b = new Coordinates(2, -6, 1);
        Coordinates c = new Coordinates(0, 1, 0);
        Coordinates d = new Coordinates(4, 3, 2);
        Coordinates e = new Coordinates(3, 0, -1);
        Coordinates f = new Coordinates(4, -2, -3);
        Coordinates g = new Coordinates(8, 8, 4);
        assertThat(Geom.eXD.isPointInSegment(n, n, n), is(true));
        assertThat(Geom.eXD.isPointInSegment(n, a, n), is(true));
        assertThat(Geom.eXD.isPointInSegment(n, n, a), is(true));
        assertThat(Geom.eXD.isPointInSegment(n, a, a), is(false));
        assertThat(Geom.eXD.isPointInSegment(n, a, b), is(false));
        assertThat(Geom.eXD.isPointInSegment(n, c, d), is(true));
        assertThat(Geom.eXD.isPointInSegment(n, e, f), is(false));
        assertThat(Geom.eXD.isPointInSegment(n, a, f), is(false));
        assertThat(Geom.eXD.isPointInSegment(n, g, g.minus()), is(true));
    }

    // ******** Point - Line ********
    @Test
    public void testPointLineRelation_Inside() {
        Coordinates o = new Coordinates(0, 0);
        Coordinates x = new Coordinates(1, 0);
        PointRelation relation = Geom.eXD.pointLineRelation(x, o, x);
        assertThat(relation.closestPoint(), isAlmost(x));
        assertThat(relation.projection(), isAlmost(x));
        assertThat(relation.projectionAsLine(), isAlmost(x));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.isPointIncluded(), is(true));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointLineRelation_Outside() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(4, 0, 4, 4);
        Coordinates p = new Coordinates(3, 1, 1, 2);
        PointRelation relation = Geom.eXD.pointLineRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(2, 0, 2, 2)));
        assertThat(relation.projection(), isAlmost(relation.closestPoint()));
        assertThat(relation.projectionAsLine(), isAlmost(relation.projection()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(3)));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test(expected = UndefinedSubspace.class)
    public void testPointLineRelation_UndefinedLine() {
        Coordinates o = new Coordinates(0, 0);
        Coordinates x = new Coordinates(1, 0);
        Geom.eXD.pointLineRelation(x, o, o);
    }

    // ******** Point - Segment ********
    @Test
    public void testPointSegmentRelation_SameLineInside() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(1, 0);
        Coordinates p = new Coordinates(0.8, 0);
        PointRelation relation = Geom.eXD.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(p));
        assertThat(relation.projection(), isAlmost(p));
        assertThat(relation.projectionAsLine(), isAlmost(p));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.isPointIncluded(), is(true));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointSegmentRelation_SameLineOutSide() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(1, 0);
        Coordinates p = new Coordinates(2.8, 0);
        PointRelation relation = Geom.eXD.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(b));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), isAlmost(p));
        assertThat(relation.distance(), isAlmost(1.8));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(false));
    }

    @Test
    public void testPointSegmentRelation_DifferentLineInside() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(1, 0);
        Coordinates p = new Coordinates(0.8, 1);
        PointRelation relation = Geom.eXD.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(new Coordinates(0.8, 0)));
        assertThat(relation.projection(), isAlmost(new Coordinates(0.8, 0)));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(0.8, 0)));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(true));
    }

    @Test
    public void testPointSegmentRelation_DifferentLineOutside() {
        Coordinates a = new Coordinates(0, 0);
        Coordinates b = new Coordinates(1, 0);
        Coordinates p = new Coordinates(3, 1);
        PointRelation relation = Geom.eXD.pointSegmentRelation(p, a, b);
        assertThat(relation.closestPoint(), isAlmost(b));
        assertThat(relation.projection(), is(nullValue()));
        assertThat(relation.projectionAsLine(), isAlmost(new Coordinates(3, 0)));
        assertThat(relation.distance(), isAlmost(Math.sqrt(5)));
        assertThat(relation.isPointIncluded(), is(false));
        assertThat(relation.isProjectionIncluded(), is(false));
    }

    @Test
    public void testPointSegmentRelation_UndefinedSegment() {
        Coordinates o = new Coordinates(0, 0);
        Coordinates x = new Coordinates(1, 0);
        PointRelation relation = Geom.eXD.pointSegmentRelation(x, o, o);
        assertThat(relation.closestPoint(), isAlmost(o));
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
        LineRelation relation = Geom.eXD.lineLineRelation(a, b, c, d);
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
        LineRelation relation = Geom.eXD.lineLineRelation(a, b, c, d);
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
        LineRelation relation = Geom.eXD.lineLineRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a));
        assertThat(relation.closestPointB(), isAlmost(a));
        assertThat(relation.intersection(), isAlmost(a));
        assertThat(relation.intersectionAsLines(), isAlmost(a));
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
        LineRelation relation = Geom.eXD.lineLineRelation(a, b, c, d);
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

    @Test(expected = UndefinedSubspace.class)
    public void testLineLineRelation_FirstUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.eXD.lineLineRelation(a, a, b, c);
    }

    @Test(expected = UndefinedSubspace.class)
    public void testLineLineRelation_SecondUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.eXD.lineLineRelation(a, b, c, c);
    }

    // ******** Line - Segment ********
    @Test
    public void testLineSegmentRelation_SameLine() {
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 0, 4);
        Coordinates d = new Coordinates(-3, 0, -6);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, d);
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
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 1, 4);
        Coordinates d = new Coordinates(-3, 1, -6);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, d);
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
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(1, 1, 0);
        Coordinates d = new Coordinates(8, 1, 1);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(c));
        assertThat(relation.closestPointB(), isAlmost(c));
        assertThat(relation.intersection(), isAlmost(c));
        assertThat(relation.intersectionAsLines(), is(c));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(true));
    }

    @Test
    public void testLineSegmentRelation_IntersectingInside() {
        Coordinates a = new Coordinates(0, 1, 0);
        Coordinates b = new Coordinates(1, 1, 2);
        Coordinates c = new Coordinates(2, 1, 0);
        Coordinates d = new Coordinates(-3, 1, 0);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a));
        assertThat(relation.closestPointB(), isAlmost(a));
        assertThat(relation.intersection(), isAlmost(a));
        assertThat(relation.intersectionAsLines(), isAlmost(a));
        assertThat(relation.distance(), isAlmost(0));
        assertThat(relation.areDisjoint(), is(false));
        assertThat(relation.areIntersecting(), is(true));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineSegmentRelation_IntersectingOutside() {
        Coordinates a = new Coordinates(0, 1, 0);
        Coordinates b = new Coordinates(0, 1, 2);
        Coordinates c = new Coordinates(1, 1, 0);
        Coordinates d = new Coordinates(3, 1, 0);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(a));
        assertThat(relation.closestPointB(), isAlmost(c));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), isAlmost(a));
        assertThat(relation.distance(), isAlmost(1));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test
    public void testLineSegmentRelation_Skew() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(2, 1, 1);
        Coordinates d = new Coordinates(8, 1, 1);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areParallel(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = UndefinedSubspace.class)
    public void testLineSegmentRelation_LineUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        Geom.eXD.lineSegmentRelation(a, a, b, c);
    }

    @Test
    public void testLineSegmentRelation_SegmentUndefined() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, c);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = UndefinedSubspace.class)
    public void testLineSegmentRelation_SegmentUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        LineRelation relation = Geom.eXD.lineSegmentRelation(a, b, c, c);
        relation.areParallel();
    }

    // ******** Segment - Segment ********
    @Test
    public void testSegmentSegmentRelation_SameLine_Overlapping() {
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 0, 4);
        Coordinates d = new Coordinates(-3, 0, -6);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, d);
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
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 0, 4);
        Coordinates d = new Coordinates(3, 0, 6);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), is(b));
        assertThat(relation.closestPointB(), is(c));
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
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 1, 4);
        Coordinates d = new Coordinates(-3, 1, -6);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, d);
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
        Coordinates a = new Coordinates(0, 0, 0);
        Coordinates b = new Coordinates(1, 0, 2);
        Coordinates c = new Coordinates(2, 1, 4);
        Coordinates d = new Coordinates(3, 1, 6);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b));
        assertThat(relation.closestPointB(), isAlmost(c));
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
        Coordinates b = new Coordinates(1, 1, 2);
        Coordinates c = new Coordinates(2, 1, 0);
        Coordinates d = new Coordinates(-3, 1, 0);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, d);
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
        Coordinates b = new Coordinates(1, 1, 2);
        Coordinates c = new Coordinates(2, 1, 0);
        Coordinates d = new Coordinates(-3, 1, 0);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b));
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
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(2, 1, 1);
        Coordinates d = new Coordinates(8, 1, 1);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, d);
        assertThat(relation.closestPointA(), isAlmost(b));
        assertThat(relation.closestPointB(), isAlmost(c));
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
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, c);
        assertThat(relation.closestPointA(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.closestPointB(), isAlmost(c));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = UndefinedSubspace.class)
    public void testSegmentSegmentRelation_FirstUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, a, b, c);
        relation.areParallel();
    }

    @Test
    public void testSegmentSegmentRelation_SecondUndefined() {
        Coordinates a = new Coordinates(0, 1, 1);
        Coordinates b = new Coordinates(1, 0, 0);
        Coordinates c = new Coordinates(1, 1, 0);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, a, b, c);
        assertThat(relation.closestPointA(), isAlmost(a));
        assertThat(relation.closestPointB(), isAlmost(new Coordinates(1, 1, 0)));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(2)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = UndefinedSubspace.class)
    public void testSegmentSegmentRelation_SecondUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        Coordinates c = new Coordinates(0, 1, 1);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, b, c, c);
        relation.areParallel();
    }

    @Test
    public void testSegmentSegmentRelation_BothUndefined() {
        Coordinates a = new Coordinates(0, 1, 1);
        Coordinates b = new Coordinates(1, 0, 0);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, a, b, b);
        assertThat(relation.closestPointA(), isAlmost(a));
        assertThat(relation.closestPointB(), isAlmost(b));
        assertThat(relation.intersection(), is(nullValue()));
        assertThat(relation.intersectionAsLines(), is(nullValue()));
        assertThat(relation.distance(), isAlmost(Math.sqrt(3)));
        assertThat(relation.areDisjoint(), is(true));
        assertThat(relation.areIntersecting(), is(false));
        assertThat(relation.areOverlapping(), is(false));
        assertThat(relation.areAdjacent(), is(false));
    }

    @Test(expected = UndefinedSubspace.class)
    public void testSegmentSegmentRelation_BothUndefined_AskingParallel() {
        Coordinates a = new Coordinates(1, 0, 0);
        Coordinates b = new Coordinates(1, 1, 0);
        LineRelation relation = Geom.eXD.segmentSegmentRelation(a, a, b, b);
        relation.areParallel();
    }
}
