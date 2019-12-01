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
package ocotillo.graph.rendering;

import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.rendering.HeatMap.Gradient;
import java.awt.Color;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class HeatMapTest {

    @Test
    public void TestFillHotSpot() throws Exception {
        int hotSpotRadius = 60;
        double[][] pixelHeat = new double[200][200];
        Whitebox.invokeMethod(HeatMap.class, "fillHotSpot", pixelHeat, 100, 100, 70.0);

        assertThat(pixelHeat[100][100 + hotSpotRadius + 1], is(0.0));
        assertThat(pixelHeat[100 + hotSpotRadius + 1][100], is(0.0));
        assertThat(pixelHeat[100][100 - hotSpotRadius - 1], is(0.0));
        assertThat(pixelHeat[100 - hotSpotRadius - 1][100], is(0.0));
        assertThat(pixelHeat[100][100], is(70.0));
        for (int i = 0; i < hotSpotRadius; i++) {
            assertThat(pixelHeat[100][101 + i], is(lessThan(pixelHeat[100][100 + i])));
        }
    }

    @Test
    public void TestGetHeatRange() throws Exception {
        Graph graph = new Graph();
        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        NodeAttribute<Double> heatValues = graph.newNodeAttribute(StdAttribute.nodeHeat, 0.0);
        heatValues.set(a, 10.0);
        heatValues.set(b, 30.0);
        heatValues.set(c, 70.0);

        assertThat(Whitebox.<Double[]>invokeMethod(HeatMap.class, "getHeatRange", graph, null, null), is(new Double[]{10.0, 70.0}));
        assertThat(Whitebox.<Double[]>invokeMethod(HeatMap.class, "getHeatRange", graph, 5.0, null), is(new Double[]{5.0, 70.0}));
        assertThat(Whitebox.<Double[]>invokeMethod(HeatMap.class, "getHeatRange", graph, null, 100.0), is(new Double[]{10.0, 100.0}));
        assertThat(Whitebox.<Double[]>invokeMethod(HeatMap.class, "getHeatRange", graph, 0.0, 100.0), is(new Double[]{0.0, 100.0}));
        assertThat(Whitebox.<Double[]>invokeMethod(HeatMap.class, "getHeatRange", graph, null, 50.0), is(new Double[]{10.0, 50.0}));
    }

    @Test
    public void TestGradientInterpolation() throws Exception {
        assertThat(Whitebox.<Integer>invokeMethod(Gradient.class, "interpolate", 0, 100, 0.4), is(40));
        assertThat(Whitebox.<Integer>invokeMethod(Gradient.class, "interpolate", 50, 100, 0.4), is(70));
        assertThat(Whitebox.<Integer>invokeMethod(Gradient.class, "interpolate", 100, 50, 0.4), is(80));
        assertThat(Whitebox.<Integer>invokeMethod(Gradient.class, "interpolate", 99, 100, 0.4), is(99));
    }

    @Test
    public void TestGradientConstructor() {
        Gradient gradient = new Gradient(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE), 200);
        assertThat(gradient.size(), is(200));
        assertThat(gradient, hasItem(Color.RED));
        assertThat(gradient, hasItem(Color.GREEN));
        assertThat(gradient, hasItem(Color.BLUE));
        assertThat(gradient.get(0), is(Color.RED));
        assertThat(gradient.get(0.499), is(Color.GREEN));
        assertThat(gradient.get(1), is(Color.BLUE));
    }

}
