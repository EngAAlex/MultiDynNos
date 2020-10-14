package ocotillo.run;

import java.io.File;

import ocotillo.Experiment;
import ocotillo.dygraph.DyGraph;
import ocotillo.export.GMLOutputWriter;
import ocotillo.graph.Graph;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor;
import ocotillo.graph.layout.fdl.sfdp.SfdpExecutor.SfdpBuilder;
import ocotillo.multilevel.flattener.DyGraphFlattener.StaticSumPresenceFlattener;
import ocotillo.samples.parsers.Commons.DyDataSet;

public class SFDPRun extends Run {

	public SFDPRun(String[] argv, DyDataSet requestedDataSet) {
		super(argv, requestedDataSet);
	}

	@Override
	protected String getDescription() {
		return "SFDP";
	}

	@Override
	protected void completeSetup() {
		// NO-OP
	}

	@Override
	protected DyGraph run() {		
		StaticSumPresenceFlattener dyg = new StaticSumPresenceFlattener();
		Graph flattened = dyg.flattenDyGraph(dygraph);
		SfdpBuilder sfdp = new SfdpBuilder();
		SfdpExecutor sfdpInstance = sfdp.build();
		System.out.println("Flattened graph has " + flattened.nodeCount() + " nodes and " + flattened.edgeCount() + " edges");

		sfdpInstance.execute(flattened);

		//		GMLOutputWriter.writeOutput(new File("C:\\Users\\Alessio Arleo\\Desktop\\"+name+"-sfdp" +".gml"), flattened);
		//		if(true)
		//			return lines;

		Experiment.copyNodeLayoutFromTo(flattened, dygraph);
		//Experiment.applyIdealScaling(dygraph, 1/1.31);

		//GMLOutputWriter.writeOutput(new File("C:\\Users\\Alessio Arleo\\Desktop\\"+this.graphName+"-sfdp.gml"), flattened);
		
		//		MultiLevelCustomRun.showGraphOnWindow(sfdpCont, dataset.suggestedInterval.leftBound(), name + " SFDP");
		//		MultiLevelCustomRun.animateGraphOnWindow(sfdpCont, dataset.suggestedInterval.leftBound(), dataset.suggestedInterval, name + " SFDP");

		return dygraph;
	}

}
