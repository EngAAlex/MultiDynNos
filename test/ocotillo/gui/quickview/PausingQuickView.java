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
package ocotillo.gui.quickview;

import javax.swing.JDialog;
import ocotillo.graph.Graph;
import ocotillo.gui.GraphCanvas;

public class PausingQuickView extends JDialog {

    private static final long serialVersionUID = 1L;

    protected PausingQuickView(Graph graph) {
        setTitle("Graph QuickView");
        add(new GraphCanvas(graph));
        setSize(800, 800);
        setModal(true);
    }

    /**
     * Runs a new instance of QuickView to display the given graph in test mode.
     *
     * @param graph the graph to be visualized.
     */
    public static void showNewWindow(final Graph graph) {
        JDialog dialog = new PausingQuickView(graph);
        dialog.setVisible(true);
    }
}
