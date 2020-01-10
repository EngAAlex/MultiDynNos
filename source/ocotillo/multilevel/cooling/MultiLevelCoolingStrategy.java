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
		private final double m;
		
		public LinearCoolingStrategy() {
			super();
			m = DEFAULT_M;
		}
		
		public LinearCoolingStrategy(double m) {
			super();
			this.m  = m;
		}

		@Override
		protected double cooldownCurrentValue(int iteration) {
			return m*iteration + 1;
		}
		
	}
}
