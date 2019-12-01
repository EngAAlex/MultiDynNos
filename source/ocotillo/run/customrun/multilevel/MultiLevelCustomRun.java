package ocotillo.run.customrun.multilevel;

import java.util.Iterator;
import java.util.Map.Entry;

import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.run.customrun.CustomRun;

public class MultiLevelCustomRun extends CustomRun {

	protected Graph appearanceGraph;
	
	public static void main(String[] argv) {
		MultiLevelCustomRun mlcr = new MultiLevelCustomRun(argv);
		mlcr.run();
	}

	@Override
	protected void run() {
		DyGraph dyGraph = createDynamicGraph();
        generateAppearanceGraph(dyGraph);		
	}
	
	public MultiLevelCustomRun(String[] argv) {
		super(argv);
	}		

	public void generateAppearanceGraph(DyGraph dygraph) {
		Graph graph = new Graph();
        //NodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
		//DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Double> nodeWeight = graph.nodeAttribute(StdAttribute.weight);
        EdgeAttribute<Double> edgeWeight = graph.edgeAttribute(StdAttribute.weight);	
        
        DyNodeAttribute<Boolean> nodePresence = dygraph.nodeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Boolean> edgePresence = dygraph.edgeAttribute(StdAttribute.dyPresence);
        
        for(Node dyN : dygraph.nodes()) {
        	Node n;
        	if(!graph.hasNode(dyN.id()))
        		n = graph.newNode(dyN.id());
        	else
        		n = graph.getNode(dyN.id());
        	double presence = 0.0;
        	Iterator<Function<Boolean>> it = nodePresence.get(dyN).iterator(); 
        	while(it.hasNext()) {
        		Function<Boolean> f = it.next();
        		Interval currInterval = f.interval();
        		presence += currInterval.rightBound() - currInterval.leftBound(); 
        	}
        	nodeWeight.set(n, presence);
        	System.out.println("Reconstructed Node " + n.id() + " with presence " + presence);
        }

        
        for(Edge dyE : dygraph.edges()) {               
        	Node src = dyE.source();
        	Node tgt = dyE.target();
        	Edge e = dygraph.betweenEdge(src, tgt);        	
        	
        	if(e == null)
        		e = graph.newEdge(src, tgt);

        	double presence = 0.0;
        	Iterator<Function<Boolean>> it = edgePresence.get(dyE).iterator(); 
        	while(it.hasNext()) {
        		Function<Boolean> f = it.next();
        		Interval currInterval = f.interval();
        		presence += currInterval.rightBound() - currInterval.leftBound(); 
        	}
        	edgeWeight.set(e, presence);
        	System.out.println("Reconstructed Edge From " + src.id() + " to " + tgt.id() + " with presence " + presence);
        }
        
        this.appearanceGraph = graph;        
	}
	

}
