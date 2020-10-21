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

package ocotillo.multilevel.placement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.multilevel.coarsening.GraphCoarsener;
import ocotillo.multilevel.coarsening.SolarMerger;

public class WeightedBarycenterPlacementStrategy extends MultilevelNodePlacementStrategy {

	protected float opt_distance;
	private final static float OPT_DISTANCE_DEFAULT = 10;

	public WeightedBarycenterPlacementStrategy() {
		super();
		opt_distance = OPT_DISTANCE_DEFAULT;
	}

	public WeightedBarycenterPlacementStrategy(double fuzzyness) {
		super(fuzzyness);
	}

	public WeightedBarycenterPlacementStrategy setOptimalDistance(float optDistance) {
		this.opt_distance = optDistance;
		return this;
	}


	@Override
	protected Coordinates computeNewCoordinates(Node lowerLevelNode, Node upperLevelNode, DyGraph finerLevel,
			Function<Node, Coordinates> getUpperLevelCoords) {
		Coordinates ownUpperClusterCoordinates = getUpperLevelCoords.apply(upperLevelNode); //upperLevelNodeCoordinates.get(upperLevelNode).getLastValue();
		if(GraphCoarsener.checkNodeIdEquivalence(lowerLevelNode.id(), upperLevelNode.id())) {
			return new Coordinates(ownUpperClusterCoordinates.x() + Math.random()*fuzzyness, ownUpperClusterCoordinates.y() + Math.random()*fuzzyness);
		}else{
			HashMap<String, HashSet<String>> neighborsMap = new HashMap<String, HashSet<String>>();
			Coordinates result = new Coordinates(0.0, 0.0);

			for(Edge e : finerLevel.inOutEdges(lowerLevelNode)) {
				Node otherEnd = e.otherEnd(lowerLevelNode);
				String otherGroupLeader = coarsener.getGroupLeader(otherEnd.id());
				if(otherGroupLeader.equals(upperLevelNode.id()))
					continue;
				else {
					if(!neighborsMap.containsKey(otherGroupLeader)) {
						HashSet<String> set = new HashSet<String>();
						set.add(otherEnd.id());
						neighborsMap.put(otherGroupLeader, set);
					}else
						neighborsMap.get(otherGroupLeader).add(otherEnd.id());
				}		
			}
			//System.out.println("Placing new vertex " + lowerLevelNode.id());
			//System.out.println("\tMy master is: " + upperLevelNode.id() + " " + ownUpperClusterCoordinates);
			
			if(neighborsMap.size() == 0) {
				//System.out.println("\tMy neighbors all belong to the same cluster -- randomizing around my master");
				double angle = Math.random()*(2*Math.PI);
				result.setX(ownUpperClusterCoordinates.x()+Math.cos(angle)*opt_distance);
				result.setY(ownUpperClusterCoordinates.y()+Math.sin(angle)*opt_distance);
			}else{

				float totalSum = 0;
				for(Set<String> set : neighborsMap.values())
					totalSum += set.size();
				for(Entry<String, HashSet<String>> current : neighborsMap.entrySet()) {
					Coordinates otherUpperClusterCoordinates = getUpperLevelCoords.apply(getNodeFromUpperLevel(current.getKey()));
					int currentSize = current.getValue().size();
					//System.out.println("\t\tAnother cluster is: " + otherUpperClusterCoordinates + " with weight " + current.getValue().size());
					result.setX(result.x()+otherUpperClusterCoordinates.x()*currentSize);
					result.setY(result.y()+otherUpperClusterCoordinates.y()*currentSize);
				}

				float ownClusterWeight = getOwnClusterWeight(totalSum, lowerLevelNode, finerLevel);
				//System.out.println("\tMy cluster has a weight of: " + ownClusterWeight);

				//ownClusterWeight += totalSum;

				result.setX(result.x()+ownUpperClusterCoordinates.x()*ownClusterWeight);
				result.setY(result.y()+ownUpperClusterCoordinates.y()*ownClusterWeight);

				totalSum += ownClusterWeight;

				result.setX(result.x()/totalSum);
				result.setY(result.y()/totalSum);
			}
			
			//System.out.println("\tCOMPUTED COORDINATES (no fuzzyness): " + result);

			result.setX(result.x()+Math.random()*fuzzyness*(Math.random()>0.5 ? 1 : -1));
			result.setY(result.y()+Math.random()*fuzzyness*(Math.random()>0.5 ? 1 : -1));
			return result;
		}

	}

	protected float getOwnClusterWeight(float totalSum, Node lowerLevelNode, DyGraph finerLevel) {
		return coarsener.getGroupMembers(coarsener.getGroupLeader(lowerLevelNode.id())).size();
	}

	public static class SolarMergerPlacementStrategy extends WeightedBarycenterPlacementStrategy {

		public SolarMergerPlacementStrategy() {
			super();
		}

		public SolarMergerPlacementStrategy(double fuzzyness) {
			super(fuzzyness);
		}

		@Override
		protected float getOwnClusterWeight(float totalSum, Node lowerLevelNode, DyGraph finerLevel) {
			DyNodeAttribute<Byte> statusAttr = finerLevel.nodeAttribute(SolarMerger.STATUS_NODE_ATTRIBUTE_NAME);
			byte status = statusAttr.get(lowerLevelNode).getDefaultValue();
			switch(status) {
			case SolarMerger.PLANET: return totalSum*.5f;
			case SolarMerger.MOON: return totalSum*0.25f;
			default: return totalSum*.6f;
			}
		}
		
		@Override
		public String getDescription() {
			return "FM3 - Solar Merger Placement Strategy";
		}

	}

	@Override
	public String getDescription() {
		return "GRIP - Weighted Barycenter Strategy";
	}


}
