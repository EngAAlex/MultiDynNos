package ocotillo.multilevel.logger;

public class Logger {

	private static Logger instance;
	private boolean logOn = false;
	
	private Logger() {
		instance = this;
	}
	
	private Logger(boolean logOn) {
		this.logOn = logOn;
		instance = this;
	}
	
	public static Logger getInstance(boolean log) {
		instance = new Logger(log); 
		return instance;
	}
	
	public static Logger getInstance() {
		if(instance == null)
			instance = new Logger(); 
		return instance;
	}
	
	public static boolean isLogOn() {
		return instance.logOn;
	}

	public void log(String log) {
		if(instance.logOn)
			System.out.println(log);
	}
	
}
