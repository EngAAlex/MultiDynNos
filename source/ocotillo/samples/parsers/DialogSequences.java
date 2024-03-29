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
package ocotillo.samples.parsers;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionConst;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;
import ocotillo.serialization.ParserTools;

/**
 * Parses a dataset of dialog sequences.
 */
public class DialogSequences extends PreloadedGraphParser{

	private final static String dataPath = "data/DialogSequences/Pride_and_Prejudice/chapters.zip";

	private static final double FIXED_DURATION = 1;
	
	private static class DialogDataset {

		List<Dialog> dialogs = new ArrayList<>();
		Set<String> characters = new HashSet<>();
		double startTime = Double.POSITIVE_INFINITY;
		double endTime = Double.NEGATIVE_INFINITY;
	}

	private static class Dialog {

		String source;
		String target;
		double time;
		double nominalDuration;
	}

	/**
	 * Produces the dynamic dataset for this data.
	 *
	 * @param mode the desired mode.
	 * @return the dynamic dataset.
	 */
	public DyDataSet parse(Mode mode) throws URISyntaxException {
		InputStream fileStream = DialogSequences.class.getClassLoader().getResourceAsStream(dataPath);        
		DialogDataset dataset;	
		try {

			if(fileStream == null) //attempt alternative loading method
				fileStream = new FileInputStream(new File(dataPath));
			
			dataset = parseDialogs(new ZipInputStream(fileStream));
			DyDataSet dyDataSet = new DyDataSet(
					parseGraph(dataset, 2, mode),
					FIXED_DURATION,
					Interval.newClosed(dataset.startTime, dataset.endTime));
	        fileStream.close();
	        return dyDataSet;
			
		} catch (IOException e) {
			throw new URISyntaxException(dataPath, "Stream reading error");
		} catch (Exception e) {
			System.out.println("General Error while reading stream!");
			throw new URISyntaxException(dataPath, e.getMessage());			
		}

	}

	/**
	 * Parses the dialog sequence graph.
	 *
	 * @param inputDir the directory with the input files.
	 * @param dialogDuration the factor that encode the duration of a dialog.
	 * One corresponds to the gap between consecutive dialogs.
	 * @param mode the desired mode.
	 * @return the dynamic graph.
	 */
	private static DyGraph parseGraph(DialogDataset dataset, double dialogDuration, Mode mode) {
		DyGraph graph = new DyGraph();
		DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
		DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
		DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);
		DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);
		DyEdgeAttribute<Color> edgeColor = graph.edgeAttribute(StdAttribute.color);

//		DialogDataset dataset = parseDialogs(new ZipInputStream(inputDir));
		Map<String, Node> nodeMap = new HashMap<>();
		for (String character : dataset.characters) {
			Node node = graph.newNode(character);
			presence.set(node, new Evolution<>(false));
			label.set(node, new Evolution<>(character));
			position.set(node, new Evolution<>(new Coordinates(0, 0)));
			color.set(node, new Evolution<>(new Color(141, 211, 199)));
			nodeMap.put(character, node);
		}

		for (Dialog dialog : dataset.dialogs) {
			Node source = nodeMap.get(dialog.source);
			Node target = nodeMap.get(dialog.target);
			Edge edge = graph.betweenEdge(source, target);
			if (edge == null) {
				edge = graph.newEdge(source, target);
				edgePresence.set(edge, new Evolution<>(false));
				edgeColor.set(edge, new Evolution<>(Color.BLACK));
			}

			Interval participantPresence = Interval.newRightClosed(
					dialog.time - dialogDuration * dialog.nominalDuration * 10.0,
					dialog.time + dialogDuration * dialog.nominalDuration * 11.0);
			Interval dialogInterval = Interval.newRightClosed(
					dialog.time,
					dialog.time + dialogDuration * dialog.nominalDuration);

			presence.get(source).insert(new FunctionConst<>(participantPresence, true));
			presence.get(target).insert(new FunctionConst<>(participantPresence, true));
			edgePresence.get(edge).insert(new FunctionConst<>(dialogInterval, true));
		}

		Commons.scatterNodes(graph, 200);
		Commons.mergeAndColor(graph, dataset.startTime, dataset.endTime + 1, mode, new Color(141, 211, 199), Color.BLACK, 0.001);
		return graph;
	}

	/**
	 * Parses the dialogs of the files in the input directory.
	 *
	 * @param inputDir the input directory.
	 * @return the dialog data set.
	 * @throws Exception 
	 */
	private static DialogDataset parseDialogs(ZipInputStream inputStream) throws Exception {
		DialogDataset dataset = new DialogDataset();
		ZipEntry zie = inputStream.getNextEntry();
		while(zie != null) {
			//			System.out.print("\rLoading file " + zie.getName());
			//			for (File fileEntry : inputStream.getNextEntry()) {			
			//				if (fileEntry.getName().toLowerCase().endsWith(".txt")) {
			List<String> fileLines = ParserTools.readFileLinesFromStream(
					new FilterInputStream(inputStream) {
						public void close() throws IOException {
							inputStream.closeEntry();
						}
					}
					);
			List<Dialog> fileDialogs = new ArrayList<>();
			for (String line : fileLines) {
				if (line.contains("\t")) {
					String[] tokens = line.split("\t");
					assert (tokens.length == 2) : "Line not breakable on tab for file: " + zie.getName();
					Dialog dialog = new Dialog();
					dialog.source = tokens[0];
					dialog.target = tokens[1];
					dataset.characters.add(tokens[0]);
					dataset.characters.add(tokens[1]);
					fileDialogs.add(dialog);
				}
			}
			int chapter = Integer.parseInt(zie.getName().replaceAll("[\\D]", ""));
			dataset.startTime = Math.min(dataset.startTime, chapter);
			dataset.endTime = Math.max(dataset.endTime, chapter + 1);
			double order = 0;
			for (Dialog dialog : fileDialogs) {
				dialog.nominalDuration = 1.0 / fileDialogs.size();
				dialog.time = chapter + order * dialog.nominalDuration;
				dataset.dialogs.add(dialog);
				order++;
			}
			//inputStream.closeEntry();
			zie = inputStream.getNextEntry();
			//				}
			//}
		}
		//System.out.println("\nLoading complete");
		return dataset;
	}
}
