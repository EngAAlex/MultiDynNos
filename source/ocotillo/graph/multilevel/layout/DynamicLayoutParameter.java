package ocotillo.graph.multilevel.layout;

import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;

public class DynamicLayoutParameter {

	private MultiLevelCoolingStrategy cooling;
	private double currentValue;
	private double initialValue;
	
	public DynamicLayoutParameter(double initialValue, MultiLevelCoolingStrategy mc) {
		this.currentValue = initialValue;
		this.initialValue = initialValue;
		cooling = mc;
	}
	
	public DynamicLayoutParameter coolDown(int iteration) {
		currentValue *= cooling.getNextValue(iteration);		
		return this;
	}
	
	public double getCurrentValue() {
		return currentValue;
	}

	public double getInitialValue() {
		return initialValue;
	} 	
	
}
