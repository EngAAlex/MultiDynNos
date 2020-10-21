/**
 * Copyright © 2020 Alessio Arleo
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Parses the rugby twitter data set.
 */
public class RealityMining extends PreloadedGraphParser{

	public static final String dataPath = "data/RealityMining/RMining_redacted.csv";

	private static Duration messageDuration = Duration.ofMinutes(15);
	long halfDuration = messageDuration.dividedBy(2).getSeconds();

	private static HashSet<String> allowedTypes = new HashSet<String>(Arrays.asList("voice call"/*, "short message"*/));

	/**
	 * Produces the dynamic dataset for this data.
	 *
	 * @param mode the desired mode.
	 * @return the dynamic dataset.
	 */
	public DyDataSet parse(Mode mode) throws URISyntaxException {
		//File file = new File("data/RealityMining/RMining_redacted.csv");

		int eventsProcessed = 0;
		DyGraph graph = new DyGraph();
		long minEpoch = Long.MAX_VALUE;
		long maxEpoch = Long.MIN_VALUE;

		try {
			InputStream fileStream = RealityMining.class.getClassLoader().getResourceAsStream(dataPath);    	
			List<String> lines = ParserTools.readFileLinesFromStream(fileStream);
			
			DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
			DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
			DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
			DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);
			DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);
			DyEdgeAttribute<Color> edgeColor = graph.edgeAttribute(StdAttribute.color);

			Map<String, Node> nodeMap = new HashMap<>();
			for (int i = 1; i < lines.size(); i++) {
				String line = lines.get(i);
				String[] tokens = line.split(",");

				String description = tokens[5];

				if(!allowedTypes.contains(description.toLowerCase()))
					continue;

				String hashNum = tokens[8];

				if(hashNum.equals("NaN"))
					continue;

				String idSource; 
				String idTarget;

				String index = tokens[9];
				String direction = tokens[6];
				if(direction.toLowerCase().equals("outgoing")) {
					idSource = index;
					idTarget = hashNum;
				}else {
					idSource = index;
					idTarget = hashNum;            	
				}
				long epoch = Long.parseLong(tokens[2]);
				Duration eventDuration = null;
				if(description.toLowerCase().equals("short message"))
					eventDuration = messageDuration;
				else 
					if(description.toLowerCase().equals("voice call") && Integer.parseInt(tokens[7]) > 0)
						eventDuration = Duration.ofSeconds(Long.parseLong(tokens[7]));
					else
						continue;

				minEpoch = Math.min(minEpoch, epoch);
				maxEpoch = Math.max(maxEpoch, epoch);

				LocalDateTime msgInstant =
						LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);

				if(!nodeMap.containsKey(idSource)) {
					Node node = graph.newNode(idSource.toString());
					presence.set(node, new Evolution<>(false));
					label.set(node, new Evolution<>(idSource));
					position.set(node, new Evolution<>(new Coordinates(0, 0)));
					color.set(node, new Evolution<>(new Color(141, 211, 199)));
					nodeMap.put(idSource, node);
				}
				if(!nodeMap.containsKey(idTarget)) {
					Node node = graph.newNode(idTarget);
					presence.set(node, new Evolution<>(false));
					label.set(node, new Evolution<>(idTarget));
					position.set(node, new Evolution<>(new Coordinates(0, 0)));
					color.set(node, new Evolution<>(new Color(141, 211, 199)));
					nodeMap.put(idTarget, node);
				}
				Node source = nodeMap.get(idSource);
				Node target = nodeMap.get(idTarget);

				Edge edge = graph.betweenEdge(source, target);
				if (edge == null) {
					edge = graph.newEdge(source, target);
					edgePresence.set(edge, new Evolution<>(false));
					edgeColor.set(edge, new Evolution<>(Color.BLACK));
				}

				Interval eventInterval = Interval.newRightClosed(
						msgInstant.toEpochSecond(ZoneOffset.UTC),
						msgInstant.plusSeconds(eventDuration.getSeconds()).toEpochSecond(ZoneOffset.UTC));

				presence.get(source).insert(new FunctionConst<>(eventInterval, true));
				presence.get(target).insert(new FunctionConst<>(eventInterval, true));
				edgePresence.get(edge).insert(new FunctionConst<>(eventInterval, true));

				eventsProcessed++;

				if(eventsProcessed > 28000) {
					fileStream.close();
					break;
				}
			}				
		} catch (IOException e) {
			System.out.println("Error while reading stream!");
			throw new URISyntaxException(dataPath, "Stream reading error");
		} catch (Exception e) {
			System.out.println("General Error while reading stream!");
			throw new URISyntaxException(dataPath, e.getMessage());			
		}

		LocalDateTime minEpochDT =
				LocalDateTime.ofInstant(Instant.ofEpochSecond(minEpoch), ZoneOffset.UTC);

		LocalDateTime maxEpochDT =
				LocalDateTime.ofInstant(Instant.ofEpochSecond(maxEpoch), ZoneOffset.UTC);

		long removedNodes = 0;
		for(Node n : graph.nodes())
			if(graph.outEdges(n).size() == 0 && graph.inEdges(n).size() == 0) {
				graph.remove(n);
				removedNodes++;
			}
		System.out.println("Removed " + removedNodes + " isolated nodes"); 

		double startTime = minEpochDT.toEpochSecond(ZoneOffset.UTC);
		double endTime = maxEpochDT.toEpochSecond(ZoneOffset.UTC);
		Commons.scatterNodes(graph, 100);
		Commons.mergeAndColor(graph, startTime - messageDuration.getSeconds(), endTime + messageDuration.getSeconds(), mode, new Color(141, 211, 199), Color.BLACK, messageDuration.getSeconds()/2);

		System.out.println("Parsing done! Processed " + eventsProcessed + " events");

		return new DyDataSet(
				graph,
				1.0 / Duration.ofHours(4).getSeconds(),
				Interval.newClosed(
						startTime,
						endTime),
				eventsProcessed);

	}
}
