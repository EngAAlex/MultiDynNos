package ocotillo.multilevel.options;

import ocotillo.geometry.Geom;
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;
import ocotillo.graph.multilevel.layout.DynamicLayoutParameter;
import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing.FlexibleTimeTrajectories;


public class MultiLevelDrawingOption<V> {

	protected V value;
	protected int activeFromLevel;

	public static class FlexibleTimeTrajectoriesPostProcessing extends MultiLevelDrawingOption<ModularPostProcessing> {

		MultiLevelCoolingStrategy cooling;
		
		public FlexibleTimeTrajectoriesPostProcessing(int increaseRate) {
			super();
			cooling = new MultiLevelCoolingStrategy.LinearCoolingStrategy(increaseRate);
		}
		
		public FlexibleTimeTrajectoriesPostProcessing(int applyFromLevel, int increaseRate) {
			super(applyFromLevel);
			cooling = new MultiLevelCoolingStrategy.LinearCoolingStrategy(increaseRate);
		}
		
		@Override
		public ModularPostProcessing getValue(int iteration, int level, double delta, double tau, DynamicLayoutParameter initial_max_movement,
				DynamicLayoutParameter contractDistance, DynamicLayoutParameter expandDistance) {
			FlexibleTimeTrajectories opt = new FlexibleTimeTrajectories(contractDistance.getInitialValue(),
					expandDistance.getInitialValue(), Geom.e3D); 
			int suggestedInterval = (int) Math.ceil(cooling.getNextValue(iteration));
			opt.refreshInterval = suggestedInterval;
			return opt;
		}
		
	}
	
	/*public MultiLevelOption() {
		
	}*/
	
	public MultiLevelDrawingOption() {
		this(Integer.MAX_VALUE);
	}
	
	public MultiLevelDrawingOption(int applyFromLevel) {
		this.activeFromLevel = applyFromLevel;
	}
	
	public MultiLevelDrawingOption(V value) {
		this();
		this.value = value;
	}
	
	public MultiLevelDrawingOption(V value, int applyFromLevel) {
		this(applyFromLevel);
		this.value = value;
	}
//
//	public String getName() {
//		return toString();
//	}
//
//	public String getDescription() {
//		return "";
//	}

	/*public V getDefaultValue() {
		return defaultValue;
	}*/

	public V getValue(int iteration, int level, double delta, double tau, DynamicLayoutParameter initial_max_movement, DynamicLayoutParameter contractDistance, DynamicLayoutParameter expandDistance) {
		return value;
	}
	
	public boolean active(int level) {
		return level <= activeFromLevel;
	}
	
	
}
