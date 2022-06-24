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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;

import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionConst;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.coarsening.GraphCoarsener;

public abstract class MultilevelNodePlacementStrategy {

	protected GraphCoarsener coarsener;
	private DyGraph currentUpperLevelGraph;
	protected double fuzzyness;
	private boolean bendTransfer = false;
	
	protected final double FUZZYNESS_DEFAULT = 0.05d;
	
	protected final Function<Evolution<Coordinates>, Coordinates> extractLastValue = (Evolution<Coordinates> evc) -> {
		Coordinates candidates = evc.getLastValue();
		if(Double.isNaN(candidates.x()) || Double.isNaN(candidates.y()))
			candidates = evc.getDefaultValue();
		return candidates;
		};
		
	protected final Function<Evolution<Coordinates>, Coordinates> extractFirstValue = (Evolution<Coordinates> evc) -> {
		Coordinates candidates = evc.getFirstValue();
		if(Double.isNaN(candidates.x()) || Double.isNaN(candidates.y()))
			candidates = evc.getDefaultValue();
		return candidates;
		};		

	public MultilevelNodePlacementStrategy(boolean bendTransferOption) {
		fuzzyness = FUZZYNESS_DEFAULT;
		bendTransfer = bendTransferOption;
	}
	
	public MultilevelNodePlacementStrategy(boolean bendTransferOption, double fuzzyness) {
		this.fuzzyness = fuzzyness;
	}
	
	public void setCoarsener(GraphCoarsener coarsener) {
		this.coarsener = coarsener;
	}

	protected Node getNodeFromUpperLevel(String id) {
		return currentUpperLevelGraph.getNode(id);
	}
	
//	protected Coordinates getStaticUpperLevelCoordinatesOfNode(Node n) {
//		return getUpperLevelCoordinatesOfNode(n, false, null);
//	}
		
	protected Coordinates getUpperLevelCoordinatesOfNode(Node n) {
		return getUpperLevelCoordinatesOfNode(n, extractLastValue);
	}
	
	/*protected Coordinates getUpperLevelCoordinatesOfNode(Node n, Function<Evolution<Coordinates>, Coordinates> extractFromEvolution) {
		return getUpperLevelCoordinatesOfNode(n, extractFromEvolution);
	}*/

	protected Coordinates getUpperLevelCoordinatesOfNode(Node n,/*, boolean dynamic, */ Function<Evolution<Coordinates>, Coordinates> extractFromEvolution) {
		//if(dynamic) {
			DyNodeAttribute<Coordinates> upperLevelNodeCoordinates = currentUpperLevelGraph.nodeAttribute(StdAttribute.nodePosition);
			return extractFromEvolution.apply(upperLevelNodeCoordinates.get(n));
		//}else{
		//	NodeAttribute<Coordinates> upperLevelCoords = currentStaticGraph.nodeAttribute(StdAttribute.nodePosition);
		//	return upperLevelCoords.get(n);
		//}	
	}
	
	public void transferCoordinatesFromStaticGraph(DyGraph coarsestLevel, Graph staticGraph) {
		NodeAttribute<Coordinates> staticGraphCoordinates = staticGraph.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Coordinates> coarsestLevelCoordinates = coarsestLevel.nodeAttribute(StdAttribute.nodePosition);

		for(Node n : staticGraph.nodes()) {				
			coarsestLevelCoordinates.set(coarsestLevel.getNode(n.id()), 
					new Evolution<Coordinates>(staticGraphCoordinates.get(n)));
		}
	}

	public void placeVertices(DyGraph finerLevel, DyGraph upperLevel) {
		if(bendTransfer)
			placeWithBendTransfer(finerLevel, upperLevel);
		else
			place(finerLevel, upperLevel);
	}
	
	private void place(DyGraph finerLevel, DyGraph upperLevel) {
		DyNodeAttribute<Coordinates> finerLevelNodeCoordinates = finerLevel.nodeAttribute(StdAttribute.nodePosition);		
		this.currentUpperLevelGraph = upperLevel;
		for(Node n : upperLevel.nodes()) {				
			for(String id : coarsener.getGroupMembers(n.id())) {
				Node lowerLevelNode = finerLevel.getNode(id);
				finerLevelNodeCoordinates.set(lowerLevelNode, new Evolution<Coordinates>(
						computeNewCoordinates(lowerLevelNode, n, finerLevel, (Node node) -> getUpperLevelCoordinatesOfNode(node))));
			}
		}
	}
	
