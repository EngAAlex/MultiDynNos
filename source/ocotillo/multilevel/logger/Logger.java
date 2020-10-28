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
