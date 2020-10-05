package ocotillo.samples.parsers;

import java.net.URISyntaxException;

import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;

public abstract class PreloadedGraphParser {

	public abstract DyDataSet parse(Mode mode) throws URISyntaxException;
		
}