	private void placeWithBendTransfer(DyGraph finerLevel, DyGraph upperLevel) {
		
		DyNodeAttribute<Coordinates> upperLevelNodeCoordinates = upperLevel.nodeAttribute(StdAttribute.nodePosition);
		DyNodeAttribute<Coordinates> finerLevelNodeCoordinates = finerLevel.nodeAttribute(StdAttribute.nodePosition);		
		this.currentUpperLevelGraph = upperLevel;
		for(Node upperLevelNode : upperLevel.nodes()) {	
			LinkedList<Coordinates> deltas = new LinkedList<Coordinates>();
			deltas.add(new Coordinates(0.0d, 0.0d));
			Evolution<Coordinates> currEv = upperLevelNodeCoordinates.get(upperLevelNode);
			
			int ind = 0;
			//for(ocotillo.dygraph.Function<Coordinates> i : currEv.getAllIntervals())
			Collection<ocotillo.dygraph.Function<Coordinates>> allIntv = currEv.getAllIntervals();
			
			if(allIntv.size() > 0) {
				Iterator<ocotillo.dygraph.Function<Coordinates>> it = allIntv.iterator();
				ocotillo.dygraph.Function<Coordinates> currInterval, lastInterval = it.next();
				currInterval = lastInterval;
				while(it.hasNext() || ind%2 == 0){
					if(ind%2 > 0)
						currInterval = it.next();
					
					Coordinates rightValue = ind%2 == 0 ? currInterval.rightValue() : currInterval.leftValue();
					Coordinates leftValue = ind%2 == 0 ? currInterval.leftValue() : lastInterval.rightValue();
					
					if(Double.isNaN(rightValue.x()) || Double.isNaN(rightValue.y()))
						rightValue = currEv.getDefaultValue();
					if(Double.isNaN(leftValue.x()) || Double.isNaN(leftValue.y()))
						leftValue = currEv.getDefaultValue();
					
					deltas.add(new Coordinates(rightValue.x() -  leftValue.x(), rightValue.y() - leftValue.y()));
					lastInterval = currInterval;
					ind++;
				}
			}
			for(String id : coarsener.getGroupMembers(upperLevelNode.id())) {
				Node lowerLevelNode = finerLevel.getNode(id);
				if(GraphCoarsener.checkNodeIdEquivalence(lowerLevelNode.id(), upperLevelNode.id())) { 
					Evolution<Coordinates> copy = new Evolution<Coordinates>(currEv.getDefaultValue());
					copy.copyFrom(currEv);
					finerLevelNodeCoordinates.set(lowerLevelNode, copy);
				} else {
					Coordinates newDefCoordinates = computeNewCoordinates(lowerLevelNode, upperLevelNode, finerLevel, (Node node) -> getUpperLevelCoordinatesOfNode(node, extractLastValue));
					Evolution<Coordinates> copy = new Evolution<Coordinates>(newDefCoordinates);
					int index = 0;
					Coordinates left = newDefCoordinates, right = newDefCoordinates;
					for(ocotillo.dygraph.Function<Coordinates> i : allIntv) {
						ocotillo.dygraph.FunctionRect.Coordinates t = (ocotillo.dygraph.FunctionRect.Coordinates) i;
						left.plusIP(deltas.get(index));
						right.plusIP(deltas.get(++index));
						copy.insert(
								t.copyTypeAndInterval(new Coordinates(left), new Coordinates(right))
								);						
						index++;
					}
					finerLevelNodeCoordinates.set(lowerLevelNode, copy);
				}
			}
		}
	}
	
	protected abstract Coordinates computeNewCoordinates(Node lowerLevelNode, Node upperLevelNode, DyGraph finerLevel, Function<Node, Coordinates> getUpperLevelCoords);

	public static class IdentityNodePlacement extends MultilevelNodePlacementStrategy {
		
		public IdentityNodePlacement(boolean bendTransferOption) {
			super(bendTransferOption);
		}
		
		public IdentityNodePlacement(boolean bendTransferOption, double fuzzyness) {
			super(bendTransferOption, fuzzyness);
		}

		@Override
		protected Coordinates computeNewCoordinates(Node lowerLevelNode, Node upperLevelNode, DyGraph finerLevel, Function<Node, Coordinates> getUpperLevelCoords) {
		
			Coordinates upperClusterCoordinates = getUpperLevelCoords.apply(upperLevelNode); //upperLevelNodeCoordinates.get(upperLevelNode).getLastValue();
			
			return new Coordinates(upperClusterCoordinates.x() + Math.random()*fuzzyness, upperClusterCoordinates.y() + Math.random()*fuzzyness);
		}

		@Override
		public String getDescription() {
			return "Identity";
		}
	}

	public abstract String getDescription();
}

