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
package ocotillo.serialization.dot;

import ocotillo.serialization.ParserTools.EscapedString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ParserToolTest {

    @Test
    public void EscapedStringConstruction() {
        EscapedString eString = new EscapedString("There are \"five\" rabbits", "\"");
        assertThat(eString.substitutionChar, is("§"));
        assertThat(eString.withSubstitutions, is("There are §0§ rabbits"));
        assertThat(eString.extractedStrings.size(), is(1));
        assertThat(eString.extractedStrings.get(0), is("five"));
        assertThat(eString.revertSubst("§0§", ""), is("five"));
    }
}
