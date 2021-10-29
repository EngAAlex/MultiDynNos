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
package ocotillo.dygraph.extra;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.FunctionRect;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.multilevel.flattener.DyGraphFlattener.StaticSumPresenceFlattener;

/**
 * Transforms a continuous dynamic graph into a discrete one.
 */
public class DyGraphDiscretiser {
	
//	private static final double FIXED_DURATION = 1.0 / Duration.ofDays(7).getSeconds();

//	public static List<Double> getEqualDiscretizationWithFixedDuration(DyGraph original, int timeslices) {
//		DiscretisationData discretizedData = new DiscretisationData(original);
//		List<Interval> sortedIntervals = discretizedData.sortedEdgeIntervalList(new Interval.CompareIntervalsByLeftBound());
//		float sliceSize = sortedIntervals.size()/(float)timeslices;		
//		int errorCycle = Math.round(1/Math.abs(sliceSize - Math.round(sliceSize)));
//		int eventsPerSlice = Math.round(sliceSize);
//		
//		List<Double> snapTimes = new ArrayList<Double>();		
//		int currentSliceSize = 0;
//		int index = -1;
//		double currentMaxSnap = sortedIntervals.get(0).leftBound();
//		//int currentlyVisibleEdges = 0;
//		snapTimes.add(sortedIntervals.get(0).leftBound());
//		for(Interval t : sortedIntervals) {
//			index++;
//			if(currentSliceSize == 0) {
//				//snapTimes.add(t.leftBound());
//				if(index > 0)
//					snapTimes.add(t.leftBound() + FIXED_DURATION);
//				currentMaxSnap = t.leftBound() + FIXED_DURATION; //t.rightBound();
//				currentSliceSize++;
//				continue;
//			}
//			currentMaxSnap = Math.max(currentMaxSnap, t.leftBound());//t.rightBound());
//			if(currentSliceSize < eventsPerSlice) {				
//				if(++currentSliceSize == eventsPerSlice) {
//					if(index%errorCycle != 0)						
//						currentSliceSize = 0;				}				
//			}else
//				currentSliceSize = 0;
//				
//		}
//		Collections.sort(snapTimes);
//		return snapTimes;		
//        //return discretiseWithSnapTimes(original, snapTimes);	
//	}
	
    /**
     * Discretise a continuous dynamic graph by providing the snapshot times.
     * For each snapshot time, a new time step is created by flattening the
     * events from the previous to the current time. For the first time step,
     * the average gap between steps is used to define the input interval.
     *
     * @param original the continuous dynamic graph.
     * @param snapshotTimes the times of the snapshots.
     * @return the discrete graph.
     */
    public static DyGraph discretiseWithSnapTimes(DyGraph original, List<Double> snapshotTimes) {
        List<Interval> intervals = new ArrayList<>();
        String times = "";
        for (int i = 0; i < snapshotTimes.size(); i++) {
            double leftBound = i > 0
                    ? (snapshotTimes.get(i - 1) + snapshotTimes.get(i)) / 2.0
                    : Double.NEGATIVE_INFINITY;
            double rightBound = i < snapshotTimes.size() - 1
                    ? (snapshotTimes.get(i) + snapshotTimes.get(i + 1)) / 2.0
                    : Double.POSITIVE_INFINITY;
            intervals.add(Interval.newRightClosed(leftBound, rightBound));
            times += snapshotTimes.get(i) + ",";
        }
        DyGraph graph = discretiseWithIntervals(original, intervals);
        graph.newGraphAttribute("SnapTimes", times);
        return graph;
    }

