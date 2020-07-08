package ocotillo.samples.parsers;

import java.awt.Color;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;

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

public class RampInfectionMap {
	
	/**
	 * Implements a node in the infection map.  It has an id and a time when introduced into the data set
	 * @author archam
	 *
	 */
	public static class RampNode {
		public final int ID;
		public final int TIME;
		
		/**
		 * Constructs a ramp node from a formated string id(time)
		 * @param formatStr - the formatted string
		 */
		public RampNode (String formatStr) {
			String[] nodeAndTime = formatStr.split("\\(|\\)");
			this.ID = Integer.parseInt(nodeAndTime[0]);
			this.TIME = Integer.parseInt(nodeAndTime[1]);
		}
		
	}
	
	public static class RampEdge {
		public final int SRC;
		public final int TGT;
		public final int TIME;
		
		/**
		 * Constructs a ramp edge from two ramp nodes at the proper time
		 */
		public RampEdge (RampNode src, RampNode tgt) {
			this.SRC = src.ID;
			this.TGT = tgt.ID;
			this.TIME = tgt.TIME;
		}
		
		public int compareTo(RampEdge o) {
			if (this.TIME < o.TIME)
				return -1;
			else if (this.TIME > o.TIME) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}
	
	/**
	 * Parses a set of ramp node children [child1, child2, ... childn ]
	 */
	public static RampNode[] parseRampChildren (String childList) {
		childList = childList.replaceAll("\\[", "").replaceAll("\\]","");
		String[] strList = childList.split(",");
		RampNode[] children = new RampNode[strList.length];
		for (int i = 0; i < strList.length; i++) {
			RampNode n = new RampNode (strList[i]);
			children[i] = n;
		}
		return children;
	}
	
	private static final boolean EDGE_PERSIST = true;
	
	 /**
     * Produces the dynamic dataset for this data.
     *
     * @param mode the desired mode.
     * @return the dynamic dataset.
     */
    public static DyDataSet parse(Mode mode) {
    	
    	File file = new File("data/RampInfectionMap/infectionMap_NoRandom.txt");
    	
    	DyGraph graph = new DyGraph();
        DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);
        DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Color> edgeColor = graph.edgeAttribute(StdAttribute.color);
        
        int numEvents = 0;
        List<String> lines = ParserTools.readFileLines(file);
        ArrayList<RampEdge> infectionEvents = new ArrayList<RampEdge>(3*lines.size());
        HashMap<String, RampNode> idToRampNode = new HashMap<String, RampNode> ();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.equals("")) continue;
            line = line.replaceAll(" ", "");
            char firstChar = line.charAt(0);
            boolean isDigit = (firstChar >= '0' && firstChar <= '9');
            boolean isDash = firstChar == '-';
            String[] branch = line.split("->");
            
