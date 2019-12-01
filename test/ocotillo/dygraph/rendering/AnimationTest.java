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
package ocotillo.dygraph.rendering;

import java.time.Duration;
import ocotillo.geometry.Interval;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Test for the Evolution class.
 */
public class AnimationTest {

    @Test
    public void testConstruction() {
        Animation animation = new Animation(Interval.newClosed(0, 10), Duration.ofSeconds(11), 1);
        assertThat(animation.playedInterval(), is(Interval.newClosed(0, 10)));
        assertThat(animation.duration(), is(Duration.ofSeconds(11)));
        assertThat(animation.framesPerSecond(), is(1));
    }

    @Test
    public void testClosedInterval() {
        Animation animation = new Animation(Interval.newClosed(0, 10), Duration.ofSeconds(11), 1);
        assertThat(animation.frames(), contains(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0));
    }

    @Test
    public void testLeftClosedInterval() {
        Animation animation = new Animation(Interval.newLeftClosed(0, 10), Duration.ofSeconds(10), 1);
        assertThat(animation.frames(), contains(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));
    }

    @Test
    public void testRightClosedInterval() {
        Animation animation = new Animation(Interval.newRightClosed(0, 10), Duration.ofSeconds(10), 1);
        assertThat(animation.frames(), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0));
    }

    @Test
    public void testOpenInterval() {
        Animation animation = new Animation(Interval.newOpen(0, 10), Duration.ofSeconds(9), 1);
        assertThat(animation.frames(), contains(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));
    }
}
