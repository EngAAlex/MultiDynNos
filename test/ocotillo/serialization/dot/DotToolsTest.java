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

import ocotillo.serialization.dot.DotTools.DotLineType;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class DotToolsTest {

    @Test
    public void detectOpeningLineType() throws Exception {
        List<String> headers = new ArrayList<>();
        headers.add("graph {");
        headers.add("strict graph franco {");
        headers.add("digraph mario {");
        headers.add("strict digraph \"luigi\" {");

        for (String header : headers) {
            assertThat(DotLineType.detect(header), is(DotLineType.opening));
        }
    }

    @Test
    public void detectGlobalLineTypes() throws Exception {
        assertThat(DotLineType.detect("graph"), is(DotLineType.graphGlobal));
        assertThat(DotLineType.detect("node"), is(DotLineType.nodeGlobal));
        assertThat(DotLineType.detect("edge"), is(DotLineType.edgeGlobal));
    }

    @Test
    public void detectNodeLineType() throws Exception {
        List<String> headers = new ArrayList<>();
        headers.add("franco");
        headers.add("§23§");

        for (String header : headers) {
            assertThat(DotLineType.detect(header), is(DotLineType.node));
        }
    }

    @Test
    public void detectEdgeLineType() throws Exception {
        List<String> headers = new ArrayList<>();
        headers.add("mario -- franco");
        headers.add("mario -- franco");
        headers.add("mario -> luigi -- §1§");

        for (String header : headers) {
            assertThat(DotLineType.detect(header), is(DotLineType.edge));
        }
    }

    @Test
    public void detectEmptyLineType() throws Exception {
        List<String> headers = new ArrayList<>();
        headers.add("");
        headers.add("}");

        for (String header : headers) {
            assertThat(DotLineType.detect(header), is(DotLineType.empty));
        }
    }
}
