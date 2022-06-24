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

package ocotillo.multilevel.cooling;

public abstract class MultiLevelCoolingStrategy {

	protected double currentValue = 1;
	
	public MultiLevelCoolingStrategy(){
	}
		
	public double getNextValue(int iteration) {
		currentValue = cooldownCurrentValue(iteration);
		return currentValue;
	}

	public double getCurrentTemperature() {
		return currentValue;
	}

	protected abstract double cooldownCurrentValue(int iteration);
	
	public static class IdentityCoolingStrategy extends MultiLevelCoolingStrategy{

		public IdentityCoolingStrategy() {
		}

		@Override
		protected double cooldownCurrentValue(int iteration) {
			return currentValue;
		}
		
	}
	
	public static class LinearCoolingStrategy extends MultiLevelCoolingStrategy{

		public static final double DEFAULT_M = -1;
		public static final double DEFAULT_Y = 1;
		private final double m;
		private final double y;
		
		public LinearCoolingStrategy() {
			super();
			m = DEFAULT_M;
			y = DEFAULT_Y;
		}
		
		public LinearCoolingStrategy(double m) {
			super();
			this.m  = m;
			y = DEFAULT_Y;
		}

		public LinearCoolingStrategy(double m, double y) {
			super();
			this.m  = m;
			this.y = y;
		}
		
		@Override
		protected double cooldownCurrentValue(int iteration) {
			return m*iteration + y;
		}	
	}
	
	public static class InverseExponentialCoolingStrategy extends MultiLevelCoolingStrategy{

		public static final double DEFAULT_ALPHA = -.12;
		private final double alpha;
		
		public InverseExponentialCoolingStrategy() {
			super();
			alpha = DEFAULT_ALPHA;
		}
		
		public InverseExponentialCoolingStrategy(double alpha) {
			super();
			if(alpha < 1)
				if(alpha > 0)
					this.alpha = -1*alpha;
				else
					this.alpha  = alpha;
			else
				this.alpha = DEFAULT_ALPHA;
		}

		@Override
		protected double cooldownCurrentValue(int iteration) {
			return Math.pow(Math.E, alpha*iteration);
		}	
	}
}
