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

import java.awt.Color;
import static ocotillo.geometry.matchers.CoreMatchers.isAlmost;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class YuvColorTest {

    @Test
    public void TestContructorFromRgb() {
        YuvColor yuvColor = new YuvColor(new Color(100, 233, 175, 87));
        assertThat(yuvColor.y(), is(187));
        assertThat(yuvColor.u(), is(121));
        assertThat(yuvColor.v(), is(66));
        assertThat(yuvColor.alpha(), is(87));
    }

    @Test
    public void TestDoubleConversion() {
        Color color = new YuvColor(new Color(100, 233, 175, 87)).toRgb();
        assertThat(color.getRed(), is(100));
        assertThat(color.getGreen(), is(233));
        assertThat(color.getBlue(), is(175));
        assertThat(color.getAlpha(), is(87));
    }

    @Test
    public void TestPlus() {
        YuvColor colorA = new YuvColor(0.1, 0.2, 0.3, 0.4);
        YuvColor colorB = new YuvColor(0.3, 0.5, 0.7, 0.11);
        YuvColor result = colorA.plus(colorB);
        assertThat(result.yFloat(), isAlmost(0.4));
        assertThat(result.uFloat(), isAlmost(0.7));
        assertThat(result.vFloat(), isAlmost(1.0));
        assertThat(result.alphaFloat(), isAlmost(0.51));
    }

    @Test
    public void TestMinus() {
        YuvColor colorA = new YuvColor(0.1, 0.2, 0.3, 0.4);
        YuvColor colorB = new YuvColor(0.3, 0.5, 0.7, 0.11);
        YuvColor result = colorA.minus(colorB);
        assertThat(result.yFloat(), isAlmost(-0.2));
        assertThat(result.uFloat(), isAlmost(-0.3));
        assertThat(result.vFloat(), isAlmost(-0.4));
        assertThat(result.alphaFloat(), isAlmost(0.29));
    }

    @Test
    public void TestTimes() {
        YuvColor colorA = new YuvColor(0.1, 0.2, 0.3, 0.4);
        YuvColor result = colorA.times(2.0);
        assertThat(result.yFloat(), isAlmost(0.2));
        assertThat(result.uFloat(), isAlmost(0.4));
        assertThat(result.vFloat(), isAlmost(0.6));
        assertThat(result.alphaFloat(), isAlmost(0.8));
    }

    @Test
    public void TestDivided() {
        YuvColor colorA = new YuvColor(0.1, 0.2, 0.3, 0.4);
        YuvColor result = colorA.divided(2.0);
        assertThat(result.yFloat(), isAlmost(0.05));
        assertThat(result.uFloat(), isAlmost(0.1));
        assertThat(result.vFloat(), isAlmost(0.15));
        assertThat(result.alphaFloat(), isAlmost(0.2));
    }
}
