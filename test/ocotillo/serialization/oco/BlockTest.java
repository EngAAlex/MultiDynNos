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

import ocotillo.serialization.oco.OcoReader.Block;
import ocotillo.serialization.oco.OcoReader.Line;
import ocotillo.serialization.oco.OcoReader.MalformedFileException;
import java.util.LinkedList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class BlockTest {

    @Test
    public void testHeaderInitialization() {
        LinkedList<Line> lines = new LinkedList<>();
        lines.add(new Line("#graph", 3));

        Block block = new Block(lines);
        assertThat(block.header, is("#graph"));
        assertThat(block.headerLn, is(3));

        lines.clear();
        lines.add(new Line("#nodes", 15));
        lines.add(new Line("mario", 18));
        lines.add(new Line("luigi", 19));

        block = new Block(lines);
        assertThat(block.header, is("#nodes"));
        assertThat(block.headerLn, is(15));
    }

    @Test
    public void testAttributeInitializationAndGet() {
        LinkedList<Line> lines = new LinkedList<>();
        lines.add(new Line("#graph", 3));
        lines.add(new Line("@attribute \t myLabel \t myMetric \t myRank", 7));
        lines.add(new Line("@type \t String", 9));
        lines.add(new Line("@default \t foo \t \t 8", 12));

        Block block = new Block(lines);
        assertThat(block.attributeNames, is(new String[]{"@attribute", "myLabel", "myMetric", "myRank"}));
        assertThat(block.attributeNamesLn, is(7));
        assertThat(block.attributeTypes, is(new String[]{"@type", "String"}));
        assertThat(block.attributeTypesLn, is(9));
        assertThat(block.attributeDefaults, is(new String[]{"@default", "foo", "", "8"}));
        assertThat(block.attributeDefaultsLn, is(12));

        assertThat(block.getAttributeName(1), is("myLabel"));
        assertThat(block.getAttributeName(2), is("myMetric"));
        assertThat(block.getAttributeName(3), is("myRank"));

        assertThat(block.getAttributeType(1), is("String"));
        assertThat(block.getAttributeType(2), is(""));
        assertThat(block.getAttributeType(3), is(""));

        assertThat(block.getAttributeDefault(1), is("foo"));
        assertThat(block.getAttributeDefault(2), is(""));
        assertThat(block.getAttributeDefault(3), is("8"));
    }

    @Test
    public void testValuesInitializationAndGet() {
        LinkedList<Line> lines = new LinkedList<>();
        lines.add(new Line("#graph", 3));
        lines.add(new Line("@attribute \t myLabel \t myMetric \t myRank", 7));
        lines.add(new Line("@default \t foo \t \t 8", 12));
        lines.add(new Line("luigi \t greenPlumber \t \t 2", 17));
        lines.add(new Line("mario \t redPlumber \t", 18));

        Block block = new Block(lines);
        assertThat(block.values[0], is(new String[]{"luigi", "greenPlumber", "", "2"}));
        assertThat(block.valuesLn[0], is(17));
        assertThat(block.values[1], is(new String[]{"mario", "redPlumber", "", ""}));
        assertThat(block.valuesLn[1], is(18));

        assertThat(block.getValue(0, 0), is("luigi"));
        assertThat(block.getValue(0, 1), is("greenPlumber"));
        assertThat(block.getValue(0, 2), is(""));
        assertThat(block.getValue(0, 3), is("2"));

        assertThat(block.getValue(1, 0), is("mario"));
        assertThat(block.getValue(1, 1), is("redPlumber"));
        assertThat(block.getValue(1, 2), is(""));
        assertThat(block.getValue(1, 3), is(""));
    }

    @Test(expected = MalformedFileException.class)
    public void testMalformedAttributeLine() {
        LinkedList<Line> lines = new LinkedList<>();
        lines.add(new Line("#graph", 3));
        lines.add(new Line("@attr \t myLabel \t myMetric \t myRank", 7));
        lines.add(new Line("@default \t foo \t \t 8", 12));

        Block block = new Block(lines);
    }

    @Test(expected = MalformedFileException.class)
    public void testEmptyAttributeName() {
        LinkedList<Line> lines = new LinkedList<>();
        lines.add(new Line("#graph", 3));
        lines.add(new Line("@attr \t myLabel \t \t myRank", 7));
        lines.add(new Line("@default \t foo \t \t 8", 12));

        Block block = new Block(lines);
    }

    @Test(expected = MalformedFileException.class)
    public void testInvalidElementId() {
        LinkedList<Line> lines = new LinkedList<>();
        lines.add(new Line("#graph", 3));
        lines.add(new Line("@attr \t myLabel \t \t myRank", 7));
        lines.add(new Line("@default \t foo \t \t 8", 12));
        lines.add(new Line("ab\"c\" \t foo \t \t 8", 16));

        Block block = new Block(lines);
    }
}
