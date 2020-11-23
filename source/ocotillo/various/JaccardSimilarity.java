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
/**
 * 
 */
package ocotillo.various;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Alessio Arleo
 *
 */
public class JaccardSimilarity {

	public static float compute(Collection<? extends Object> a, Collection<? extends Object> b) {
				
		HashSet<Object> intersection = new HashSet<Object>();
		HashSet<Object> union = new HashSet<Object>();
		
		for(Object o : a)
			if(b.contains(o)) {
				if(intersection.add(o))
					union.add(o);
			}else
				union.add(o);
		
		for(Object o : b)
			if(a.contains(o)) {
				if(intersection.add(o))
					union.add(o);
			}else
				union.add(o);
		
		return intersection.size()/(float)union.size();
	}
	
}
