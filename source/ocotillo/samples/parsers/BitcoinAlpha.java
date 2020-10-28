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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class BitcoinAlpha extends PreloadedGraphParser{

	public static final String dataPath = "data/BitcoinAlpha/soc-sign-bitcoinalpha.csv";

	private static Duration messageDuration = Duration.ofDays(1);
	long halfDuration = messageDuration.dividedBy(2).getSeconds();
	
	/**
	 * Produces the dynamic dataset for this data.
	 *
	 * @param mode the desired mode.
	 * @return the dynamic dataset.
	 */
	public DyDataSet parse(Mode mode) throws URISyntaxException {
		//        File file = new File("data/BitcoinAlpha/soc-sign-bitcoinalpha.csv");
		int eventsProcessed = 0;
		DyGraph graph = new DyGraph();
		long minEpoch = Long.MAX_VALUE;
		long maxEpoch = Long.MIN_VALUE;
		try {
			InputStream fileStream = BitcoinAlpha.class.getClassLoader().getResourceAsStream(dataPath);    	
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
				String idSource = tokens[0];
				String idTarget = tokens[1];
				if(Long.parseLong(tokens[2]) < 1)
					continue;
				long epoch = Long.parseLong(tokens[3]);

				minEpoch = Math.min(minEpoch, epoch);
				maxEpoch = Math.max(maxEpoch, epoch);

				LocalDateTime msgInstant =
						LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);

				if(!nodeMap.containsKey(idSource)) {
					Node node = graph.newNode(idSource);
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

				Interval msgInterval = Interval.newRightClosed(
						msgInstant.minusSeconds(halfDuration).toEpochSecond(ZoneOffset.UTC),
						msgInstant.plusSeconds(halfDuration).toEpochSecond(ZoneOffset.UTC));

				presence.get(source).insert(new FunctionConst<>(msgInterval, true));
				presence.get(target).insert(new FunctionConst<>(msgInterval, true));
				edgePresence.get(edge).insert(new FunctionConst<>(msgInterval, true));

				eventsProcessed++;

				if(eventsProcessed > 15000) {
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

		System.out.println("Events Processed: " + eventsProcessed);

		LocalDateTime minEpochDT =
				LocalDateTime.ofInstant(Instant.ofEpochSecond(minEpoch), ZoneOffset.UTC);

		LocalDateTime maxEpochDT =
				LocalDateTime.ofInstant(Instant.ofEpochSecond(maxEpoch), ZoneOffset.UTC);

		double startTime = minEpochDT.toEpochSecond(ZoneOffset.UTC);
		double endTime = maxEpochDT.toEpochSecond(ZoneOffset.UTC);
		Commons.scatterNodes(graph, 100);
		Commons.mergeAndColor(graph, startTime - halfDuration, endTime + halfDuration, mode, new Color(141, 211, 199), Color.BLACK, messageDuration.dividedBy(4).getSeconds());

		return new DyDataSet(
				graph,
				1.0/(Duration.ofDays(5).getSeconds()),
				Interval.newClosed(
						startTime,
						endTime),
				eventsProcessed);

	}
}
