package ocotillo.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

public class GMLOutputWriter {

	public static final String INDENT = "\t";

	public static void writeOutput(File f, Graph graph) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(f);
			short indentLevel = 0;
			pw.println(indentString("graph [", indentLevel));
			indentLevel++;
			pw.println(indentString("directed 0", indentLevel));
			writeVerticesOnPrintWriter(graph, pw, indentLevel);
			writeEdgesOnPrintWriter(graph, pw, indentLevel);
			indentLevel--;
			pw.println(indentString("]", indentLevel));
			pw.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected static void writeVerticesOnPrintWriter(Graph graph, PrintWriter pw, short indentLevel){
		Iterator<Node> itN = graph.nodes().iterator();

		NodeAttribute<Coordinates> coordsAttribute = graph.nodeAttribute(StdAttribute.nodePosition);

		while(itN.hasNext()){
			Node current = itN.next();
			String id = current.id();
			//			String label = graph.getVertex(id).getLabel().equals("") ? ""+id : graph.getVertex(id).getLabel();
			pw.println(indentString("node [", indentLevel));
			indentLevel++;
			double[] coords = coordsAttribute.get(graph.getNode(id)).getArray();
			pw.println(indentString("id " + id, indentLevel));
			//			pw.println(indentString("label \"" + label +"\"", indentLevel));			
			pw.println(indentString("graphics [ ", indentLevel));	
			indentLevel++;
			pw.println(indentString("x " + coords[0], indentLevel));
			pw.println(indentString("y " + coords[1], indentLevel));
			pw.println(indentString("w 5", indentLevel));
			pw.println(indentString("h 5", indentLevel));
			indentLevel--;
			pw.println(indentString("]", indentLevel));
			indentLevel--;
			pw.println(indentString("]", indentLevel));
		}
	}

	protected static void writeEdgesOnPrintWriter(Graph graph, PrintWriter pw, short indentLevel){
		Iterator<Edge> edges = graph.edges().iterator();
		while(edges.hasNext()){
			Edge currentEdge = edges.next();
			pw.println(indentString("edge [", indentLevel));
			indentLevel++;
			pw.println(indentString("source " + currentEdge.source(), indentLevel));
			pw.println(indentString("target " + currentEdge.target(), indentLevel));
			pw.println(indentString("graphics [", indentLevel));
			//			indentLevel++;
			//			pw.println(indentString("type \"line\"", indentLevel));
			//			pw.println(indentString("width " + (currentEdge.getWeight() == -1 ? 1 : currentEdge.getWeight() + 1), indentLevel));		
			indentLevel--;
			pw.println(indentString("]", indentLevel));
			indentLevel--;
			pw.println(indentString("]", indentLevel));
		}
	}

	protected static String indentString(String in, short indentLevel){
		String result = "";
		for(int i=0; i<indentLevel; i++)
			result += INDENT;
		return result + in;
	}

}


