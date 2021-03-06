/**
 * Copyright ? 2020 Alessio Arleo
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

package ocotillo.multilevel.options;

import ocotillo.geometry.Geom;
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;
import ocotillo.graph.multilevel.layout.DynamicLayoutParameter;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing.FlexibleTimeTrajectories;


public class MultiLevelDrawingOption<V> {
	
	static final int ACTIVE_FROM_LEVEL_DEFAULT = Integer.MAX_VALUE;
	
	protected V value;
	final protected int activeFromLevel;

	public static class FlexibleTimeTrajectoriesPostProcessing extends MultiLevelDrawingOption<ModularPostProcessing> {

		MultiLevelCoolingStrategy cooling = new MultiLevelCoolingStrategy.IdentityCoolingStrategy();
		int refreshInterval = 1;
		
		public FlexibleTimeTrajectoriesPostProcessing(int applyFromLevel, int refreshInterval) {
			super(applyFromLevel);
			this.refreshInterval = refreshInterval;
		}
		
		public FlexibleTimeTrajectoriesPostProcessing(int applyFromLevel, int refreshInterval, MultiLevelCoolingStrategy cooling) {
			this(applyFromLevel, refreshInterval);
			this.cooling = cooling;
		}
		
		public FlexibleTimeTrajectoriesPostProcessing(int refreshInterval, MultiLevelCoolingStrategy cooling) {
			this(ACTIVE_FROM_LEVEL_DEFAULT, refreshInterval);
			this.cooling = cooling;
		}		
		
		@Override
		public ModularPostProcessing getValue(int iteration, int level, double delta, double tau, DynamicLayoutParameter initial_max_movement,
				DynamicLayoutParameter contractDistance, DynamicLayoutParameter expandDistance) {
			FlexibleTimeTrajectories opt = new FlexibleTimeTrajectories(contractDistance.getInitialValue(),
					expandDistance.getInitialValue(), Geom.e3D); 			
			opt.refreshInterval = refreshInterval * (int) Math.ceil(cooling.getNextValue(iteration));
			return opt;
		}
	}
	
//	public MultiLevelDrawingOption() {
//		this(Integer.MAX_VALUE);
//	}
	
	public MultiLevelDrawingOption(int applyFromLevel) {
		this.activeFromLevel = applyFromLevel;
	}
	
	public MultiLevelDrawingOption(V value) {
		this(ACTIVE_FROM_LEVEL_DEFAULT);
		this.value = value;
	}
	
	public MultiLevelDrawingOption(V value, int applyFromLevel) {
		this(applyFromLevel);
		this.value = value;
	}

	public V getValue(int iteration, int level, double delta, double tau, DynamicLayoutParameter initial_max_movement, DynamicLayoutParameter contractDistance, DynamicLayoutParameter expandDistance) {
		return value;
	}
	
	public boolean active(int level) {
		return level <= activeFromLevel;
	}
	
	
}