    /**
     * Discretise a continuous dynamic graph by providing the snapshot times and
     * interval radius. For each snapshot time, a new time step is created by
     * flattening the events in the interval with given centre and radius.
     *
     * @param original the continuous dynamic graph.
     * @param snapshotTimes the times of the snapshots.
     * @param intervalRadius the radius of the interval.
     * @return the discrete graph.
     */
    public static DyGraph discretiseWithSnapTimes(DyGraph original, List<Double> snapshotTimes, double intervalRadius) {
        List<Interval> intervals = new ArrayList<>();
        String times = "";
        for (Double center : snapshotTimes) {
            double leftBound = center - intervalRadius;
            double rightBound = center + intervalRadius;
            intervals.add(Interval.newRightClosed(leftBound, rightBound));
            times += center + ",";
        }
        DyGraph graph = discretiseWithIntervals(original, intervals);
        graph.newGraphAttribute("SnapTimes", times);
        return graph;
    }

    /**
     * Discretise a continuous dynamic graph by providing the intervals to
     * flatten. A time step is created by flattening the events in the interval.
     *
     * @param original the continuous dynamic graph.
     * @param intervals the intervals.
     * @return the discrete graph.
     */
    public static DyGraph discretiseWithIntervals(DyGraph original, List<Interval> intervals) {
        DiscretisationData data = new DiscretisationData(original);
        for (int i = 0; i < intervals.size(); i++) {
            double leftBound = i > 0
                    ? (intervals.get(i - 1).rightBound() + intervals.get(i).leftBound()) / 2.0
                    : intervals.get(i).leftBound() - (intervals.get(i).rightBound() - intervals.get(i).leftBound()) * 0.2;
            double rightBound = i < intervals.size() - 1
                    ? (intervals.get(i).rightBound() + intervals.get(i + 1).leftBound()) / 2.0
                    : intervals.get(i).rightBound() + (intervals.get(i).rightBound() - intervals.get(i).leftBound()) * 0.2;
            Interval outputInterval = Interval.newRightClosed(leftBound, rightBound);
//            System.out.print(i + ";");
            applyBlockAttributes(data, intervals.get(i), outputInterval);
        }
        return data.discrete;
    }    

    /**
     * Applies the time step to the discrete graph.
     *
     * @param data the discretisation data.
     * @param inputInterval the interval containing the information to flatten.
     * @param outputInterval the interval describing the time step in the output
     * graph.
     */
    private static void applyBlockAttributes(DiscretisationData data, Interval inputInterval, Interval outputInterval) {
//        int presentNodes = 0;
//        int presentEdges = 0;

    	for (Node node : data.original.nodes()) {
            if (data.isPresentInInterval(node, inputInterval)) {
                data.discreteNodePresence.get(node).insert(new FunctionConst<>(outputInterval, true));
//                presentNodes++;
            }
            data.discreteNodePositions.get(node).insert(new FunctionRect.Coordinates(outputInterval,
                    data.originalNodePositions.get(node).valueAt(outputInterval.leftBound()),
                    data.originalNodePositions.get(node).valueAt(outputInterval.rightBound()),
                    Interpolation.Std.linear));
            data.discreteNodeLabels.get(node).insert(new FunctionConst<>(outputInterval,
                    data.originalNodeLabels.get(node).valueAt(outputInterval.rightBound())));
            data.discreteNodeLabelSizes.get(node).insert(new FunctionConst<>(outputInterval,
                    data.originalNodeLabelSizes.get(node).valueAt(outputInterval.rightBound())));
        }
        for (Edge edge : data.original.edges()) {
            if (data.isPresentInInterval(edge, inputInterval)) {
            	data.discreteEdgePresence.get(edge).insert(new FunctionConst<>(outputInterval, true));
//                presentEdges++;
            }
        }
        //System.out.println(presentNodes + ";" + presentEdges);
    }
    
    private void updateVisibleEdgesList(List<Interval> visibleEdges, double snapTime) {
    	visibleEdges.removeIf(new Predicate<Interval>() {
			@Override
			public boolean test(Interval t) {
				return t.rightBound() < snapTime;
			}
		});
    		
    }

    /**
     * A collection of information useful to flatten a contiguous dynamic graph.
     */
    private static class DiscretisationData {

