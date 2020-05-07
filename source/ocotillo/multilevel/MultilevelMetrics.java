package ocotillo.multilevel;

import ocotillo.graph.layout.fdl.modular.ModularMetric;

public class MultilevelMetrics {
	
	public static class MultiLevelPreProcessTime extends ModularMetric{
		
		@Override
		public String metricName() {
			return "PreProcess Time";
		}	
	}
	
	public static class CoarseningTime extends ModularMetric{
		
		@Override
		public String metricName() {
			return "Coarsening Time";
		}	
	}
	
	public static class PlacementTime extends ModularMetric{
		
		@Override
		public String metricName() {
			return "Placement Time";
		}	
	}
	
	public static class HierarchyDepth extends ModularMetric{
		
		@Override
		public String metricName() {
			return "Hierarchy Depth";
		}	
	}
	
	public static class LayoutTime extends ModularMetric{
		
		@Override
		public String metricName() {
			return "Layout Time";
		}	
	}


}
