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