        private final DyGraph original;
        private final DyGraph discrete;
        private final DyNodeAttribute<Boolean> originalNodePresence;
        private final DyEdgeAttribute<Boolean> originalEdgePresence;
        private final DyNodeAttribute<Boolean> discreteNodePresence;
        private final DyEdgeAttribute<Boolean> discreteEdgePresence;
        private final Map<Node, List<Interval>> simplifiedNodePresence = new HashMap<>();
        private final Map<Edge, List<Interval>> simplifiedEdgePresence = new HashMap<>();

        private final DyNodeAttribute<Coordinates> originalNodePositions;
        private final DyNodeAttribute<Coordinates> discreteNodePositions;
        private final DyNodeAttribute<String> originalNodeLabels;
        private final DyNodeAttribute<String> discreteNodeLabels;
        private final DyNodeAttribute<Double> originalNodeLabelSizes;
        private final DyNodeAttribute<Double> discreteNodeLabelSizes;

        /**
         * Recovers the discretisation data from the original graph.
         *
         * @param original the continuous dynamic graph.
         */
        private DiscretisationData(DyGraph original) {
            this.original = original;
            this.discrete = new DyGraph();
            this.originalNodePresence = original.nodeAttribute(StdAttribute.dyPresence);
            this.originalEdgePresence = original.edgeAttribute(StdAttribute.dyPresence);
            this.discreteNodePresence = discrete.nodeAttribute(StdAttribute.dyPresence);
            this.discreteEdgePresence = discrete.edgeAttribute(StdAttribute.dyPresence);

            this.originalNodePositions = original.nodeAttribute(StdAttribute.nodePosition);
            this.discreteNodePositions = discrete.nodeAttribute(StdAttribute.nodePosition);
            this.originalNodeLabels = original.nodeAttribute(StdAttribute.label);
            this.discreteNodeLabels = discrete.nodeAttribute(StdAttribute.label);
            this.originalNodeLabelSizes = original.nodeAttribute(StdAttribute.labelScaling);
            this.discreteNodeLabelSizes = discrete.nodeAttribute(StdAttribute.labelScaling);

            for (Node node : original.nodes()) {
                discrete.add(node);
                discreteNodePresence.set(node, new Evolution<>(false));
                simplifiedNodePresence.put(node,
                        EvolutionAnalyser.getIntervalsWithValue(originalNodePresence.get(node), true));
                discreteNodePositions.set(node, new Evolution<>(originalNodePositions.get(node).getDefaultValue()));
                discreteNodeLabels.set(node, new Evolution<>(""));
                discreteNodeLabelSizes.set(node, new Evolution<>(1.0));
            }
            for (Edge edge : original.edges()) {
                discrete.add(edge);
                discreteEdgePresence.set(edge, new Evolution<>(false));
                simplifiedEdgePresence.put(edge,
                        EvolutionAnalyser.getIntervalsWithValue(originalEdgePresence.get(edge), true));
            }
        }