            //starts a new tree
            if (isDigit) {
            	RampNode src = new RampNode (branch[0]);
            	idToRampNode.put(new Integer (src.ID).toString(), src);
            	RampNode[] tgts = parseRampChildren(branch[1]);
            	for (int j = 0; j < tgts.length; j++) {
            		infectionEvents.add (new RampEdge(src, tgts[j]));
            		idToRampNode.put(new Integer (tgts[j].ID).toString(), tgts[j]);
            		numEvents++;
            	}
            }
            else if (isDash) {
            	//continues a branch of a tree
            	RampNode src = new RampNode (branch[1]);
            	idToRampNode.put(new Integer (src.ID).toString(), src);
            	RampNode[] tgts = parseRampChildren(branch[2]);
            	for (int j = 0; j < tgts.length; j++) {
            		infectionEvents.add(new RampEdge(src, tgts[j]));
            		idToRampNode.put(new Integer (tgts[j].ID).toString(), tgts[j]);
            		numEvents++;
            	}
            }
            else {
            	System.err.println ("Syntax error in the passed file.");
            	System.err.println ("Line:\"" + line + "\"");
            	System.exit(0);
            }
        }
        
        //sort with nulls at the end of the array
        Collections.sort(infectionEvents, new Comparator<RampEdge>() {
            @Override
            public int compare(RampEdge o1, RampEdge o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2);
            }});
        
        Map<String, Node> nodeMap = new HashMap<>();
        int curEvent = 0;
        final int infectionDuration = 2;
        final double halfDuration = infectionDuration/2.0;
        final double fracOfTotal = 1.0 / numEvents;
        double startTime = Double.POSITIVE_INFINITY;
        double endTime = Double.NEGATIVE_INFINITY;
        while (curEvent < numEvents) {
        	String srcNode = (new Integer (infectionEvents.get(curEvent).SRC)).toString();
        	String tgtNode = (new Integer (infectionEvents.get(curEvent).TGT)).toString();
        	if(!nodeMap.containsKey(srcNode)) {
        		Node node = graph.newNode(srcNode);
        		presence.set(node, new Evolution<>(false));
        		label.set(node, new Evolution<>(srcNode));
        		position.set(node, new Evolution<>(new Coordinates(0, 0)));
        		color.set(node, new Evolution<>(new Color(141, 211, 199)));
        		nodeMap.put(srcNode, node);
        	}
        	if (!nodeMap.containsKey(tgtNode)) {
        		Node node = graph.newNode(tgtNode);
        		presence.set(node, new Evolution<>(false));
        		label.set(node, new Evolution<>(tgtNode));
        		position.set(node, new Evolution<>(new Coordinates(0, 0)));
        		color.set(node, new Evolution<>(new Color(141, 211, 199)));
        		nodeMap.put(tgtNode, node);
        	}
        	Node source = nodeMap.get(srcNode);
            Node target = nodeMap.get(tgtNode);
            
            Edge edge = graph.betweenEdge(source, target);
            if (edge == null) {
                edge = graph.newEdge(source, target);
                edgePresence.set(edge, new Evolution<>(false));
                edgeColor.set(edge, new Evolution<>(Color.BLACK));
            }
            
            double minEdgePresence = infectionEvents.get(curEvent).TIME - halfDuration;
            double maxEdgePresence = infectionEvents.get(curEvent).TIME + halfDuration;
            double minSrcNodePresence = idToRampNode.get(srcNode).TIME;
            double minTgtNodePresence = idToRampNode.get(tgtNode).TIME;
       
            
            startTime = Math.min(startTime, minSrcNodePresence);
            startTime = Math.min(startTime, minTgtNodePresence);
            startTime = Math.min(startTime, minEdgePresence);
        	endTime = Math.max(endTime, maxEdgePresence);
            
            if (!EDGE_PERSIST) {
            	Interval srcInterval = Interval.newRightClosed(minSrcNodePresence, maxEdgePresence);
            	Interval tgtInterval = Interval.newRightClosed(minTgtNodePresence, maxEdgePresence);
            	Interval infectionInterval = Interval.newRightClosed(minEdgePresence, maxEdgePresence);

            	presence.get(source).insert(new FunctionConst<>(srcInterval, true));
            	presence.get(target).insert(new FunctionConst<>(tgtInterval, true));
            	edgePresence.get(edge).insert(new FunctionConst<>(infectionInterval, true));
            }
            curEvent++;
        }
        
        if (EDGE_PERSIST) {
        	curEvent = 0;
        	while (curEvent < numEvents) {
        		String srcNode = (new Integer (infectionEvents.get(curEvent).SRC)).toString();
            	String tgtNode = (new Integer (infectionEvents.get(curEvent).TGT)).toString();
            	Node source = nodeMap.get(srcNode);
                Node target = nodeMap.get(tgtNode);
            	Edge edge = graph.betweenEdge(source, target);
            	
            	double minSrcNodePresence = idToRampNode.get(srcNode).TIME;
                double minTgtNodePresence = idToRampNode.get(tgtNode).TIME;
                
            	Interval srcInterval = Interval.newRightClosed(minSrcNodePresence, endTime + halfDuration);
            	Interval tgtInterval = Interval.newRightClosed(minTgtNodePresence, endTime + halfDuration);
                double minEdgePresence = infectionEvents.get(curEvent).TIME - halfDuration;
                
                Interval infectionInterval = Interval.newRightClosed(minEdgePresence, endTime + halfDuration);
                presence.get(source).insert(new FunctionConst<>(srcInterval, true));
            	presence.get(target).insert(new FunctionConst<>(tgtInterval, true));
            	edgePresence.get(edge).insert(new FunctionConst<>(infectionInterval, true));
            	curEvent++;
        	}
        }
        
        double tau = infectionDuration/endTime;
        System.out.println ("Start, end, tau: " + startTime + " " + endTime + " " + tau);
        Commons.scatterNodes(graph, 100);
        Commons.mergeAndColor(graph, startTime, endTime, mode, new Color(141, 211, 199), Color.BLACK, halfDuration);
        return new DyDataSet(graph, tau, Interval.newClosed(startTime, endTime));
        
    }

}
