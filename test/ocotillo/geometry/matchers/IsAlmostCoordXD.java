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
package ocotillo.geometry.matchers;

import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

public class IsAlmostCoordXD extends TypeSafeMatcher<Coordinates> {

    private final Coordinates expected;

    public IsAlmostCoordXD(Coordinates expected) {
        this.expected = expected;
    }

    @Override
    public boolean matchesSafely(Coordinates actual) {
        return Geom.eXD.almostEqual(actual, expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("should be almost equal to ").appendValue(expected);
    }

    @Factory
    public static IsAlmostCoordXD isAlmost(Coordinates expected) {
        return new IsAlmostCoordXD(expected);
    }
}