        /**
         * Checks if a node is present in the given interval.
         *
         * @param node the node.
         * @param interval the interval.
         * @return true if the node is ever present in the interval.
         */
        private boolean isPresentInInterval(Node node, Interval interval) {
            for (Interval presence : simplifiedNodePresence.get(node)) {
                if (presence.overlapsWith(interval)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks if an edge is present in the given interval.
         *
         * @param edge the edge.
         * @param interval the interval.
         * @return true if the edge is ever present in the interval.
         */
        private boolean isPresentInInterval(Edge edge, Interval interval) {
            for (Interval presence : simplifiedEdgePresence.get(edge)) {
                if (presence.overlapsWith(interval)) {
                    return true;
                }
            }
            return false;
        }
        
        protected List<Interval> sortedEdgeIntervalList(Comparator<Interval> comparator){      
        	List<Interval> completeList = new ArrayList<Interval>();
        	for(List<Interval> list : simplifiedEdgePresence.values())
        		completeList.addAll(list);
        	Collections.sort(completeList, comparator);
        	return completeList;
        }               
    }
    
    /**
     * Provides the snapshot of the graph at the end of the supplied interval. 
     *
     * @param original the continuous dynamic graph.
     * @param intervals the intervals.
     * @return the discrete graph.
     */
	public static Graph displayWithinInterval(DyGraph drawnGraph, Interval newClosed) {
		DyGraph returnGraph = new DyGraph();
		
//		DyNodeAttribute<Double> nodeWeight = returnGraph.nodeAttribute(StdAttribute.weight);
//		DyEdgeAttribute<Double> edgeWeight = returnGraph.edgeAttribute(StdAttribute.weight);	
//		
		DyNodeAttribute<Boolean> nodePresence = drawnGraph.nodeAttribute(StdAttribute.dyPresence);
		DyEdgeAttribute<Boolean> edgePresence = drawnGraph.edgeAttribute(StdAttribute.dyPresence);
		
		DyNodeAttribute<Coordinates> dyPosition = drawnGraph.nodeAttribute(StdAttribute.nodePosition);
		
		//DiscretisationData discdata = new DiscretisationData(drawnGraph);
		for(Node n : drawnGraph.nodes())
			if(nodePresence.get(n).valueAt(newClosed.rightBound()))
				returnGraph.add(n);
		for(Edge e : drawnGraph.edges())
			if(edgePresence.get(e).valueAt(newClosed.rightBound()))
				returnGraph.add(e);		
		Graph temp = new StaticSumPresenceFlattener().flattenDyGraph(returnGraph);
		
		NodeAttribute<Coordinates> staticPosition = temp.nodeAttribute(StdAttribute.nodePosition);
		
		for(Node n : drawnGraph.nodes()) {
			Evolution<Coordinates> gino = dyPosition.get(n);			
			staticPosition.set(temp.getNode(n.id()), gino.valueAt(newClosed.rightBound()));
		}
		
		return temp;
	}
	
    /**
     * Flatten a dynamic graph within an interval.  
     * This method returns a static graph representing the flattened graph. 
     *
     * @param original the continuous dynamic graph.
     * @param intervals the intervals.
     * @return the discrete graph.
     */
	public static Graph flattenWithinInterval(DyGraph drawnGraph, Interval newClosed) {
		DyGraph returnGraph = new DyGraph();
		
//		DyNodeAttribute<Double> nodeWeight = returnGraph.nodeAttribute(StdAttribute.weight);
//		DyEdgeAttribute<Double> edgeWeight = returnGraph.edgeAttribute(StdAttribute.weight);	
//		
//		DyNodeAttribute<Boolean> nodePresence = drawnGraph.nodeAttribute(StdAttribute.dyPresence);
//		DyEdgeAttribute<Boolean> edgePresence = drawnGraph.edgeAttribute(StdAttribute.dyPresence);
		
		DyNodeAttribute<Coordinates> dyPosition = drawnGraph.nodeAttribute(StdAttribute.nodePosition);
		
		DiscretisationData discdata = new DiscretisationData(drawnGraph);
		for(Node n : drawnGraph.nodes())
			if(discdata.isPresentInInterval(n, newClosed))
				returnGraph.add(n);
		for(Edge e : drawnGraph.edges())
			if(discdata.isPresentInInterval(e, newClosed))
				returnGraph.add(e);		
		Graph temp = new StaticSumPresenceFlattener().flattenDyGraph(returnGraph);
		
		NodeAttribute<Coordinates> staticPosition = temp.nodeAttribute(StdAttribute.nodePosition);
		
		for(Node n : drawnGraph.nodes()) {
			Evolution<Coordinates> gino = dyPosition.get(n);
			staticPosition.set(temp.getNode(n.id()), gino.valueAt(newClosed.rightBound()));
		}
		
		return temp;
	}
}
