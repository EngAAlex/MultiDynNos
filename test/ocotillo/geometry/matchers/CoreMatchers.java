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
import org.hamcrest.Matcher;

public class CoreMatchers {

    public static <T> Matcher<Number> isAlmost(Number value) {
        return ocotillo.geometry.matchers.IsAlmostNumber.isAlmost(value);
    }

    public static <T> Matcher<Coordinates> isAlmost(Coordinates value) {
        return ocotillo.geometry.matchers.IsAlmostCoordXD.isAlmost(value);
    }

}
