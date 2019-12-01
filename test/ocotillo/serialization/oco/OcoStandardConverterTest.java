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
package ocotillo.serialization.oco;

import java.awt.Color;
import ocotillo.dygraph.EvoBuilder;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Coordinates;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.EdgeShape;
import ocotillo.graph.StdAttribute.NodeShape;
import ocotillo.geometry.Interval;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

/**
 * Tests the oco standard converters.
 */
public class OcoStandardConverterTest {

    @Test
    public void testStaticBoolean() {
        OcoValueConverter<Boolean> converter = new OcoStandardConverter.StaticBoolean();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Boolean.class)));
        assertThat(converter.defaultValue(), is(false));
        assertThat(converter.typeName(), is("Boolean"));
        assertThat(converter.graphLibToOco(false), is("false"));
        assertThat(converter.graphLibToOco(true), is("true"));
        assertThat(converter.ocoToGraphLib("false"), is(false));
        assertThat(converter.ocoToGraphLib("true"), is(true));
    }

    @Test
    public void testDynamicBoolean() {
        OcoValueConverter<Evolution<Boolean>> converter = new OcoStandardConverter.DynamicBoolean();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Boolean.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>(false)));
        assertThat(converter.typeName(), is("Boolean"));

        Evolution<Boolean> evolution = EvoBuilder.defaultAt(true)
                .withConst(Interval.newClosed(4, 9), false)
                .withRect(Interval.newRightClosed(9, 13), true, false, Interpolation.Std.step)
                .build();
        String stringRep = "true § const ^ [4.0, 9.0] ^ false § rect ^ (9.0, 13.0] ^ true ^ false ^ step";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticInteger() {
        OcoValueConverter<Integer> converter = new OcoStandardConverter.StaticInteger();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Integer.class)));
        assertThat(converter.defaultValue(), is(0));
        assertThat(converter.typeName(), is("Integer"));
        assertThat(converter.graphLibToOco(1), is("1"));
        assertThat(converter.graphLibToOco(-4), is("-4"));
        assertThat(converter.ocoToGraphLib("-8"), is(-8));
        assertThat(converter.ocoToGraphLib("16"), is(16));
    }

    @Test
    public void testDynamicInteger() {
        OcoValueConverter<Evolution<Integer>> converter = new OcoStandardConverter.DynamicInteger();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Integer.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>(0)));
        assertThat(converter.typeName(), is("Integer"));

        Evolution<Integer> evolution = EvoBuilder.defaultAt(4)
                .withConst(Interval.newClosed(4, 9), 13)
                .withRect(Interval.newRightClosed(9, 13), 13, 18, Interpolation.Std.linear)
                .build();
        String stringRep = "4 § const ^ [4.0, 9.0] ^ 13 § rect ^ (9.0, 13.0] ^ 13 ^ 18 ^ linear";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticDouble() {
        OcoValueConverter<Double> converter = new OcoStandardConverter.StaticDouble();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Double.class)));
        assertThat(converter.defaultValue(), is(0.0));
        assertThat(converter.typeName(), is("Double"));
        assertThat(converter.graphLibToOco(1.0), is("1.0"));
        assertThat(converter.graphLibToOco(-4.0), is("-4.0"));
        assertThat(converter.ocoToGraphLib("-8.0"), is(-8.0));
        assertThat(converter.ocoToGraphLib("16.0"), is(16.0));
    }

    @Test
    public void testDynamicDouble() {
        OcoValueConverter<Evolution<Double>> converter = new OcoStandardConverter.DynamicDouble();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Double.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>(0.0)));
        assertThat(converter.typeName(), is("Double"));

        Evolution<Double> evolution = EvoBuilder.defaultAt(4.0)
                .withConst(Interval.newClosed(4, 9), 13.0)
                .withRect(Interval.newRightClosed(9, 13), 13.0, 18.0, Interpolation.Std.linear)
                .build();
        String stringRep = "4.0 § const ^ [4.0, 9.0] ^ 13.0 § rect ^ (9.0, 13.0] ^ 13.0 ^ 18.0 ^ linear";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticString() {
        OcoValueConverter<String> converter = new OcoStandardConverter.StaticString();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(String.class)));
        assertThat(converter.defaultValue(), is(""));
        assertThat(converter.typeName(), is("String"));
        assertThat(converter.graphLibToOco("Coco"), is("Coco"));
        assertThat(converter.graphLibToOco("Jambo"), is("Jambo"));
        assertThat(converter.ocoToGraphLib("Yay"), is("Yay"));
        assertThat(converter.ocoToGraphLib("eh"), is("eh"));
    }

    @Test
    public void testDynamicString() {
        OcoValueConverter<Evolution<String>> converter = new OcoStandardConverter.DynamicString();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(String.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>("")));
        assertThat(converter.typeName(), is("String"));

        Evolution<String> evolution = EvoBuilder.defaultAt("alpha")
                .withConst(Interval.newClosed(4, 9), "beta")
                .withRect(Interval.newRightClosed(9, 13), "beta", "gamma", Interpolation.Std.step)
                .build();
        String stringRep = "alpha § const ^ [4.0, 9.0] ^ beta § rect ^ (9.0, 13.0] ^ beta ^ gamma ^ step";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticCoordinates() {
        OcoValueConverter<Coordinates> converter = new OcoStandardConverter.StaticCoordinates();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Coordinates.class)));
        assertThat(converter.defaultValue(), is(new Coordinates(0, 0)));
        assertThat(converter.typeName(), is("Coordinates"));
        assertThat(converter.graphLibToOco(new Coordinates(4, 5.3, 4)), is("(4.0, 5.3, 4.0)"));
        assertThat(converter.graphLibToOco(new Coordinates(7, 0)), is("(7.0, 0.0)"));
        assertThat(converter.ocoToGraphLib("(-4.9, 12.0)"), isAlmost(new Coordinates(-4.9, 12.0)));
        assertThat(converter.ocoToGraphLib("(0.0, 0.0 ,0.0 ,0.4, 0.0)"), isAlmost(new Coordinates(0.0, 0.0, 0.0, 0.4, 0.0)));
    }

    @Test
    public void testDynamicCoordinates() {
        OcoValueConverter<Evolution<Coordinates>> converter = new OcoStandardConverter.DynamicCoordinates();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Coordinates.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>(new Coordinates(0, 0))));
        assertThat(converter.typeName(), is("Coordinates"));

        Evolution<Coordinates> evolution = EvoBuilder.defaultAt(new Coordinates(6, 8))
                .withConst(Interval.newClosed(4, 9), new Coordinates(5, 7))
                .withRect(Interval.newRightClosed(9, 13), new Coordinates(5, 7), new Coordinates(5, 10), Interpolation.Std.smallGaussian)
                .build();
        String stringRep = "(6.0, 8.0) § const ^ [4.0, 9.0] ^ (5.0, 7.0) § rect ^ (9.0, 13.0] ^ (5.0, 7.0) ^ (5.0, 10.0) ^ smallGaussian";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticColor() {
        OcoValueConverter<Color> converter = new OcoStandardConverter.StaticColor();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Color.class)));
        assertThat(converter.defaultValue(), is(Color.BLACK));
        assertThat(converter.typeName(), is("Color"));
        assertThat(converter.graphLibToOco(new Color(12, 23, 34)), is("#0c1722ff"));
        assertThat(converter.graphLibToOco(new Color(64, 32, 0)), is("#402000ff"));
        assertThat(converter.ocoToGraphLib("#0c1722ff"), is(new Color(12, 23, 34)));
        assertThat(converter.ocoToGraphLib("#40200010"), is(new Color(64, 32, 0, 16)));
    }

    @Test
    public void testDynamicColor() {
        OcoValueConverter<Evolution<Color>> converter = new OcoStandardConverter.DynamicColor();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(Color.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>(Color.BLACK)));
        assertThat(converter.typeName(), is("Color"));

        Evolution<Color> evolution = EvoBuilder.defaultAt(Color.RED)
                .withConst(Interval.newClosed(4, 9), Color.GREEN)
                .withRect(Interval.newRightClosed(9, 13), Color.BLUE, Color.RED, Interpolation.Std.smallGaussian)
                .build();
        String stringRep = "#ff0000ff § const ^ [4.0, 9.0] ^ #00ff00ff § rect ^ (9.0, 13.0] ^ #0000ffff ^ #ff0000ff ^ smallGaussian";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticNodeShape() {
        OcoValueConverter<NodeShape> converter = new OcoStandardConverter.StaticNodeShape();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(NodeShape.class)));
        assertThat(converter.defaultValue(), is(NodeShape.cuboid));
        assertThat(converter.typeName(), is("NodeShape"));
        assertThat(converter.graphLibToOco(NodeShape.cuboid), is("cuboid"));
        assertThat(converter.graphLibToOco(NodeShape.spheroid), is("spheroid"));
        assertThat(converter.ocoToGraphLib("cuboid"), is(NodeShape.cuboid));
        assertThat(converter.ocoToGraphLib("spheroid"), is(NodeShape.spheroid));
    }

    @Test
    public void testDynamicNodeShape() {
        OcoValueConverter<Evolution<NodeShape>> converter = new OcoStandardConverter.DynamicNodeShape();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(NodeShape.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>(NodeShape.cuboid)));
        assertThat(converter.typeName(), is("NodeShape"));

        Evolution<NodeShape> evolution = EvoBuilder.defaultAt(NodeShape.cuboid)
                .withConst(Interval.newClosed(4, 9), NodeShape.spheroid)
                .withRect(Interval.newRightClosed(9, 13), NodeShape.spheroid, NodeShape.cuboid, Interpolation.Std.step)
                .build();
        String stringRep = "cuboid § const ^ [4.0, 9.0] ^ spheroid § rect ^ (9.0, 13.0] ^ spheroid ^ cuboid ^ step";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticEdgeShape() {
        OcoValueConverter<EdgeShape> converter = new OcoStandardConverter.StaticEdgeShape();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(EdgeShape.class)));
        assertThat(converter.defaultValue(), is(EdgeShape.polyline));
        assertThat(converter.typeName(), is("EdgeShape"));
        assertThat(converter.graphLibToOco(EdgeShape.polyline), is("polyline"));
        assertThat(converter.ocoToGraphLib("polyline"), is(EdgeShape.polyline));
    }

    @Test
    public void testDynamicEdgeShape() {
        OcoValueConverter<Evolution<EdgeShape>> converter = new OcoStandardConverter.DynamicEdgeShape();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(EdgeShape.class)));
        assertThat(converter.defaultValue(), is(new Evolution<>(EdgeShape.polyline)));
        assertThat(converter.typeName(), is("EdgeShape"));

        Evolution<EdgeShape> evolution = EvoBuilder.defaultAt(EdgeShape.polyline)
                .withConst(Interval.newClosed(4, 9), EdgeShape.polyline)
                .withRect(Interval.newRightClosed(9, 13), EdgeShape.polyline, EdgeShape.polyline, Interpolation.Std.step)
                .build();
        String stringRep = "polyline § const ^ [4.0, 9.0] ^ polyline § rect ^ (9.0, 13.0] ^ polyline ^ polyline ^ step";
        assertThat(converter.graphLibToOco(evolution), is(stringRep));
        assertThat(converter.ocoToGraphLib(stringRep), is(evolution));
    }

    @Test
    public void testStaticControlPoints() {
        OcoValueConverter<ControlPoints> converter = new OcoStandardConverter.StaticControlPoints();
        assertThat(converter.typeClass(), is(CoreMatchers.<Class<?>>equalTo(ControlPoints.class)));
        assertThat(converter.defaultValue(), is(new ControlPoints()));
        assertThat(converter.typeName(), is("ControlPoints"));
        assertThat(converter.graphLibToOco(new ControlPoints(new Coordinates(0, 5), new Coordinates(0, 10))),
                is("(0.0, 5.0) | (0.0, 10.0)"));
        assertThat(converter.graphLibToOco(new ControlPoints(new Coordinates(-40, 15), new Coordinates(0.6, 1.5, 63.2))),
                is("(-40.0, 15.0) | (0.6, 1.5, 63.2)"));
        assertThat(converter.ocoToGraphLib("(0.0, 5.0) | (0.0, 10.0)"),
                is(new ControlPoints(new Coordinates(0, 5), new Coordinates(0, 10))));
        assertThat(converter.ocoToGraphLib("(-40.0, 15.0) | (0.6, 1.5, 63.2)"),
                is(new ControlPoints(new Coordinates(-40, 15), new Coordinates(0.6, 1.5, 63.2))));
    }
}
