/**
 * Copyright © 2020 Alessio Arleo
 * Copyright © 2014-2017 Paolo Simonetto
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

package ocotillo.graph.multilevel.layout;

import ocotillo.multilevel.cooling.MultiLevelCoolingStrategy;

public class DynamicLayoutParameter {

	private MultiLevelCoolingStrategy cooling;
	protected final double minimumValue; //USED TO LIMIT THE MINIMUM VALUE

	private double currentValue;
	private double initialValue;

	public DynamicLayoutParameter(double initialValue, MultiLevelCoolingStrategy mc) {
		this.currentValue = initialValue;
		this.initialValue = initialValue;
		cooling = mc;
		minimumValue =  Double.MIN_VALUE;
	}
	
	public DynamicLayoutParameter(double initialValue, MultiLevelCoolingStrategy mc, double minimumValue) {
		this.currentValue = initialValue;
		this.initialValue = initialValue;
		cooling = mc;
		this.minimumValue =  minimumValue;
	}
	
	public DynamicLayoutParameter coolDown(int iteration) {
		double tmpCurrentValue = currentValue * cooling.getNextValue(iteration);		
		currentValue = tmpCurrentValue > minimumValue ? tmpCurrentValue : currentValue; 
		return this;
	}
	
	public double getCurrentValue() {
		return currentValue;
	}

	public double getInitialValue() {
		return initialValue;
	} 	
	
}
