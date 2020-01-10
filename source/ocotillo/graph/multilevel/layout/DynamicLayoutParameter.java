package ocotillo.graph.multilevel.layout;

import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;

public class DynamicLayoutParameter {

	private MultiLevelCoolingStrategy cooling;
	private double currentValue;
	
	public DynamicLayoutParameter(double initialValue, MultiLevelCoolingStrategy mc) {
		this.currentValue = initialValue;
		cooling = mc;
	}
	
	public void coolDown(int iteration) {
		currentValue *= cooling.getNextValue(iteration);		
	}
	
	public double getCurrentValue() {
		return currentValue;
	} 	
	
}
